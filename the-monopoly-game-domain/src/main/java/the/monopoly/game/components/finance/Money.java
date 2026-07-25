package the.monopoly.game.components.finance;

/**
 * An amount of game money. A plain value: it knows how to combine with other
 * amounts and nothing about where an amount came from.
 */
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
}
