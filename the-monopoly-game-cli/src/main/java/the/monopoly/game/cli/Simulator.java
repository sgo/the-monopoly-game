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
    int maxYears = SimulatorFlags.maxYears(arguments);
    Long seed = SimulatorFlags.seed(arguments);
    List<String> strategyNames = List.of(arguments).subList(Math.min(1, arguments.length), arguments.length).stream()
        .filter(argument -> !SimulatorFlags.recognized(argument)).toList();
    if (!strategyNames.isEmpty() && strategyNames.size() != playerCount)
      return new Result(1, "Supply one strategy for each player. " + usage());
    return run(playerCount, strategiesFor(playerCount, strategyNames, stalemateTrading, legalEntityTrading,
            assetRichOpening, developmentLoans, fullDrawDevelopmentLoans),
        stalemateTrading, legalEntityTrading, developmentLoans, fullDrawDevelopmentLoans, maxYears, seed,
        warProfitsTax, rentRelief);
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
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, rentRelief).awaitEnd();
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
        developmentLoans, fullDrawDevelopmentLoans, maxYears, seed, warProfitsTax, false);
  }

  public static Running start(int playerCount, Strategy.OfPlayers strategies, boolean stalemateTrading,
                              boolean legalEntityTrading, boolean developmentLoans,
                              boolean fullDrawDevelopmentLoans, int maxYears, Long seed,
                              boolean warProfitsTax, boolean rentRelief) {
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
          developmentLoans, fullDrawDevelopmentLoans, maxYears, null, warProfitsTax, rentRelief));
    }
    return new Running(new Game(rules, players, player -> Cup.of(rules.dice().toList()), strategies, deeds,
        Cards.Decks.official(deeds), new Jail(rules), stalemateTrading, legalEntityTrading,
        developmentLoans, fullDrawDevelopmentLoans, maxYears, null, warProfitsTax, rentRelief));
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
moduleHash=bff0559776448d8db886f95c5eb1dd20bcea6cd3a1710a3c5df35a12271f1490
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoyOA
scope.0.kind=class
scope.0.startLine=28
scope.0.endLine=331
scope.0.semanticHash=c1db687cd778dad9510a3aef18d4da7fccc5b12bd2b8dd9b0c3c0ba9956f73c8
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6MzI2
scope.1.kind=class
scope.1.startLine=326
scope.1.endLine=330
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=Y2xhc3M6U2ltdWxhdG9yLlJ1bm5pbmcjUnVubmluZzoyNzM
scope.2.kind=class
scope.2.startLine=273
scope.2.endLine=323
scope.2.semanticHash=1480823e9ac9ab8974960a9d5b7cd474c0973dd11391b463c6f67e4aac908e0b
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI1NUUkFURUdJRVM6Mjk
scope.3.kind=field
scope.3.startLine=29
scope.3.endLine=29
scope.3.semanticHash=a0d71b682545282f4dd0e2ea0f6440bf8d50abff2388137bd5d7c96afe40ca6f
scope.4.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZTozMjY
scope.4.kind=field
scope.4.startLine=326
scope.4.endLine=326
scope.4.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6MzI2
scope.5.kind=field
scope.5.startLine=326
scope.5.endLine=326
scope.5.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.6.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjcmVzdWx0OjI3Ng
scope.6.kind=field
scope.6.startLine=276
scope.6.endLine=276
scope.6.semanticHash=d2e7f6ca2447c4aa1190311c997c469265754f0289baf3770fd1a906bc0d5ec5
scope.7.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjc3RvcFJlcXVlc3RlZDoyNzQ
scope.7.kind=field
scope.7.startLine=274
scope.7.endLine=274
scope.7.semanticHash=191ae8305a204a5f6a55f8726d4a3b97cf9a327513b2f0a3d1be6f2c5846d473
scope.8.id=ZmllbGQ6U2ltdWxhdG9yLlJ1bm5pbmcjdGhyZWFkOjI3NQ
scope.8.kind=field
scope.8.startLine=275
scope.8.endLine=275
scope.8.semanticHash=72ca458daec5b99afaa95b49fdaa041ec41371feab5d9c7c75c60d45a9c2fe62
scope.9.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjMx
scope.9.kind=method
scope.9.startLine=31
scope.9.endLine=32
scope.9.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.10.id=bWV0aG9kOlNpbXVsYXRvciNleGVjdXRlKDEpOjYy
scope.10.kind=method
scope.10.startLine=62
scope.10.endLine=73
scope.10.semanticHash=6da7ae3effd301a4efd70ebb9b4bb88df4d8ce1fe92f4d89cee8adfa227e4d17
scope.11.id=bWV0aG9kOlNpbXVsYXRvciNpc0hlbHBSZXF1ZXN0ZWQoMSk6NzU
scope.11.kind=method
scope.11.startLine=75
scope.11.endLine=77
scope.11.semanticHash=f60dc5d3b6fb064fb40f13514faf5e1d06898f82708991f4abfe1635a28db532
scope.12.id=bWV0aG9kOlNpbXVsYXRvciNtYWluKDEpOjM1
scope.12.kind=method
scope.12.startLine=35
scope.12.endLine=59
scope.12.semanticHash=f1aee2f670322505677869269cd72b981acb39b705fda24b247571255614b388
scope.13.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RJbnZhbGlkWWVhckxpbWl0KDEpOjI2Mw
scope.13.kind=method
scope.13.startLine=263
scope.13.endLine=266
scope.13.semanticHash=f53e973a24b1b8fecd89573c1031abfee4e68cbfcdf2243b007be1ca6f24d226
scope.14.id=bWV0aG9kOlNpbXVsYXRvciNyZWplY3RPdXRPZlJhbmdlKDEpOjI1Ng
scope.14.kind=method
scope.14.startLine=256
scope.14.endLine=261
scope.14.semanticHash=07cad2fd67d3d8c72f2629443a191903e74160e2b7c87d492195e1776c66be96
scope.15.id=bWV0aG9kOlNpbXVsYXRvciNyZXBvcnRQYXRoKDApOjE1MQ
scope.15.kind=method
scope.15.startLine=151
scope.15.endLine=153
scope.15.semanticHash=a29593cd723be0dd63afa6aab489d48f62885610b852790668af6361e9e17381
scope.16.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6MTU1
scope.16.kind=method
scope.16.startLine=155
scope.16.endLine=157
scope.16.semanticHash=83d83ccc08307545bbedaaa89e2a4168cc3c7cac47ed505cdb77f3d66e43a7b3
scope.17.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMyk6MTU5
scope.17.kind=method
scope.17.startLine=159
scope.17.endLine=161
scope.17.semanticHash=14a2103391f4238bc998fab3d1fe3854827be4f62ec11264ab6ea54840ce5fe7
scope.18.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNCk6MTYz
scope.18.kind=method
scope.18.startLine=163
scope.18.endLine=166
scope.18.semanticHash=dc54af66a93db6b73980e5659bae00398f4d5100a9bd247f717a74e49260cae5
scope.19.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNSk6MTY4
scope.19.kind=method
scope.19.startLine=168
scope.19.endLine=171
scope.19.semanticHash=ed839a5930fcb0973ee73a0fe659468b8463dd66f8cfc80cb164a7cf105e2090
scope.20.id=bWV0aG9kOlNpbXVsYXRvciNydW4oNyk6MTcz
scope.20.kind=method
scope.20.startLine=173
scope.20.endLine=178
scope.20.semanticHash=e489bf80bb3732fc497866a1c394eab63e1b13874965bbe806d344f96cabafc5
scope.21.id=bWV0aG9kOlNpbXVsYXRvciNydW4oOCk6MTgw
scope.21.kind=method
scope.21.startLine=180
scope.21.endLine=185
scope.21.semanticHash=185829253982ff864f77da77987fff0894a305052774c9a883912b6b99776fcd
scope.22.id=bWV0aG9kOlNpbXVsYXRvciNydW4oOSk6MTg3
scope.22.kind=method
scope.22.startLine=187
scope.22.endLine=192
scope.22.semanticHash=407a4a4137aada9a0fbd3a36691f695f67e37b96e7ffc2eec4175d580e3ee252
scope.23.id=bWV0aG9kOlNpbXVsYXRvciNydW5TZWxlY3RlZCgxKTo3OQ
scope.23.kind=method
scope.23.startLine=79
scope.23.endLine=97
scope.23.semanticHash=a2e550f3a91555585b66bdea272ad8268f618744e3f734dc968aef817747cebf
scope.24.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgyKToyMDA
scope.24.kind=method
scope.24.startLine=200
scope.24.endLine=202
scope.24.semanticHash=a26ebf4207873c2d4acb86580dd8cbe4d669ed6e89a6b61237976a85f79d0861
scope.25.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCgzKToyMDQ
scope.25.kind=method
scope.25.startLine=204
scope.25.endLine=206
scope.25.semanticHash=3eba73e0f49af9450502034809417230388e0c1900877b20464f6b8ce4790202
scope.26.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg0KToyMDg
scope.26.kind=method
scope.26.startLine=208
scope.26.endLine=211
scope.26.semanticHash=4ba27b2a47f5786787fd29ea9c322eeb8e3ec69aad2b7a1bf32e47ae83db5a7e
scope.27.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg1KToyMTM
scope.27.kind=method
scope.27.startLine=213
scope.27.endLine=217
scope.27.semanticHash=3a1ef3995263351f28a5540d4652fdfe43a0bdd575e1084736e2c7b7b577ebd9
scope.28.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg3KToyMTk
scope.28.kind=method
scope.28.startLine=219
scope.28.endLine=224
scope.28.semanticHash=1a0b218169469d08f814f72255b449bcc67a13752b5574cd19411cc2cb4ef037
scope.29.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg4KToyMjY
scope.29.kind=method
scope.29.startLine=226
scope.29.endLine=231
scope.29.semanticHash=2b4dad6c2cefdc020a184c60e87a05d5b912f4a639dc38c252b504b1c381fa21
scope.30.id=bWV0aG9kOlNpbXVsYXRvciNzdGFydCg5KToyMzM
scope.30.kind=method
scope.30.startLine=233
scope.30.endLine=254
scope.30.semanticHash=c5ffa2e9e97d7402d272e52085ab9e2eaad50c2aa984c062d1b1d8601bd78aa2
scope.31.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDIpOjk5
scope.31.kind=method
scope.31.startLine=99
scope.31.endLine=101
scope.31.semanticHash=117c386b8a408594aa4dbc660f679c9e023e8cf670ae3f1cf142d8dc61c26fee
scope.32.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDQpOjEwMw
scope.32.kind=method
scope.32.startLine=103
scope.32.endLine=106
scope.32.semanticHash=089444f7c5292caa1c186c21bba30ea9d43cd37eaf832bd8015267dc98d95054
scope.33.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDUpOjEwOA
scope.33.kind=method
scope.33.startLine=108
scope.33.endLine=113
scope.33.semanticHash=85eab0ea3cc240ca505ed4ad42247274ab4620bdef1c2d92aff67d0ce5c8db0b
scope.34.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDcpOjExNQ
scope.34.kind=method
scope.34.startLine=115
scope.34.endLine=134
scope.34.semanticHash=470fcf268e3a10ce6359dab52d922cba98b643b0ab0170a749ac804a30cb1d7f
scope.35.id=bWV0aG9kOlNpbXVsYXRvciN1c2FnZSgwKToxMzY
scope.35.kind=method
scope.35.startLine=136
scope.35.endLine=149
scope.35.semanticHash=000ca014d0585c08bf27ffcba4c39568bf1aafa5c9ca03261122f281a3663d39
scope.36.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKTozMjY
scope.36.kind=method
scope.36.startLine=1
scope.36.endLine=331
scope.36.semanticHash=fb0f6bebdcd043af7bcf693076af02c2122ecde6caedabc2a17993efe3334669
scope.37.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjMyNw
scope.37.kind=method
scope.37.startLine=327
scope.37.endLine=329
scope.37.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
scope.38.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2F3YWl0RW5kKDApOjMxMw
scope.38.kind=method
scope.38.startLine=313
scope.38.endLine=322
scope.38.semanticHash=7aa54499049dd0b207433fb986790356879122012b7eb5d2f382643606e54e94
scope.39.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6Mjc4
scope.39.kind=method
scope.39.startLine=278
scope.39.endLine=295
scope.39.semanticHash=7fb40f2cf5ef2e7f2272962998bd65883224f706d64c3d8b2a97194806ad3401
scope.40.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2N0b3IoMSk6Mjk3
scope.40.kind=method
scope.40.startLine=297
scope.40.endLine=300
scope.40.semanticHash=0d457f3b1fd9134c9ab67118e0a2cc5c2cdd40ddcca6ebd2375f829302b6ada0
scope.41.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI2lzUGxheWluZygwKTozMDg
scope.41.kind=method
scope.41.startLine=308
scope.41.endLine=310
scope.41.semanticHash=d5862f1399623e3f0d0e68470fa2f8a029d7ca718dc068e6d794429014795a9d
scope.42.id=bWV0aG9kOlNpbXVsYXRvci5SdW5uaW5nI3N0b3AoMCk6MzAz
scope.42.kind=method
scope.42.startLine=303
scope.42.endLine=305
scope.42.semanticHash=8468764595e918a627b87f64c24db48d9b960b4b077013ceab738797cb830f2c
*/
