package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/** Caps a tenant's rent at the salary amount when government can fund the rest. */
public final class RentRelief {
  private static final Money RENT_CAP = new Money(200);

  private final GovernmentAccount government;

  public RentRelief(Bank bank) {
    government = new GovernmentAccount(bank);
  }

  public void pay(Player tenant, Player landlord, Money rent) {
    Money relief = reliefFor(rent);
    tenant.account().withdraw(rent.minus(relief));
    landlord.account().deposit(rent);
    if (!relief.equals(Money.ZERO)) government.withdraw(relief);
  }

  public Money governmentBalance() {
    return government.balance();
  }

  public void setGovernmentBalance(Money amount) {
    government.setBalance(amount);
  }

  private Money reliefFor(Money rent) {
    if (!rent.exceeds(RENT_CAP)) return Money.ZERO;
    Money difference = rent.minus(RENT_CAP);
    return government.balance().covers(difference) ? difference : Money.ZERO;
  }
}
