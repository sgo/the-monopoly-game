package the.monopoly.game;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private final List<Player> players = ruleSet.players().select(3).toList();

  @Test
  void aGameStartsWithWhoeverWonInitiative() {
    Game.Result result = play(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

    assertThat(result.turnOrder().getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void playThenContinuesClockwiseFromTheWinner() {
    Game.Result result = play(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

    assertThat(result.turnOrder()).extracting(Player::id)
        .containsExactly(Pawn.high_hat.id(), Pawn.iron_box.id(), Pawn.dog.id());
  }

  @Test
  void aTiedGameIsSettledBeforeTheFirstTurn() {
    Game.Result result = play(
        new Roll(4, 4), new Roll(4, 4), new Roll(2, 3),
        new Roll(3, 3), new Roll(5, 4)
    );

    assertThat(result.turnOrder().getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void aGameAccountsForWhoIsPlayingAndInWhatOrder() {
    Game.Result result = play(new Roll(2, 2), new Roll(5, 5), new Roll(3, 3));

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

  private Game.Result play(Roll... rolls) {
    return new Game(ruleSet, players, Cup.of(rolls)).play();
  }
}
