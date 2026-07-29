package the.monopoly.game.cli;

import the.monopoly.game.Game;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private static final List<Roll> INITIATIVE_ROLLS = List.of(
      new Roll(1, 1), new Roll(1, 2), new Roll(1, 3), new Roll(1, 4),
      new Roll(1, 5), new Roll(1, 6), new Roll(2, 6), new Roll(3, 6)
  );
  private static final Roll BANKRUPTING_ROLL = new Roll(1, 3);

  private Simulator() {
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
moduleHash=c07e792b76ae10042db6325ff47673ec29c314d245a4292665ba900b858af9e7
scope.0.id=Y2xhc3M6U2ltdWxhdG9yI1NpbXVsYXRvcjoxNw
scope.0.kind=class
scope.0.startLine=17
scope.0.endLine=51
scope.0.semanticHash=a3256f0f1ea6be3baf9a91f75045923debd5d58be20cf2bf40ccc7fd913abec3
scope.1.id=Y2xhc3M6U2ltdWxhdG9yLlJlc3VsdCNSZXN1bHQ6NDY
scope.1.kind=class
scope.1.startLine=46
scope.1.endLine=50
scope.1.semanticHash=14f4bc79e304db21a279f62b26999cd0732ceb7f95c624abca3dd43746194699
scope.2.id=ZmllbGQ6U2ltdWxhdG9yI0JBTktSVVBUSU5HX1JPTEw6MjI
scope.2.kind=field
scope.2.startLine=22
scope.2.endLine=22
scope.2.semanticHash=87af80c5ab01615b6703d6988c7ff0f150e69cc1f64090da103e9b62d4ba5729
scope.3.id=ZmllbGQ6U2ltdWxhdG9yI0lOSVRJQVRJVkVfUk9MTFM6MTg
scope.3.kind=field
scope.3.startLine=18
scope.3.endLine=21
scope.3.semanticHash=e5b7aaef7900d74869b01ca3435b748bf5197f78ab28550170e93f3d5a1aa49a
scope.4.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNleGl0Q29kZTo0Ng
scope.4.kind=field
scope.4.startLine=46
scope.4.endLine=46
scope.4.semanticHash=22d20a5f7c9173958dfb701f79fb99a4bb0b0451e48a6d3c48c78a1f0d2ef019
scope.5.id=ZmllbGQ6U2ltdWxhdG9yLlJlc3VsdCNvdXRwdXQ6NDY
scope.5.kind=field
scope.5.startLine=46
scope.5.endLine=46
scope.5.semanticHash=a7385d49a8e8309a95b92e10b7c0f0563448f907f09582aa98f5020ce07e6008
scope.6.id=bWV0aG9kOlNpbXVsYXRvciNjdG9yKDApOjI0
scope.6.kind=method
scope.6.startLine=24
scope.6.endLine=25
scope.6.semanticHash=579ef74c106f592ee31aec788cfff645732ddf47b9fa3f02254324260e81f964
scope.7.id=bWV0aG9kOlNpbXVsYXRvciNydW4oMik6Mjc
scope.7.kind=method
scope.7.startLine=27
scope.7.endLine=37
scope.7.semanticHash=1e88e6135daffd57ba9f9240a137b2a6551d272693aaae7deee3c638f751e180
scope.8.id=bWV0aG9kOlNpbXVsYXRvciNzaW11bGF0aW9uQ3VwcygxKTozOQ
scope.8.kind=method
scope.8.startLine=39
scope.8.endLine=44
scope.8.semanticHash=9ce5b178bf196e0f61db91d9ad5ed73a65a9c6b94d569fb57216d1e528d11736
scope.9.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjY3RvcigyKTo0Ng
scope.9.kind=method
scope.9.startLine=1
scope.9.endLine=51
scope.9.semanticHash=d63bec8ea657702f94fee8c4cde1a0087bf445837602b1cdb7f8ed93a6609592
scope.10.id=bWV0aG9kOlNpbXVsYXRvci5SZXN1bHQjc3VjY2VlZGVkKDApOjQ3
scope.10.kind=method
scope.10.startLine=47
scope.10.endLine=49
scope.10.semanticHash=c4c9cac424bcc0774b321ab1a6cfd6055519976e4524d6a2ede5947bc0f8a465
*/
