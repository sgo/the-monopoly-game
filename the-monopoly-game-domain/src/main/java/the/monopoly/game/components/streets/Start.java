package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Official;
import the.monopoly.game.rules.Rule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.IntStream;

import static the.monopoly.game.components.streets.Street.Type.start;

public class Start implements Street.Factory {
  @Override
  public Street create(Set<Rule> activatedRules) {
    Money base = new Money(-200);
    return new Street(
        start,
        activatedRules,
        new RentFactory(base),
        new TollFactory(base)
    );
  }

  private static class RentFactory implements Money.Factory.Rent, Rule.Processor<Integer> {
    private final Money base;

    public RentFactory(Money base) {
      this.base = base;
    }

    @Override
    public Money create(Set<Rule> rules) {
      return new Money(rules.stream()
          .map(it -> it.process(RentFactory.this))
          .mapToInt(Integer::intValue)
          .sum());
    }

    @Override
    public Integer process(Official.DoubleSalaryWhenLandingOnStart rule) {
        return base.amount() * 2;
    }
  }

  private static class TollFactory implements Money.Factory.Toll {
    private final Money base;

    public TollFactory(Money base) {
      this.base = base;
    }

    @Override
    public Money create(Set<Rule> rules) {
      return base;
    }
  }
}
