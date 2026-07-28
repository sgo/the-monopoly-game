package the.monopoly.game;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * A game told as it happened, one line per entry, in the order the journal
 * recorded them.
 * <p>
 * The wording lives here and nowhere else: the journal keeps what happened as
 * data, so that how it reads can change without the game being touched.
 */
public final class Report {
  private Report() {
  }

  public static String of(List<Entry> journal) {
    return journal.stream().map(Report::line).collect(joining("\n"));
  }

  /**
   * Switching over the sealed {@link Entry} rather than looking a handler up by
   * class is what makes a new kind of entry a compile error here. Told any
   * other way — a map keyed by class, a chain of instanceof — an entry nobody
   * wrote a line for compiles, and goes silently unreported until a game hits
   * it. That is the guarantee {@link Entry} is sealed for, and it is what asked
   * for the last two lines below when buying and auctions arrived.
   * <p>
   * One case per entry is one branch per entry, so complexity counts as high as
   * the journal is wide. That is the shape of the thing being described, not
   * complication to be refactored away; a form that measures lower gets there
   * by hiding the same branching from the compiler as well as the metric. When
   * the cases outgrow a screen, give each one a named method and leave the
   * switch itself alone.
   */
  private static String line(Entry entry) {
    return switch (entry) {
      case Entry.Start it -> "The game starts with " + names(it.players());
      case Entry.InitiativeRoll it -> name(it.player()) + " rolls " + it.total() + " for initiative";
      case Entry.InitiativeWon it -> name(it.player()) + " wins initiative";
      case Entry.TurnStarted it -> name(it.player()) + " starts a turn";
      case Entry.Rolled it -> name(it.player()) + " rolls a total of " + it.total();
      case Entry.Moved it -> name(it.player()) + " moves from position " + it.from() + " to " + it.to();
      case Entry.SalaryCollected it -> name(it.player()) + " collects a salary of $" + it.salary().amount();
      case Entry.Bought it -> name(it.player()) + " buys " + spaceName(it.land()) + " for $" + it.price().amount();
      case Entry.AuctionWon it ->
          name(it.player()) + " wins the auction for " + spaceName(it.land()) + " at $" + it.price().amount();
    };
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
