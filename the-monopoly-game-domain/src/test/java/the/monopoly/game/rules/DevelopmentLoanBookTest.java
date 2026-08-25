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

class DevelopmentLoanBookTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Bank bank = rules.bank();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");
  private final ColourStreet street = (ColourStreet) rules.create(the.monopoly.game.components.streets.Street.Type.RueGrandeDinant);

  @Test
  void shortfallIsCappedAtEightyPercentAndNeedsAPlayerToFundIt() {
    dog.account().deposit(new Money(30));
    highHat.account().deposit(new Money(500));
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);

    DevelopmentLoanBook.Position position = book.raise(dog, street, false, List.of(dog, highHat)).orElseThrow();

    assertThat(position.outstanding()).isEqualTo(new Money(20));
    assertThat(position.bondholder()).isEqualTo(highHat);
    assertThat(highHat.account().balance().amount()).isEqualTo(new Money(480));
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(50));
  }

  @Test
  void noBondholderMeansNoLoanAndNoAccountChanges() {
    dog.account().deposit(new Money(30));
    highHat.account().deposit(new Money(5));
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);

    assertThat(book.raise(dog, street, false, List.of(dog, highHat))).isEmpty();
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(30));
    assertThat(highHat.account().balance().amount()).isEqualTo(new Money(5));
  }

  @Test
  void recycledCapitalFundsTheLoanBeforeAFreshBondAndLeavesTheRemainderAvailable() {
    dog.account().deposit(new Money(30));
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    book.setRecycledCapital(new Money(50));

    DevelopmentLoanBook.Position position = book.raise(dog, street, false, List.of(dog)).orElseThrow();

    assertThat(position.outstanding()).isEqualTo(new Money(20));
    assertThat(position.bondholder()).isNull();
    assertThat(book.recycledCapital()).isEqualTo(new Money(30));
    assertThat(book.bankBalance()).isEqualTo(Money.ZERO);
  }

  @Test
  void anActiveLoanCannotFinanceAnotherDevelopmentOnTheSameCollateral() {
    dog.account().deposit(new Money(30));
    highHat.account().deposit(new Money(500));
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    book.recordPlayerLoan(dog, street.type(), new Money(20), 0, highHat);

    assertThat(book.canRaise(dog, street, false, List.of(dog, highHat))).isFalse();
  }

  @Test
  void anEntityCanRepayItsOutstandingLoanInFullBeforeDissolution() {
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, List.of(dog), rules);
    entity.depositToBank(new Money(100));
    DevelopmentLoanBook.Position position = book.recordEntityLoan(
        entity, street.type(), new Money(40), 0, null);

    assertThat(book.repayEntityLoan(position)).isTrue();
    assertThat(position.outstanding()).isEqualTo(Money.ZERO);
    assertThat(entity.loan()).isEqualTo(Money.ZERO);
    assertThat(entity.bankBalance()).isEqualTo(new Money(60));
  }

  @Test
  void forecloseEntitySellsTheCollateralToTheBankAndClearsTheEntitysLoan() {
    Deeds deeds = new Deeds();
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, List.of(dog), rules);
    deeds.form(entity);
    ColourStreet collateral = entity.streets().getFirst();
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    DevelopmentLoanBook.Position position = book.recordEntityLoan(entity, collateral.type(), new Money(100), 0, null);

    DevelopmentLoanBook.Foreclosure foreclosure = book.forecloseEntity(
        position, deeds, rules, List.of(dog, highHat), Strategy.OfPlayers.NOBODY_DECIDES);

    assertThat(position.outstanding()).isEqualTo(Money.ZERO);
    assertThat(entity.loan()).isEqualTo(Money.ZERO);
    assertThat(deeds.entityOwnerOf(collateral.type())).isEmpty();
    assertThat(foreclosure.recovered()).isEqualTo(Money.ZERO);
  }

  @Test
  void forecloseEntityReturnsCollateralToTheBankWhenNoBidReachesTheOpeningPrice() {
    Deeds deeds = new Deeds();
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, List.of(dog), rules);
    deeds.form(entity);
    ColourStreet collateral = entity.streets().getFirst();
    int belowOpening = collateral.landMortgageValue().amount() / 3;
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    DevelopmentLoanBook.Position position = book.recordEntityLoan(entity, collateral.type(), new Money(100), 0, null);
    Strategy.OfPlayers strategies = player -> player.id().equals(highHat.id())
        ? new Strategy() {
          @Override
          public Money bidFor(Offer offer) {
            return new Money(belowOpening);
          }
        }
        : new Strategy() {
          @Override
          public Money bidFor(Offer offer) {
            return new Money(belowOpening + 1);
          }
        };

    book.forecloseEntity(position, deeds, rules, List.of(dog, highHat), strategies);

    assertThat(deeds.entityOwnerOf(collateral.type())).isEmpty();
    assertThat(deeds.ownerOf(collateral.type())).isEmpty();
    assertThat(position.outstanding()).isEqualTo(Money.ZERO);
  }

  @Test
  void aShortfallAboveTheLoanToValueCapCannotBeRaised() {
    dog.account().deposit(new Money(5));
    highHat.account().deposit(new Money(500));
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);

    assertThat(book.raise(dog, street, false, List.of(dog, highHat))).isEmpty();
  }

  private Player player(String name) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
