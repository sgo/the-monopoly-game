package the.monopoly.game.components.finance;

import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.players.Player;

import java.util.Set;
import java.util.stream.Stream;

public interface Bank {
  Stream<Account> accounts();

  void createAccountFor(Player player);

  Account accountOf(Player player);

  record Account(Owner owner, Balance balance) {
    public void deposit(Money amount) {
      balance.incrementWith(amount);
    }

    public record Owner(String name) {
    }

    public static class Balance {
      private Money amount;

      public Balance(Money amount) {
        this.amount = amount;
      }

      public static Balance of(int amount) {
        return new Balance(new Money(amount));
      }

      public void incrementWith(Money amount) {
        this.amount = this.amount.plus(amount);
      }

      @Override
      public final boolean equals(Object o) {
        if (!(o instanceof Balance balance)) return false;

        return amount.equals(balance.amount);
      }

      @Override
      public int hashCode() {
        return amount.hashCode();
      }

      @Override
      public String toString() {
        return "Balance[" +
            "amount=" + amount +
            ']';
      }
    }
  }

  record Simple(Set<Account> accountsBuffer) implements Bank {
    @Override
    public Stream<Account> accounts() {
      return accountsBuffer.stream();
    }

    @Override
    public void createAccountFor(Player player) {
      accountsBuffer.add(new Account(
          new Account.Owner(player.id().value()),
          Balance.of(0)
      ));
    }

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Account accountOf(Player player) {
      return accounts()
          .filter(it -> it.owner.name().equals(player.id().value()))
          .findAny()
          .get();
    }
  }
}
