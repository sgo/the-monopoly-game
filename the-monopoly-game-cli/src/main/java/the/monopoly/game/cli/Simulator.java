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
import the.monopoly.game.strategies.Billionaire;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private static final Set<String> STRATEGIES = Set.of("greedo", "billionaire");
  private static final String MAX_YEARS_FLAG = "--max-years=";
  private static final String SEED_FLAG = "--seed=";

  private Simulator() {
  }

  /** Starts the simulator as a command-line program. */
  public static void main(String[] arguments) {
    if (List.of(arguments).contains("--optional-greedo-stalemate-trading"))
      System.out.println("Stalemate trading enabled");
    if (List.of(arguments).contains("--optional-greedo-legal-entity"))
      System.out.println("Legal entity trading enabled");
    if (List.of(arguments).contains("--optional-asset-rich-billionaire"))
      System.out.println("Asset-rich opening enabled");
    if (List.of(arguments).contains("--optional-development-loans"))
      System.out.println("Development loans enabled");
    if (List.of(arguments).contains("--optional-development-loans-full-draw"))
      System.out.println("Full-draw development loans enabled");
    for (String argument : arguments) {
      if (argument.startsWith(MAX_YEARS_FLAG)) {
        System.out.println("Year limit is " + argument.substring(MAX_YEARS_FLAG.length()) + " years");
      }
      if (argument.startsWith(SEED_FLAG)) {
        System.out.println("Seed is " + argument.substring(SEED_FLAG.length()));
      }
    }
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
    boolean assetRichOpening = List.of(arguments).contains("--optional-asset-rich-billionaire");
    boolean developmentLoans = List.of(arguments).contains("--optional-development-loans");
    boolean fullDrawDevelopmentLoans = List.of(arguments).contains("--optional-development-loans-full-draw");
    int maxYears = extractMaxYears(arguments);
    Long seed = extractSeed(arguments);
    List<String> strategyNames = List.of(arguments).subList(Math.min(1, arguments.length), arguments.length).stream()
        .filter(argument -> !isRecognizedFlag(argument)).toList();
    if (!strategyNames.isEmpty() && strategyNames.size() != playerCount)
      return new Result(1, "Supply one strategy for each player. " + usage());
    return run(playerCount, strategiesFor(playerCount, strategyNames, stalemateTrading, legalEntityTrading,
            assetRichOpening, developmentLoans, fullDrawDevelopmentLoans),
        stalemateTrading, legalEntityTrading, developmentLoans, fullDrawDevelopmentLoans, maxYears, seed);
  }

  private static boolean isRecognizedFlag(String argument) {
    return argument.equals("--optional-greedo-stalemate-trading")
        || argument.equals("--optional-greedo-legal-entity")
        || argument.equals("--optional-asset-rich-billionaire")
        || argument.equals("--optional-development-loans")
        || argument.equals("--optional-development-loans-full-draw")
        || argument.startsWith("--max-years")
        || argument.startsWith(SEED_FLAG);
  }

  private static int extractMaxYears(String... arguments) {
    for (String argument : arguments) {
      if (argument.startsWith(MAX_YEARS_FLAG)) {
        return Integer.parseInt(argument.substring(MAX_YEARS_FLAG.length()));
      }
    }
    return -1;
  }

  private static Long extractSeed(String... arguments) {
    for (String argument : arguments) {
      if (argument.startsWith(SEED_FLAG)) {
        return Long.parseLong(argument.substring(SEED_FLAG.length()));
      }
    }
    return null;
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames) {
    return strategiesFor(playerCount, strategyNames, false, false);
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames,
                                          boolean stalemateTrading, boolean legalEntityTrading) {
    return strategiesFor(playerCount, strategyNames, stalemateTrading, legalEntityTrading, false);
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames,
                                          boolean stalemateTrading, boolean legalEntityTrading,
                                          boolean assetRichOpening) {
    return strategiesFor(playerCount, strategyNames, stalemateTrading, legalEntityTrading,
        assetRichOpening, false, false);
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames,
                                          boolean stalemateTrading, boolean legalEntityTrading,
                                          boolean assetRichOpening, boolean developmentLoans,
                                          boolean fullDrawDevelopmentLoans) {
    List<String> names = strategyNames.isEmpty()
        ? java.util.Collections.nCopies(playerCount, "greedo")
        : strategyNames;
    Map<Player.ID, Strategy> selections = new HashMap<>();
    for (int index = 0; index < playerCount; index++) {
      String name = names.get(index);
      if (!STRATEGIES.contains(name)) throw new IllegalArgumentException("Unknown strategy: " + name + ".");
      Strategy strategy = name.equals("billionaire")
          ? new Billionaire(Money.ZERO, stalemateTrading, legalEntityTrading, true, assetRichOpening,
              developmentLoans, fullDrawDevelopmentLoans)
          : new Greedo(Money.ZERO, stalemateTrading, legalEntityTrading,
              developmentLoans, fullDrawDevelopmentLoans);
      selections.put(Pawn.values()[index].id(), strategy);
    }
    return player -> selections.get(player.id());
  }

  private static String usage() {
    return "Usage: simulator [number of players] [strategy for each player]"
        + System.lineSeparator() + "Available strategies: " + String.join(", ", STRATEGIES)
        + System.lineSeparator() + "Optional flags:"
        + System.lineSeparator() + "  --optional-greedo-stalemate-trading"
        + System.lineSeparator() + "  --optional-greedo-legal-entity"
        + System.lineSeparator() + "  --optional-asset-rich-billionaire"
        + System.lineSeparator() + "  --optional-development-loans"
        + System.lineSeparator() + "  --optional-development-loans-full-draw"
        + System.lineSeparator() + "  --max-years=N"
        + System.lineSeparator() + "  --seed=N"
        + System.lineSeparator() + "Report file: " + reportPath();
  }

  static String reportPath() {
    return Path.of(System.getProperty("java.io.tmpdir"), "the-monopoly-game.report").toString();
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies) {
    return run(playerCount, strategies, false);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading) {
    return run(playerCount, strategies, stalemateTrading, false);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading) {
    return run(playerCount, strategies, stalemateTrading, legalEntityTrading, -1);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading, int maxYears) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading, maxYears).awaitEnd();
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading, boolean developmentLoans,
                           boolean fullDrawDevelopmentLoans, int maxYears) {
    return run(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, null);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading, boolean developmentLoans,
                           boolean fullDrawDevelopmentLoans, int maxYears, Long seed) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed).awaitEnd();
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
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading, -1);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, int maxYears) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading,
        false, false, maxYears, null);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, boolean developmentLoans,
                              boolean fullDrawDevelopmentLoans, int maxYears) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, null);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, boolean developmentLoans,
                              boolean fullDrawDevelopmentLoans, int maxYears, Long seed) {
    Result rejected = rejectOutOfRange(playerCount);
    if (rejected == null) rejected = rejectInvalidYearLimit(maxYears);
    if (rejected != null) return new Running(rejected);

    Rule.Set rules = Rule.Set.Type.official.create();
    List<Player> players = rules.players().select(playerCount).toList();
    Deeds deeds = new Deeds();
    if (seed != null) {
      java.util.Random random = new java.util.Random(seed);
      return new Running(new Game(rules, players,
          player -> Cup.of(rules.dice().map(die -> die.withRandom(random)).toList()),
          strategies, deeds,
          Cards.Decks.official(deeds, random), new Jail(rules), stalemateTrading, legalEntityTrading,
          developmentLoans, fullDrawDevelopmentLoans, maxYears));
    }
    return new Running(new Game(rules, players, player -> Cup.of(rules.dice().toList()), strategies, deeds,
        Cards.Decks.official(deeds), new Jail(rules), stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears));
  }

  private static Result rejectOutOfRange(int playerCount) {
    Rule.Set rules = Rule.Set.Type.official.create();
    if (playerCount < rules.players().min() || playerCount > rules.players().max())
      return new Result(1, "The number of players must be between 2 and 8; received " + playerCount + " players.");
    return null;
  }

  private static Result rejectInvalidYearLimit(int maxYears) {
    if (maxYears == 0) return new Result(1, "A game needs at least one year. " + usage());
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
          String report = Report.of(outcome.journal());
          Files.writeString(Path.of(reportPath()), report);
          result = new Result(0, report);
        } catch (RuntimeException cause) {
          String message = cause.getMessage() == null
              ? cause.getClass().getSimpleName() : cause.getMessage();
          result = new Result(1, "Simulation failed: " + message);
        } catch (IOException cause) {
          result = new Result(1, "Simulation failed: could not write " + reportPath() + ": " + cause.getMessage());
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
moduleHash=6fa580a4350c50e564a36f0f0a745d37c14603961c3244bbaa81fc4bf9dd2cd0
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoyNw
scope.0.kind=class
scope.0.startLine=27
scope.0.endLine=256
scope.0.semanticHash=5d0ef8e2c58a846d8d61b0151ca9bde28512aa6a24e91564663780e0084106cf
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6MjUx
scope.1.kind=class
scope.1.startLine=251
scope.1.endLine=255
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=Y2xhc3M6U2ltdWxhdG9yLlJ1bm5pbmcjUnVubmluZzoxOTg
scope.2.kind=class
scope.2.startLine=198
scope.2.endLine=248
scope.2.semanticHash=1480823e9ac9ab8974960a9d5b7cd474c0973dd11391b463c6f67e4aac908e0b
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI01BWF9ZRUFSU19GTEFHOjI5
scope.3.kind=field
scope.3.startLine=29
scope.3.endLine=29
scope.3.semanticHash=8010888616f015b1379fe5e029007cc822fb74f53fac14653b1b933593f8f2db
scope.4.id=ZmllbGQ6U2ltdWxhdG9yI1NUUkFURUdJRVM6Mjg
scope.4.kind=field
scope.4.startLine=28
scope.4.endLine=28
scope.4.semanticHash=a0d71b682545282f4dd0e2ea0f6440bf8d50abff2388137bd5d7c96afe40ca6f
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZToyNTE
scope.5.kind=field
scope.5.startLine=251
scope.5.endLine=251
scope.5.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.6.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6MjUx
scope.6.kind=field
scope.6.startLine=251
scope.6.endLine=251
scope.6.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.7.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjcmVzdWx0OjIwMQ
scope.7.kind=field
scope.7.startLine=201
scope.7.endLine=201
scope.7.semanticHash=d2e7f6ca2447c4aa1190311c997c469265754f0289baf3770fd1a906bc0d5ec5
scope.8.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjc3RvcFJlcXVlc3RlZDoxOTk
scope.8.kind=field
scope.8.startLine=199
scope.8.endLine=199
scope.8.semanticHash=191ae8305a204a5f6a55f8726d4a3b97cf9a327513b2f0a3d1be6f2c5846d473
scope.9.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjdGhyZWFkOjIwMA
scope.9.kind=field
scope.9.startLine=200
scope.9.endLine=200
scope.9.semanticHash=72ca458daec5b99afaa95b49fdaa041ec41371feab5d9c7c75c60d45a9c2fe62
scope.10.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjMx
scope.10.kind=method
scope.10.startLine=31
scope.10.endLine=32
scope.10.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.11.id=bWV0aG9kOlNpbXVsYXRvciNleGVjdXRlKDEpOjUx
scope.11.kind=method
scope.11.startLine=51
scope.11.endLine=62
scope.11.semanticHash=6da7ae3effd301a4efd70ebb9b4bb88df4d8ce1fe92f4d89cee8adfa227e4d17
scope.12.id=bWV0aG9kOlNpbXVsYXRvciNleHRyYWN0TWF4WWVhcnMoMSk6ODc
scope.12.kind=method
scope.12.startLine=87
scope.12.endLine=94
scope.12.semanticHash=344e326e4c3cff72d40710de12bf419af8add847395283e97b5a042e2eef2431
scope.13.id=bWV0aG9kOlNpbXVsYXRvciNpc0hlbHBSZXF1ZXN0ZWQoMSk6NjQ
scope.13.kind=method
scope.13.startLine=64
scope.13.endLine=66
scope.13.semanticHash=f60dc5d3b6fb064fb40f13514faf5e1d06898f82708991f4abfe1635a28db532
scope.14.id=bWV0aG9kOlNpbXVsYXRvciNpc1JlY29nbml6ZWRGbGFnKDEpOjgx
scope.14.kind=method
scope.14.startLine=81
scope.14.endLine=85
scope.14.semanticHash=e5bc7fe52ecc55a64eecf4ad81f5e6314e0c6f528ca0ed092e1e63e91171b7bf
scope.15.id=bWV0aG9kOlNpbXVsYXRvciNtYWluKDEpOjM1
scope.15.kind=method
scope.15.startLine=35
scope.15.endLine=48
scope.15.semanticHash=badfd61e9fa55af4746014f08f0d83c51c001a8cb8ea0ca67797165e43a7914b
scope.16.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RJbnZhbGlkWWVhckxpbWl0KDEpOjE4OA
scope.16.kind=method
scope.16.startLine=188
scope.16.endLine=191
scope.16.semanticHash=f53e973a24b1b8fecd89573c1031abfee4e68cbfcdf2243b007be1ca6f24d226
scope.17.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RPdXRPZlJhbmdlKDEpOjE4MQ
scope.17.kind=method
scope.17.startLine=181
scope.17.endLine=186
scope.17.semanticHash=07cad2fd67d3d8c72f2629443a191903e74160e2b7c87d492195e1776c66be96
scope.18.id=bWV0aG9kOlNpbXVsYXRvciNyZXBvcnRQYXRoKDApOjEyNw
scope.18.kind=method
scope.18.startLine=127
scope.18.endLine=129
scope.18.semanticHash=a29593cd723be0dd63afa6aab489d48f62885610b852790668af6361e9e17381
scope.19.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6MTMx
scope.19.kind=method
scope.19.startLine=131
scope.19.endLine=133
scope.19.semanticHash=83d83ccc08307545bbedaaa89e2a4168cc3c7cac47ed505cdb77f3d66e43a7b3
scope.20.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMyk6MTM1
scope.20.kind=method
scope.20.startLine=135
scope.20.endLine=137
scope.20.semanticHash=14a2103391f4238bc998fab3d1fe3854827be4f62ec11264ab6ea54840ce5fe7
scope.21.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNCk6MTM5
scope.21.kind=method
scope.21.startLine=139
scope.21.endLine=142
scope.21.semanticHash=dc54af66a93db6b73980e5659bae00398f4d5100a9bd247f717a74e49260cae5
scope.22.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNSk6MTQ0
scope.22.kind=method
scope.22.startLine=144
scope.22.endLine=147
scope.22.semanticHash=ed839a5930fcb0973ee73a0fe659468b8463dd66f8cfc80cb164a7cf105e2090
scope.23.id=bWV0aG9kOlNpbXVsYXRvciNydW5TZWxlY3RlZCgxKTo2OA
scope.23.kind=method
scope.23.startLine=68
scope.23.endLine=79
scope.23.semanticHash=3aa438d2cf1a464a37e94ce7c2acbea522025485700b8c1e0e44a822c65b5b0a
scope.24.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgyKToxNTU
scope.24.kind=method
scope.24.startLine=155
scope.24.endLine=157
scope.24.semanticHash=a26ebf4207873c2d4acb86580dd8cbe4d669ed6e89a6b61237976a85f79d0861
scope.25.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgzKToxNTk
scope.25.kind=method
scope.25.startLine=159
scope.25.endLine=161
scope.25.semanticHash=3eba73e0f49af9450502034809417230388e0c1900877b20464f6b8ce4790202
scope.26.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg0KToxNjM
scope.26.kind=method
scope.26.startLine=163
scope.26.endLine=166
scope.26.semanticHash=4ba27b2a47f5786787fd29ea9c322eeb8e3ec69aad2b7a1bf32e47ae83db5a7e
scope.27.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg1KToxNjg
scope.27.kind=method
scope.27.startLine=168
scope.27.endLine=179
scope.27.semanticHash=540febbbe9db58368e09b32b3358d8e2ea64a9f800384bf3372357a33bdf7e72
scope.28.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDIpOjk2
scope.28.kind=method
scope.28.startLine=96
scope.28.endLine=98
scope.28.semanticHash=117c386b8a408594aa4dbc660f679c9e023e8cf670ae3f1cf142d8dc61c26fee
scope.29.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDQpOjEwMA
scope.29.kind=method
scope.29.startLine=100
scope.29.endLine=115
scope.29.semanticHash=b66d8e2ef1e02c2b492856235241929285df2722e2c31c29c89c5cfa6daba7e2
scope.30.id=bWV0aG9kOlNpbXVsYXRvciN1c2FnZSgwKToxMTc
scope.30.kind=method
scope.30.startLine=117
scope.30.endLine=125
scope.30.semanticHash=879155ee66b65a2145317de2101b320941f71deee86c185b2c6024722589b06a
scope.31.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKToyNTE
scope.31.kind=method
scope.31.startLine=1
scope.31.endLine=256
scope.31.semanticHash=34a43293135a8089bf1ae23b5d83fa698e5fb47d6d2e3a28ccab08f13cc0d3f0
scope.32.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjI1Mg
scope.32.kind=method
scope.32.startLine=252
scope.32.endLine=254
scope.32.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
scope.33.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2F3YWl0RW5kKDApOjIzOA
scope.33.kind=method
scope.33.startLine=238
scope.33.endLine=247
scope.33.semanticHash=7aa54499049dd0b207433fb986790356879122012b7eb5d2f382643606e54e94
scope.34.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MjAz
scope.34.kind=method
scope.34.startLine=203
scope.34.endLine=220
scope.34.semanticHash=7fb40f2cf5ef2e7f2272962998bd65883224f706d64c3d8b2a97194806ad3401
scope.35.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MjIy
scope.35.kind=method
scope.35.startLine=222
scope.35.endLine=225
scope.35.semanticHash=0d457f3b1fd9134c9ab67118e0a2cc5c2cdd40ddcca6ebd2375f829302b6ada0
scope.36.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2lzUGxheWluZygwKToyMzM
scope.36.kind=method
scope.36.startLine=233
scope.36.endLine=235
scope.36.semanticHash=d5862f1399623e3f0d0e68470fa2f8a029d7ca718dc068e6d794429014795a9d
scope.37.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI3N0b3AoMCk6MjI4
scope.37.kind=method
scope.37.startLine=228
scope.37.endLine=230
scope.37.semanticHash=8468764595e918a627b87f64c24db48d9b960b4b077013ceab738797cb830f2c
*/
