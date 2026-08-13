package the.monopoly.game.cli;

import the.monopoly.game.Game;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.Cards;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Jail;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private static final Set<String> STRATEGIES = Set.of("greedo");

  private Simulator() {
  }

  /** Starts the simulator as a command-line program. */
  public static void main(String[] arguments) {
    if (List.of(arguments).contains("--optional-greedo-stalemate-trading"))
      System.out.println("Stalemate trading enabled");
    if (List.of(arguments).contains("--optional-greedo-legal-entity"))
      System.out.println("Legal entity trading enabled");
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
    boolean stalemateTrading = List.of(arguments).contains("--optional-greedo-stalemate-trading");
    boolean legalEntityTrading = List.of(arguments).contains("--optional-greedo-legal-entity");
    List<String> strategyNames = List.of(arguments).subList(Math.min(1, arguments.length), arguments.length).stream()
        .filter(argument -> !argument.equals("--optional-greedo-stalemate-trading")
            && !argument.equals("--optional-greedo-legal-entity")).toList();
    if (!strategyNames.isEmpty() && strategyNames.size() != playerCount)
      return new Result(1, "Supply one strategy for each player. " + usage());
    return run(playerCount, strategiesFor(playerCount, strategyNames, stalemateTrading, legalEntityTrading),
        stalemateTrading, legalEntityTrading);
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames) {
    return strategiesFor(playerCount, strategyNames, false, false);
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames,
                                          boolean stalemateTrading, boolean legalEntityTrading) {
    List<String> names = strategyNames.isEmpty()
        ? java.util.Collections.nCopies(playerCount, "greedo")
        : strategyNames;
    Map<Player.ID, Strategy> selections = new HashMap<>();
    for (int index = 0; index < playerCount; index++) {
      String name = names.get(index);
      if (!STRATEGIES.contains(name)) throw new IllegalArgumentException("Unknown strategy: " + name + ".");
      selections.put(Pawn.values()[index].id(), new Greedo(Money.ZERO, stalemateTrading, legalEntityTrading));
    }
    return player -> selections.get(player.id());
  }

  private static String usage() {
    return "Usage: simulator [number of players] [strategy for each player]"
        + System.lineSeparator() + "Available strategies: " + String.join(", ", STRATEGIES)
        + System.lineSeparator() + "Optional flags:"
        + System.lineSeparator() + "  --optional-greedo-stalemate-trading"
        + System.lineSeparator() + "  --optional-greedo-legal-entity"
        + System.lineSeparator() + "Report file: " + reportPath();
  }

  private static String reportPath() {
    return java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "the-monopoly-game.report").toString();
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies) {
    return run(playerCount, strategies, false);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading) {
    return run(playerCount, strategies, stalemateTrading, false);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading).awaitEnd();
  }

  /**
   * Starts the simulator playing in the background, so its progress can be
   * watched as it happens. The simulation runs until it is {@link
   * Running#stop() stopped} or the game reaches its natural end, whichever
   * comes first.
   */
  public static Running start(int playerCount, Strategy.OfPlayers strategies) {
    return start(playerCount, strategies, false);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading) {
    return start(playerCount, strategies, stalemateTrading, false);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading) {
    Result rejected = rejectOutOfRange(playerCount);
    if (rejected != null) return new Running(rejected);

    Rule.Set rules = Rule.Set.Type.official.create();
    List<Player> players = rules.players().select(playerCount).toList();
    Deeds deeds = new Deeds();
    return new Running(new Game(rules, players, player -> Cup.of(rules.dice().toList()), strategies, deeds,
        Cards.Decks.official(deeds), new Jail(rules), stalemateTrading, legalEntityTrading));
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
        try {
          Game.Result outcome = game.playUntilStopped(() -> !stopRequested.get());
          result = new Result(0, Report.of(outcome.journal()));
        } catch (RuntimeException cause) {
          String message = cause.getMessage() == null
              ? cause.getClass().getSimpleName() : cause.getMessage();
          result = new Result(1, "Simulation failed: " + message);
        }
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
moduleHash=3385e2a48ed3847d9f0bde59edd56769db5582ef6bb50a65a945a94ec0710a97
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoyMw
scope.0.kind=class
scope.0.startLine=23
scope.0.endLine=207
scope.0.semanticHash=8120c89c14863f3fb7f96260ae6b770bc7bb6194d5416d448686dc9d82581ed4
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6MjAy
scope.1.kind=class
scope.1.startLine=202
scope.1.endLine=206
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=Y2xhc3M6U2ltdWxhdG9yLlJ1bm5pbmcjUnVubmluZzoxNTM
scope.2.kind=class
scope.2.startLine=153
scope.2.endLine=199
scope.2.semanticHash=5b3b5dcdffc51e20fbfe8e321eeee959e65c91fef2d5fd4cd63f9f5e709034c8
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI1NUUkFURUdJRVM6MjQ
scope.3.kind=field
scope.3.startLine=24
scope.3.endLine=24
scope.3.semanticHash=1535859376b02b1e0f73840a6f5d37c085d22cc75eaf47ee8247456a004e8a14
scope.4.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZToyMDI
scope.4.kind=field
scope.4.startLine=202
scope.4.endLine=202
scope.4.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6MjAy
scope.5.kind=field
scope.5.startLine=202
scope.5.endLine=202
scope.5.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.6.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjcmVzdWx0OjE1Ng
scope.6.kind=field
scope.6.startLine=156
scope.6.endLine=156
scope.6.semanticHash=d2e7f6ca2447c4aa1190311c997c469265754f0289baf3770fd1a906bc0d5ec5
scope.7.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjc3RvcFJlcXVlc3RlZDoxNTQ
scope.7.kind=field
scope.7.startLine=154
scope.7.endLine=154
scope.7.semanticHash=191ae8305a204a5f6a55f8726d4a3b97cf9a327513b2f0a3d1be6f2c5846d473
scope.8.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjdGhyZWFkOjE1NQ
scope.8.kind=field
scope.8.startLine=155
scope.8.endLine=155
scope.8.semanticHash=72ca458daec5b99afaa95b49fdaa041ec41371feab5d9c7c75c60d45a9c2fe62
scope.9.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjI2
scope.9.kind=method
scope.9.startLine=26
scope.9.endLine=27
scope.9.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.10.id=bWV0aG9kOlNpbXVsYXRvciNleGVjdXRlKDEpOjQx
scope.10.kind=method
scope.10.startLine=41
scope.10.endLine=52
scope.10.semanticHash=6da7ae3effd301a4efd70ebb9b4bb88df4d8ce1fe92f4d89cee8adfa227e4d17
scope.11.id=bWV0aG9kOlNpbXVsYXRvciNpc0hlbHBSZXF1ZXN0ZWQoMSk6NTQ
scope.11.kind=method
scope.11.startLine=54
scope.11.endLine=56
scope.11.semanticHash=f60dc5d3b6fb064fb40f13514faf5e1d06898f82708991f4abfe1635a28db532
scope.12.id=bWV0aG9kOlNpbXVsYXRvciNtYWluKDEpOjMw
scope.12.kind=method
scope.12.startLine=30
scope.12.endLine=38
scope.12.semanticHash=5b69aef79629a447a10380ec9567e42f43d043ebd6ad7758d1075ffd399932b3
scope.13.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RPdXRPZlJhbmdlKDEpOjE0MQ
scope.13.kind=method
scope.13.startLine=141
scope.13.endLine=146
scope.13.semanticHash=07cad2fd67d3d8c72f2629443a191903e74160e2b7c87d492195e1776c66be96
scope.14.id=bWV0aG9kOlNpbXVsYXRvciNyZXBvcnRQYXRoKDApOjk4
scope.14.kind=method
scope.14.startLine=98
scope.14.endLine=100
scope.14.semanticHash=2c5cc3fc3cfe8a0197307b33be8f5c3a21f3a28e2493acd57c9141f0fceb30ea
scope.15.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6MTAy
scope.15.kind=method
scope.15.startLine=102
scope.15.endLine=104
scope.15.semanticHash=83d83ccc08307545bbedaaa89e2a4168cc3c7cac47ed505cdb77f3d66e43a7b3
scope.16.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMyk6MTA2
scope.16.kind=method
scope.16.startLine=106
scope.16.endLine=108
scope.16.semanticHash=14a2103391f4238bc998fab3d1fe3854827be4f62ec11264ab6ea54840ce5fe7
scope.17.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNCk6MTEw
scope.17.kind=method
scope.17.startLine=110
scope.17.endLine=113
scope.17.semanticHash=a2eb3cb1d830cdecc048429a97316467b26651dbdcca2189e9b94427332f141f
scope.18.id=bWV0aG9kOlNpbXVsYXRvciNydW5TZWxlY3RlZCgxKTo1OA
scope.18.kind=method
scope.18.startLine=58
scope.18.endLine=69
scope.18.semanticHash=3525c67893de22f2e2014504d4d12fbc99b820a5f61e04cfdd520a54cc48dbdd
scope.19.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgyKToxMjE
scope.19.kind=method
scope.19.startLine=121
scope.19.endLine=123
scope.19.semanticHash=a26ebf4207873c2d4acb86580dd8cbe4d669ed6e89a6b61237976a85f79d0861
scope.20.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgzKToxMjU
scope.20.kind=method
scope.20.startLine=125
scope.20.endLine=127
scope.20.semanticHash=3eba73e0f49af9450502034809417230388e0c1900877b20464f6b8ce4790202
scope.21.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg0KToxMjk
scope.21.kind=method
scope.21.startLine=129
scope.21.endLine=139
scope.21.semanticHash=95f28d4cc0e9f80a8c1146f312b5668f53f5748fad79a5009194584c3bd84c23
scope.22.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDIpOjcx
scope.22.kind=method
scope.22.startLine=71
scope.22.endLine=73
scope.22.semanticHash=117c386b8a408594aa4dbc660f679c9e023e8cf670ae3f1cf142d8dc61c26fee
scope.23.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDQpOjc1
scope.23.kind=method
scope.23.startLine=75
scope.23.endLine=87
scope.23.semanticHash=74a4078201fd1968205f8b9d1041fad5efa768ffbfe0b9a2631ae7742cc7c7a9
scope.24.id=bWV0aG9kOlNpbXVsYXRvciN1c2FnZSgwKTo4OQ
scope.24.kind=method
scope.24.startLine=89
scope.24.endLine=96
scope.24.semanticHash=0a6a3ebabde184570bfab3c5fe7964ee7acfd33d1323e4a286fd797f0a438b13
scope.25.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKToyMDI
scope.25.kind=method
scope.25.startLine=1
scope.25.endLine=207
scope.25.semanticHash=4ee92215417a35c4a07b1fcc5f60475bbc0b42ae6552497ae14a9e9259c6934d
scope.26.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjIwMw
scope.26.kind=method
scope.26.startLine=203
scope.26.endLine=205
scope.26.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
scope.27.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2F3YWl0RW5kKDApOjE4OQ
scope.27.kind=method
scope.27.startLine=189
scope.27.endLine=198
scope.27.semanticHash=7aa54499049dd0b207433fb986790356879122012b7eb5d2f382643606e54e94
scope.28.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MTU4
scope.28.kind=method
scope.28.startLine=158
scope.28.endLine=171
scope.28.semanticHash=21f770304f8c490cdf155266c5148f6a26b0556b99ac5dd25df7144a649c4698
scope.29.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MTcz
scope.29.kind=method
scope.29.startLine=173
scope.29.endLine=176
scope.29.semanticHash=0d457f3b1fd9134c9ab67118e0a2cc5c2cdd40ddcca6ebd2375f829302b6ada0
scope.30.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2lzUGxheWluZygwKToxODQ
scope.30.kind=method
scope.30.startLine=184
scope.30.endLine=186
scope.30.semanticHash=d5862f1399623e3f0d0e68470fa2f8a029d7ca718dc068e6d794429014795a9d
scope.31.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI3N0b3AoMCk6MTc5
scope.31.kind=method
scope.31.startLine=179
scope.31.endLine=181
scope.31.semanticHash=8468764595e918a627b87f64c24db48d9b960b4b077013ceab738797cb830f2c
*/
