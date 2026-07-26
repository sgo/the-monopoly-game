package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

/**
 * A space that charges whoever lands on it a fixed amount. Nobody can buy it.
 */
public record TaxSpace(Street.Type type, Money tax) implements Street {
  @Override
  public Street.Kind kind() {
    return Street.Kind.tax;
  }

  static Street.Factory of(int tax) {
    Money amount = new Money(tax);
    return (type, activatedRules) -> new TaxSpace(type, amount);
  }
}
