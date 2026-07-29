package the.monopoly.game.cli;

import the.monopoly.game.Game;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private Simulator() {
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies) {
    Rule.Set rules = Rule.Set.Type.official.create();
    if (playerCount < rules.players().min() || playerCount > rules.players().max())
      return new Result(1, "The number of players must be between 2 and 8.");

    List<Player> players = rules.players().select(playerCount).toList();
    Game.Result game = new Game(rules, players, player -> Cup.of(rules.dice().toList()), strategies).play();
    Player winner = game.turnOrder().getFirst();
    return new Result(0, Report.of(game.journal()) + System.lineSeparator() + winner.id().value() + " wins the game");
  }

  public record Result(int exitCode, String output) {
    public boolean succeeded() {
      return exitCode == 0;
    }
  }
}
