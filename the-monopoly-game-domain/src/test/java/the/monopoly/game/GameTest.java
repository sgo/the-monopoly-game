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
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.rules.Cards;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
        new Entry.StalemateTrading(false),
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
        new Entry.TurnStarted(Pawn.high_hat.id(), new Money(1500)),
        new Entry.Rolled(Pawn.high_hat.id(), 3),
        new Entry.Moved(Pawn.high_hat.id(), 0, 3),
        new Entry.TurnStarted(Pawn.iron_box.id(), new Money(1500)),
        new Entry.Rolled(Pawn.iron_box.id(), 6),
        new Entry.Moved(Pawn.iron_box.id(), 0, 6),
        new Entry.TurnStarted(Pawn.dog.id(), new Money(1500)),
        new Entry.Rolled(Pawn.dog.id(), 7),
        new Entry.Moved(Pawn.dog.id(), 0, 7)
    );
  }

  @Test
  void playStopsAfterOneRoundRatherThanPlayingToCompletion() {
    Game.Result result = play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3),
        new Roll(1, 3), new Roll(2, 3), new Roll(1, 4)
    );

    assertThat(result.journal()).filteredOn(entry -> entry instanceof Entry.TurnStarted).hasSize(3);
  }

  @Test
  void aTurnStartedEntryCarriesThePlayersCurrentBalanceRatherThanTheirStartingCapital() {
    players.getFirst().account().withdraw(new Money(200));

    Game.Result result = play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3)
    );

    assertThat(result.journal()).contains(new Entry.TurnStarted(Pawn.dog.id(), new Money(1300)));
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
    Game.Result result = game(players, Cup.of(ruleSet.dice().toList())).play();

    assertThat(result.turnOrder()).containsExactlyInAnyOrderElementsOf(players);
  }

  @Test
  void aCompleteGameKeepsTakingTurnsUntilBankruptcyLeavesOneWinner() {
    List<Player> twoPlayers = ruleSet.players().select(2).toList();
    Player dog = twoPlayers.getFirst();
    dog.position().moveTo(1);
    dog.account().withdraw(dog.account().balance().amount().minus(new Money(5)));

    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(2, 2), new Roll(1, 2)),
        twoPlayers.get(1).id(), Cup.of(new Roll(5, 5), new Roll(4, 6))
    );
    Game.Result result = game(twoPlayers, player -> cups.get(player.id())).playToCompletion();

    assertThat(result.journal()).endsWith(
        new Entry.Bankrupt(dog.id(), null), new Entry.Won(twoPlayers.get(1).id())
    );
    assertThat(result.winner()).contains(twoPlayers.get(1));
  }

  /**
   * The previous test's bankruptcy lands on the losing player's very first
   * turn, so on its own it cannot tell a game that plays every round until
   * completion from one that stops after just one round regardless. This
   * scenario survives a first round unscathed and only goes bankrupt, to a
   * player rather than the bank, on the second.
   */
  @Test
  void aCompleteGameContinuesPastASurvivedRoundUntilBankruptcyLeavesOneWinner() {
    List<Player> twoPlayers = ruleSet.players().select(2).toList();
    Player dog = twoPlayers.getFirst();
    Player highHat = twoPlayers.get(1);
    dog.account().withdraw(dog.account().balance().amount().minus(new Money(5)));

    Deeds deeds = new Deeds();
    ColourStreet rentedStreet = street(Street.Type.SteenstraatBrugge);
    giveStreetTo(deeds, highHat, rentedStreet);

    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(2, 2), new Roll(1, 2), new Roll(1, 2)),
        highHat.id(), Cup.of(new Roll(5, 5), new Roll(4, 6), new Roll(2, 6))
    );
    Strategy.OfPlayers strategies = player ->
        player.id().equals(highHat.id()) ? new Greedo() : Strategy.UNDECIDED;
    Game.Result result = game(
        twoPlayers, player -> cups.get(player.id()), strategies, deeds
    ).playToCompletion();

    assertThat(result.journal()).containsSubsequence(
        new Entry.Moved(dog.id(), 0, 3),
        new Entry.Moved(dog.id(), 3, 6),
        new Entry.RentPaid(dog.id(), highHat.id(), Street.Type.SteenstraatBrugge, new Money(6)),
        new Entry.Bankrupt(dog.id(), highHat.id()), new Entry.Won(highHat.id())
    );
    assertThat(result.winner()).contains(highHat);
  }

  @Test
  void aSingleRoundDeclaresAStalemateOnceEveryRemainingPlayerClearsTheThreshold() {
    players.forEach(player -> player.account().deposit(new Money(25000)));

    Game.Result result = game(players, Cup.of(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3)
    )).play();

    assertThat(result.journal()).contains(new Entry.Stalemate());
    assertThat(result.journal()).filteredOn(entry -> entry instanceof Entry.FinalBalance)
        .hasSize(3);
  }

  @Test
  void aStalemateStopsTheGameBeforeTheNextRound() {
    players.forEach(player -> player.account().deposit(new Money(25000)));

    Game.Result result = game(players, Cup.of(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3)
    )).playUpToRounds(3);

    assertThat(result.journal()).filteredOn(Entry.Stalemate.class::isInstance).hasSize(1);
  }

  @Test
  void aStalemateTradingGreedoTradesWhenBothPlayersCompleteColourGroupsAtTheStartOfItsTurn() {
    Player dog = players.get(0);
    Player highHat = players.get(1);
    Player ironBox = players.get(2);
    Deeds deeds = new Deeds();
    ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .filter(land -> land.type() != Street.Type.DiestsestraatLeuven
            && land.type() != Street.Type.MeirAntwerpen)
        .forEach(land -> deeds.sell(land, dog, Money.ZERO));
    deeds.sell((Ownable) ruleSet.create(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);
    deeds.sell((Ownable) ruleSet.create(Street.Type.MeirAntwerpen), highHat, Money.ZERO);
    Strategy.OfPlayers strategies = player ->
        player.id().equals(dog.id()) ? new Greedo(Money.ZERO, true) : Strategy.UNDECIDED;
    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(1, 2), new Roll(4, 6)),
        highHat.id(), Cup.of(new Roll(1, 3), new Roll(4, 6)),
        ironBox.id(), Cup.of(new Roll(1, 4), new Roll(4, 6))
    );

    Game.Result result = new Game(
        ruleSet, players, player -> cups.get(player.id()), strategies, deeds, Cards.Decks.EMPTY,
        new the.monopoly.game.rules.Jail(ruleSet), true
    ).playUpToRounds(1);

    assertThat(result.journal()).contains(new Entry.PeerTrade(
        dog.id(), Street.Type.NieuwstraatBrussel, highHat.id(), Street.Type.DiestsestraatLeuven));
  }

  @Test
  void peerTradingPrecedesBuyoutWhenThePlayersAreNotCashTied() {
    Player dog = players.get(0);
    Player highHat = players.get(1);
    Player ironBox = players.get(2);
    Deeds deeds = new Deeds();
    ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .filter(land -> land.type() != Street.Type.DiestsestraatLeuven
            && land.type() != Street.Type.MeirAntwerpen)
        .forEach(land -> deeds.sell(land, dog, Money.ZERO));
    deeds.sell((Ownable) ruleSet.create(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);
    deeds.sell((Ownable) ruleSet.create(Street.Type.MeirAntwerpen), highHat, Money.ZERO);
    highHat.account().withdraw(new Money(100));
    Strategy.OfPlayers strategies = player ->
        player.id().equals(dog.id()) ? new Greedo(Money.ZERO, true) : Strategy.UNDECIDED;
    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(5, 5), new Roll(4, 6)),
        highHat.id(), Cup.of(new Roll(1, 3), new Roll(4, 6)),
        ironBox.id(), Cup.of(new Roll(1, 4), new Roll(4, 6))
    );

    Game.Result result = new Game(
        ruleSet, players, player -> cups.get(player.id()), strategies, deeds, Cards.Decks.EMPTY,
        new the.monopoly.game.rules.Jail(ruleSet), true
    ).playUpToRounds(1);

    assertThat(result.journal()).contains(new Entry.PeerTrade(
        dog.id(), Street.Type.NieuwstraatBrussel, highHat.id(), Street.Type.DiestsestraatLeuven));
    assertThat(result.journal()).doesNotContain(new Entry.SplitMonopolyWon(dog.id(), highHat.id()));
  }

  @Test
  void aStalemateTradingGreedoUsesTheCanonicalBuyoutSettlementAtTheStartOfItsTurn() {
    List<Player> twoPlayers = ruleSet.players().select(2).toList();
    Player dog = twoPlayers.get(0);
    Player highHat = twoPlayers.get(1);
    Deeds deeds = new Deeds();
    ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .filter(land -> land.type() != Street.Type.NieuwstraatBrussel)
        .forEach(land -> deeds.sell(land, dog, Money.ZERO));
    deeds.sell((Ownable) ruleSet.create(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);
    dog.account().withdraw(new Money(500));
    highHat.account().withdraw(new Money(1400));
    Strategy.OfPlayers strategies = player ->
        player.id().equals(dog.id()) ? new Greedo(Money.ZERO, true) : Strategy.UNDECIDED;
    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(1, 2), new Roll(4, 6)),
        highHat.id(), Cup.of(new Roll(5, 5), new Roll(4, 6))
    );

    Game.Result result = new Game(
        ruleSet, twoPlayers, player -> cups.get(player.id()), strategies, deeds, Cards.Decks.EMPTY,
        new the.monopoly.game.rules.Jail(ruleSet), true
    ).playUpToRounds(1);

    assertThat(result.journal()).containsSubsequence(
        new Entry.SplitMonopolyWon(dog.id(), highHat.id()),
        new Entry.SplitMonopolyPaid(dog.id(), highHat.id(), new Money(40)),
        new Entry.TurnStarted(dog.id(), new Money(960))
    );
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
  }

  @Test
  void aStalemateTradingGreedoLeavesASplitMonopolyAloneWhenTiedOnCashAtTheStartOfItsTurn() {
    List<Player> twoPlayers = ruleSet.players().select(2).toList();
    Player dog = twoPlayers.get(0);
    Player highHat = twoPlayers.get(1);
    Deeds deeds = new Deeds();
    ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .filter(land -> land.type() != Street.Type.NieuwstraatBrussel)
        .forEach(land -> deeds.sell(land, dog, Money.ZERO));
    deeds.sell((Ownable) ruleSet.create(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);
    Strategy.OfPlayers strategies = player ->
        player.id().equals(dog.id()) ? new Greedo(Money.ZERO, true) : Strategy.UNDECIDED;
    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(1, 2), new Roll(4, 6)),
        highHat.id(), Cup.of(new Roll(5, 5), new Roll(4, 6))
    );

    Game.Result result = new Game(
        ruleSet, twoPlayers, player -> cups.get(player.id()), strategies, deeds, Cards.Decks.EMPTY,
        new the.monopoly.game.rules.Jail(ruleSet), true
    ).playUpToRounds(1);

    assertThat(result.journal()).filteredOn(Entry.SplitMonopolyWon.class::isInstance).isEmpty();
  }

  @Test
  void aRicherGreedoResolvesAgainstAnyLowerBalancePartnerEvenWhenTheFirstPartnerIsTied() {
    Player dog = players.get(0);
    Player highHat = players.get(1);
    Player ironBox = players.get(2);
    ironBox.account().withdraw(new Money(1400));
    Deeds deeds = new Deeds();
    ruleSet.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .filter(land -> land.type() != Street.Type.NieuwstraatBrussel)
        .forEach(land -> deeds.sell(land, dog, Money.ZERO));
    deeds.sell((Ownable) ruleSet.create(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);
    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(3, 2), new Roll(4, 3)),
        highHat.id(), Cup.of(new Roll(2, 2), new Roll(4, 3)),
        ironBox.id(), Cup.of(new Roll(1, 1), new Roll(4, 3))
    );

    Game.Result result = new Game(
        ruleSet, players, player -> cups.get(player.id()),
        player -> player.id().equals(dog.id()) ? new Greedo(Money.ZERO, true) : Strategy.UNDECIDED,
        deeds, Cards.Decks.EMPTY, new the.monopoly.game.rules.Jail(ruleSet), true
    ).playUpToRounds(1);

    assertThat(result.journal()).contains(new Entry.SplitMonopolyWon(dog.id(), highHat.id()));
  }

  @Test
  void aGameStopsBetweenRoundsWhenToldTo() {
    AtomicBoolean stop = new AtomicBoolean();

    Game.Result result = game(players, Cup.of(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3)
    )).playUntilStopped(() -> !stop.compareAndSet(false, true));

    assertThat(result.journal()).containsSubsequence(
        new Entry.TurnStarted(Pawn.dog.id(), new Money(1500)),
        new Entry.Rolled(Pawn.dog.id(), 7),
        new Entry.Moved(Pawn.dog.id(), 0, 7)
    );
    assertThat(result.winner()).isEmpty();
  }

  /**
   * Bankruptcy happens once, but the fixed turn order is asked about every
   * player every round for as long as the game goes on. A bankrupt player
   * must be skipped without ending the round early for whoever is still
   * playing after them in that same order.
   */
  @Test
  void aBankruptPlayerIsSkippedWithoutEndingTheRoundForWhoeverPlaysAfterThem() {
    Player dog = players.get(0);
    Player highHat = players.get(1);
    Player ironBox = players.get(2);
    ironBox.account().withdraw(ironBox.account().balance().amount().minus(new Money(5)));

    Map<Player.ID, Cup> cups = Map.of(
        dog.id(), Cup.of(new Roll(2, 2), new Roll(2, 3), new Roll(1, 4)),
        highHat.id(), Cup.of(new Roll(5, 5), new Roll(2, 3), new Roll(2, 4)),
        ironBox.id(), Cup.of(new Roll(3, 3), new Roll(1, 3))
    );
    AtomicInteger additionalRoundsAllowed = new AtomicInteger(1);
    Game.Result result = game(
        players, player -> cups.get(player.id()), Strategy.OfPlayers.NOBODY_DECIDES
    ).playUntilStopped(() -> additionalRoundsAllowed.getAndDecrement() > 0);

    assertThat(result.journal()).containsSubsequence(
        new Entry.Bankrupt(ironBox.id(), null),
        new Entry.TurnStarted(highHat.id(), new Money(1500)),
        new Entry.TurnStarted(dog.id(), new Money(1500))
    );
  }

  @Test
  void aGamePlaysAnotherRoundWhenToldItMay() {
    AtomicInteger additionalRoundsAllowed = new AtomicInteger(1);

    Game.Result result = game(players, Cup.of(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(4, 3),
        new Roll(1, 3), new Roll(2, 3), new Roll(1, 4)
    )).playUntilStopped(() -> additionalRoundsAllowed.getAndDecrement() > 0);

    assertThat(result.journal()).containsSubsequence(
        new Entry.TurnStarted(Pawn.dog.id(), new Money(1500)),
        new Entry.Moved(Pawn.dog.id(), 0, 7),
        new Entry.TurnStarted(Pawn.dog.id(), new Money(1500))
    );
    assertThat(result.winner()).isEmpty();
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

    game(players, player -> cups.get(player.id())).play();

    assertThat(positionsOf(players)).containsExactly(3, 6, 7);
  }

  @Test
  void landingOnIncomeTaxChargesTheFixedTax() {
    Game.Result result = play(
        new Roll(2, 2), new Roll(5, 5), new Roll(3, 3),
        new Roll(1, 2), new Roll(2, 4), new Roll(1, 3)
    );

    Player dog = players.getFirst();
    assertThat(spaceAt(dog.position().index())).isInstanceOf(TaxSpace.class);
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1300));
    assertThat(result.journal()).containsSubsequence(
        new Entry.Moved(Pawn.dog.id(), 0, 4),
        new Entry.BankPaid(Pawn.dog.id(), new Money(200))
    );
  }

  @Test
  void landingOnFreeParkingChangesNothingButThePawnPosition() {
    players.getFirst().position().moveTo(17);

    Game.Result result = play(
        new Roll(5, 5), new Roll(1, 1), new Roll(1, 2),
        new Roll(1, 2), new Roll(4, 6), new Roll(4, 6)
    );

    Player dog = players.getFirst();
    assertThat(dog.position().index()).isEqualTo(20);
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(result.journal()).containsSubsequence(new Entry.Moved(Pawn.dog.id(), 17, 20));
  }

  @Test
  void aGameSellsUnownedLandToWhoeverStopsOnItAndAgreesToBuyIt() {
    Game.Result result = playWith(Map.of(Pawn.dog.id(), new Greedo()));

    assertThat(result.deeds().ownerOf(Street.Type.DiestsestraatLeuven)).contains(Pawn.dog.id());
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1448));
  }

  @Test
  void aGameAccountsForAPurchaseAfterTheMoveThatReachedIt() {
    Game.Result result = playWith(Map.of(Pawn.dog.id(), new Greedo()));

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

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new Greedo()), deeds);

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

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new Greedo()), deeds);

    assertThat(result.deeds().hasHotelOn(street(Street.Type.RueGrandeDinant))).isTrue();
    assertThat(result.deeds().hasHotelOn(street(Street.Type.DiestsestraatLeuven))).isTrue();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(0));
  }

  @Test
  void anAgreeablePlayerDoesNotBuildWhileAnyStreetInTheColourGroupIsMortgaged() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    deeds.arrangeMortgaged((ColourStreet) ruleSet.create(Street.Type.RueGrandeDinant));
    players.getFirst().account().withdraw(new Money(1400));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new Greedo()), deeds);

    assertThat(result.deeds().housesBuiltOn(street(Street.Type.RueGrandeDinant))).isZero();
    assertThat(result.deeds().housesBuiltOn(street(Street.Type.DiestsestraatLeuven))).isZero();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(100));
  }

  @Test
  void aGameRecordsWhenBuildingIsRefusedBecauseAStreetInTheColourGroupIsMortgaged() {
    Deeds deeds = monopolyFor(Pawn.dog.id());
    deeds.arrangeMortgaged((ColourStreet) ruleSet.create(Street.Type.RueGrandeDinant));
    players.getFirst().account().withdraw(new Money(1400));

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new Greedo()), deeds);

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

    Game.Result result = playWithQuietTurns(Map.of(Pawn.dog.id(), new Greedo()), deeds);

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
        Map.of(Pawn.high_hat.id(), new Greedo()),
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
        Map.of(Pawn.high_hat.id(), new Greedo()),
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
  void aChanceCardIsRecordedBeforeItsDirectBankPayout() {
    Game.Result result = playWithCards(
        Map.of(),
        new Deeds(),
        new Roll(3, 4),
        "De bank betaald je een dividend van M50.",
        null
    );

    assertThat(result.journal()).containsSubsequence(
        new Entry.ChanceCardDrawn(Pawn.dog.id(), "De bank betaald je een dividend van M50."),
        new Entry.BankReceived(Pawn.dog.id(), new Money(50))
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
  void aChanceCardCanBuyBuurtspoorwegenWhenItIsTheNearestStation() {
    players.getFirst().position().moveTo(15);
    Deeds deeds = new Deeds();

    playWithCards(
        Map.of(Pawn.dog.id(), new Greedo()), deeds, new Roll(3, 4),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        null
    );

    assertThat(deeds.ownerOf(Street.Type.Buurtspoorwegen)).contains(Pawn.dog.id());
    assertThat(deeds.ownerOf(Street.Type.CentraalStation)).isEmpty();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1300));
  }

  @Test
  void aChanceCardNearestStationBoundariesChooseTheNextStation() {
    assertNearestStationFrom(15, Street.Type.Buurtspoorwegen);
    assertNearestStationFrom(25, Street.Type.ZuidStation);
    assertNearestStationFrom(35, Street.Type.NoordStation);
  }

  @Test
  void aChanceCardAdvancingBackwardToNoordStationDoesNotCollectSalary() {
    players.getFirst().position().moveTo(36);

    resolveChanceCardAt(
        36, new Deeds(), Map.of(),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        new Cards.Events() {
        }
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.NoordStation));
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1500));
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

  @Test
  void aChanceCardChargesWatermaatschappijRentWhenItIsTheNearestUtility() {
    players.getFirst().position().moveTo(15);
    Deeds deeds = new Deeds();
    deeds.sell(
        (the.monopoly.game.components.streets.Utility) ruleSet.create(Street.Type.Watermaatschappij),
        players.get(1),
        new Money(150)
    );
    players.get(1).account().deposit(new Money(150));

    play(
        Map.of(Pawn.high_hat.id(), new Greedo()),
        deeds,
        Map.of(
            Pawn.dog.id(), Cup.of(new Roll(5, 5), new Roll(3, 4), new Roll(3, 4)),
            Pawn.high_hat.id(), Cup.of(new Roll(1, 1), new Roll(4, 6)),
            Pawn.iron_box.id(), Cup.of(new Roll(1, 2), new Roll(4, 6))
        ),
        chanceDeck("Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.")
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.Watermaatschappij));
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1430));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1570));
  }

  @Test
  void aChanceCardNearestUtilityBoundariesChooseTheNextUtility() {
    assertNearestUtilityFrom(12, Street.Type.Watermaatschappij);
    assertNearestUtilityFrom(28, Street.Type.Elektriciteitscentrale);
  }

  @Test
  void aChanceCardAdvancingBackwardToElektriciteitscentraleDoesNotCollectSalary() {
    players.getFirst().position().moveTo(36);
    Deeds deeds = new Deeds();
    deeds.sell(
        (the.monopoly.game.components.streets.Utility) ruleSet.create(Street.Type.Elektriciteitscentrale),
        players.get(1),
        new Money(150)
    );
    players.get(1).account().deposit(new Money(150));

    resolveChanceCardAt(
        36, deeds, Map.of(),
        "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.",
        new Cards.Events() {
        },
        new Roll(3, 4)
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.Elektriciteitscentrale));
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1430));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1570));
  }

  @Test
  void aChanceCardAdvancingFromStartToStartCollectsSalary() {
    List<Entry> journal = new ArrayList<>();

    resolveChanceCardAt(
        0, new Deeds(), Map.of(),
        "Ga door naar START (Ontvang M200).",
        new Cards.Events() {
          @Override
          public void collectedSalary(Player player, Money salary) {
            journal.add(new Entry.SalaryCollected(player.id(), salary));
          }
        }
    );

    assertThat(players.getFirst().position().index()).isZero();
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1700));
    assertThat(journal).containsExactly(new Entry.SalaryCollected(Pawn.dog.id(), new Money(200)));
  }

  @Test
  void aChanceCardToRueDeDiekirchArlonBuysItWhenUnownedAndAccepted() {
    Deeds deeds = new Deeds();

    resolveChanceCardAt(
        0, deeds, Map.of(Pawn.dog.id(), new Greedo()),
        "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.",
        new Cards.Events() {
        }
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.RueDeDiekirchArlon));
    assertThat(deeds.ownerOf(Street.Type.RueDeDiekirchArlon)).contains(Pawn.dog.id());
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1360));
  }

  @Test
  void aChanceCardToRueDeDiekirchArlonChargesTheOwnerVacantRentWhenOwnedByAnotherPlayer() {
    Deeds deeds = new Deeds();
    Player highHat = players.get(1);
    giveStreetTo(deeds, highHat, street(Street.Type.RueDeDiekirchArlon));
    List<Entry> journal = new ArrayList<>();

    resolveChanceCardAt(
        0, deeds, Map.of(highHat.id(), biddingAndClaiming(0)),
        "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.",
        rentJournal(journal)
    );

    assertThat(players.getFirst().position().index())
        .isEqualTo(ruleSet.gameboard().positionOf(Street.Type.RueDeDiekirchArlon));
    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1490));
    assertThat(journal).containsExactly(
        new Entry.RentPaid(Pawn.dog.id(), highHat.id(), Street.Type.RueDeDiekirchArlon, new Money(10))
    );
  }

  @Test
  void aChanceCardDoesNotChargeSpecialRentWhenTheNearestStationIsOwnedByTheDrawer() {
    Player dog = players.getFirst();
    dog.position().moveTo(15);
    Deeds deeds = new Deeds();
    deeds.sell(
        (the.monopoly.game.components.streets.Station) ruleSet.create(Street.Type.Buurtspoorwegen),
        dog,
        new Money(200)
    );
    dog.account().deposit(new Money(200));
    List<Entry> journal = new ArrayList<>();

    resolveChanceCardAt(
        15, deeds, Map.of(),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        rentJournal(journal)
    );

    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(journal).filteredOn(Entry.RentPaid.class::isInstance).isEmpty();
  }

  @Test
  void aChanceCardDoesNotChargeSpecialRentWhenTheNearestStationIsMortgaged() {
    players.getFirst().position().moveTo(15);
    Deeds deeds = new Deeds();
    var station = (the.monopoly.game.components.streets.Station) ruleSet.create(Street.Type.Buurtspoorwegen);
    deeds.sell(station, players.get(1), new Money(200));
    players.get(1).account().deposit(new Money(200));
    deeds.arrangeMortgaged(station);

    resolveChanceCardAt(
        15, deeds, Map.of(),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        new Cards.Events() {
        }
    );

    assertThat(players.getFirst().account().balance()).isEqualTo(Balance.of(1500));
    assertThat(players.get(1).account().balance()).isEqualTo(Balance.of(1500));
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

  private void assertNearestStationFrom(int position, Street.Type expectedStation) {
    players.getFirst().position().moveTo(position);

    resolveChanceCardAt(
        position, new Deeds(), Map.of(),
        "Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.",
        new Cards.Events() {
        }
    );

    assertThat(players.getFirst().position().index()).isEqualTo(ruleSet.gameboard().positionOf(expectedStation));
  }

  private void assertNearestUtilityFrom(int position, Street.Type expectedUtility) {
    players.getFirst().position().moveTo(position);

    resolveChanceCardAt(
        position, new Deeds(), Map.of(Pawn.dog.id(), new Greedo()),
        "Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.",
        new Cards.Events() {
        }
    );

    assertThat(players.getFirst().position().index()).isEqualTo(ruleSet.gameboard().positionOf(expectedUtility));
  }

  private void resolveChanceCardAt(
      int position, Deeds deeds, Map<Player.ID, Strategy> strategies, String chanceCard, Cards.Events events,
      Roll... extraRolls
  ) {
    Player dog = players.getFirst();
    dog.position().moveTo(position);
    new Cards(
        deeds,
        ruleSet,
        players,
        player -> strategies.getOrDefault(player.id(), Strategy.UNDECIDED),
        chanceDeck(chanceCard),
        events,
        Cup.of(extraRolls)
    ).resolve(dog, ruleSet.create(Street.Type.Kans), new Roll(1, 1));
  }

  private static Cards.Events rentJournal(List<Entry> journal) {
    return new Cards.Events() {
      @Override
      public void paid(Player tenant, Player owner, Ownable land, Money rent) {
        journal.add(new Entry.RentPaid(tenant.id(), owner.id(), land.type(), rent));
      }
    };
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
    return game(players, Cup.of(rolls)).play();
  }

  /**
   * {@code Game}'s own convenience constructors now default to the shuffled
   * official decks, correct for real play but wrong for the fixed-roll,
   * fixed-position assertions most of this class makes: a chance or
   * community chest landing drawing a real card would move a pawn, or change
   * a balance, this class never asked for. These helpers keep every other
   * test in this file deterministic by asking for {@link Cards.Decks#EMPTY}
   * explicitly, the way the handful of tests that do care about a specific
   * card already do via {@link #decks}.
   */
  private Game game(List<Player> players, Cup cup) {
    return game(players, player -> cup);
  }

  private Game game(List<Player> players, Game.Cups cups) {
    return game(players, cups, Strategy.OfPlayers.NOBODY_DECIDES, new Deeds());
  }

  private Game game(List<Player> players, Game.Cups cups, Strategy.OfPlayers strategies) {
    return game(players, cups, strategies, new Deeds());
  }

  private Game game(List<Player> players, Game.Cups cups, Strategy.OfPlayers strategies, Deeds deeds) {
    return new Game(ruleSet, players, cups, strategies, deeds, Cards.Decks.EMPTY);
  }
}
