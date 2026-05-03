package the.monopoly.game.rules;

import the.monopoly.game.components.board.Board;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Rule {
  <T> T process(Processor<T> processor);

  enum Type {
    double_salary_when_landing_on_start
  }

  interface Processor<T> {
    T process(Official.DoubleSalaryWhenLandingOnStart rule);
  }

  interface Set {
    Stream<Dice> dice();

    Player.Pool players();

    Bank bank();

    void activate(Rule.Type type);

    Street create(Street.Type type);

    Board gameboard();

    enum Type implements Factory {
      official(new Official());

      private final Factory factory;

      Type(Factory factory) {
        this.factory = factory;
      }

      @Override
      public Set create() {
        return factory.create();
      }
    }

    interface Factory {
      Set create();
    }

    record Simple(
        Board board,
        List<Dice> diceBuffer,
        Player.Pool players,
        Bank bank,
        java.util.Set<Rule> activatedRules,
        Map<Rule.Type, Rule> optionalRules
    ) implements Set {
      @Override
      public Stream<Dice> dice() {
        return diceBuffer.stream();
      }

      @Override
      public void activate(Rule.Type type) {
        activatedRules.add(optionalRules.get(type));
      }

      @Override
      public Street create(Street.Type type) {
        return type.create(activatedRules);
      }

      @Override
      public Board gameboard() {
        return board;
      }
    }
  }
}
