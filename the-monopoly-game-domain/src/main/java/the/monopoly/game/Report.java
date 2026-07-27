package the.monopoly.game;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;

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
   * other way, one would simply go unreported.
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
    };
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
moduleHash=b71c6f5b882cd2e347a6e6ab2e3980b68c7736cdf4969b5875dd721b780f34d9
scope.0.id=Y2xhc3M6UmVwb3J0I1JlcG9ydDoxNw
scope.0.kind=class
scope.0.startLine=17
scope.0.endLine=49
scope.0.semanticHash=fb4ca5525a456e9a992f15bfb4e0190626d0a9ede349804f4eed581c86d25348
scope.1.id=bWV0aG9kOlJlcG9ydCNjdG9yKDApOjE4
scope.1.kind=method
scope.1.startLine=18
scope.1.endLine=19
scope.1.semanticHash=fa4ab1d8c774b5a49e26e7b36ba0ec25ba0d7069b207d38eb37beccb7d02dc9c
scope.2.id=bWV0aG9kOlJlcG9ydCNsaW5lKDEpOjMw
scope.2.kind=method
scope.2.startLine=30
scope.2.endLine=40
scope.2.semanticHash=6a5837f79119034af38b21a9f2c8fde1596d803e7aac6130517f06366b07b640
scope.3.id=bWV0aG9kOlJlcG9ydCNuYW1lKDEpOjQ2
scope.3.kind=method
scope.3.startLine=46
scope.3.endLine=48
scope.3.semanticHash=cd39b08d1576cbc2e9d2ae25bb54b6b608f1219509ee3d03086e8f3ab4c238fa
scope.4.id=bWV0aG9kOlJlcG9ydCNuYW1lcygxKTo0Mg
scope.4.kind=method
scope.4.startLine=42
scope.4.endLine=44
scope.4.semanticHash=7baca973d9baa23bf1205536bbfd229d494b1e2c863e02f6e50c5ce7dcb42959
scope.5.id=bWV0aG9kOlJlcG9ydCNvZigxKToyMQ
scope.5.kind=method
scope.5.startLine=21
scope.5.endLine=23
scope.5.semanticHash=3bddf559e6362200e39e3d2024808e23da1e3da3deb262e8b89f1402e9edd25e
*/
