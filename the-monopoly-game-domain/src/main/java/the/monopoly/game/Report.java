package the.monopoly.game;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

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
      Entry.SalaryCollected.class, entry -> salaryCollected((Entry.SalaryCollected) entry),
      Entry.Bought.class, entry -> bought((Entry.Bought) entry),
      Entry.AuctionWon.class, entry -> auctionWon((Entry.AuctionWon) entry)
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

  private static String bought(Entry.Bought entry) {
    return name(entry.player()) + " buys " + spaceName(entry.land()) + " for $" + entry.price().amount();
  }

  private static String auctionWon(Entry.AuctionWon entry) {
    return name(entry.player()) + " wins the auction for " + spaceName(entry.land()) + " at $" + entry.price().amount();
  }

  /**
   * A space is named on the board in words, and in the domain as one name, so
   * the run-together words are told apart again here. A space whose printed
   * name is not its own name spelled out will have to be given one when a
   * specification asks the report for it.
   */
  private static String spaceName(Street.Type land) {
    return land.name().replaceAll("(?<=[a-z])(?=[A-Z])", " ");
  }

  private static String names(List<Player.ID> players) {
    return players.stream().map(Report::name).collect(joining(", "));
  }

  private static String name(Player.ID player) {
    return player.value();
  }
}

/* mutate4java-manifest
version=1
moduleHash=bd2d142aaa0ddd848415988e83db9497855468fae040f94950c45689810d0ad9
scope.0.id=Y2xhc3M6UmVwb3J0I1JlcG9ydDoxOA
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=73
scope.0.semanticHash=655c29996be96996e971c9770f09ad4257e45524563432a2e787584612a8cb2c
scope.1.id=bWV0aG9kOlJlcG9ydCNjdG9yKDApOjE5
scope.1.kind=method
scope.1.startLine=19
scope.1.endLine=20
scope.1.semanticHash=fa4ab1d8c774b5a49e26e7b36ba0ec25ba0d7069b207d38eb37beccb7d02dc9c
scope.2.id=bWV0aG9kOlJlcG9ydCNsaW5lKDEpOjQx
scope.2.kind=method
scope.2.startLine=41
scope.2.endLine=54
scope.2.semanticHash=a88fbaba922aeeed1d50410a2db255de8519cf4e27dd0f2d8eba413152310297
scope.3.id=bWV0aG9kOlJlcG9ydCNuYW1lKDEpOjcw
scope.3.kind=method
scope.3.startLine=70
scope.3.endLine=72
scope.3.semanticHash=cd39b08d1576cbc2e9d2ae25bb54b6b608f1219509ee3d03086e8f3ab4c238fa
scope.4.id=bWV0aG9kOlJlcG9ydCNuYW1lcygxKTo2Ng
scope.4.kind=method
scope.4.startLine=66
scope.4.endLine=68
scope.4.semanticHash=7baca973d9baa23bf1205536bbfd229d494b1e2c863e02f6e50c5ce7dcb42959
scope.5.id=bWV0aG9kOlJlcG9ydCNvZigxKToyMg
scope.5.kind=method
scope.5.startLine=22
scope.5.endLine=24
scope.5.semanticHash=3bddf559e6362200e39e3d2024808e23da1e3da3deb262e8b89f1402e9edd25e
scope.6.id=bWV0aG9kOlJlcG9ydCNzcGFjZU5hbWUoMSk6NjI
scope.6.kind=method
scope.6.startLine=62
scope.6.endLine=64
scope.6.semanticHash=56eb8b748063de6798efd6f1a96d5a30f07cf019c0478f7f004006d2fe61eaf8
*/
