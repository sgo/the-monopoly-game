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
    if (List.of(arguments).contains("--optional-war-profits-tax"))
      System.out.println("War profits tax enabled");
    if (List.of(arguments).contains("--optional-rent-relief"))
      System.out.println("Rent relief enabled");
    for (String argument : arguments) {
      if (argument.startsWith(SimulatorFlags.MAX_YEARS_FLAG)) {
        System.out.println("Year limit is " + argument.substring(SimulatorFlags.MAX_YEARS_FLAG.length()) + " years");
      }
      if (argument.startsWith(SimulatorFlags.SEED_FLAG)) {
        System.out.println("Seed is " + argument.substring(SimulatorFlags.SEED_FLAG.length()));
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
    boolean stalemateTrading = SimulatorFlags.stalemateTrading(arguments);
    boolean legalEntityTrading = SimulatorFlags.legalEntityTrading(arguments);
    boolean assetRichOpening = SimulatorFlags.assetRichOpening(arguments);
    boolean developmentLoans = SimulatorFlags.developmentLoans(arguments);
    boolean fullDrawDevelopmentLoans = SimulatorFlags.fullDrawDevelopmentLoans(arguments);
    boolean warProfitsTax = SimulatorFlags.warProfitsTax(arguments);
    boolean rentRelief = SimulatorFlags.rentRelief(arguments);
    boolean unifiedIncomeTax = SimulatorFlags.unifiedIncomeTax(arguments);
    int maxYears = SimulatorFlags.maxYears(arguments);
    Long seed = SimulatorFlags.seed(arguments);
    List<String> strategyNames = List.of(arguments).subList(Math.min(1, arguments.length), arguments.length).stream()
        .filter(argument -> !SimulatorFlags.recognized(argument)).toList();
    if (!strategyNames.isEmpty() && strategyNames.size() != playerCount)
      return new Result(1, "Supply one strategy for each player. " + usage());
    return run(playerCount, strategiesFor(playerCount, strategyNames, stalemateTrading, legalEntityTrading,
            assetRichOpening, developmentLoans, fullDrawDevelopmentLoans),
        stalemateTrading, legalEntityTrading, developmentLoans, fullDrawDevelopmentLoans, maxYears, seed,
        warProfitsTax, rentRelief, unifiedIncomeTax);
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
        + System.lineSeparator() + "  --optional-war-profits-tax"
        + System.lineSeparator() + "  --optional-rent-relief"
        + System.lineSeparator() + "  --optional-unified-income-tax"
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
    return run(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, false);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading, boolean developmentLoans,
                           boolean fullDrawDevelopmentLoans, int maxYears, Long seed, boolean warProfitsTax) {
    return run(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, false);
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                           boolean legalEntityTrading, boolean developmentLoans,
                           boolean fullDrawDevelopmentLoans, int maxYears, Long seed,
                           boolean warProfitsTax, boolean rentRelief) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, rentRelief, false).awaitEnd();
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                            boolean legalEntityTrading, boolean developmentLoans,
                            boolean fullDrawDevelopmentLoans, int maxYears, Long seed,
                            boolean warProfitsTax, boolean rentRelief, boolean unifiedIncomeTax) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading, developmentLoans,
        fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, rentRelief, unifiedIncomeTax).awaitEnd();
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
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, false);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, boolean developmentLoans,
                              boolean fullDrawDevelopmentLoans, int maxYears, Long seed, boolean warProfitsTax) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, false, false);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, boolean developmentLoans,
                              boolean fullDrawDevelopmentLoans, int maxYears, Long seed,
                              boolean warProfitsTax, boolean rentRelief) {
    return start(playerCount, strategies, stalemateTrading, legalEntityTrading, developmentLoans,
        fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, rentRelief, false);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, boolean developmentLoans,
                              boolean fullDrawDevelopmentLoans, int maxYears, Long seed,
                              boolean warProfitsTax, boolean rentRelief, boolean unifiedIncomeTax) {
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
          developmentLoans, fullDrawDevelopmentLoans, maxYears, null, warProfitsTax, rentRelief, unifiedIncomeTax));
    }
    return new Running(new Game(rules, players, player -> Cup.of(rules.dice().toList()), strategies, deeds,
        Cards.Decks.official(deeds), new Jail(rules), stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, null, warProfitsTax, rentRelief, unifiedIncomeTax));
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
moduleHash=b15b8b9107196da966b5745cd0090e2cc53e4afa9be4737f491c6c20af450846
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoyOA
scope.0.kind=class
scope.0.startLine=28
scope.0.endLine=351
scope.0.semanticHash=ae2011137c1fb4e159a9cb37fd5612d54bbbc359375285fd1336f343f371e9f6
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6MzQ2
scope.1.kind=class
scope.1.startLine=346
scope.1.endLine=350
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=Y2xhc3M6U2ltdWxhdG9yLlJ1bm5pbmcjUnVubmluZzoyOTM
scope.2.kind=class
scope.2.startLine=293
scope.2.endLine=343
scope.2.semanticHash=1480823e9ac9ab8974960a9d5b7cd474c0973dd11391b463c6f67e4aac908e0b
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI1NUUkFURUdJRVM6Mjk
scope.3.kind=field
scope.3.startLine=29
scope.3.endLine=29
scope.3.semanticHash=a0d71b682545282f4dd0e2ea0f6440bf8d50abff2388137bd5d7c96afe40ca6f
scope.4.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZTozNDY
scope.4.kind=field
scope.4.startLine=346
scope.4.endLine=346
scope.4.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6MzQ2
scope.5.kind=field
scope.5.startLine=346
scope.5.endLine=346
scope.5.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.6.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjcmVzdWx0OjI5Ng
scope.6.kind=field
scope.6.startLine=296
scope.6.endLine=296
scope.6.semanticHash=d2e7f6ca2447c4aa1190311c997c469265754f0289baf3770fd1a906bc0d5ec5
scope.7.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjc3RvcFJlcXVlc3RlZDoyOTQ
scope.7.kind=field
scope.7.startLine=294
scope.7.endLine=294
scope.7.semanticHash=191ae8305a204a5f6a55f8726d4a3b97cf9a327513b2f0a3d1be6f2c5846d473
scope.8.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjdGhyZWFkOjI5NQ
scope.8.kind=field
scope.8.startLine=295
scope.8.endLine=295
scope.8.semanticHash=72ca458daec5b99afaa95b49fdaa041ec41371feab5d9c7c75c60d45a9c2fe62
scope.9.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjMx
scope.9.kind=method
scope.9.startLine=31
scope.9.endLine=32
scope.9.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.10.id=bWV0aG9kOlNpbXVsYXRvciNleGVjdXRlKDEpOjY0
scope.10.kind=method
scope.10.startLine=64
scope.10.endLine=75
scope.10.semanticHash=6da7ae3effd301a4efd70ebb9b4bb88df4d8ce1fe92f4d89cee8adfa227e4d17
scope.11.id=bWV0aG9kOlNpbXVsYXRvciNpc0hlbHBSZXF1ZXN0ZWQoMSk6Nzc
scope.11.kind=method
scope.11.startLine=77
scope.11.endLine=79
scope.11.semanticHash=f60dc5d3b6fb064fb40f13514faf5e1d06898f82708991f4abfe1635a28db532
scope.12.id=bWV0aG9kOlNpbXVsYXRvciNtYWluKDEpOjM1
scope.12.kind=method
scope.12.startLine=35
scope.12.endLine=61
scope.12.semanticHash=ea64a95b9b4089d75e4a8472700e6a5523b57a1afecbdd36d655e134931aa319
scope.13.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RJbnZhbGlkWWVhckxpbWl0KDEpOjI4Mw
scope.13.kind=method
scope.13.startLine=283
scope.13.endLine=286
scope.13.semanticHash=f53e973a24b1b8fecd89573c1031abfee4e68cbfcdf2243b007be1ca6f24d226
scope.14.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RPdXRPZlJhbmdlKDEpOjI3Ng
scope.14.kind=method
scope.14.startLine=276
scope.14.endLine=281
scope.14.semanticHash=07cad2fd67d3d8c72f2629443a191903e74160e2b7c87d492195e1776c66be96
scope.15.id=bWV0aG9kOlNpbXVsYXRvciNyZXBvcnRQYXRoKDApOjE1NQ
scope.15.kind=method
scope.15.startLine=155
scope.15.endLine=157
scope.15.semanticHash=a29593cd723be0dd63afa6aab489d48f62885610b852790668af6361e9e17381
scope.16.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMTApOjE5OA
scope.16.kind=method
scope.16.startLine=198
scope.16.endLine=204
scope.16.semanticHash=c3af80afa901cac80c21c95d848d68bc81fe0a53913468c09b660b5dd831b7fd
scope.17.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6MTU5
scope.17.kind=method
scope.17.startLine=159
scope.17.endLine=161
scope.17.semanticHash=83d83ccc08307545bbedaaa89e2a4168cc3c7cac47ed505cdb77f3d66e43a7b3
scope.18.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMyk6MTYz
scope.18.kind=method
scope.18.startLine=163
scope.18.endLine=165
scope.18.semanticHash=14a2103391f4238bc998fab3d1fe3854827be4f62ec11264ab6ea54840ce5fe7
scope.19.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNCk6MTY3
scope.19.kind=method
scope.19.startLine=167
scope.19.endLine=170
scope.19.semanticHash=dc54af66a93db6b73980e5659bae00398f4d5100a9bd247f717a74e49260cae5
scope.20.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNSk6MTcy
scope.20.kind=method
scope.20.startLine=172
scope.20.endLine=175
scope.20.semanticHash=ed839a5930fcb0973ee73a0fe659468b8463dd66f8cfc80cb164a7cf105e2090
scope.21.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNyk6MTc3
scope.21.kind=method
scope.21.startLine=177
scope.21.endLine=182
scope.21.semanticHash=e489bf80bb3732fc497866a1c394eab63e1b13874965bbe806d344f96cabafc5
scope.22.id=bWV0aG9kOlNpbXVsYXRvciNydW4oOCk6MTg0
scope.22.kind=method
scope.22.startLine=184
scope.22.endLine=189
scope.22.semanticHash=185829253982ff864f77da77987fff0894a305052774c9a883912b6b99776fcd
scope.23.id=bWV0aG9kOlNpbXVsYXRvciNydW4oOSk6MTkx
scope.23.kind=method
scope.23.startLine=191
scope.23.endLine=196
scope.23.semanticHash=5f733e10bd01e60a2ddb1ae1b3557f9649a02070422ce5efd80d0c8981b03b7a
scope.24.id=bWV0aG9kOlNpbXVsYXRvciNydW5TZWxlY3RlZCgxKTo4MQ
scope.24.kind=method
scope.24.startLine=81
scope.24.endLine=100
scope.24.semanticHash=a49e1963b2d0cba36168d8b8c50ec862492b401b212414027c1442f1355f5550
scope.25.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgxMCk6MjUy
scope.25.kind=method
scope.25.startLine=252
scope.25.endLine=274
scope.25.semanticHash=8ba9bf1c75004fd0957ccf150045f283aafb592f4c8e63bf69174e024e6d6d1c
scope.26.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgyKToyMTI
scope.26.kind=method
scope.26.startLine=212
scope.26.endLine=214
scope.26.semanticHash=a26ebf4207873c2d4acb86580dd8cbe4d669ed6e89a6b61237976a85f79d0861
scope.27.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgzKToyMTY
scope.27.kind=method
scope.27.startLine=216
scope.27.endLine=218
scope.27.semanticHash=3eba73e0f49af9450502034809417230388e0c1900877b20464f6b8ce4790202
scope.28.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg0KToyMjA
scope.28.kind=method
scope.28.startLine=220
scope.28.endLine=223
scope.28.semanticHash=4ba27b2a47f5786787fd29ea9c322eeb8e3ec69aad2b7a1bf32e47ae83db5a7e
scope.29.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg1KToyMjU
scope.29.kind=method
scope.29.startLine=225
scope.29.endLine=229
scope.29.semanticHash=3a1ef3995263351f28a5540d4652fdfe43a0bdd575e1084736e2c7b7b577ebd9
scope.30.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg3KToyMzE
scope.30.kind=method
scope.30.startLine=231
scope.30.endLine=236
scope.30.semanticHash=1a0b218169469d08f814f72255b449bcc67a13752b5574cd19411cc2cb4ef037
scope.31.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg4KToyMzg
scope.31.kind=method
scope.31.startLine=238
scope.31.endLine=243
scope.31.semanticHash=2b4dad6c2cefdc020a184c60e87a05d5b912f4a639dc38c252b504b1c381fa21
scope.32.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg5KToyNDU
scope.32.kind=method
scope.32.startLine=245
scope.32.endLine=250
scope.32.semanticHash=91cb50f375e1458ee5698eaf01676def232e1263ae4a18645a779709a68f84a8
scope.33.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDIpOjEwMg
scope.33.kind=method
scope.33.startLine=102
scope.33.endLine=104
scope.33.semanticHash=117c386b8a408594aa4dbc660f679c9e023e8cf670ae3f1cf142d8dc61c26fee
scope.34.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDQpOjEwNg
scope.34.kind=method
scope.34.startLine=106
scope.34.endLine=109
scope.34.semanticHash=089444f7c5292caa1c186c21bba30ea9d43cd37eaf832bd8015267dc98d95054
scope.35.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDUpOjExMQ
scope.35.kind=method
scope.35.startLine=111
scope.35.endLine=116
scope.35.semanticHash=85eab0ea3cc240ca505ed4ad42247274ab4620bdef1c2d92aff67d0ce5c8db0b
scope.36.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDcpOjExOA
scope.36.kind=method
scope.36.startLine=118
scope.36.endLine=137
scope.36.semanticHash=470fcf268e3a10ce6359dab52d922cba98b643b0ab0170a749ac804a30cb1d7f
scope.37.id=bWV0aG9kOlNpbXVsYXRvciN1c2FnZSgwKToxMzk
scope.37.kind=method
scope.37.startLine=139
scope.37.endLine=153
scope.37.semanticHash=5d0de531488475d792195cf64d3e9b49b477893c4d8eea5e33b8a742f525f44a
scope.38.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKTozNDY
scope.38.kind=method
scope.38.startLine=1
scope.38.endLine=351
scope.38.semanticHash=3dc965cf701bf2ede57553859f51801a12ef9f8ce9c1278dc969714bfcc6ebc6
scope.39.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjM0Nw
scope.39.kind=method
scope.39.startLine=347
scope.39.endLine=349
scope.39.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
scope.40.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2F3YWl0RW5kKDApOjMzMw
scope.40.kind=method
scope.40.startLine=333
scope.40.endLine=342
scope.40.semanticHash=7aa54499049dd0b207433fb986790356879122012b7eb5d2f382643606e54e94
scope.41.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6Mjk4
scope.41.kind=method
scope.41.startLine=298
scope.41.endLine=315
scope.41.semanticHash=7fb40f2cf5ef2e7f2272962998bd65883224f706d64c3d8b2a97194806ad3401
scope.42.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6MzE3
scope.42.kind=method
scope.42.startLine=317
scope.42.endLine=320
scope.42.semanticHash=0d457f3b1fd9134c9ab67118e0a2cc5c2cdd40ddcca6ebd2375f829302b6ada0
scope.43.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2lzUGxheWluZygwKTozMjg
scope.43.kind=method
scope.43.startLine=328
scope.43.endLine=330
scope.43.semanticHash=d5862f1399623e3f0d0e68470fa2f8a029d7ca718dc068e6d794429014795a9d
scope.44.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI3N0b3AoMCk6MzIz
scope.44.kind=method
scope.44.startLine=323
scope.44.endLine=325
scope.44.semanticHash=8468764595e918a627b87f64c24db48d9b960b4b077013ceab738797cb830f2c
*/
