package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

/**
 * A space that charges whoever lands on it a fixed amount.
 */
class TaxSpace implements Street.Factory {
  private final Money tax;

  TaxSpace(int tax) {
    this.tax = new Money(tax);
  }

  @Override
  public Street create(Street.Type type, Set<Rule> activatedRules) {
    return Street.taxSpace(type, activatedRules, tax);
  }
}
