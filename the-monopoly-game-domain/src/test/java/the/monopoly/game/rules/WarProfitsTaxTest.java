package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WarProfitsTaxTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Money boardValue = WarProfitsTax.boardValue(rules);

  @Test
  void boardValueMatchesTheStalemateThreshold() {
    assertThat(boardValue).isEqualTo(new Money(22790));
  }

  @Test
  void assessmentRaisesTheCashShortfallBeforePayingGovernment() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog");
    WarProfitsTaxBook book = new WarProfitsTaxBook(bank, boardValue);
    book.accumulate(dog, new Money(1000));
    AtomicReference<Money> requested = new AtomicReference<>();

    Money paid = book.assess(dog, new Money(5698), shortfall -> {
      requested.set(shortfall);
      dog.account().deposit(shortfall);
    });

    assertThat(requested).hasValue(new Money(1000));
    assertThat(paid).isEqualTo(new Money(1000));
    assertThat(dog.account().balance().amount()).isEqualTo(Money.ZERO);
    assertThat(book.governmentBalance()).isEqualTo(new Money(1000));
  }

  @Test
  void assessmentsAccumulateTaxesInTheGovernmentAccount() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog");
    Player highHat = player(bank, "high hat");
    WarProfitsTaxBook book = new WarProfitsTaxBook(bank, boardValue);
    book.accumulate(dog, new Money(1000));
    book.accumulate(highHat, new Money(1000));
    dog.account().deposit(new Money(1000));
    highHat.account().deposit(new Money(1000));

    book.assess(dog, new Money(6000));
    book.assess(highHat, new Money(6000));

    assertThat(book.governmentBalance()).isEqualTo(new Money(2000));
  }

  @Test
  void belowTwentyFivePercentOwnershipAttractsNoRate() {
    assertThat(WarProfitsTax.rate(boardValue, new Money(5000))).isZero();
    assertThat(WarProfitsTax.rate(boardValue, new Money(5697))).isZero();
  }

  @Test
  void eachBandRateAppliesAtAndAboveItsLowerBound() {
    assertThat(WarProfitsTax.rate(boardValue, new Money(5698))).isEqualTo(100);
    assertThat(WarProfitsTax.rate(boardValue, new Money(9115))).isEqualTo(100);
    assertThat(WarProfitsTax.rate(boardValue, new Money(9116))).isEqualTo(150);
    assertThat(WarProfitsTax.rate(boardValue, new Money(13673))).isEqualTo(150);
    assertThat(WarProfitsTax.rate(boardValue, new Money(13674))).isEqualTo(200);
    assertThat(WarProfitsTax.rate(boardValue, new Money(18231))).isEqualTo(200);
    assertThat(WarProfitsTax.rate(boardValue, new Money(18232))).isEqualTo(300);
    assertThat(WarProfitsTax.rate(boardValue, new Money(22789))).isEqualTo(300);
    assertThat(WarProfitsTax.rate(boardValue, new Money(22790))).isEqualTo(400);
  }

  @Test
  void taxIsCollectedRentScaledByTheBandRate() {
    assertThat(WarProfitsTax.tax(boardValue, new Money(5698), new Money(1000))).isEqualTo(new Money(1000));
    assertThat(WarProfitsTax.tax(boardValue, new Money(9116), new Money(1000))).isEqualTo(new Money(1500));
    assertThat(WarProfitsTax.tax(boardValue, new Money(13674), new Money(1000))).isEqualTo(new Money(2000));
    assertThat(WarProfitsTax.tax(boardValue, new Money(18232), new Money(1000))).isEqualTo(new Money(3000));
    assertThat(WarProfitsTax.tax(boardValue, new Money(22790), new Money(1000))).isEqualTo(new Money(4000));
  }

  @Test
  void exactTwentyFivePercentOwnershipEntersTheFirstTaxBand() {
    long exact = boardValue.cents() * 25 / 100;
    assertThat(WarProfitsTax.rate(boardValue, Money.fromCents(exact - 1))).isZero();
    assertThat(WarProfitsTax.rate(boardValue, Money.fromCents(exact))).isEqualTo(100);
  }

  @Test
  void landValueUsesTheOneHouseRentForOneHouse() {
    Deeds deeds = new Deeds();
    Player dog = player("dog");
    ColourStreet meir = street(Street.Type.MeirAntwerpen);
    deeds.sell(meir, dog, Money.ZERO);
    deeds.arrangeHouses(meir, 1);

    assertThat(WarProfitsTax.landValue(rules, deeds, dog)).isEqualTo(meir.rentForHouses(1));
  }

  @Test
  void landValueUsesTheCurrentHouseTierRent() {
    Deeds deeds = new Deeds();
    Player dog = player("dog");
    ColourStreet meir = street(Street.Type.MeirAntwerpen);
    deeds.sell(meir, dog, Money.ZERO);
    deeds.arrangeHouses(meir, 2);

    assertThat(WarProfitsTax.landValue(rules, deeds, dog)).isEqualTo(meir.rentForHouses(2));
  }

  @Test
  void landValueDoesNotDoubleVacantRentWithoutAnUnmortgagedMonopoly() {
    Deeds deeds = new Deeds();
    Player dog = player("dog");
    ColourStreet meir = street(Street.Type.MeirAntwerpen);
    ColourStreet nieuwstraat = street(Street.Type.NieuwstraatBrussel);
    deeds.sell(meir, dog, Money.ZERO);
    deeds.sell(nieuwstraat, dog, Money.ZERO);
    deeds.arrangeMortgaged(nieuwstraat);

    assertThat(WarProfitsTax.landValue(rules, deeds, dog)).isEqualTo(meir.vacantRent());
  }

  @Test
  void landValueUsesVacantRentForOwnedUndevelopedLand() {
    Deeds deeds = new Deeds();
    Player dog = player("dog");
    ColourStreet meir = street(Street.Type.MeirAntwerpen);
    ColourStreet nieuwstraat = street(Street.Type.NieuwstraatBrussel);
    deeds.sell(meir, dog, Money.ZERO);
    deeds.sell(nieuwstraat, dog, Money.ZERO);

    Money expected = meir.vacantRent().plus(meir.vacantRent())
        .plus(nieuwstraat.vacantRent()).plus(nieuwstraat.vacantRent());
    assertThat(WarProfitsTax.landValue(rules, deeds, dog)).isEqualTo(expected);
  }

  @Test
  void landValueUsesCurrentHotelRentForDevelopedLand() {
    Deeds deeds = new Deeds();
    Player dog = player("dog");
    List<ColourStreet> streets = List.of(
        street(Street.Type.MeirAntwerpen),
        street(Street.Type.NieuwstraatBrussel),
        street(Street.Type.BoulevardTirouCharleroi),
        street(Street.Type.VeldstraatGent),
        street(Street.Type.BoulevardDAvroyLiege));
    streets.forEach(street -> {
      deeds.sell(street, dog, Money.ZERO);
      deeds.arrangeHotel(street);
    });

    Money expected = streets.stream().map(ColourStreet::rentForOneHotel).reduce(Money.ZERO, Money::plus);
    assertThat(WarProfitsTax.landValue(rules, deeds, dog)).isEqualTo(expected);
  }

  @Test
  void landValueExcludesMortgagedAndLegalEntityLand() {
    Deeds deeds = new Deeds();
    Player dog = player("dog");
    ColourStreet direct = street(Street.Type.MeirAntwerpen);
    deeds.sell(direct, dog, Money.ZERO);
    deeds.arrangeMortgaged(direct);
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, List.of(dog), rules);
    deeds.form(entity);

    assertThat(WarProfitsTax.landValue(rules, deeds, dog)).isEqualTo(Money.ZERO);
  }

  private ColourStreet street(Street.Type type) {
    return (ColourStreet) rules.create(type);
  }

  private Player player(String name) {
    return player(rules.bank(), name);
  }

  private Player player(Bank bank, String name) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}