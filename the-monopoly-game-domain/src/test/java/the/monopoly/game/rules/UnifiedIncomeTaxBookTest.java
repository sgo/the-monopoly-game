package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedIncomeTaxBookTest {
  @Test
  void combinesGrossSalaryAndAccumulatedRentAtFortyThreePercent() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog");
    UnifiedIncomeTaxBook tax = new UnifiedIncomeTaxBook(bank);

    tax.accumulate(dog, Money.fromDollars("649.12"));
    assertThat(tax.assess(dog, Money.fromDollars("200"))).isEqualTo(Money.fromDollars("430.00"));
    assertThat(tax.governmentBalance()).isEqualTo(Money.fromDollars("430.00"));
  }

  @Test
  void resetsRentAfterEverySalaryAssessment() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog");
    UnifiedIncomeTaxBook tax = new UnifiedIncomeTaxBook(bank);

    tax.accumulate(dog, Money.fromDollars("649.12"));
    tax.assess(dog, Money.fromDollars("200"));
    tax.assess(dog, Money.fromDollars("200"));

    assertThat(tax.governmentBalance()).isEqualTo(Money.fromDollars("580.88"));
    assertThat(tax.collected(dog)).isEqualTo(Money.ZERO);
  }

  private Player player(Bank bank, String name) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
