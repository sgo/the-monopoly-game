package the.monopoly.game.cli;

import the.monopoly.game.Game;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.AgreeIfAffordable;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private static final List<Roll> INITIATIVE_ROLLS = List.of(
      new Roll(1, 1), new Roll(1, 2), new Roll(1, 3), new Roll(1, 4),
      new Roll(1, 5), new Roll(1, 6), new Roll(2, 6), new Roll(3, 6)
  );
  private static final Roll BANKRUPTING_ROLL = new Roll(1, 3);
  private static final Map<String, Supplier<Strategy>> STRATEGIES = Map.of(
      "agree-if-affordable", AgreeIfAffordable::new
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
    if (arguments.length == 1 && (arguments[0].equals("-h") || arguments[0].equals("--h")))
      return new Result(0, usage());

    try {
      int playerCount = arguments.length == 0 ? 2 : Integer.parseInt(arguments[0]);
      List<String> strategyNames = List.of(arguments).subList(Math.min(1, arguments.length), arguments.length);
      if (!strategyNames.isEmpty() && strategyNames.size() != playerCount)
        return new Result(1, "Supply one strategy for each player. " + usage());
      return run(playerCount, strategiesFor(playerCount, strategyNames));
    } catch (NumberFormatException cause) {
      String received = arguments.length == 0 ? "" : arguments[0];
      return new Result(1, "The number of players must be between 2 and 8; received " + received + " players.");
    } catch (IllegalArgumentException cause) {
      return new Result(1, cause.getMessage() + " " + usage());
    }
  }

  static Strategy.OfPlayers strategiesFor(int playerCount, List<String> strategyNames) {
    List<String> names = strategyNames.isEmpty()
        ? java.util.Collections.nCopies(playerCount, "agree-if-affordable")
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
    Rule.Set rules = Rule.Set.Type.official.create();
    if (playerCount < rules.players().min() || playerCount > rules.players().max())
      return new Result(1, "The number of players must be between 2 and 8; received " + playerCount + " players.");

    List<Player> players = rules.players().select(playerCount).toList();
    players.forEach(player -> player.account().withdraw(player.account().balance().amount().minus(new Money(5))));
    Game.Result game = new Game(rules, players, simulationCups(players), strategies).playToCompletion();
    game.winner().orElseThrow();
    return new Result(0, Report.of(game.journal()));
  }

  private static Game.Cups simulationCups(List<Player> players) {
    Map<Player.ID, Cup> cups = new HashMap<>();
    for (int index = 0; index < players.size(); index++)
      cups.put(players.get(index).id(), Cup.of(INITIATIVE_ROLLS.get(index), BANKRUPTING_ROLL));
    return player -> cups.get(player.id());
  }

  public record Result(int exitCode, String output) {
    public boolean succeeded() {
      return exitCode == 0;
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=531092195af0faf95ac7d452fd08c438d76ba5862827d973d0ef70f2995f744c
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoyMA
scope.0.kind=class
scope.0.startLine=20
scope.0.endLine=102
scope.0.semanticHash=be32dec5f6be59a7ef4d00d03eacd15c7105907eeec08e2697ff41a6264dd947
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6OTc
scope.1.kind=class
scope.1.startLine=97
scope.1.endLine=101
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=ZmllbGQ6U2ltdWxhdG9yI0JBTktSVVBUSU5HX1JPTEw6MjU
scope.2.kind=field
scope.2.startLine=25
scope.2.endLine=25
scope.2.semanticHash=87af80c5ab01615b6703d6988c7ff0f150e69cc1f64090da103e9b62d4ba5729
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI0lOSVRJQVRJVkVfUk9MTFM6MjE
scope.3.kind=field
scope.3.startLine=21
scope.3.endLine=24
scope.3.semanticHash=e5b7aaef7900d74869b01ca3435b748bf5197f78ab28550170e93f3d5a1aa49a
scope.4.id=ZmllbGQ6U2ltdWxhdG9yI1NUUkFURUdJRVM6MjY
scope.4.kind=field
scope.4.startLine=26
scope.4.endLine=28
scope.4.semanticHash=af6be9ee38f915f737e01b36d8591ba82a76480e48079c94b4aa9c42bd118663
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZTo5Nw
scope.5.kind=field
scope.5.startLine=97
scope.5.endLine=97
scope.5.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.6.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6OTc
scope.6.kind=field
scope.6.startLine=97
scope.6.endLine=97
scope.6.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.7.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjMw
scope.7.kind=method
scope.7.startLine=30
scope.7.endLine=31
scope.7.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.8.id=bWV0aG9kOlNpbXVsYXRvciNleGVjdXRlKDEpOjQx
scope.8.kind=method
scope.8.startLine=41
scope.8.endLine=57
scope.8.semanticHash=051522562a0fc2133eb381fcba3ffe035a171a4dda74a10da2ba57ab08015143
scope.9.id=bWV0aG9kOlNpbXVsYXRvciNtYWluKDEpOjM0
scope.9.kind=method
scope.9.startLine=34
scope.9.endLine=38
scope.9.semanticHash=ddfa3d4b690062a78d607287d22c2473d9290516e11b1b8ee722ea5f27325ee9
scope.10.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6Nzg
scope.10.kind=method
scope.10.startLine=78
scope.10.endLine=88
scope.10.semanticHash=e0a733a817b0850e919049984ef02ee1a1c9f8726308911151cf886407ab7fde
scope.11.id=bWV0aG9kOlNpbXVsYXRvciNzaW11bGF0aW9uQ3VwcygxKTo5MA
scope.11.kind=method
scope.11.startLine=90
scope.11.endLine=95
scope.11.semanticHash=9ce5b178bf196e0f61db91d9ad5ed73a65a9c6b94d569fb57216d1e528d11736
scope.12.id=bWV0aG9kOlNpbXVsYXRvciNzdHJhdGVnaWVzRm9yKDIpOjU5
scope.12.kind=method
scope.12.startLine=59
scope.12.endLine=71
scope.12.semanticHash=fa751dc3f0c27d994063cf11f3b50f65d23b267d37a8f1b6faa4cad59d640837
scope.13.id=bWV0aG9kOlNpbXVsYXRvciN1c2FnZSgwKTo3Mw
scope.13.kind=method
scope.13.startLine=73
scope.13.endLine=76
scope.13.semanticHash=2656cc8ce2cb8d742bf03fda1c46337b359c40a65d98156b1df4c0749b6859e7
scope.14.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKTo5Nw
scope.14.kind=method
scope.14.startLine=1
scope.14.endLine=102
scope.14.semanticHash=e7dd9a664242a39a92bdcfa9070e95a1cd9dfef580c9ca7c7df09af9b9b43d76
scope.15.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjk4
scope.15.kind=method
scope.15.startLine=98
scope.15.endLine=100
scope.15.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
*/
