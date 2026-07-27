package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Who starts, and who follows. The highest roll takes the first turn and play
 * then continues clockwise, which is the order the players are sitting in.
 */
public class Initiative {
  private final Rolls rolls;

  public Initiative(Rolls rolls) {
    this.rolls = rolls;
  }

  /** The seated players, rotated so that whoever wins initiative goes first. */
  public List<Player> order(List<Player> seated) {
    int winner = seated.indexOf(winnerAmong(seated));
    List<Player> order = new ArrayList<>(seated.subList(winner, seated.size()));
    order.addAll(seated.subList(0, winner));
    return order;
  }

  /**
   * Everyone rolls; the highest wins. Players who tie for the highest roll
   * settle it among themselves, as many times as it takes.
   */
  private Player winnerAmong(List<Player> players) {
    List<Player> contenders = players;
    while (contenders.size() > 1) {
      Map<Integer, List<Player>> byRoll = contenders.stream()
          .collect(groupingBy(rolls::totalFor, LinkedHashMap::new, toList()));
      contenders = byRoll.get(Collections.max(byRoll.keySet()));
    }
    return contenders.getFirst();
  }

  /** What a player rolls when rolling for initiative. */
  @FunctionalInterface
  public interface Rolls {
    int totalFor(Player player);
  }
}
