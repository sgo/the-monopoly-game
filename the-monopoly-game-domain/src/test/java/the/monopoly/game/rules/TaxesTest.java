package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaxesTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Player dog = playerWith("dog", 1500);
  private final Reported reported = new Reported();
  private final Taxes taxes = new Taxes(reported);

  @Test
  void incomeTaxIsPaidToTheBank() {
    taxes.resolve(dog, rules.create(Street.Type.InkomstenBelasting), new Roll(1, 1));

    assertThat(dog.account().balance()).isEqualTo(Balance.of(1300));
    assertThat(reported.payments).containsExactly(new Money(200));
  }

  @Test
  void luxuryTaxIsPaidToTheBank() {
    taxes.resolve(dog, rules.create(Street.Type.ExtraBelasting), new Roll(1, 1));

    assertThat(dog.account().balance()).isEqualTo(Balance.of(1400));
    assertThat(reported.payments).containsExactly(new Money(100));
  }

  @Test
  void aNonTaxSpaceDoesNotChargeTax() {
    taxes.resolve(dog, rules.create(Street.Type.start), new Roll(1, 1));

    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(reported.payments).isEmpty();
  }

  private Player playerWith(String name, int balance) {
    Bank bank = rules.bank();
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }

  private static final class Reported implements Taxes.Events {
    private final List<Money> payments = new ArrayList<>();

    @Override
    public void paidBank(Player player, Money amount) {
      payments.add(amount);
    }
  }
}
