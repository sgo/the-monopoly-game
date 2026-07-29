package the.monopoly.game.cli;

import the.monopoly.game.Game;
import the.monopoly.game.Report;
import the.monopoly.game.components.dice.Cup;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Thin command boundary for running a configured Monopoly simulation. */
public final class Simulator {
  private Simulator() {
  }

  public static Result run(int playerCount, Strategy.OfPlayers strategies) {
    Rule.Set rules = Rule.Set.Type.official.create();
    if (playerCount < rules.players().min() || playerCount > rules.players().max())
      return new Result(1, "The number of players must be between 2 and 8.");

    List<Player> players = rules.players().select(playerCount).toList();
    players.forEach(player -> player.account().withdraw(player.account().balance().amount().minus(new Money(5))));
    Game.Result game = new Game(rules, players, simulationCups(players), strategies).playToCompletion();
    Player winner = game.winner().orElseThrow();
    return new Result(0, Report.of(game.journal()) + System.lineSeparator() + winner.id().value() + " wins the game");
  }

  private static Game.Cups simulationCups(List<Player> players) {
    Map<Player.ID, Cup> cups = new HashMap<>();
    for (int index = 0; index < players.size(); index++)
      cups.put(players.get(index).id(), cupFor(index));
    return player -> cups.get(player.id());
  }

  private static Cup cupFor(int playerIndex) {
    return new Cup() {
      private boolean initiativeRoll = true;

      @Override
      public the.monopoly.game.components.dice.Roll roll() {
        if (initiativeRoll) {
          initiativeRoll = false;
          return playerIndex < 6
              ? new the.monopoly.game.components.dice.Roll(1, playerIndex + 1)
              : new the.monopoly.game.components.dice.Roll(playerIndex - 4, 6);
        }
        return new the.monopoly.game.components.dice.Roll(1, 3);
      }
    };
  }

  public record Result(int exitCode, String output) {
    public boolean succeeded() {
      return exitCode == 0;
    }
  }
}
