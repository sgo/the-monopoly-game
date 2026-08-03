package the.monopoly.game.cli;

import the.monopoly.game.Game;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private static final Map<String, Supplier<Strategy>> STRATEGIES = Map.of(
      "greedo", Greedo::new
  );

  private Simulator() {
  }

  /** Starts the simulator as a command-line program. */
  public static void main(String[] arguments) {
    Result result = execute(arguments);
    System.out.println(result.output());
    if (!result.succeeded()) System.exit(result.exitCode());
  }

  /** Parses the command line without performing process termination. */
  public static Result execute(String... arguments) {
    if (isHelpRequested(arguments)) return new Result(0, usage());

    try {
      return runSelected(arguments);
    } catch (NumberFormatException cause) {
      String received = arguments.length == 0 ? "" : arguments[0];
      return new Result(1, "The number of players must be between 2 and 8; received " + received + " players.");
    } catch (IllegalArgumentException cause) {
      return new Result(1, cause.getMessage() + " " + usage());
    }
  }

  private static boolean isHelpRequested(String... arguments) {
    return arguments.length == 1 && (arguments[0].equals("-h") || arguments[0].equals("--h"));
  }

  private static Result runSelected(String... arguments) {
    int playerCount = arguments.length == 0 ? 2 : Integer.parseInt(arguments[0]);
    List<String> strategyNames = List.of(arguments).subList(Math.min(1, arguments.length), arguments.length);
    if (!strategyNames.isEmpty() && strategyNames.size() != playerCount)
      return new Result(1, "Supply one strategy for each player. " + usage());
    return run(playerCount, strategiesFor(playerCount, strategyNames));
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames) {
    List<String> names = strategyNames.isEmpty()
        ? java.util.Collections.nCopies(playerCount, "greedo")
        : strategyNames;
    Map<Player.ID, Strategy> selections = new HashMap<>();
    for (int index = 0; index < playerCount; index++) {
      String name = names.get(index);
      Supplier<Strategy> strategy = STRATEGIES.get(name);
      if (strategy == null) throw new IllegalArgumentException("Unknown strategy: " + name + ".");
      selections.put(Pawn.values()[index].id(), strategy.get());
    }
    return player -> selections.get(player.id());
  }

  private static String usage() {
    return "Usage: simulator [number of players] [strategy for each player]"
        + System.lineSeparator() + "Available strategies: " + String.join(", ", STRATEGIES.keySet());
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies) {
    return start(playerCount, strategies).awaitEnd();
  }

  /**
   * Starts the simulator playing in the background, so its progress can be
   * watched as it happens. The simulation runs until it is {@link
   * Running#stop() stopped} or the game reaches its natural end, whichever
   * comes first.
   */
  public static Running start(int playerCount, Strategy.OfPlayers strategies) {
    Result rejected = rejectOutOfRange(playerCount);
    if (rejected != null) return new Running(rejected);

    Rule.Set rules = Rule.Set.Type.official.create();
    List<Player> players = rules.players().select(playerCount).toList();
    return new Running(new Game(rules, players, player -> Cup.of(rules.dice().toList()), strategies));
  }

  private static Result rejectOutOfRange(int playerCount) {
    Rule.Set rules = Rule.Set.Type.official.create();
    if (playerCount < rules.players().min() || playerCount > rules.players().max())
      return new Result(1, "The number of players must be between 2 and 8; received " + playerCount + " players.");
    return null;
  }

  /**
   * A simulation being played out of sight, stopped before the game ends.
   * Stopping is cooperative: the game finishes the round it is on and then
   * ends, however long it would still have gone on.
   */
  public static final class Running {
    private final AtomicBoolean stopRequested = new AtomicBoolean();
    private final Thread thread;
    private volatile Result result;

    private Running(Game game) {
      thread = new Thread(() -> {
        Game.Result outcome = game.playUntilStopped(() -> !stopRequested.get());
        result = new Result(0, Report.of(outcome.journal()));
      }, "monopoly-simulator");
      thread.setDaemon(true);
      thread.start();
    }

    private Running(Result failure) {
      thread = null;
      result = failure;
    }

    /** Asks the simulation to stop before the game ends. */
    public void stop() {
      stopRequested.set(true);
    }

    /** Whether the simulation is still playing. */
    public boolean isPlaying() {
      return thread != null && thread.isAlive();
    }

    /** Waits for the simulation to end and returns how it ended. */
    public Result awaitEnd() {
      if (thread == null) return result;
      try {
        thread.join();
      } catch (InterruptedException cause) {
        Thread.currentThread().interrupt();
        throw new AssertionError("Waiting for the simulator to end was interrupted.", cause);
      }
      return result;
    }
  }


  public record Result(int exitCode, String output) {
    public boolean succeeded() {
      return exitCode == 0;
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=1e014c8ab3c9a3efb4a4fa6a77ae932c36455a7565e6233bbb32f0cbb225fffb
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoxOQ
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=165
scope.0.semanticHash=fc6485debfbcb1f8eb413a7c53c711c6467f73a98e536641764321498d563d7d
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6MTYw
scope.1.kind=class
scope.1.startLine=160
scope.1.endLine=164
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=Y2xhc3M6U2ltdWxhdG9yLlJ1bm5pbmcjUnVubmluZzoxMTc
scope.2.kind=class
scope.2.startLine=117
scope.2.endLine=157
scope.2.semanticHash=83e5ba6213d8b70e980392a109fb18d59a042afb98b554b6834b499533a8bbc7
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI1NUUkFURUdJRVM6MjA
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=22
scope.3.semanticHash=af6be9ee38f915f737e01b36d8591ba82a76480e48079c94b4aa9c42bd118663
scope.4.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZToxNjA
scope.4.kind=field
scope.4.startLine=160
scope.4.endLine=160
scope.4.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6MTYw
scope.5.kind=field
scope.5.startLine=160
scope.5.endLine=160
scope.5.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.6.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjcmVzdWx0OjEyMA
scope.6.kind=field
scope.6.startLine=120
scope.6.endLine=120
scope.6.semanticHash=d2e7f6ca2447c4aa1190311c997c469265754f0289baf3770fd1a906bc0d5ec5
scope.7.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjc3RvcFJlcXVlc3RlZDoxMTg
scope.7.kind=field
scope.7.startLine=118
scope.7.endLine=118
scope.7.semanticHash=191ae8305a204a5f6a55f8726d4a3b97cf9a327513b2f0a3d1be6f2c5846d473
scope.8.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjdGhyZWFkOjExOQ
scope.8.kind=field
scope.8.startLine=119
scope.8.endLine=119
scope.8.semanticHash=72ca458daec5b99afaa95b49fdaa041ec41371feab5d9c7c75c60d45a9c2fe62
scope.9.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjI0
scope.9.kind=method
scope.9.startLine=24
scope.9.endLine=25
scope.9.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.10.id=bWV0aG9kOlNpbXVsYXRvciNleGVjdXRlKDEpOjM1
scope.10.kind=method
scope.10.startLine=35
scope.10.endLine=46
scope.10.semanticHash=6da7ae3effd301a4efd70ebb9b4bb88df4d8ce1fe92f4d89cee8adfa227e4d17
scope.11.id=bWV0aG9kOlNpbXVsYXRvciNpc0hlbHBSZXF1ZXN0ZWQoMSk6NDg
scope.11.kind=method
scope.11.startLine=48
scope.11.endLine=50
scope.11.semanticHash=f60dc5d3b6fb064fb40f13514faf5e1d06898f82708991f4abfe1635a28db532
scope.12.id=bWV0aG9kOlNpbXVsYXRvciNtYWluKDEpOjI4
scope.12.kind=method
scope.12.startLine=28
scope.12.endLine=32
scope.12.semanticHash=ddfa3d4b690062a78d607287d22c2473d9290516e11b1b8ee722ea5f27325ee9
scope.13.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RPdXRPZlJhbmdlKDEpOjEwNQ
scope.13.kind=method
scope.13.startLine=105
scope.13.endLine=110
scope.13.semanticHash=07cad2fd67d3d8c72f2629443a191903e74160e2b7c87d492195e1776c66be96
scope.14.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6Nzk
scope.14.kind=method
scope.14.startLine=79
scope.14.endLine=88
scope.14.semanticHash=b593e1fb90ee2b5102c734e46ecb57e2dbec49b3f64cfde40ec7ce7cfb1f8a74
scope.15.id=bWV0aG9kOlNpbXVsYXRvciNydW5TZWxlY3RlZCgxKTo1Mg
scope.15.kind=method
scope.15.startLine=52
scope.15.endLine=58
scope.15.semanticHash=e4b61adafd0d2ae37b9b69ad71e626ea59d2f768336432f14708cf367a2986e9
scope.16.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgyKTo5Ng
scope.16.kind=method
scope.16.startLine=96
scope.16.endLine=103
scope.16.semanticHash=9c64668b4c0eeada1950306bb897bdd416db1e22fb81088433f11253f5a948af
scope.17.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDIpOjYw
scope.17.kind=method
scope.17.startLine=60
scope.17.endLine=72
scope.17.semanticHash=fa751dc3f0c27d994063cf11f3b50f65d23b267d37a8f1b6faa4cad59d640837
scope.18.id=bWV0aG9kOlNpbXVsYXRvciN1c2FnZSgwKTo3NA
scope.18.kind=method
scope.18.startLine=74
scope.18.endLine=77
scope.18.semanticHash=2656cc8ce2cb8d742bf03fda1c46337b359c40a65d98156b1df4c0749b6859e7
scope.19.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKToxNjA
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=165
scope.19.semanticHash=ac120389dc6d46c5ebb8158efa21ec1ecd0c1ef66097045f927ed0ee13363ba1
scope.20.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjE2MQ
scope.20.kind=method
scope.20.startLine=161
scope.20.endLine=163
scope.20.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
scope.21.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2F3YWl0RW5kKDApOjE0Nw
scope.21.kind=method
scope.21.startLine=147
scope.21.endLine=156
scope.21.semanticHash=7aa54499049dd0b207433fb986790356879122012b7eb5d2f382643606e54e94
scope.22.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MTIy
scope.22.kind=method
scope.22.startLine=122
scope.22.endLine=129
scope.22.semanticHash=16e4a92e82a30bd444163fb558e631a49a4c3862d5786ce8114772a3bf827c94
scope.23.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MTMx
scope.23.kind=method
scope.23.startLine=131
scope.23.endLine=134
scope.23.semanticHash=0d457f3b1fd9134c9ab67118e0a2cc5c2cdd40ddcca6ebd2375f829302b6ada0
scope.24.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2lzUGxheWluZygwKToxNDI
scope.24.kind=method
scope.24.startLine=142
scope.24.endLine=144
scope.24.semanticHash=d5862f1399623e3f0d0e68470fa2f8a029d7ca718dc068e6d794429014795a9d
scope.25.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI3N0b3AoMCk6MTM3
scope.25.kind=method
scope.25.startLine=137
scope.25.endLine=139
scope.25.semanticHash=8468764595e918a627b87f64c24db48d9b960b4b077013ceab738797cb830f2c
*/
