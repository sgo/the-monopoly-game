package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks what the war-profits tax needs between yearly assessments: the rent
 * each player has collected since their last assessment, and the government
 * account every tax payment lands in. The rate is the pure
 * {@link WarProfitsTax} computation; this book only remembers state.
 */
public final class WarProfitsTaxBook {
  private static final Bank.Account.Owner GOVERNMENT = new Bank.Account.Owner("government");

  private final Bank bank;
  private final Money boardValue;
  private final Map<Player.ID, Money> collected = new HashMap<>();

  public WarProfitsTaxBook(Bank bank, Money boardValue) {
    this.bank = bank;
    this.boardValue = boardValue;
    bank.createAccountFor(GOVERNMENT);
  }

  public Money governmentBalance() {
    return bank.accountOf(GOVERNMENT).balance().amount();
  }

  public void setGovernmentBalance(Money amount) {
    Money current = governmentBalance();
    if (amount.exceeds(current)) bank.accountOf(GOVERNMENT).deposit(amount.minus(current));
    else if (current.exceeds(amount)) bank.accountOf(GOVERNMENT).withdraw(current.minus(amount));
  }

  /** The rent a player has collected since their last war-profits-tax assessment. */
  public Money collected(Player player) {
    return collected.getOrDefault(player.id(), Money.ZERO);
  }

  public void accumulate(Player owner, Money rent) {
    collected.merge(owner.id(), rent, Money::plus);
  }

  /**
   * The tax owed by {@code player} on the rent they collected, at their current
   * {@code landValue} ownership share. Below 25% ownership this is zero. A rate
   * above 100% means the player owes more than the rent was worth.
   */
  public Money taxFor(Player player, Money landValue) {
    return WarProfitsTax.tax(boardValue, landValue, collected(player));
  }

  /**
   * Collects the tax {@code player} owes, pays it into the government account,
   * and resets that player's rent counter for the next year. Returns the amount
   * paid (zero when nothing is owed).
   */
  public Money assess(Player player, Money landValue) {
    Money owed = taxFor(player, landValue);
    if (owed.equals(Money.ZERO)) {
      collected.put(player.id(), Money.ZERO);
      return Money.ZERO;
    }
    player.account().withdraw(owed);
    bank.accountOf(GOVERNMENT).deposit(owed);
    collected.put(player.id(), Money.ZERO);
    return owed;
  }
}