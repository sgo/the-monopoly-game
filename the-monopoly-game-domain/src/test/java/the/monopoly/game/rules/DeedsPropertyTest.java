package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;

/**
 * DeedsTest exercises building, selling, and exchanging on one hand-picked
 * street. This sweeps every colour street, every valid starting house count,
 * and a wide range of starting balances, to pin down the price and
 * improvement-count invariants for each mechanic together, rather than at a
 * single point.
 */
@Tag("property-test")
class DeedsPropertyTest {
  private static final Generator<Integer> BALANCES = Generator.integers(0, 5_000);

  @Test
  void buildingAHouseChargesItsConstructionCostAndAddsOneHouse() {
    PropertyChecker.forAll(cases(Generator.integers(0, 3)), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = ownerWith(rules, deeds, c.street(), c.balance());
      deeds.arrangeHouses(c.street(), c.houses());
      Money before = owner.account().balance().amount();

      deeds.buildHouse(c.street(), owner);

      return owner.account().balance().amount().equals(before.minus(c.street().houseConstructionCost()))
          && deeds.housesBuiltOn(c.street()) == c.houses() + 1
          && !deeds.hasHotelOn(c.street());
    });
  }

  @Test
  void buildingAHotelChargesItsPrintedRentAndReplacesFourHouses() {
    PropertyChecker.forAll(streetsAndBalances(), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = ownerWith(rules, deeds, c.street(), c.balance());
      deeds.arrangeHouses(c.street(), c.street().hotelConstructionRequiresNumberOfHouses());
      Money before = owner.account().balance().amount();

      deeds.buildHotel(c.street(), owner);

      return owner.account().balance().amount().equals(before.minus(c.street().rentForOneHotel()))
          && deeds.hasHotelOn(c.street())
          && deeds.housesBuiltOn(c.street()) == 0;
    });
  }

  @Test
  void sellingAHouseRefundsHalfItsConstructionCostAndRemovesOneHouse() {
    PropertyChecker.forAll(cases(Generator.integers(1, 4)), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = ownerWith(rules, deeds, c.street(), c.balance());
      deeds.arrangeHouses(c.street(), c.houses());
      Money before = owner.account().balance().amount();

      Money refund = deeds.sellHouse(c.street(), owner);

      Money expectedRefund = new Money(c.street().houseConstructionCost().amount() / 2);
      return refund.equals(expectedRefund)
          && owner.account().balance().amount().equals(before.plus(expectedRefund))
          && deeds.housesBuiltOn(c.street()) == c.houses() - 1;
    });
  }

  @Test
  void exchangingAHotelRefundsHalfItsPrintedRentAndRestoresFourHouses() {
    PropertyChecker.forAll(streetsAndBalances(), c -> {
      Rule.Set rules = Rule.Set.Type.official.create();
      Deeds deeds = new Deeds();
      Player owner = ownerWith(rules, deeds, c.street(), c.balance());
      deeds.arrangeHotel(c.street());
      Money before = owner.account().balance().amount();

      Money refund = deeds.exchangeHotelForHouses(c.street(), owner);

      Money expectedRefund = new Money(c.street().rentForOneHotel().amount() / 2);
      return refund.equals(expectedRefund)
          && owner.account().balance().amount().equals(before.plus(expectedRefund))
          && deeds.housesBuiltOn(c.street()) == c.street().hotelConstructionRequiresNumberOfHouses()
          && !deeds.hasHotelOn(c.street());
    });
  }

  private Player ownerWith(Rule.Set rules, Deeds deeds, ColourStreet street, int balance) {
    Player.ID id = new Player.ID("owner");
    rules.bank().createAccountFor(id);
    Player owner = new Player(id, rules.bank().accountOf(id));
    deeds.sell(street, owner, Money.ZERO);
    owner.account().deposit(new Money(balance));
    return owner;
  }

  private Generator<Case> cases(Generator<Integer> houseCounts) {
    return colourStreetTypes().flatMap(street ->
        houseCounts.flatMap(houses ->
            BALANCES.map(balance -> new Case(street, houses, balance))));
  }

  private Generator<Case> streetsAndBalances() {
    return colourStreetTypes().flatMap(street ->
        BALANCES.map(balance -> new Case(street, 0, balance)));
  }

  private Generator<ColourStreet> colourStreetTypes() {
    return Generator.sampledFrom(
        Rule.Set.Type.official.create().streets()
            .filter(ColourStreet.class::isInstance)
            .map(ColourStreet.class::cast)
            .toList()
    );
  }

  private record Case(ColourStreet street, int houses, int balance) {
  }
}
