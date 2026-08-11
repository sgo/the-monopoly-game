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
  void anEntityWithoutShareholdersIsInactiveAndHasNoShareValue() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, List.of(), rules);

    assertThat(entity.hasShareholders()).isFalse();
    assertThat(entity.shareOf(dog)).isZero();
    assertThat(entity.shareValue()).isEqualTo(Money.ZERO);
    assertThat(entity.operate(deeds)).isEqualTo(new LegalEntity.Operation.NoAction());
  }

  @Test
  void aColourGroupLedByTheHighestPriorityStreetNeverConsolidates() {
    assertFormationIsImpossible("Orange Realty", Street.Colour.orange,
        List.of(Street.Type.LippenslaanKnokke, Street.Type.RueRoyaleTournai, Street.Type.GroenplaatsAntwerpen),
        List.of(dog, highHat, ironBox), List.of(dog, highHat, ironBox));
  }

  @Test
  void aGroupNotSplitAcrossThreeOwnersPreventsFormation() {
    assertFormationIsImpossible("Pink Realty", Street.Colour.pink,
        List.of(Street.Type.RueDeDiekirchArlon, Street.Type.BruulMechelen, Street.Type.PlaceVerteVerviers),
        List.of(dog, dog, highHat), List.of(dog, highHat, ironBox));
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
  void operatingBuildsOnTheStreetWithFewestHousesFirst() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    ColourStreet ahead = entity.streets().get(0);
    ColourStreet behind = entity.streets().get(1);
    deeds.arrangeHouses(ahead, 1);
    entity.depositToBank(behind.houseConstructionCost());

    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.HouseBuilt(behind));
    assertThat(deeds.housesBuiltOn(behind)).isEqualTo(1);
    assertThat(deeds.housesBuiltOn(ahead)).isEqualTo(1);
  }

  @Test
  void operatingRepaysAnOutstandingLoanWithFivePercentInterestWhenNothingIsBuildable() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    entity.streets().forEach(street -> deeds.arrangeHouses(street, 4));
    entity.raiseLoan(new Money(100));
    entity.depositToBank(new Money(5));

    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.LoanRepaid(dog, new Money(100), new Money(105)));
    assertThat(entity.loan()).isEqualTo(Money.ZERO);
    assertThat(entity.bankBalance()).isEqualTo(Money.ZERO);
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(105));
  }

  @Test
  void operatingPaysADividendWhenNothingIsBuildableAndNoLoanIsOutstanding() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    entity.streets().forEach(street -> deeds.arrangeHouses(street, 4));
    entity.depositToBank(new Money(150));
    entity.recordCapitalization(dog);
    entity.shareholderGrewOlder(dog);

    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.DividendPaid(new Money(50)));
    assertThat(entity.bankBalance()).isEqualTo(Money.ZERO);
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(50));
    assertThat(highHat.account().balance().amount()).isEqualTo(new Money(50));
    assertThat(ironBox.account().balance().amount()).isEqualTo(new Money(50));
  }

  @Test
  void operatingDoesNotPayADividendBeforeTheLastCapitalizedShareholderGrowsOlder() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    entity.streets().forEach(street -> deeds.arrangeHouses(street, 4));
    entity.depositToBank(new Money(150));
    entity.recordCapitalization(dog);

    assertThat(entity.operate(deeds)).isEqualTo(new LegalEntity.Operation.NoAction());
    assertThat(entity.bankBalance()).isEqualTo(new Money(150));
  }

  @Test
  void onlyTheLastCapitalizedShareholdersAgeIncreaseEnablesADividend() {
    LegalEntity entity = dividendEligibleEntity(new Money(150));
    entity.shareholderGrewOlder(highHat);

    assertThat(entity.operate(deeds)).isEqualTo(new LegalEntity.Operation.NoAction());

    LegalEntity eligible = dividendEligibleEntity(new Money(150));
    eligible.shareholderGrewOlder(dog);

    assertThat(eligible.operate(deeds)).isEqualTo(new LegalEntity.Operation.DividendPaid(new Money(100)));
  }

  @Test
  void aDividendCannotBePaidAgainUntilTheQualifyingShareholderGrowsOlderAgain() {
    LegalEntity entity = dividendEligibleEntity(new Money(300));
    entity.shareholderGrewOlder(dog);

    assertThat(entity.operate(deeds)).isEqualTo(new LegalEntity.Operation.DividendPaid(new Money(100)));
    entity.markOperated();
    assertThat(entity.operate(deeds)).isEqualTo(new LegalEntity.Operation.NoAction());
    assertThat(entity.bankBalance()).isEqualTo(Money.ZERO);
  }

  @Test
  void aDividendKeepsAnIntegerRemainderInTheEntityBank() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    entity.streets().forEach(street -> deeds.arrangeHouses(street, 4));
    entity.depositToBank(new Money(170));
    entity.recordCapitalization(dog);
    entity.shareholderGrewOlder(dog);

    assertThat(entity.operate(deeds)).isEqualTo(new LegalEntity.Operation.DividendPaid(new Money(56)));
    assertThat(entity.bankBalance()).isEqualTo(new Money(2));
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(56));
    assertThat(highHat.account().balance().amount()).isEqualTo(new Money(56));
    assertThat(ironBox.account().balance().amount()).isEqualTo(new Money(56));
  }

  @Test
  void borrowsAnExactRemainderAwareShortfallWhenEveryShareholderCanAffordTheirShare() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    ColourStreet street = entity.streets().getFirst();
    entity.depositToBank(new Money(3));
    dog.account().deposit(new Money(50));
    highHat.account().deposit(new Money(50));
    ironBox.account().deposit(new Money(50));
    entity.commitToBuild(dog, new Money(50));
    entity.commitToBuild(highHat, new Money(50));
    entity.commitToBuild(ironBox, new Money(50));

    LegalEntity.Operation operation = entity.operate(deeds);

    assertThat(operation).isEqualTo(new LegalEntity.Operation.LoanRaisedAndHouseBuilt(new Money(97), street));
    assertThat(deeds.housesBuiltOn(street)).isEqualTo(1);
    assertThat(entity.loan()).isEqualTo(new Money(97));
    assertThat(entity.shareholderPayment(dog)).isEqualTo(new Money(33));
    assertThat(entity.shareholderPayment(highHat)).isEqualTo(new Money(32));
    assertThat(entity.shareholderPayment(ironBox)).isEqualTo(new Money(32));
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(17));
    assertThat(highHat.account().balance().amount()).isEqualTo(new Money(18));
    assertThat(ironBox.account().balance().amount()).isEqualTo(new Money(18));
  }

  @Test
  void zeroRemainderSharesDoNotBecomeTheLastCapitalizedShareholder() {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    ColourStreet street = entity.streets().getFirst();
    entity.depositToBank(new Money(99));
    dog.account().deposit(new Money(10));
    highHat.account().deposit(new Money(10));
    ironBox.account().deposit(new Money(10));
    entity.commitToBuild(dog, new Money(10));
    entity.commitToBuild(highHat, new Money(10));
    entity.commitToBuild(ironBox, new Money(10));

    assertThat(entity.operate(deeds))
        .isEqualTo(new LegalEntity.Operation.LoanRaisedAndHouseBuilt(new Money(1), street));
    assertThat(entity.lastCapitalizedShareholder()).isEqualTo(dog);
    assertThat(entity.shareholderPayment(dog)).isEqualTo(new Money(1));
    assertThat(entity.shareholderPayment(highHat)).isEqualTo(Money.ZERO);
    assertThat(entity.shareholderPayment(ironBox)).isEqualTo(Money.ZERO);
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

  private void ownColourGroupAndRemainingSpaces(List<Street.Type> types, List<Player> owners, Player remainingOwner) {
    for (int index = 0; index < types.size(); index++) own(types.get(index), owners.get(index));
    ownEveryRemainingSpace(remainingOwner);
  }

  private void assertFormationIsImpossible(String name, Street.Colour colour, List<Street.Type> types,
                                           List<Player> owners, List<Player> shareholders) {
    ownColourGroupAndRemainingSpaces(types, owners, highHat);
    assertThat(LegalEntity.form(name, colour, shareholders, rules, deeds, LegalEntityTest::highestPriority)).isEmpty();
  }

  private static boolean highestPriority(ColourStreet street) {
    return Strategy.priorityOf(street) == Strategy.Priority.HIGHEST;
  }

  private void own(Street.Type type, Player owner) {
    deeds.sell((the.monopoly.game.components.streets.Ownable) rules.create(type), owner, Money.ZERO);
  }

  private LegalEntity dividendEligibleEntity(Money bankBalance) {
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink,
        List.of(dog, highHat, ironBox), rules);
    entity.streets().forEach(street -> deeds.arrangeHouses(street, 4));
    entity.depositToBank(bankBalance);
    entity.recordCapitalization(dog);
    return entity;
  }

  private static Player player(String name) {
    Player.ID id = new Player.ID(name);
    Bank bank = new Bank.Simple();
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
