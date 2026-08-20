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

/* mutate4java-manifest
version=1
moduleHash=719104188d119354a019bfb37b82a617623cc9004fabb797a48053a47fdcb47b
scope.0.id=Y2xhc3M6V2FyUHJvZml0c1RheEJvb2sjV2FyUHJvZml0c1RheEJvb2s6MTY
scope.0.kind=class
scope.0.startLine=16
scope.0.endLine=73
scope.0.semanticHash=ebb967bc98bf789c1657604f7f648a652894ab696e48f3308a1e917b962a3280
scope.1.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjR09WRVJOTUVOVDoxNw
scope.1.kind=field
scope.1.startLine=17
scope.1.endLine=17
scope.1.semanticHash=6497cffb000e8322a977f6f9ea65cb330df38a79c134e8b9b7254df6c5cce3cc
scope.2.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjYmFuazoxOQ
scope.2.kind=field
scope.2.startLine=19
scope.2.endLine=19
scope.2.semanticHash=2602ad708f2cc00537a58fdb7754137949649c0cfc7956e052576d8b6dd0faec
scope.3.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjYm9hcmRWYWx1ZToyMA
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=20
scope.3.semanticHash=dd4aaa410a472a57e7c7a5ccabf683ea2cd2be29e58ddce0fdf9dcb0639297eb
scope.4.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjY29sbGVjdGVkOjIx
scope.4.kind=field
scope.4.startLine=21
scope.4.endLine=21
scope.4.semanticHash=f2aa0339b2d247fb6180478b053e00a0e46375d7c82e52fbcbf7ae05876c185e
scope.5.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2FjY3VtdWxhdGUoMik6NDQ
scope.5.kind=method
scope.5.startLine=44
scope.5.endLine=46
scope.5.semanticHash=8470e52a93888c5301597f337c5b8c9bc522ab47fd7e5ab627e733d27e045e3b
scope.6.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2Fzc2VzcygyKTo2Mg
scope.6.kind=method
scope.6.startLine=62
scope.6.endLine=72
scope.6.semanticHash=c54a2cad59c87cf18e8756df5a5c36506e211b7dacbe5bf6bc32cd789012b4e3
scope.7.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2NvbGxlY3RlZCgxKTo0MA
scope.7.kind=method
scope.7.startLine=40
scope.7.endLine=42
scope.7.semanticHash=9db0ba7409a45d11d28109db409b4a86b23c7be44338cc2b2df99cc7ee3cfd6f
scope.8.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2N0b3IoMik6MjM
scope.8.kind=method
scope.8.startLine=23
scope.8.endLine=27
scope.8.semanticHash=36907ec17d30a0ed02264e3908188d938c24f2a8b95c2178ef2814a1ec415e3c
scope.9.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2dvdmVybm1lbnRCYWxhbmNlKDApOjI5
scope.9.kind=method
scope.9.startLine=29
scope.9.endLine=31
scope.9.semanticHash=2f9ff4add028e9858bcdf837ecf447b9ccca693ccd2eda6611a2e941b524efeb
scope.10.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI3NldEdvdmVybm1lbnRCYWxhbmNlKDEpOjMz
scope.10.kind=method
scope.10.startLine=33
scope.10.endLine=37
scope.10.semanticHash=311663940efb1a255a78c77abce6886082582e718f69346154997f8af672aabf
scope.11.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI3RheEZvcigyKTo1Mw
scope.11.kind=method
scope.11.startLine=53
scope.11.endLine=55
scope.11.semanticHash=d2cca8a95cec89471e7d752b4311f93527027ee02ef52f56ae31e98c8a8a02ec
*/
