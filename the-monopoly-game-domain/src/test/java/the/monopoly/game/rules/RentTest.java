package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RentTest {
  private static final Roll IRRELEVANT_ROLL = new Roll(1, 1);

  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();
  private final Map<Player.ID, Strategy> strategies = new HashMap<>();
  private final Paid paid = new Paid();
  private final Player tenant = playerWith("tenant");
  private final Player owner = playerWith("owner");

  @Test
  void aTenantPaysTheVacantRentToTheOwnerWhoClaimsIt() {
    sell(Street.Type.DiestsestraatLeuven);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street(Street.Type.DiestsestraatLeuven), IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1496));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1504));
    assertThat(paid.amount).isEqualTo(new Money(4));
  }

  @Test
  void owningEveryStreetInAColourGroupDoublesTheUnimprovedRent() {
    sell(Street.Type.RueGrandeDinant);
    sell(Street.Type.DiestsestraatLeuven);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street(Street.Type.DiestsestraatLeuven), IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1492));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1508));
  }

  @Test
  void rentIsNotCollectedWhenTheOwnerDeclinesIt() {
    sell(Street.Type.DiestsestraatLeuven);

    rent().resolve(tenant, street(Street.Type.DiestsestraatLeuven), IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1500));
  }

  @Test
  void stationRentDependsOnHowManyStationsTheOwnerHolds() {
    sell(Street.Type.NoordStation);
    sell(Street.Type.CentraalStation);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, rules.create(Street.Type.NoordStation), IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1450));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1550));
    assertThat(paid.amount).isEqualTo(new Money(50));
  }

  @Test
  void utilityRentMultipliesTheLandingRollByTheOwnedCountRate() {
    sell(Street.Type.Elektriciteitscentrale);
    sell(Street.Type.Watermaatschappij);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, rules.create(Street.Type.Elektriciteitscentrale), new Roll(2, 5));

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1430));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1570));
    assertThat(paid.amount).isEqualTo(new Money(70));
  }

  @Test
  void housesChargeTheirPrintedRent() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    deeds.arrangeHouses(street, 2);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1440));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1560));
  }

  @Test
  void oneHouseChargesItsPrintedRent() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    deeds.arrangeHouses(street, 1);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(paid.amount).isEqualTo(street.rentForHouses(1));
  }

  @Test
  void aHotelChargesItsPrintedRent() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    deeds.arrangeHotel(street);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1050));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1950));
  }

  @Test
  void mortgagedLandCollectsNoRent() {
    sell(Street.Type.DiestsestraatLeuven);
    deeds.arrangeMortgaged(street(Street.Type.DiestsestraatLeuven));
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street(Street.Type.DiestsestraatLeuven), IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1500));
  }

  @Test
  void aMortgagedStreetInAColourGroupStopsDoubleRentOnTheOtherStreetToo() {
    sell(Street.Type.RueGrandeDinant);
    sell(Street.Type.DiestsestraatLeuven);
    deeds.arrangeMortgaged((Ownable) rules.create(Street.Type.RueGrandeDinant));
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street(Street.Type.DiestsestraatLeuven), IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1496));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1504));
  }

  @Test
  void anUnimprovedStreetInAPartiallyBuiltMonopolyStillChargesDoubleRent() {
    sell(Street.Type.RueGrandeDinant);
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    deeds.arrangeHouses((ColourStreet) rules.create(Street.Type.RueGrandeDinant), 2);
    strategies.put(owner.id(), new Claiming());

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1492));
    assertThat(owner.account().balance()).isEqualTo(Balance.of(1508));
  }

  @Test
  void aNonShareholderTenantPaysDoubleVacantRentToAnEntityOwnedStreet() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    LegalEntity entity = formEntityOwning(street);

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    Money doubleVacantRent = street.vacantRent().plus(street.vacantRent());
    assertThat(tenant.account().balance()).isEqualTo(new Balance(new Money(1500).minus(doubleVacantRent)));
    assertThat(entity.receivedRent()).isTrue();
  }

  @Test
  void aShareholderPaysRentToTheirOwnEntity() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    LegalEntity entity = LegalEntity.formed("Test Realty", street.colourGroup(), List.of(tenant, owner,
        playerWith("third")), rules);
    deeds.form(entity);

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1492));
    assertThat(entity.receivedRent()).isTrue();
  }

  @Test
  void aDevelopedEntityOwnedStreetChargesItsPrintedHouseRent() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    LegalEntity entity = formEntityOwning(street);
    deeds.arrangeHouses(street, 2);

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1440));
    assertThat(entity.receivedRent()).isTrue();
  }

  @Test
  void anEntityOwnedHotelChargesItsPrintedHotelRent() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    LegalEntity entity = formEntityOwning(street);
    deeds.arrangeHotel(street);

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1050));
    assertThat(entity.receivedRent()).isTrue();
  }

  @Test
  void aMortgagedEntityOwnedStreetCollectsNoRent() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    LegalEntity entity = formEntityOwning(street);
    deeds.arrangeMortgaged(street);

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(entity.receivedRent()).isFalse();
  }

  @Test
  void aTenantWhoCannotAffordEntityRentPaysNothing() {
    ColourStreet street = sell(Street.Type.DiestsestraatLeuven);
    LegalEntity entity = formEntityOwning(street);
    tenant.account().withdraw(new Money(1500));

    rent().resolve(tenant, street, IRRELEVANT_ROLL);

    assertThat(tenant.account().balance()).isEqualTo(Balance.of(0));
    assertThat(entity.receivedRent()).isFalse();
  }

  private LegalEntity formEntityOwning(ColourStreet street) {
    LegalEntity entity = LegalEntity.formed("Test Realty", street.colourGroup(),
        List.of(owner, playerWith("second"), playerWith("third")), rules);
    deeds.form(entity);
    return entity;
  }

  private Rent rent() {
    return new Rent(deeds, rules, List.of(tenant, owner), this::strategyFor, paid);
  }

  private Strategy strategyFor(Player player) {
    return strategies.getOrDefault(player.id(), Strategy.UNDECIDED);
  }

  private ColourStreet sell(Street.Type type) {
    Ownable land = (Ownable) rules.create(type);
    deeds.sell(land, owner, land.price());
    owner.account().deposit(land.price());
    return land instanceof ColourStreet street ? street : null;
  }

  private ColourStreet street(Street.Type type) {
    return (ColourStreet) rules.create(type);
  }

  private Player playerWith(String name) {
    Player.ID id = new Player.ID(name);
    rules.bank().createAccountFor(id);
    Player player = new Player(id, rules.bank().accountOf(id));
    player.account().deposit(new Money(1500));
    return player;
  }

  private static final class Claiming implements Strategy {
    @Override
    public boolean claims(RentClaim claim) {
      return true;
    }
  }

  private static final class Paid implements Rent.Events {
    private Money amount;

    @Override
    public void paid(Player tenant, Player owner, Ownable land, Money rent) {
      amount = rent;
    }
  }
}
