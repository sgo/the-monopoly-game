package the.monopoly.game;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.rules.Rule;

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
  void aGameAccountsForWhoIsPlayingAndInWhatOrder() {
    Game.Result result = playInitiative(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

    assertThat(result.journal()).containsExactly(
        new Game.Journal.Entry.Start(List.of(Pawn.dog.id(), Pawn.high_hat.id(), Pawn.iron_box.id())),
        new Game.Journal.Entry.TurnOrder(List.of(Pawn.high_hat.id(), Pawn.iron_box.id(), Pawn.dog.id()))
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
