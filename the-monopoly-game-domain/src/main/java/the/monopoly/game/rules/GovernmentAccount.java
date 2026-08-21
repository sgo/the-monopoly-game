package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;

/** The government account shared by the optional tax and relief rules. */
public final class GovernmentAccount {
  private static final Bank.Account.Owner OWNER = new Bank.Account.Owner("government");

  private final Bank.Account account;

  public GovernmentAccount(Bank bank) {
    bank.createAccountFor(OWNER);
    account = bank.accountOf(OWNER);
  }

  public Money balance() {
    return account.balance().amount();
  }

  public void deposit(Money amount) {
    account.deposit(amount);
  }

  public void withdraw(Money amount) {
    account.withdraw(amount);
  }

  public void setBalance(Money amount) {
    Money current = balance();
    if (amount.exceeds(current)) deposit(amount.minus(current));
    else if (current.exceeds(amount)) withdraw(current.minus(amount));
  }
}
