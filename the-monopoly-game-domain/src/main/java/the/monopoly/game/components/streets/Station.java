package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

import java.util.List;

import static the.monopoly.game.components.finance.Money.ZERO;

/**
 * A station cannot be built on. Its rent depends on how many of the four
 * stations its owner holds, doubling with each additional one.
 */
public record Station(Street.Type type) implements Ownable {
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
  public Street.Kind kind() {
    return Street.Kind.station;
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
   * Rent owed on this station, which depends on how many stations its owner
   * holds.
   */
  public Money rentForOwning(int stations) {
    return RENT_BY_OWNED_COUNT.get(
        OwnedCount.checked(stations, RENT_BY_OWNED_COUNT, type, "stations")
    );
  }

  static Street.Factory factory() {
    return (type, activatedRules) -> new Station(type);
  }
}
