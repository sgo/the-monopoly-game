package the.monopoly.game;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Cards;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.AgreeIfAffordable;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GameTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private final List<Player> players = ruleSet.players().select(3).toList();

  @Test
  void aGameStartsWithWhoeverWonInitiative() {
    Game.Result result = playInitiative(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

    assertThat(result.turnOrder().getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void playThenContinuesClockwiseFromTheWinner() {
    Game.Result result = playInitiative(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

    assertThat(result.turnOrder()).extracting(Player::id)
        .containsExactly(Pawn.high_hat.id(), Pawn.iron_box.id(), Pawn.dog.id());
  }

  @Test
  void aTiedGameIsSettledBeforeTheFirstTurn() {
    Game.Result result = playInitiative(
        new Roll(4, 4), new Roll(4, 4), new Roll(2, 3),
        new Roll(3, 3), new Roll(5, 4)
    );

    assertThat(result.turnOrder().getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void aGameAccountsForWhoIsPlayingAndWhatEachOfThemRolledForInitiative() {
    Game.Result result = playInitiative(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

    assertThat(result.journal()).startsWith(
        new Entry.Start(List.of(Pawn.dog.id(), Pawn.high_hat.id(), Pawn.iron_box.id())),
        new Entry.InitiativeRoll(Pawn.dog.id(), 4),
        new Entry.InitiativeRoll(Pawn.high_hat.id(), 10),
        new Entry.InitiativeRoll(Pawn.iron_box.id(), 6),
        new Entry.InitiativeWon(Pawn.high_hat.id())
    );
  }

  @Test
  void aGameAccountsForEachTurnAsItIsTaken() {
    Game.Result result = play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3)
    );

    assertThat(result.journal()).containsSubsequence(
        new Entry.TurnStarted(Pawn.high_hat.id()),
        new Entry.Rolled(Pawn.high_hat.id(), 3),
        new Entry.Moved(Pawn.high_hat.id(), 0, 3),
        new Entry.TurnStarted(Pawn.iron_box.id()),
        new Entry.Rolled(Pawn.iron_box.id(), 6),
        new Entry.Moved(Pawn.iron_box.id(), 0, 6),
        new Entry.TurnStarted(Pawn.dog.id()),
        new Entry.Rolled(Pawn.dog.id(), 7),
        new Entry.Moved(Pawn.dog.id(), 0, 7)
    );
  }

  @Test
  void aGameAccountsForASalaryAfterTheMoveThatEarnedIt() {
    players.getFirst().position().moveTo(37);

    Game.Result result = play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(1, 2)
    );

    assertThat(result.journal()).containsSubsequence(
        new Entry.Moved(Pawn.dog.id(), 37, 0),
        new Entry.SalaryCollected(Pawn.dog.id(), new Money(200))
    );
  }

  @Test
  void aGameThrowsTheDiceTheRulesCallForWhenGivenNoneOfItsOwn() {
    Game.Result result = new Game(ruleSet, players).play();

    assertThat(result.turnOrder()).containsExactlyInAnyOrderElementsOf(players);
  }

  /**
   * The three turn rolls differ from each other, so a game that moved everyone
   * by one roll, or moved only whoever leads, lands them somewhere else.
   */
  @Test
  void everyPlayerTakesATurnMovedByTheirOwnRoll() {
    play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3)
    );

    assertThat(positionsOf(players)).containsExactly(7, 3, 6);
  }

  @Test
  void aPlayerWithACupOfTheirOwnRollsThatOneRatherThanTheTable() {
    Map<Player.ID, Cup> cups = Map.of(
        Pawn.dog.id(), Cup.of(new Roll(2, 2), new Roll(1, 2)),
        Pawn.high_hat.id(), Cup.of(new Roll(5, 5), new Roll(2, 4)),
        Pawn.iron_box.id(), Cup.of(new Roll(3, 3), new Roll(4, 3))
    );

    new Game(ruleSet, players, player -> cups.get(player.id())).play();

    assertThat(positionsOf(players)).containsExactly(3, 6, 7);
  }

  @Test
  void landingOnASpaceIsWorthNothingEitherWayYet() {
    play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(1, 3)
    );

    Player dog = players.getFirst();
    assertThat(spaceAt(dog.position().index())).isInstanceOf(TaxSpace.class);
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
  }

  @Test
  void aGameSellsUnownedLandToWhoeverStopsOnItAndAgreesToBuyIt() {
    Game.Result result = playWith(Map.of(Pawn.dog.id(), new AgreeIfAffordable()));

    assertThat(result.deeds().ownerOf(Street.Type.DiestsestraatLeuven)).contains(Pawn.dog.id());
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1448));
  }

  @Test
  void aGameAccountsForAPurchaseAfterTheMoveThatReachedIt() {
    Game.Result result = playWith(Map.of(Pawn.dog.id(), new AgreeIfAffordable()));

    assertThat(result.journal()).containsSubsequence(
        new Entry.Moved(Pawn.dog.id(), 0, 3),
        new Entry.Bought(Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(60))
    );
  }

  @Test
  void aGameAccountsForAnAuctionAfterTheMoveThatReachedTheLand() {
    Game.Result result = playWith(Map.of(Pawn.high_hat.id(), bidding(120)));

    assertThat(result.deeds().ownerOf(Street.Type.DiestsestraatLeuven)).contains(Pawn.high_hat.id());
    assertThat(result.journal()).containsSubsequence(
        new Entry.Moved(Pawn.dog.id(), 0, 3),
        new Entry.AuctionWon(Pawn.high_hat.id(), Street.Type.DiestsestraatLeuven, new Money(120))
    );
  }

  @Test
  void auctioningUnownedLandDoesNotMakeTheLandingPlayerPayRent() {
    Game.Result result = playWith(Map.of(Pawn.high_hat.id(), biddingAndClaiming(120)));

    assertThat(result.journal()).doesNotContain(
        new Entry.RentPaid(
            Pawn.dog.id(),
            Pawn.high_hat.id(),
            Street.Type.DiestsestraatLeuven,
            new Money(4)
        )
    );
  }

  @Test
  void aGameNobodyDecidesAnythingInLeavesTheBoardWithTheBank() {
    Game.Result result = playWith(Map.of());

    assertThat(result.deeds().isUnowned(Street.Type.DiestsestraatLeuven)).isTrue();
  }

  @Test
  void anAgreeablePlayerBuildsEvenlyAcrossAFullColourGroupTheyOwn() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    players.getFirst().account().withdraw(new Money(1400));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new AgreeIfAffordable()), deeds);

    assertThat(result.deeds().housesBuiltOn(street(Street.Type.RueGrandeDinant))).isEqualTo(1);
    assertThat(result.deeds().housesBuiltOn(street(Street.Type.DiestsestraatLeuven))).isEqualTo(1);
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(0));
    assertThat(result.journal()).contains(new Entry.HouseBuilt(
        Pawn.dog.id(), Street.Type.RueGrandeDinant, new Money(50)
    ));
  }

  @Test
  void anAgreeablePlayerExchangesFourHousesForAHotelOnEveryStreetItCanAfford() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    deeds.arrangeHouses(street(Street.Type.RueGrandeDinant), 4);
    deeds.arrangeHouses(street(Street.Type.DiestsestraatLeuven), 4);
    players.getFirst().account().withdraw(new Money(800));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new AgreeIfAffordable()), deeds);

    assertThat(result.deeds().hasHotelOn(street(Street.Type.RueGrandeDinant))).isTrue();
    assertThat(result.deeds().hasHotelOn(street(Street.Type.DiestsestraatLeuven))).isTrue();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(0));
  }

  @Test
  void anAgreeablePlayerDoesNotBuildWhileAnyStreetInTheColourGroupIsMortgaged() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    deeds.arrangeMortgaged((ColourStreet) ruleSet.create(Street.Type.RueGrandeDinant));
    players.getFirst().account().withdraw(new Money(1400));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new AgreeIfAffordable()), deeds);

    assertThat(result.deeds().housesBuiltOn(street(Street.Type.RueGrandeDinant))).isZero();
    assertThat(result.deeds().housesBuiltOn(street(Street.Type.DiestsestraatLeuven))).isZero();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(100));
  }

  @Test
  void aGameRecordsWhenBuildingIsRefusedBecauseAStreetInTheColourGroupIsMortgaged() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    deeds.arrangeMortgaged((ColourStreet) ruleSet.create(Street.Type.RueGrandeDinant));
    players.getFirst().account().withdraw(new Money(1400));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new AgreeIfAffordable()), deeds);

    assertThat(result.journal()).contains(new Entry.BuildingRefused(
        Pawn.dog.id(), Street.Type.RueGrandeDinant, new Money(50)
    ));
  }

  @Test
  void aMortgagedColourGroupDoesNotBlockBuildingOnAnotherColourGroup() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    giveMonopolyTo(deeds, Pawn.dog.id(), Street.Type.MeirAntwerpen, Street.Type.NieuwstraatBrussel);
    deeds.arrangeMortgaged((ColourStreet) ruleSet.create(Street.Type.RueGrandeDinant));
    players.getFirst().account().withdraw(new Money(1100));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new AgreeIfAffordable()), deeds);

    assertThat(result.deeds().housesBuiltOn(street(Street.Type.RueGrandeDinant))).isZero();
    assertThat(result.deeds().housesBuiltOn(street(Street.Type.DiestsestraatLeuven))).isZero();
    assertThat(result.deeds().housesBuiltOn(street(Street.Type.MeirAntwerpen))).isEqualTo(1);
    assertThat(result.deeds().housesBuiltOn(street(Street.Type.NieuwstraatBrussel))).isEqualTo(1);
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(0));
  }

  @Test
  void aChanceCardCanAdvanceAPawnToStartAndPayTheSalary() {
    Game.Result result = playWithCards(
        Map.of(),
        new Deeds(),
        new Roll(3, 4),
        "Ga door naar START (Ontvang M200).",
        null
    );

    assertThat(players.getFirst().position().index()).isZero();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1700));
    assertThat(result.journal()).containsSubsequence(
        new Entry.ChanceCardDrawn(Pawn.dog.id(), "Ga door naar START (Ontvang M200)."),
        new Entry.SalaryCollected(Pawn.dog.id(), new Money(200))
    );
  }

  @Test
  void aChanceCardCanMakeItsDrawerPayEveryOtherPlayer() {
    Game.Result result = playWithCards(
        Map.of(),
        new Deeds(),
        new Roll(3, 4),
        "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.",
        null
    );

    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1400));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1550));
    assertThat(players.get(2).account().balance()).isEqualTo(Balance.of(1550));
    assertThat(result.journal()).contains(new Entry.ChanceCardDrawn(
        Pawn.dog.id(), "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    ));
  }

  @Test
  void aCommunityChestCardCanBeKeptAndSoldToAnotherPlayer() {
    players.getFirst().position().moveTo(14);

    Game.Result result = playWithCards(
        Map.of(),
        new Deeds(),
        new Roll(1, 2),
        null,
        "Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen."
    );

    assertThat(result.deeds().holdsGetOutOfJailFreeCard(players.getFirst())).isTrue();

    result.deeds().sellGetOutOfJailFreeCard(players.getFirst(), players.get(1), new Money(50));

    assertThat(result.deeds().holdsGetOutOfJailFreeCard(players.get(1))).isTrue();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1550));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1450));
  }

  @Test
  void aChanceCardCanAdvanceToTheNearestStationAndChargeDoubleRent() {
    Deeds deeds = new Deeds();
    deeds.sell((the.monopoly.game.components.streets.Station) ruleSet.create(Street.Type.CentraalStation), players.get(1),
        new Money(200));
    players.get(1).account().deposit(new Money(200));

    playWithCards(
        Map.of(Pawn.high_hat.id(), new AgreeIfAffordable()),
        deeds,
        new Roll(3, 4),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        null
    );

    assertThat(players.getFirst().position().index()).isEqualTo(15);
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1450));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1550));
  }

  @Test
  void aChanceCardCanChargeUtilityRentUsingAFreshRoll() {
    players.getFirst().position().moveTo(4);
    Deeds deeds = new Deeds();
    deeds.sell((the.monopoly.game.components.streets.Utility) ruleSet.create(Street.Type.Elektriciteitscentrale), players.get(1), new Money(150));
    players.get(1).account().deposit(new Money(150));

    play(
        Map.of(Pawn.high_hat.id(), new AgreeIfAffordable()),
        deeds,
        Map.of(
            Pawn.dog.id(), Cup.of(new Roll(5, 5), new Roll(1, 2), new Roll(3, 4)),
            Pawn.high_hat.id(), Cup.of(new Roll(1, 1), new Roll(4, 6)),
            Pawn.iron_box.id(), Cup.of(new Roll(1, 2), new Roll(4, 6))
        ),
        chanceDeck("Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.")
    );

    assertThat(players.getFirst().position().index()).isEqualTo(12);
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1430));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1570));
  }

  @Test
  void aChanceCardIsRecordedBeforeItsBankPaymentEffect() {
    Game.Result result = playWithCards(
        Map.of(),
        new Deeds(),
        new Roll(3, 4),
        "Boete voor te snel rijden. Betaal M15.",
        null
    );

    assertThat(result.journal()).containsSubsequence(
        new Entry.ChanceCardDrawn(Pawn.dog.id(), "Boete voor te snel rijden. Betaal M15."),
        new Entry.BankPaid(Pawn.dog.id(), new Money(15))
    );
  }

  @Test
  void aChanceCardChargesRepairCostsForEveryHouseAndHotelOwned() {
    Deeds deeds = new Deeds();
    Player dog = players.getFirst();
    ColourStreet withHouses = street(Street.Type.RueGrandeDinant);
    ColourStreet withHotel = street(Street.Type.DiestsestraatLeuven);
    giveStreetTo(deeds, dog, withHouses);
    giveStreetTo(deeds, dog, withHotel);
    deeds.arrangeHouses(withHouses, 2);
    deeds.arrangeHotel(withHotel);

    Game.Result result = playWithCards(
        Map.of(), deeds, new Roll(3, 4),
        "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.",
        null
    );

    assertThat(result.journal()).contains(new Entry.BankPaid(Pawn.dog.id(), new Money(150)));
  }

  @Test
  void aChanceCardRepairCostsNothingWhenNoImprovedPropertyIsOwned() {
    Game.Result result = playWithCards(
        Map.of(), new Deeds(), new Roll(3, 4),
        "Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.",
        null
    );

    assertThat(result.journal()).filteredOn(Entry.BankPaid.class::isInstance).isEmpty();
  }

  @Test
  void aCommunityChestCardCanMakeItsDrawerCollectFromEveryOtherPlayer() {
    players.getFirst().position().moveTo(14);

    playWithCards(
        Map.of(), new Deeds(), new Roll(1, 2),
        null,
        "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    );

    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1520));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1490));
    assertThat(players.get(2).account().balance()).isEqualTo(Balance.of(1490));
  }

  @Test
  void aChanceCardAdvancesToBuurtspoorwegenWhenItIsTheNearestStation() {
    players.getFirst().position().moveTo(15);

    playWithCards(
        Map.of(), new Deeds(), new Roll(3, 4),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        null
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.Buurtspoorwegen));
  }

  @Test
  void aChanceCardAdvancesToNoordStationWhenItIsTheNearestStation() {
    players.getFirst().position().moveTo(29);

    playWithCards(
        Map.of(), new Deeds(), new Roll(3, 4),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        null
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.NoordStation));
  }

  /** A player who wants the land at auction but never at the asking price. */
  private static Strategy bidding(int amount) {
    return new Strategy() {
      @Override
      public Money bidFor(Offer offer) {
        return new Money(amount);
      }
    };
  }

  private static Strategy biddingAndClaiming(int amount) {
    return new Strategy() {
      @Override
      public Money bidFor(Offer offer) {
        return new Money(amount);
      }

      @Override
      public boolean claims(RentClaim claim) {
        return true;
      }
    };
  }

  /**
   * Plays a game in which the winner of initiative stops on the first street
   * anyone can buy, and everyone after them stops on the same street.
   */
  private Game.Result playWith(Map<Player.ID, Strategy> strategies) {
    return playWith(strategies, new Deeds());
  }

  private Game.Result playWith(Map<Player.ID, Strategy> strategies, Deeds deeds) {
    return play(strategies, deeds, new Roll(1, 2));
  }

  /** Plays a game where every turn after the first ends without landing anywhere notable. */
  private Game.Result playWithQuietTurns(Map<Player.ID, Strategy> strategies, Deeds deeds) {
    return play(strategies, deeds, new Roll(4, 6));
  }

  private Game.Result play(Map<Player.ID, Strategy> strategies, Deeds deeds, Roll secondRoll) {
    return playWithCards(strategies, deeds, secondRoll, null, null);
  }

  private Game.Result playWithCards(
      Map<Player.ID, Strategy> strategies, Deeds deeds, Roll secondRoll, String chanceCard, String communityChestCard
  ) {
    Map<Player.ID, Cup> cups = Map.of(
        Pawn.dog.id(), Cup.of(new Roll(5, 5), secondRoll),
        Pawn.high_hat.id(), Cup.of(new Roll(1, 1), secondRoll),
        Pawn.iron_box.id(), Cup.of(new Roll(1, 2), secondRoll)
    );
    return play(strategies, deeds, cups, decks(chanceCard, communityChestCard));
  }

  private Game.Result play(
      Map<Player.ID, Strategy> strategies, Deeds deeds, Map<Player.ID, Cup> cups, Cards.Decks decks
  ) {
    return new Game(
        ruleSet, players,
        player -> cups.get(player.id()),
        player -> strategies.getOrDefault(player.id(), Strategy.UNDECIDED),
        deeds,
        decks
    ).play();
  }

  private static Cards.Decks chanceDeck(String chanceCard) {
    return decks(chanceCard, null);
  }

  private static Cards.Decks decks(String chanceCard, String communityChestCard) {
    AtomicReference<String> nextChance = new AtomicReference<>(chanceCard);
    AtomicReference<String> nextCommunityChest = new AtomicReference<>(communityChestCard);
    return new Cards.Decks() {
      @Override
      public String drawChance() {
        return nextChance.getAndSet(null);
      }

      @Override
      public String drawCommunityChest() {
        return nextCommunityChest.getAndSet(null);
      }
    };
  }

  private Deeds monopolyFor(Player.ID owner) {
    Deeds deeds = new Deeds();
    giveMonopolyTo(deeds, owner, Street.Type.RueGrandeDinant, Street.Type.DiestsestraatLeuven);
    return deeds;
  }

  private void giveMonopolyTo(Deeds deeds, Player.ID owner, Street.Type first, Street.Type second) {
    Player player = players.stream().filter(it -> it.id().equals(owner)).findFirst().orElseThrow();
    giveStreetTo(deeds, player, street(first));
    giveStreetTo(deeds, player, street(second));
  }

  private void giveStreetTo(Deeds deeds, Player player, ColourStreet street) {
    deeds.sell(street, player, street.price());
    player.account().deposit(street.price());
  }

  private ColourStreet street(Street.Type type) {
    return (ColourStreet) ruleSet.create(type);
  }

  private Street spaceAt(int position) {
    return ruleSet.create(ruleSet.gameboard().layout().get(position));
  }

  private static List<Integer> positionsOf(List<Player> players) {
    return players.stream().map(it -> it.position().index()).toList();
  }

  /**
   * Plays these rolls for initiative, and an unremarkable turn for everyone
   * after, for the tests that only care about the order settled beforehand.
   */
  private Game.Result playInitiative(Roll... rolls) {
    return play(Stream.concat(Stream.of(rolls), players.stream().map(it -> new Roll(1, 2)))
        .toArray(Roll[]::new));
  }

  private Game.Result play(Roll... rolls) {
    return new Game(ruleSet, players, Cup.of(rolls)).play();
  }
}
