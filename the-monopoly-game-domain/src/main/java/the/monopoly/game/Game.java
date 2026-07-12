package the.monopoly.game;

import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;

import java.util.List;

public record Game(Rule.Set rules, List<Player> players) {
  public Result play() {
    players.forEach(player-> {
//      rules.dice()
    });
    return new Result();
  }

  public record Result() {
  }
}
