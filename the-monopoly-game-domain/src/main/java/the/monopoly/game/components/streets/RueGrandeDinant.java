package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

public class RueGrandeDinant implements Street.Factory {
  @Override
  public Street create(Set<Rule> activatedRules) {
    return new Street(
        Street.Type.RueGrandeDinant,
        activatedRules,
        new RentFactory(
            new Money(2),
            new Money(10),
            new Money(30),
            new Money(90),
            new Money(160),
            new Money(250)
        ),
        new Money.Factory.Fixed(new Money(60)),
        new ConstructionCostFactory(
            new Money(50),
            new Money(50)
        ),
        new Money(30)
    );
  }

  private static class RentFactory implements Money.Factory.Rent {
    private final Money vacant, forOneHouse, forTwoHouses, forThreeHouses, forFourHouses, forOneHotel;

    public RentFactory(
        Money vacant,
        Money forOneHouse,
        Money forTwoHouses,
        Money forThreeHouses,
        Money forFourHouses,
        Money forOneHotel
    ) {
      this.vacant = vacant;
      this.forOneHouse = forOneHouse;
      this.forTwoHouses = forTwoHouses;
      this.forThreeHouses = forThreeHouses;
      this.forFourHouses = forFourHouses;
      this.forOneHotel = forOneHotel;
    }

    @Override
    public Money vacant() {
      return vacant;
    }

    @Override
    public Money forOneHouse() {
      return forOneHouse;
    }

    @Override
    public Money forTwoHouses() {
      return forTwoHouses;
    }

    @Override
    public Money forThreeHouses() {
      return forThreeHouses;
    }

    @Override
    public Money forFourHouses() {
      return forFourHouses;
    }

    @Override
    public Money forOneHotel() {
      return forOneHotel;
    }

    @Override
    public Money create(Set<Rule> rules) {
      return null;
    }
  }

  private static class ConstructionCostFactory implements Money.Factory.ConstructionCost {
    private final Money house, hotel;

    private ConstructionCostFactory(Money house, Money hotel) {
      this.house = house;
      this.hotel = hotel;
    }

    @Override
    public Money create(Set<Rule> rules) {
      return null;
    }

    @Override
    public Money house() {
      return house;
    }

    @Override
    public Money hotel() {
      return hotel;
    }
  }
}
