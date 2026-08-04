package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.Comparator;
import java.util.List;

/** Orders a player's land from least to most strategically valuable for liquidation. */
final class Liquidation {
  private Liquidation() {
  }

  static List<Street.Type> order(Deeds deeds, Rule.Set rules, Strategy.OfPlayers strategies, Player owner) {
    Strategy strategy = strategies.forPlayer(owner);
    return rules.gameboard().layout().stream().filter(deeds.landOwnedBy(owner)::contains)
        .sorted(Comparator.comparingInt(type -> switch (strategy.priority((Ownable) rules.create(type))) {
          case LOWEST -> 0;
          case MIDDLE -> 1;
          case HIGHEST -> 2;
        })).toList();
  }
}

/* mutate4java-manifest
version=1
moduleHash=78e530ff2193eeb142744484a1400c8025f8abb2d4eda592c5c9687a06a843a3
scope.0.id=Y2xhc3M6TGlxdWlkYXRpb24jTGlxdWlkYXRpb246MTI
scope.0.kind=class
scope.0.startLine=12
scope.0.endLine=25
scope.0.semanticHash=023afcf94073e723a4d91a5b18a155555d117b144279946de04a54e69701e8fe
scope.1.id=bWV0aG9kOkxpcXVpZGF0aW9uI2N0b3IoMCk6MTM
scope.1.kind=method
scope.1.startLine=13
scope.1.endLine=14
scope.1.semanticHash=38920289623b22c946d25ae67cd9c8d8b368c331a6f1671e70295c1251957623
scope.2.id=bWV0aG9kOkxpcXVpZGF0aW9uI29yZGVyKDQpOjE2
scope.2.kind=method
scope.2.startLine=16
scope.2.endLine=24
scope.2.semanticHash=8f4dca0d4662b0e02cd0cda63e5eda73fcb7b3e5a1f923c0760f5033dcf396a8
*/
