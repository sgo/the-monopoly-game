package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import static org.assertj.core.api.Assertions.assertThat;

class MegacorpSalaryTaxTest {
  @Test
  void salaryIsPaidInFullToThePlayerAndFortyThreePercentGoesToTheGovernment() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog", 1500);
    MegacorpSalaryTax tax = new MegacorpSalaryTax(bank);

    tax.collect(dog, new Money(200));

    assertThat(dog.account().balance().amount()).isEqualTo(new Money(1700));
    assertThat(tax.governmentBalance()).isEqualTo(new Money(86));
  }

  @Test
  void taxScalesWithA400Salary() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog", 1500);
    MegacorpSalaryTax tax = new MegacorpSalaryTax(bank);

    tax.collect(dog, new Money(400));

    assertThat(dog.account().balance().amount()).isEqualTo(new Money(1900));
    assertThat(tax.governmentBalance()).isEqualTo(new Money(172));
  }

  @Test
  void paymentsForMultiplePlayersAccumulateInTheGovernmentAccount() {
    Bank.Simple bank = new Bank.Simple();
    Player dog = player(bank, "dog", 1500);
    Player highHat = player(bank, "high hat", 1500);
    MegacorpSalaryTax tax = new MegacorpSalaryTax(bank);

    tax.collect(dog, new Money(200));
    tax.collect(highHat, new Money(200));

    assertThat(tax.governmentBalance()).isEqualTo(new Money(172));
  }

  private Player player(Bank bank, String name, int balance) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }
}
