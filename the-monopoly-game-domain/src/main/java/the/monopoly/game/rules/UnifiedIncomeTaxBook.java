package the.monopoly.game.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

/** Combines a player's gross salary and rent collected since their last assessment. */
public final class UnifiedIncomeTaxBook {
  private static final BigDecimal RATE = BigDecimal.valueOf(43, 2);
  private static final BigDecimal NET_TO_GROSS = BigDecimal.valueOf(100, 2)
      .divide(BigDecimal.valueOf(57, 2), 12, RoundingMode.HALF_EVEN);

  private final GovernmentAccount government;
  private final Map<Player.ID, Money> collected = new HashMap<>();

  public UnifiedIncomeTaxBook(Bank bank) {
    this.government = new GovernmentAccount(bank);
  }

  public UnifiedIncomeTaxBook(GovernmentAccount government) {
    this.government = government;
  }

  public void accumulate(Player owner, Money rent) {
    collected.merge(owner.id(), rent, Money::plus);
  }

  public Money collected(Player player) {
    return collected.getOrDefault(player.id(), Money.ZERO);
  }

  public Money assess(Player player, Money netSalary) {
    BigDecimal grossSalaryCents = BigDecimal.valueOf(netSalary.cents()).multiply(NET_TO_GROSS);
    BigDecimal taxableCents = grossSalaryCents.add(BigDecimal.valueOf(collected(player).cents()));
    Money tax = Money.fromCents(taxableCents.multiply(RATE).setScale(0, RoundingMode.HALF_EVEN).longValueExact());
    government.deposit(tax);
    collected.put(player.id(), Money.ZERO);
    return tax;
  }

  public Money governmentBalance() {
    return government.balance();
  }
}
