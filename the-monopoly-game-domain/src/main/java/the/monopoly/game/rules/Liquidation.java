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
