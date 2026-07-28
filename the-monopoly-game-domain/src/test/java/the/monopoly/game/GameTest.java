package the.monopoly.game;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.AgreeIfAffordable;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.Map;
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
    Map<Player.ID, Cup> cups = Map.of(
        Pawn.dog.id(), Cup.of(new Roll(5, 5), new Roll(1, 2)),
        Pawn.high_hat.id(), Cup.of(new Roll(1, 1), new Roll(1, 2)),
        Pawn.iron_box.id(), Cup.of(new Roll(1, 2), new Roll(1, 2))
    );
    return new Game(
        ruleSet, players,
        player -> cups.get(player.id()),
        player -> strategies.getOrDefault(player.id(), Strategy.UNDECIDED)
    ).play();
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
