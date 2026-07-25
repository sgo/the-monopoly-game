package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Official;
import the.monopoly.game.rules.Rule;

import java.util.Set;

class Start implements Street.Factory {
  @Override
  public Street create(Street.Type type, Set<Rule> activatedRules) {
    Money base = new Money(-200);
    return Street.startSpace(
        type,
        activatedRules,
        new RentFactory(base),
        new TollFactory(base)
    );
  }

  private static class RentFactory implements Money.Factory.Rent, Rule.Processor<Integer> {
    private final Money vacant;

    public RentFactory(Money vacant) {
      this.vacant = vacant;
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
      return vacant.amount() * 2;
    }

    @Override
    public Money vacant() {
      return vacant;
    }

    @Override
    public Money forOneHouse() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Money forTwoHouses() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Money forThreeHouses() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Money forFourHouses() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Money forOneHotel() {
      throw new UnsupportedOperationException();
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
