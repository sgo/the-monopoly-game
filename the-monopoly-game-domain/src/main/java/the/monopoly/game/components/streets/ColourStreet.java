package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

/**
 * A buildable street in a colour group. Every such street differs only in its
 * financial figures, so they are all built from this one data-driven factory.
 */
class ColourStreet implements Street.Factory {
  private final Street.Colour colourGroup;
  private final Money price;
  private final Money.Factory.Rent rent;
  private final Money.Factory.ConstructionCost constructionCost;
  private final Money landMortgageValue;

  private ColourStreet(
      Street.Colour colourGroup,
      Money price,
      Money.Factory.Rent rent,
      Money.Factory.ConstructionCost constructionCost,
      Money landMortgageValue
  ) {
    this.colourGroup = colourGroup;
    this.price = price;
    this.rent = rent;
    this.constructionCost = constructionCost;
    this.landMortgageValue = landMortgageValue;
  }

  /**
   * A hotel costs the same as a house on the official board, so one
   * construction cost covers both.
   */
  static ColourStreet of(
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
    return new ColourStreet(
        colourGroup,
        new Money(price),
        new HouseRent(
            new Money(vacantRent),
            new Money(rentForOneHouse),
            new Money(rentForTwoHouses),
            new Money(rentForThreeHouses),
            new Money(rentForFourHouses),
            new Money(rentForOneHotel)
        ),
        new ConstructionCosts(new Money(constructionCost)),
        new Money(landMortgageValue)
    );
  }

  @Override
  public Street create(Street.Type type, Set<Rule> activatedRules) {
    return Street.colourStreet(type, colourGroup, activatedRules, rent, price, constructionCost, landMortgageValue);
  }

  private record HouseRent(
      Money vacant,
      Money forOneHouse,
      Money forTwoHouses,
      Money forThreeHouses,
      Money forFourHouses,
      Money forOneHotel
  ) implements Money.Factory.Rent {
    @Override
    public Money create(Set<Rule> rules) {
      return vacant;
    }
  }

  private record ConstructionCosts(Money cost) implements Money.Factory.ConstructionCost {
    @Override
    public Money create(Set<Rule> rules) {
      return cost;
    }

    @Override
    public Money house() {
      return cost;
    }

    @Override
    public Money hotel() {
      return cost;
    }
  }
}
