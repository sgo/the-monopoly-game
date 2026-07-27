package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.IntDistribution;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.players.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

/**
 * Initiative.order only ever runs against a handful of hand-picked rolls in
 * the example-based tests. This property sweeps player counts and roll
 * assignments to pin down what those examples only sample: with no tie for the
 * highest roll, the winner leads, play still runs round the table in the
 * direction the players are sitting, and nobody is added, dropped, or
 * duplicated.
 */
@Tag("property-test")
class InitiativePropertyTest {
  @Test
  void theWinnerLeadsAndPlayContinuesClockwise() {
    PropertyChecker.forAll(distinctRolls(), rolls -> {
      List<Player> seated = Rule.Set.Type.official.create().players().select(rolls.size()).toList();
      Map<Player.ID, Integer> rollById = rollById(seated, rolls);

      List<Player> order = new Initiative(player -> rollById.get(player.id())).order(seated);

      return everyoneTakesOneTurn(order, seated)
          && order.getFirst().equals(highestRoller(seated, rollById))
          && seatingIsUnbroken(order, seated);
    });
  }

  private static boolean everyoneTakesOneTurn(List<Player> order, List<Player> seated) {
    return order.size() == seated.size() && new HashSet<>(order).equals(new HashSet<>(seated));
  }

  /**
   * Whoever sits clockwise of a player also plays after them. Stating the rule
   * this way rather than building the expected list keeps the property from
   * restating the rotation {@link Initiative#order} performs, which would only
   * ever agree with itself.
   */
  private static boolean seatingIsUnbroken(List<Player> order, List<Player> seated) {
    return IntStream.range(0, order.size()).allMatch(turn ->
        after(order, turn).equals(after(seated, seated.indexOf(order.get(turn)))));
  }

  private static Player after(List<Player> players, int index) {
    return players.get((index + 1) % players.size());
  }

  private static Player highestRoller(List<Player> seated, Map<Player.ID, Integer> rollById) {
    return Collections.max(seated, Comparator.comparingInt(player -> rollById.get(player.id())));
  }

  private static Map<Player.ID, Integer> rollById(List<Player> seated, List<Integer> rolls) {
    return IntStream.range(0, seated.size()).boxed()
        .collect(toMap(index -> seated.get(index).id(), rolls::get));
  }

  private static Generator<List<Integer>> distinctRolls() {
    return Generator.integers(2, 8).flatMap(n ->
        Generator.listsOf(IntDistribution.uniform(n, n), Generator.integers(0, 1_000_000))
            .suchThat(candidate -> new HashSet<>(candidate).size() == n)
    );
  }
}
