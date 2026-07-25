package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.List;
import java.util.Set;

/**
 * A utility cannot be built on. Its rent is the dice roll multiplied by a
 * factor that depends on how many of the two utilities its owner holds.
 */
class Utility implements Street.Factory {
  private static final Money PRICE = new Money(150);
  private static final Money LAND_MORTGAGE_VALUE = new Money(75);
  private static final List<Integer> DICE_MULTIPLIER_BY_OWNED_COUNT = List.of(0, 4, 10);

  @Override
  public Street create(Street.Type type, Set<Rule> activatedRules) {
    return Street.utility(type, activatedRules, PRICE, LAND_MORTGAGE_VALUE, DICE_MULTIPLIER_BY_OWNED_COUNT);
  }
}
