package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;

import java.util.List;

/**
 * A buildable street in a colour group. Every such street differs only in its
 * financial figures, so they are all built from this one data-driven factory.
 */
public record ColourStreet(
    Street.Type type,
    Street.Colour colourGroup,
    Money price,
    List<Money> rentByHouses,
    Money rentForOneHotel,
    Money constructionCost,
    Money landMortgageValue
) implements Ownable {
  /** A street holds four houses before a hotel replaces them. */
  public static final int HOUSES_PER_HOTEL = 4;

  @Override
  public Street.Kind kind() {
    return Street.Kind.street;
  }

  /**
   * Rent owed by a visitor, which depends on how far the street has been built
   * up. Nought houses is the vacant rent.
   */
  public Money rentForHouses(int houses) {
    return rentByHouses.get(OwnedCount.checked(houses, rentByHouses, type, "houses"));
  }

  public Money vacantRent() {
    return rentForHouses(0);
  }

  /**
   * A hotel costs the same as a house on the official board, so one
   * construction cost covers both.
   */
  public Money houseConstructionCost() {
    return constructionCost;
  }

  public Money hotelConstructionCost() {
    return constructionCost;
  }

  public int hotelConstructionRequiresNumberOfHouses() {
    return HOUSES_PER_HOTEL;
  }

  static Street.Factory of(
      Street.Colour colourGroup,
      int price,
      int vacantRent,
      int rentForOneHouse,
      int rentForTwoHouses,
      int rentForThreeHouses,
      int rentForFourHouses,
      int rentForOneHotel,
      int constructionCost,
      int landMortgageValue
  ) {
    List<Money> rentByHouses = List.of(
        new Money(vacantRent),
        new Money(rentForOneHouse),
        new Money(rentForTwoHouses),
        new Money(rentForThreeHouses),
        new Money(rentForFourHouses)
    );
    return (type, activatedRules) -> new ColourStreet(
        type,
        colourGroup,
        new Money(price),
        rentByHouses,
        new Money(rentForOneHotel),
        new Money(constructionCost),
        new Money(landMortgageValue)
    );
  }
}
