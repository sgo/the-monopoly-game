package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.players.Player;

import java.util.List;
import java.util.stream.Stream;

public class Rule {
  public enum Type implements Set.Factory {
    official(new Official());

    private final Set.Factory factory;

    Type(Set.Factory factory) {
      this.factory = factory;
    }

    @Override
    public Set create() {
      return factory.create();
    }
  }

  public interface Set {
    Stream<Dice> dice();

    Player.Pool players();

    interface Factory {
      Set create();
    }

    record Simple(
        List<Dice> diceBuffer,
        Player.Pool players
    ) implements Set {
      @Override
      public Stream<Dice> dice() {
        return diceBuffer.stream();
      }
    }
  }
}
