package the.monopoly.game.components.finance;

import the.monopoly.game.rules.Rule;

import java.util.Set;

public record Money(int amount) {
  public Money plus(Money money) {
    return new Money(amount + money.amount());
  }

  public Money minus(Money money) {
    return new Money(amount - money.amount);
  }

  public interface Factory {
    Money create(Set<Rule> rules);

    interface Toll extends Factory {
    }

    interface Rent extends Factory {
    }
  }
}
