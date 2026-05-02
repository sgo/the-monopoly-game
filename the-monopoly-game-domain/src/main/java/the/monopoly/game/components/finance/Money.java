package the.monopoly.game.components.finance;

import the.monopoly.game.rules.Rule;

import java.util.Set;

public record Money(int amount) {
  public static final Money ZERO = new Money(0);

  public Money plus(Money money) {
    return new Money(amount + money.amount());
  }

  public Money minus(Money money) {
    return new Money(amount - money.amount);
  }

  public Money multipliedBy(Money money) {
    return new Money(amount * money.amount);
  }

  public interface Factory {
    Money create(Set<Rule> rules);

    interface Toll extends Factory {
    }

    interface Rent extends Factory {
      Money vacant();

      Money forOneHouse();

      Money forTwoHouses();

      Money forThreeHouses();

      Money forFourHouses();

      Money forOneHotel();
    }

    interface ConstructionCost extends Factory {
      Money house();

      Money hotel();
    }

    class Fixed implements Toll, Rent, ConstructionCost {
      private final Money money;

      public Fixed(Money money) {
        this.money = money;
      }

      @Override
      public Money create(Set<Rule> rules) {
        return money;
      }

      @Override
      public Money vacant() {
        throw new UnsupportedOperationException();
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

      @Override
      public Money house() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Money hotel() {
        throw new UnsupportedOperationException();
      }
    }
  }
}
