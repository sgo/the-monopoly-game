package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/** Pays a player's salary and MegaCorp's 43% income tax into government. */
public final class MegacorpSalaryTax {
  private static final int RATE = 43;

  private final GovernmentAccount government;

  public MegacorpSalaryTax(Bank bank) {
    government = new GovernmentAccount(bank);
  }

  public Money collect(Player player, Money salary) {
    player.account().deposit(salary);
    Money tax = salary.percentage(RATE);
    government.deposit(tax);
    return tax;
  }

  public Money governmentBalance() {
    return government.balance();
  }
}
