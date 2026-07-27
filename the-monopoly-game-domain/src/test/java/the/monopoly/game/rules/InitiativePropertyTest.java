package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.IntDistribution;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.players.Player;

import java.util.ArrayList;
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
 * assignments to pin down what those examples only sample: with no tie for
 * the highest roll, the winner leads and everyone else keeps their seating
 * order, and nobody is added, dropped, or duplicated.
 */
@Tag("property-test")
class InitiativePropertyTest {
  @Test
  void theWinnerLeadsAndEveryoneElseKeepsTheirSeatingOrder() {
    PropertyChecker.forAll(distinctRolls(), rolls -> {
      List<Player> seated = Rule.Set.Type.official.create().players().select(rolls.size()).toList();
      Map<Player.ID, Integer> rollById = rollById(seated, rolls);

      List<Player> order = new Initiative(player -> rollById.get(player.id())).order(seated);

      return order.size() == seated.size()
          && new HashSet<>(order).equals(new HashSet<>(seated))
          && order.equals(rotatedToStartAtTheWinner(seated, rollById));
    });
  }

  private static List<Player> rotatedToStartAtTheWinner(List<Player> seated, Map<Player.ID, Integer> rollById) {
    Player winner = Collections.max(seated, Comparator.comparingInt(p -> rollById.get(p.id())));
    int winnerIndex = seated.indexOf(winner);
    List<Player> rotated = new ArrayList<>(seated.subList(winnerIndex, seated.size()));
    rotated.addAll(seated.subList(0, winnerIndex));
    return rotated;
  }

  private static Map<Player.ID, Integer> rollById(List<Player> seated, List<Integer> rolls) {
    return IntStream.range(0, seated.size()).boxed()
        .collect(toMap(i -> seated.get(i).id(), rolls::get));
  }

  private static Generator<List<Integer>> distinctRolls() {
    return Generator.integers(2, 8).flatMap(n ->
        Generator.listsOf(IntDistribution.uniform(n, n), Generator.integers(0, 1_000_000))
            .suchThat(candidate -> new HashSet<>(candidate).size() == n)
    );
  }
}
