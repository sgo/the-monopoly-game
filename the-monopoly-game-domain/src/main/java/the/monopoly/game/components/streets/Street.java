package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.Set;

import static java.util.Collections.emptySet;
import static the.monopoly.game.components.finance.Money.ZERO;

public class Street {
  private final Type type;
  private final Set<Rule> activatedRules;
  private final Money.Factory.Rent rent;
  private final Money.Factory.Toll toll;
  private final Money.Factory.ConstructionCost constructionCost;
  private final Money landMortgageValue;

  public Street(
      Type type,
      Set<Rule> activatedRules,
      Money.Factory.Rent rent,
      Money.Factory.Toll toll,
      Money.Factory.ConstructionCost constructionCost,
      Money landMortgageValue
  ) {
    this.type = type;
    this.activatedRules = activatedRules;
    this.rent = rent;
    this.toll = toll;
    this.constructionCost = constructionCost;
    this.landMortgageValue = landMortgageValue;
  }

  public Street(
      Type type,
      Set<Rule> activatedRules,
      Money.Factory.Rent rent,
      Money.Factory.Toll toll
  ) {
    this(type, activatedRules, rent, toll, new Money.Factory.Fixed(ZERO), ZERO);
  }

  public Street(Type type, Money rent) {
    this(type, new Money.Factory.Fixed(rent));
  }

  private Street(Type type, Money.Factory.Fixed rent) {
    this(type, emptySet(), rent, rent, new Money.Factory.Fixed(ZERO), ZERO);
  }

  public Street(Type type, Money.Factory.Rent rent, Money toll) {
    this(type, emptySet(), rent, new Money.Factory.Fixed(toll), new Money.Factory.Fixed(ZERO), ZERO);
  }

  public Street(Type type, Money.Factory.Rent rent, Money toll, Money.Factory.ConstructionCost constructionCost) {
    this(type, emptySet(), rent, new Money.Factory.Fixed(toll), constructionCost, ZERO);
  }

  public Type type() {
    return type;
  }

  public Money toll() {
    return toll.create(activatedRules);
  }

  public Money rent() {
    return rent.create(activatedRules);
  }

  public Money vacantRent() {
    return rent.vacant();
  }

  public Money rentForOneHouse() {
    return rent.forOneHouse();
  }

  public Money rentForTwoHouses() {
    return rent.forTwoHouses();
  }

  public Money rentForThreeHouses() {
    return rent.forThreeHouses();
  }

  public Money rentForFourHouses() {
    return rent.forFourHouses();
  }

  public Money rentForOneHotel() {
    return rent.forOneHotel();
  }

  public Money houseConstructionCost() {
    return constructionCost.house();
  }

  public Money hotelConstructionCost() {
    return constructionCost.hotel();
  }

  public int hotelConstructionRequiresNumberOfHouses() {
    return 4;
  }

  public Money landMortgageValue() {
    return landMortgageValue;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Street street)) return false;

    return type == street.type;
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  public Colour colour() {
    return null;
  }

  public enum Type implements Factory {
    start(new Start()),
    RueGrandeDinant(new RueGrandeDinant()),
    DiestsestraatLeuven(new DiestsestraatLeuven());

    private final Factory factory;

    Type(Factory factory) {
      this.factory = factory;
    }

    @Override
    public Street create(Set<Rule> activatedRules) {
      return factory.create(activatedRules);
    }
  }

  public enum Colour {
    brown
  }

  public interface Factory {
    Street create(Set<Rule> activatedRules);
  }
}
