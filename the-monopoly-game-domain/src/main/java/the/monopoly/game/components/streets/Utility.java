package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

import java.util.List;

/**
 * A utility cannot be built on. Its rent is the dice roll multiplied by a
 * factor that depends on how many of the two utilities its owner holds.
 */
public record Utility(Street.Type type) implements Ownable {
  private static final Money PRICE = new Money(150);
  private static final Money LAND_MORTGAGE_VALUE = new Money(75);
  private static final List<Integer> DICE_MULTIPLIER_BY_OWNED_COUNT = List.of(0, 4, 10);

  @Override
  public Street.Kind kind() {
    return Street.Kind.utility;
  }

  @Override
  public Money price() {
    return PRICE;
  }

  @Override
  public Money landMortgageValue() {
    return LAND_MORTGAGE_VALUE;
  }

  /**
   * The factor applied to the dice roll on this utility, which depends on how
   * many utilities its owner holds.
   */
  public int rentDiceMultiplierForOwning(int utilities) {
    return DICE_MULTIPLIER_BY_OWNED_COUNT.get(
        OwnedCount.checked(utilities, DICE_MULTIPLIER_BY_OWNED_COUNT, type, "utilities")
    );
  }

  static Street.Factory factory() {
    return (type, activatedRules) -> new Utility(type);
  }
}
