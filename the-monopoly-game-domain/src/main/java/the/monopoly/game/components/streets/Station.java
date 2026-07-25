package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.List;
import java.util.Set;

import static the.monopoly.game.components.finance.Money.ZERO;

/**
 * A station cannot be built on. Its rent depends on how many of the four
 * stations its owner holds, doubling with each additional one.
 */
class Station implements Street.Factory {
  private static final Money PRICE = new Money(200);
  private static final Money LAND_MORTGAGE_VALUE = new Money(100);
  private static final List<Money> RENT_BY_OWNED_COUNT = List.of(
      ZERO,
      new Money(25),
      new Money(50),
      new Money(100),
      new Money(200)
  );

  @Override
  public Street create(Street.Type type, Set<Rule> activatedRules) {
    return Street.station(type, activatedRules, PRICE, LAND_MORTGAGE_VALUE, RENT_BY_OWNED_COUNT);
  }
}
