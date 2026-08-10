package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegalEntityTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");
  private final Player ironBox = player("iron box");

  @Test
  void threeDistinctGreedoOwnersCanFormAnEligibleEntityOnlyWhenTheBoardIsOwned() {
    own(Street.Type.RueDeDiekirchArlon, dog);
    own(Street.Type.BruulMechelen, highHat);
    own(Street.Type.PlaceVerteVerviers, ironBox);
    ownEveryRemainingSpace(highHat);

    LegalEntity entity = LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules, deeds, LegalEntityTest::highestPriority).orElseThrow();

    assertThat(entity.shareholders()).containsExactly(dog, highHat, ironBox);
    assertThat(entity.shareOf(dog)).isEqualTo(1.0 / 3.0);
    assertThat(entity.streets()).hasSize(3);
  }

  @Test
  void anUnownedStreetPreventsFormation() {
    own(Street.Type.RueDeDiekirchArlon, dog);
    own(Street.Type.BruulMechelen, highHat);
    own(Street.Type.PlaceVerteVerviers, ironBox);

    assertThat(LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules, deeds, LegalEntityTest::highestPriority)).isEmpty();
  }

  @Test
  void fewerThanThreeDistinctShareholdersPreventsFormation() {
    assertThat(LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, dog, highHat), rules, deeds, LegalEntityTest::highestPriority)).isEmpty();
  }

  @Test
  void aColourGroupLedByTheHighestPriorityStreetNeverConsolidates() {
    own(Street.Type.LippenslaanKnokke, dog);
    own(Street.Type.RueRoyaleTournai, highHat);
    own(Street.Type.GroenplaatsAntwerpen, ironBox);
    ownEveryRemainingSpace(highHat);

    assertThat(LegalEntity.form("Orange Realty", Street.Colour.orange,
        List.of(dog, highHat, ironBox), rules, deeds, LegalEntityTest::highestPriority)).isEmpty();
  }

  @Test
  void aGroupNotSplitAcrossThreeOwnersPreventsFormation() {
    own(Street.Type.RueDeDiekirchArlon, dog);
    own(Street.Type.BruulMechelen, dog);
    own(Street.Type.PlaceVerteVerviers, highHat);
    ownEveryRemainingSpace(highHat);

    assertThat(LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules, deeds, LegalEntityTest::highestPriority)).isEmpty();
  }

  @Test
  void aShareholderWhoOwnsNoStreetInTheGroupPreventsFormation() {
    own(Street.Type.RueDeDiekirchArlon, dog);
    own(Street.Type.BruulMechelen, highHat);
    own(Street.Type.PlaceVerteVerviers, ironBox);
    ownEveryRemainingSpace(highHat);
    Player outsider = player("outsider");

    assertThat(LegalEntity.form("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, outsider), rules, deeds, LegalEntityTest::highestPriority)).isEmpty();
  }

  @Test
  void operatingWithNoOutstandingLoanRaisesOneAndPaysADividend() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);

    LegalEntity.Operation operation = entity.operate();

    assertThat(operation).isEqualTo(new LegalEntity.Operation.LoanRaisedWithDividend(
        new Money(150), new Money(50)));
    assertThat(entity.loan()).isEqualTo(new Money(150));
    assertThat(entity.operated()).isTrue();
  }

  @Test
  void operatingWithAnOutstandingLoanRepaysItWithFivePercentInterest() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    entity.raiseLoan(new Money(100));

    LegalEntity.Operation operation = entity.operate();

    assertThat(operation).isEqualTo(new LegalEntity.Operation.LoanRepaid(
        dog, new Money(100), new Money(105)));
    assertThat(entity.loan()).isEqualTo(Money.ZERO);
  }

  @Test
  void operatingBuildsAHouseFromTheEntityTreasury() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    ColourStreet street = entity.streets().getFirst();
    entity.depositToBank(new Money(100));

    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.HouseBuilt(street));
    assertThat(deeds.housesBuiltOn(street)).isEqualTo(1);
    assertThat(entity.operated()).isTrue();
    assertThat(entity.bankBalance()).isEqualTo(Money.ZERO);
  }

  @Test
  void operatingTakesNoPaymentWhenShareholdersCannotFundTheFullHouseCost() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    ColourStreet street = entity.streets().getFirst();
    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.NoAction());
    assertThat(deeds.housesBuiltOn(street)).isEqualTo(0);
    assertThat(entity.operated()).isTrue();
    assertThat(entity.shareholderPayment(dog)).isEqualTo(Money.ZERO);
    assertThat(dog.account().balance().amount()).isEqualTo(Money.ZERO);
  }

  @Test
  void operatingSkipsReinvestmentWhenTheRentedStreetAlreadyHasAHouse() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    ColourStreet street = entity.streets().getFirst();
    deeds.arrangeHouses(street, 1);
    entity.receiveRent(street);

    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.NoAction());
  }

  private Money contributionFor(ColourStreet street) {
    int cost = street.houseConstructionCost().amount();
    return new Money((cost + 2) / 3);
  }

  private void fund(Player... shareholders) {
    for (Player shareholder : shareholders) shareholder.account().deposit(new Money(50));
  }

  @Test
  void recordsPaymentsMadeByShareholdersToTheEntity() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);

    entity.recordShareholderPayment(highHat, new Money(30));
    entity.recordShareholderPayment(highHat, new Money(10));

    assertThat(entity.shareholderPayment(highHat)).isEqualTo(new Money(40));
    assertThat(entity.shareholderPayment(ironBox)).isEqualTo(Money.ZERO);
  }

  @Test
  void fundsAnEntityHouseWithEqualShareholderContributions() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    dog.account().deposit(new Money(1500));
    highHat.account().deposit(new Money(1500));
    ironBox.account().deposit(new Money(1500));
    ColourStreet street = (ColourStreet) rules.create(Street.Type.RueDeDiekirchArlon);
    entity.depositToBank(new Money(100));

    entity.operate(deeds);

    assertThat(deeds.housesBuiltOn(street)).isEqualTo(1);
    assertThat(entity.bankBalance()).isEqualTo(Money.ZERO);
  }

  @Test
  void doesNotTakePartialShareholderContributionsWhenHouseIsUnaffordable() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    dog.account().deposit(new Money(30));
    highHat.account().deposit(new Money(30));
    ironBox.account().deposit(new Money(30));
    ColourStreet street = (ColourStreet) rules.create(Street.Type.RueDeDiekirchArlon);
    entity.receiveRent(street);

    entity.operate(deeds);

    assertThat(deeds.housesBuiltOn(street)).isZero();
    assertThat(entity.shareholderPayment(dog)).isEqualTo(Money.ZERO);
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(30));
  }

  private void ownEveryRemainingSpace(Player owner) {
    rules.streets().filter(it -> it instanceof the.monopoly.game.components.streets.Ownable)
        .map(it -> (the.monopoly.game.components.streets.Ownable) it)
        .filter(it -> deeds.isUnowned(it.type()))
        .forEach(it -> deeds.sell(it, owner, Money.ZERO));
  }

  private static boolean highestPriority(ColourStreet street) {
    return Strategy.priorityOf(street) == Strategy.Priority.HIGHEST;
  }

  private void own(Street.Type type, Player owner) {
    deeds.sell((the.monopoly.game.components.streets.Ownable) rules.create(type), owner, Money.ZERO);
  }

  private static Player player(String name) {
    Player.ID id = new Player.ID(name);
    Bank bank = new Bank.Simple();
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
