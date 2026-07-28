package the.monopoly.game.components.finance;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.players.Player;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankTest {
  private final Bank bank = new Bank.Simple(new HashSet<>());
  private final Player.ID player = new Player.ID("0");

  @Test
  void aNewBankHoldsNoAccounts() {
    assertThat(bank.accounts()).isEmpty();
  }

  @Test
  void anAccountOpensEmptyAndIsFoundByItsOwner() {
    bank.createAccountFor(player);

    assertThat(bank.accounts()).hasSize(1);
    assertThat(bank.accountOf(player).owner()).isEqualTo(new Bank.Account.Owner("0"));
    assertThat(bank.accountOf(player).balance()).isEqualTo(Balance.of(0));
  }

  @Test
  void accountsAreKeptApartByOwner() {
    Player.ID other = new Player.ID("1");
    bank.createAccountFor(player);
    bank.createAccountFor(other);

    bank.accountOf(player).deposit(new Money(1500));

    assertThat(bank.accountOf(player).balance()).isEqualTo(Balance.of(1500));
    assertThat(bank.accountOf(other).balance()).isEqualTo(Balance.of(0));
  }

  @Test
  void askingForAnAccountNobodyOpenedSaysSo() {
    assertThatThrownBy(() -> bank.accountOf(player))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("0");
  }

  @Test
  void aDepositRaisesTheBalanceAndACreditLowersIt() {
    bank.createAccountFor(player);
    Bank.Account account = bank.accountOf(player);

    account.deposit(new Money(1500));
    account.withdraw(new Money(200));

    assertThat(account.balance()).isEqualTo(Balance.of(1300));
  }

  @Test
  void anAccountCanBeOverdrawn() {
    bank.createAccountFor(player);
    Bank.Account account = bank.accountOf(player);

    account.withdraw(new Money(50));

    assertThat(account.balance()).isEqualTo(Balance.of(-50));
  }

  @Test
  void balancesOfTheSameAmountAreTheSameBalance() {
    assertThat(Balance.of(200)).isEqualTo(new Balance(new Money(200)));
    assertThat(Balance.of(200)).hasSameHashCodeAs(new Balance(new Money(200)));
    assertThat(Balance.of(200)).isNotEqualTo(Balance.of(201));
    assertThat(Balance.of(200)).isNotEqualTo("200");
  }

  @Test
  void aBalanceDescribesItself() {
    assertThat(Balance.of(200)).hasToString("Balance[amount=Money[amount=200]]");
  }
}
