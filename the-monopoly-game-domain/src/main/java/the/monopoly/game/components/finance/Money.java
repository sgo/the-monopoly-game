package the.monopoly.game.components.finance;

public record Money(int amount) {
  public Money plus(Money money) {
    return new Money(amount + money.amount());
  }

  public Money minus(Money money) {
    return new Money(amount - money.amount);
  }
}
