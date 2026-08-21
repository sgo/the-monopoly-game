package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import static org.assertj.core.api.Assertions.assertThat;

class RentReliefTest {
  @Test
  void rentAtTheCapIsPaidInFullByTheTenant() {
    Bank.Simple bank = new Bank.Simple();
    Player tenant = player(bank, "high hat", 1500);
    Player landlord = player(bank, "dog", 1500);
    RentRelief relief = new RentRelief(bank);

    relief.pay(tenant, landlord, new Money(200));

    assertThat(tenant.account().balance().amount()).isEqualTo(new Money(1300));
    assertThat(landlord.account().balance().amount()).isEqualTo(new Money(1700));
    assertThat(relief.governmentBalance()).isEqualTo(Money.ZERO);
  }

  @Test
  void rentAboveTheCapUsesGovernmentFundsWhenTheyCoverTheDifference() {
    Bank.Simple bank = new Bank.Simple();
    Player tenant = player(bank, "high hat", 1500);
    Player landlord = player(bank, "dog", 1500);
    RentRelief relief = new RentRelief(bank);
    relief.setGovernmentBalance(new Money(550));

    relief.pay(tenant, landlord, new Money(750));

    assertThat(tenant.account().balance().amount()).isEqualTo(new Money(1300));
    assertThat(landlord.account().balance().amount()).isEqualTo(new Money(2250));
    assertThat(relief.governmentBalance()).isEqualTo(Money.ZERO);
  }

  @Test
  void rentReliefIsNotGivenWhenTheGovernmentIsOneDollarShort() {
    Bank.Simple bank = new Bank.Simple();
    Player tenant = player(bank, "high hat", 1500);
    Player landlord = player(bank, "dog", 1500);
    RentRelief relief = new RentRelief(bank);
    relief.setGovernmentBalance(new Money(549));

    relief.pay(tenant, landlord, new Money(750));

    assertThat(tenant.account().balance().amount()).isEqualTo(new Money(750));
    assertThat(landlord.account().balance().amount()).isEqualTo(new Money(2250));
    assertThat(relief.governmentBalance()).isEqualTo(new Money(549));
  }

  @Test
  void rentBelowTheCapDoesNotSpendTheGovernmentAccount() {
    Bank.Simple bank = new Bank.Simple();
    Player tenant = player(bank, "high hat", 1500);
    Player landlord = player(bank, "dog", 1500);
    RentRelief relief = new RentRelief(bank);
    relief.setGovernmentBalance(new Money(5000));

    relief.pay(tenant, landlord, new Money(200));

    assertThat(tenant.account().balance().amount()).isEqualTo(new Money(1300));
    assertThat(landlord.account().balance().amount()).isEqualTo(new Money(1700));
    assertThat(relief.governmentBalance()).isEqualTo(new Money(5000));
  }

  private Player player(Bank bank, String name, int balance) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }
}
