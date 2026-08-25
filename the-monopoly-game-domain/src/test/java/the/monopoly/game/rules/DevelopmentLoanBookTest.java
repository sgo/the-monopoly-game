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
  void anEntityLoanFollowsItsCollateralToTheFinalShareholderWhenTheEntityDissolves() {
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    LegalEntity entity = LegalEntity.formed("Pink Realty", Street.Colour.pink, List.of(dog), rules);
    book.recordEntityLoan(entity, street.type(), new Money(20), 0, highHat);

    book.transferEntityLoans(entity, dog);

    DevelopmentLoanBook.Position transferred = book.positions().getFirst();
    assertThat(transferred.isEntityLoan()).isFalse();
    assertThat(transferred.borrower()).isEqualTo(dog);
    assertThat(transferred.entity()).isNull();
    assertThat(transferred.outstanding()).isEqualTo(new Money(20));
    assertThat(entity.loan()).isEqualTo(Money.ZERO);
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
