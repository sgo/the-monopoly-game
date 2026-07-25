package the.monopoly.game;

import org.slf4j.Logger;
import the.monopoly.game.Game.Journal.Entry.RollForInitiative;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

public record Game(Rule.Set rules, List<Player> players) {
  public Result play() {
    var journal = new Journal();
    journal.log(new Journal.Entry.Start(players.stream().map(Player::id).toArray(Player.ID[]::new)));
    // TODO - there are no tests to assert that the code below is correct
    RollForInitiative rollForInitiative = new RollForInitiative(players.stream().collect(groupingBy(
        player -> rules.dice().map(Dice::roll).toList()
    )));
    journal.log(rollForInitiative);
    return new Result();
  }

  public record Result() {
  }

  public class Journal {
    private static final Logger logger = getLogger(Journal.class);

    private final List<Entry> entries = new ArrayList<>();

    public void log(Entry evt) {
      logger.info(evt.toString());
    }

    public interface Entry {
      record Start(Player.ID... players) implements Entry {
        @Override
        public String toString() {
          return """
              Start game with players:
              - %s
              """.formatted(join(
              "\n- ",
              Stream.of(players).map(Player.ID::value).toArray(String[]::new))
          );
        }
      }

      record RollForInitiative(Map<List<Dice.Face>, List<Player>> result) implements Entry {
        @Override
        public String toString() {
          return """
              roll for initiative:
              - %s
              Winner: %s
              """.formatted(join("\n- ", result
              .entrySet()
              .stream()
              .map(it -> it.getKey().stream()
                  .map(Dice.Face::symbol)
                  .collect(joining(" + ")) + ": " + it.getValue().stream().map(player -> player.id().value()).collect(joining(", ")))
              .toList()), winner().value());
        }

        private Player.ID winner() {
          int winningRoll = result.keySet().stream().map(RollForInitiative::sum).sorted().toList().getLast();
          return result.keySet().stream()
              .filter(it -> sum(it) == winningRoll)
              .map(result::get)
              .findFirst()
              .get()
              .getFirst()
              .id();
        }

        private static int sum(List<Dice.Face> faces) {
          return faces.stream().map(it -> Integer.parseInt(it.symbol())).reduce(0, Integer::sum);
        }
      }
    }
  }
}
