package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class InitiativeTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private final List<Player> players = ruleSet.players().select(3).toList();

  @Test
  void theHighestRollTakesTheFirstTurn() {
    assertThat(orderWithAClearWinner().getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void playThenContinuesClockwiseFromTheWinner() {
    assertThat(orderWithAClearWinner()).extracting(Player::id)
        .containsExactly(Pawn.high_hat.id(), Pawn.iron_box.id(), Pawn.dog.id());
  }

  @Test
  void everyPlayerTakesATurnInTheOrder() {
    assertThat(orderWithAClearWinner()).containsExactlyInAnyOrderElementsOf(players);
  }

  @Test
  void onlyTheTiedPlayersRollAgain() {
    assertThat(orderWithATie().getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void tiedPlayersKeepRollingUntilOneOfThemWins() {
    List<Player> order = order(Map.of(
        Pawn.dog, List.of(8, 7, 3),
        Pawn.high_hat, List.of(8, 7, 11),
        Pawn.iron_box, List.of(5)
    ));

    assertThat(order.getFirst().id()).isEqualTo(Pawn.high_hat.id());
  }

  @Test
  void aPlayerWhoLostTheFirstRoundDoesNotRollAgain() {
    assertThat(orderWithATie()).extracting(Player::id)
        .containsExactly(Pawn.high_hat.id(), Pawn.iron_box.id(), Pawn.dog.id());
  }

  private List<Player> orderWithAClearWinner() {
    return order(Map.of(
        Pawn.dog, List.of(4),
        Pawn.high_hat, List.of(10),
        Pawn.iron_box, List.of(6)
    ));
  }

  private List<Player> orderWithATie() {
    return order(Map.of(
        Pawn.dog, List.of(8, 6),
        Pawn.high_hat, List.of(8, 9),
        Pawn.iron_box, List.of(5)
    ));
  }

  private List<Player> order(Map<Pawn, List<Integer>> rolls) {
    Map<String, Deque<Integer>> queues = rolls.entrySet().stream().collect(toMap(
        entry -> entry.getKey().pawnName(),
        entry -> new ArrayDeque<>(entry.getValue())
    ));
    Initiative.Rolls source = player -> {
      Deque<Integer> queue = queues.get(player.id().value());
      if (queue == null || queue.isEmpty())
        throw new IllegalStateException("No initiative roll left for " + player.id().value() + ".");
      return queue.removeFirst();
    };
    return new Initiative(source).order(players);
  }
}
