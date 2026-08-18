package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentLoanBookPaymentTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Bank bank = rules.bank();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");

  @Test
  void servicingPaysTheBorrowerInterestToTheBankAndPassesYieldAndPrincipalToTheBondholder() {
    dog.account().deposit(new Money(100));
    highHat.account().deposit(new Money(500));
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    DevelopmentLoanBook.Position position = book.recordPlayerLoan(
        dog, Street.Type.RueGrandeDinant, new Money(20), 0, highHat);

    DevelopmentLoanBook.Payment payment = book.service(position).orElseThrow();

    assertThat(payment.interest()).isEqualTo(new Money(1));
    assertThat(payment.principal()).isEqualTo(new Money(1));
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(98));
    assertThat(highHat.account().balance().amount()).isEqualTo(Money.fromDollars("501.60"));
    assertThat(book.bankBalance()).isEqualTo(Money.fromDollars("0.40"));
    assertThat(position.outstanding()).isEqualTo(new Money(19));
  }

  @Test
  void anUnaffordablePaymentIsNotTakenAndLeavesTheLoanOutstanding() {
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    DevelopmentLoanBook.Position position = book.recordPlayerLoan(
        dog, Street.Type.RueGrandeDinant, new Money(20), 0, highHat);

    assertThat(book.service(position)).isEmpty();
    assertThat(position.outstanding()).isEqualTo(new Money(20));
    assertThat(dog.account().balance().amount()).isEqualTo(Money.ZERO);
    assertThat(highHat.account().balance().amount()).isEqualTo(Money.ZERO);
  }

  private Player player(String name) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
