package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

/**
 * What a played game says happened, asked the two ways the features ask it:
 * of the journal, which keeps entries, and of the report, which keeps words.
 * <p>
 * A scenario names a moment rather than an index, so every question here is
 * "where is this?" and the answer says what was there instead when it is
 * nowhere.
 */
final class GameAccount {
  private GameAccount() {
  }

  /** A moment a scenario names: one particular entry, or any entry of a kind. */
  record Claim(Predicate<Entry> matches, String description) {
    static Claim of(Entry entry) {
      return new Claim(entry::equals, entry.toString());
    }

    static Claim ofAny(Class<? extends Entry> kind) {
      return new Claim(kind::isInstance, "any " + kind.getSimpleName());
    }
  }

  static void records(World world, Claim claim) {
    recordedAt(world.journal(), claim);
  }

  static void recordsInOrder(World world, Claim first, Claim second) {
    List<Entry> journal = world.journal();
    if (recordedAt(journal, first) >= recordedAt(journal, second))
      throw new AssertionError(
          "The game journal records " + second.description() + " before "
              + first.description() + ":\n" + written(journal)
      );
  }

  /** Whether a game started with these pawns seated in this order. */
  static void recordsStartWith(World world, String firstPawn, String secondPawn) {
    List<String> seated = world.journal().stream()
        .filter(Entry.Start.class::isInstance)
        .map(Entry.Start.class::cast)
        .findFirst()
        .orElseThrow(() -> new AssertionError("The game journal records no start."))
        .players().stream().map(it -> it.value()).toList();
    if (seated.indexOf(firstPawn) < 0 || seated.indexOf(secondPawn) < 0
        || seated.indexOf(firstPawn) >= seated.indexOf(secondPawn))
      throw new AssertionError(
          "The game started with " + seated + ", not with \"" + firstPawn
              + "\" before \"" + secondPawn + "\"."
      );
  }

  static void saysInOrder(World world, String first, String second) {
    String report = world.report();
    if (saidAt(report, first) >= saidAt(report, second))
      throw new AssertionError(
          "The game report says \"" + second + "\" before \"" + first + "\":\n" + report
      );
  }

  /** Whether the line naming everyone at the table names these two in this order. */
  static void saysStartWith(World world, String firstPawn, String secondPawn) {
    String start = world.report().lines()
        .filter(it -> it.startsWith("The game starts with "))
        .findFirst()
        .orElseThrow(() -> new AssertionError("The game report says nothing about the game starting."));
    if (start.indexOf(firstPawn) < 0 || start.indexOf(secondPawn) < 0
        || start.indexOf(firstPawn) >= start.indexOf(secondPawn))
      throw new AssertionError(
          "The game report says \"" + start + "\", not \"" + firstPawn
              + "\" before \"" + secondPawn + "\"."
      );
  }

  private static int recordedAt(List<Entry> journal, Claim claim) {
    for (int at = 0; at < journal.size(); at++)
      if (claim.matches().test(journal.get(at))) return at;
    throw new AssertionError(
        "The game journal records no " + claim.description() + "; it records:\n" + written(journal)
    );
  }

  private static int saidAt(String report, String phrase) {
    int at = report.indexOf(phrase);
    if (at < 0)
      throw new AssertionError("The game report never says \"" + phrase + "\":\n" + report);
    return at;
  }

  private static String written(List<Entry> journal) {
    return journal.stream().map(Entry::toString).collect(joining("\n"));
  }
}
