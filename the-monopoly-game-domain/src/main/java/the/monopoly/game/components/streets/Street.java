package the.monopoly.game.components.streets;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static the.monopoly.game.components.finance.Money.ZERO;
import static the.monopoly.game.components.streets.Street.Colour.*;
import static the.monopoly.game.components.streets.Street.Kind.*;

public class Street {
  private final Type type;
  private final Kind kind;
  private final Colour colourGroup;
  private final Set<Rule> activatedRules;
  private final Money.Factory.Rent rent;
  private final Money.Factory.Toll toll;
  private final Money.Factory.ConstructionCost constructionCost;
  private final Money landMortgageValue;
  private final List<Money> rentByOwnedCount;
  private final List<Integer> diceMultiplierByOwnedCount;
  private final Money tax;

  private Street(
      Type type,
      Kind kind,
      Colour colourGroup,
      Set<Rule> activatedRules,
      Money.Factory.Rent rent,
      Money.Factory.Toll toll,
      Money.Factory.ConstructionCost constructionCost,
      Money landMortgageValue,
      List<Money> rentByOwnedCount,
      List<Integer> diceMultiplierByOwnedCount,
      Money tax
  ) {
    this.type = type;
    this.kind = kind;
    this.colourGroup = colourGroup;
    this.activatedRules = activatedRules;
    this.rent = rent;
    this.toll = toll;
    this.constructionCost = constructionCost;
    this.landMortgageValue = landMortgageValue;
    this.rentByOwnedCount = rentByOwnedCount;
    this.diceMultiplierByOwnedCount = diceMultiplierByOwnedCount;
    this.tax = tax;
  }

  static Street startSpace(
      Type type,
      Set<Rule> activatedRules,
      Money.Factory.Rent rent,
      Money.Factory.Toll toll
  ) {
    return new Street(
        type, Kind.start, null, activatedRules,
        rent, toll, new Money.Factory.Fixed(ZERO), ZERO,
        null, null, null
    );
  }

  static Street colourStreet(
      Type type,
      Colour colourGroup,
      Set<Rule> activatedRules,
      Money.Factory.Rent rent,
      Money price,
      Money.Factory.ConstructionCost constructionCost,
      Money landMortgageValue
  ) {
    return new Street(
        type, street, colourGroup, activatedRules,
        rent, new Money.Factory.Fixed(price), constructionCost, landMortgageValue,
        null, null, null
    );
  }

  static Street station(
      Type type,
      Set<Rule> activatedRules,
      Money price,
      Money landMortgageValue,
      List<Money> rentByOwnedCount
  ) {
    return new Street(
        type, Kind.station, null, activatedRules,
        null, new Money.Factory.Fixed(price), null, landMortgageValue,
        rentByOwnedCount, null, null
    );
  }

  static Street utility(
      Type type,
      Set<Rule> activatedRules,
      Money price,
      Money landMortgageValue,
      List<Integer> diceMultiplierByOwnedCount
  ) {
    return new Street(
        type, Kind.utility, null, activatedRules,
        null, new Money.Factory.Fixed(price), null, landMortgageValue,
        null, diceMultiplierByOwnedCount, null
    );
  }

  static Street taxSpace(Type type, Set<Rule> activatedRules, Money tax) {
    return new Street(
        type, Kind.tax, null, activatedRules,
        null, null, null, ZERO,
        null, null, tax
    );
  }

  static Street unownable(Type type, Kind kind, Set<Rule> activatedRules) {
    return new Street(
        type, kind, null, activatedRules,
        null, null, null, ZERO,
        null, null, null
    );
  }

  public Type type() {
    return type;
  }

  public Kind kind() {
    return kind;
  }

  public Colour colourGroup() {
    return colourGroup;
  }

  public Money toll() {
    return require(toll, "a price").create(activatedRules);
  }

  public Money rent() {
    return require(rent, "rent").create(activatedRules);
  }

  public Money vacantRent() {
    return require(rent, "rent").vacant();
  }

  public Money rentForOneHouse() {
    return require(rent, "rent").forOneHouse();
  }

  public Money rentForTwoHouses() {
    return require(rent, "rent").forTwoHouses();
  }

  public Money rentForThreeHouses() {
    return require(rent, "rent").forThreeHouses();
  }

  public Money rentForFourHouses() {
    return require(rent, "rent").forFourHouses();
  }

  public Money rentForOneHotel() {
    return require(rent, "rent").forOneHotel();
  }

  public Money houseConstructionCost() {
    return require(constructionCost, "construction costs").house();
  }

  public Money hotelConstructionCost() {
    return require(constructionCost, "construction costs").hotel();
  }

  public int hotelConstructionRequiresNumberOfHouses() {
    return 4;
  }

  public Money landMortgageValue() {
    return landMortgageValue;
  }

  /**
   * Rent owed on a station, which depends on how many stations its owner holds.
   */
  public Money rentForOwning(int stations) {
    List<Money> rents = require(rentByOwnedCount, "rent per owned station");
    return rents.get(ownedCount(stations, rents));
  }

  /**
   * The factor applied to the dice roll on a utility, which depends on how many
   * utilities its owner holds.
   */
  public int rentDiceMultiplierForOwning(int utilities) {
    List<Integer> multipliers = require(diceMultiplierByOwnedCount, "a dice multiplier");
    return multipliers.get(ownedCount(utilities, multipliers));
  }

  public Money tax() {
    return require(tax, "a tax");
  }

  private int ownedCount(int owned, List<?> valuesByCount) {
    if (owned < 0 || owned >= valuesByCount.size())
      throw new IllegalArgumentException(
          "Cannot own " + owned + " of " + type + "; the board holds " + (valuesByCount.size() - 1) + "."
      );
    return owned;
  }

  private <T> T require(T aspect, String description) {
    if (aspect == null)
      throw new UnsupportedOperationException(type + " does not have " + description + ".");
    return aspect;
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

  public enum Type {
    start(new Start()),

    RueGrandeDinant(ColourStreet.of(brown, 60, 2, 10, 30, 90, 160, 250, 50, 30)),
    DiestsestraatLeuven(ColourStreet.of(brown, 60, 4, 20, 60, 180, 320, 450, 50, 30)),
    SteenstraatBrugge(ColourStreet.of(light_blue, 100, 6, 30, 90, 270, 400, 550, 50, 50)),
    PlaceDuMonumentSpa(ColourStreet.of(light_blue, 100, 6, 30, 90, 270, 400, 550, 50, 50)),
    KapellestraatOostende(ColourStreet.of(light_blue, 120, 8, 40, 100, 300, 450, 600, 50, 60)),
    RueDeDiekirchArlon(ColourStreet.of(pink, 140, 10, 50, 150, 450, 625, 750, 100, 70)),
    BruulMechelen(ColourStreet.of(pink, 140, 10, 50, 150, 450, 625, 750, 100, 70)),
    PlaceVerteVerviers(ColourStreet.of(pink, 160, 12, 60, 180, 500, 700, 900, 100, 80)),
    LippenslaanKnokke(ColourStreet.of(orange, 180, 14, 70, 200, 550, 750, 950, 100, 90)),
    RueRoyaleTournai(ColourStreet.of(orange, 180, 14, 70, 200, 550, 750, 950, 100, 90)),
    GroenplaatsAntwerpen(ColourStreet.of(orange, 200, 16, 80, 220, 600, 800, 1000, 100, 100)),
    RueStLeonardLiege(ColourStreet.of(red, 220, 18, 90, 250, 700, 875, 1050, 150, 110)),
    LangeSteenstraatKortrijk(ColourStreet.of(red, 220, 18, 90, 250, 700, 875, 1050, 150, 110)),
    GrandPlaceMons(ColourStreet.of(red, 240, 20, 100, 300, 750, 925, 1100, 150, 120)),
    GroteMarktHasselt(ColourStreet.of(yellow, 260, 22, 110, 330, 800, 975, 1150, 150, 130)),
    PlaceDeLAngeNamur(ColourStreet.of(yellow, 260, 22, 110, 330, 800, 975, 1150, 150, 130)),
    HoogstraatBrussel(ColourStreet.of(yellow, 280, 24, 120, 360, 850, 1025, 1200, 150, 140)),
    BoulevardTirouCharleroi(ColourStreet.of(green, 300, 26, 130, 390, 900, 1100, 1275, 200, 150)),
    VeldstraatGent(ColourStreet.of(green, 300, 26, 130, 390, 900, 1100, 1275, 200, 150)),
    BoulevardDAvroyLiege(ColourStreet.of(green, 320, 28, 150, 450, 1000, 1200, 1400, 200, 160)),
    MeirAntwerpen(ColourStreet.of(dark_blue, 350, 35, 175, 500, 1100, 1300, 1500, 200, 175)),
    NieuwstraatBrussel(ColourStreet.of(dark_blue, 400, 50, 200, 600, 1400, 1700, 2000, 200, 200)),

    NoordStation(new Station()),
    CentraalStation(new Station()),
    Buurtspoorwegen(new Station()),
    ZuidStation(new Station()),

    Elektriciteitscentrale(new Utility()),
    Watermaatschappij(new Utility()),

    InkomstenBelasting(new TaxSpace(200)),
    ExtraBelasting(new TaxSpace(100)),

    Kans(new Unownable(chance)),
    AlgemeenFonds(new Unownable(community_chest)),
    OpBezoek(new Unownable(jail)),
    GratisParkeren(new Unownable(free_parking)),
    NaarDeGevangenis(new Unownable(go_to_jail));

    private final Factory factory;

    Type(Factory factory) {
      this.factory = factory;
    }

    public Street create(Set<Rule> activatedRules) {
      return factory.create(this, activatedRules == null ? emptySet() : activatedRules);
    }
  }

  public enum Kind {
    start, street, station, utility, tax, chance, community_chest, jail, free_parking, go_to_jail
  }

  public enum Colour {
    brown, light_blue, pink, orange, red, yellow, green, dark_blue
  }

  public interface Factory {
    Street create(Type type, Set<Rule> activatedRules);
  }
}
