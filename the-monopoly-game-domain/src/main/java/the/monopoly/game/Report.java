package the.monopoly.game;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/**
 * A game told as it happened, one line per entry, in the order the journal
 * recorded them.
 * <p>
 * The wording lives here and nowhere else: the journal keeps what happened as
 * data, so that how it reads can change without the game being touched.
 */
public final class Report {
  private static final Map<Class<? extends Entry>, Function<Entry, String>> LINES = Map.of(
      Entry.Start.class, entry -> start((Entry.Start) entry),
      Entry.InitiativeRoll.class, entry -> initiativeRoll((Entry.InitiativeRoll) entry),
      Entry.InitiativeWon.class, entry -> initiativeWon((Entry.InitiativeWon) entry),
      Entry.TurnStarted.class, entry -> turnStarted((Entry.TurnStarted) entry),
      Entry.Rolled.class, entry -> rolled((Entry.Rolled) entry),
      Entry.Moved.class, entry -> moved((Entry.Moved) entry),
      Entry.SalaryCollected.class, entry -> salaryCollected((Entry.SalaryCollected) entry)
  );

  private Report() {
  }

  public static String of(List<Entry> journal) {
    return journal.stream().map(Report::line).collect(joining("\n"));
  }

  private static String line(Entry entry) {
    return LINES.get(entry.getClass()).apply(entry);
  }

  private static String start(Entry.Start entry) {
    return "The game starts with " + names(entry.players());
  }

  private static String initiativeRoll(Entry.InitiativeRoll entry) {
    return name(entry.player()) + " rolls " + entry.total() + " for initiative";
  }

  private static String initiativeWon(Entry.InitiativeWon entry) {
    return name(entry.player()) + " wins initiative";
  }

  private static String turnStarted(Entry.TurnStarted entry) {
    return name(entry.player()) + " starts a turn";
  }

  private static String rolled(Entry.Rolled entry) {
    return name(entry.player()) + " rolls a total of " + entry.total();
  }

  private static String moved(Entry.Moved entry) {
    return name(entry.player()) + " moves from position " + entry.from() + " to " + entry.to();
  }

  private static String salaryCollected(Entry.SalaryCollected entry) {
    return name(entry.player()) + " collects a salary of $" + entry.salary().amount();
  }

  private static String names(List<Player.ID> players) {
    return players.stream().map(Report::name).collect(joining(", "));
  }

  private static String name(Player.ID player) {
    return player.value();
  }
}
