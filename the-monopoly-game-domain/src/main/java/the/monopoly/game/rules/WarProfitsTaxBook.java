package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tracks what the war-profits tax needs between yearly assessments: the rent
 * each player has collected since their last assessment, and the government
 * account every tax payment lands in. The rate is the pure
 * {@link WarProfitsTax} computation; this book only remembers state.
 */
public final class WarProfitsTaxBook {
  private final GovernmentAccount government;
  private final Money boardValue;
  private final Map<Player.ID, Money> collected = new HashMap<>();

  public WarProfitsTaxBook(Bank bank, Money boardValue) {
    government = new GovernmentAccount(bank);
    this.boardValue = boardValue;
  }

  public Money governmentBalance() {
    return government.balance();
  }

  public void setGovernmentBalance(Money amount) {
    government.setBalance(amount);
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
    return assess(player, landValue, ignored -> {
    });
  }

  /**
   * Assesses tax after giving the caller an opportunity to raise any cash
   * shortfall, such as by mortgaging the player's spare property.
   */
  public Money assess(Player player, Money landValue, Consumer<Money> raiseShortfall) {
    Money owed = taxFor(player, landValue);
    if (owed.equals(Money.ZERO)) {
      collected.put(player.id(), Money.ZERO);
      return Money.ZERO;
    }
    Money balance = player.account().balance().amount();
    if (!balance.covers(owed)) raiseShortfall.accept(owed.minus(balance));
    player.account().withdraw(owed);
    government.deposit(owed);
    collected.put(player.id(), Money.ZERO);
    return owed;
  }
}

/* mutate4java-manifest
version=1
moduleHash=aa3214593a34f332b8b9c9a81ad9948615223483c99ec2cd8c1dbdb3c7220bca
scope.0.id=Y2xhc3M6V2FyUHJvZml0c1RheEJvb2sjV2FyUHJvZml0c1RheEJvb2s6MTc
scope.0.kind=class
scope.0.startLine=17
scope.0.endLine=80
scope.0.semanticHash=2907720a02dd68a0719d59e193811208d8f96fbd01be9531ad4af81b3b356ee2
scope.1.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjYm9hcmRWYWx1ZToxOQ
scope.1.kind=field
scope.1.startLine=19
scope.1.endLine=19
scope.1.semanticHash=dd4aaa410a472a57e7c7a5ccabf683ea2cd2be29e58ddce0fdf9dcb0639297eb
scope.2.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjY29sbGVjdGVkOjIw
scope.2.kind=field
scope.2.startLine=20
scope.2.endLine=20
scope.2.semanticHash=f2aa0339b2d247fb6180478b053e00a0e46375d7c82e52fbcbf7ae05876c185e
scope.3.id=ZmllbGQ6V2FyUHJvZml0c1RheEJvb2sjZ292ZXJubWVudDoxOA
scope.3.kind=field
scope.3.startLine=18
scope.3.endLine=18
scope.3.semanticHash=e0abeecdb8ad2c5a84be07f721ec67044477e0ae88b308c43f8307c4f9342009
scope.4.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2FjY3VtdWxhdGUoMik6NDA
scope.4.kind=method
scope.4.startLine=40
scope.4.endLine=42
scope.4.semanticHash=8470e52a93888c5301597f337c5b8c9bc522ab47fd7e5ab627e733d27e045e3b
scope.5.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2Fzc2VzcygyKTo1OA
scope.5.kind=method
scope.5.startLine=58
scope.5.endLine=61
scope.5.semanticHash=3c99902157b458c74f53cf0556adccb7c8f1e43b552dc65342eee34b691bb02b
scope.6.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2Fzc2VzcygzKTo2Nw
scope.6.kind=method
scope.6.startLine=67
scope.6.endLine=79
scope.6.semanticHash=d603838849f26bcc77468da86e56f7b1f1c33daa75e08060cd1410731b613cd7
scope.7.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2NvbGxlY3RlZCgxKTozNg
scope.7.kind=method
scope.7.startLine=36
scope.7.endLine=38
scope.7.semanticHash=9db0ba7409a45d11d28109db409b4a86b23c7be44338cc2b2df99cc7ee3cfd6f
scope.8.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2N0b3IoMik6MjI
scope.8.kind=method
scope.8.startLine=22
scope.8.endLine=25
scope.8.semanticHash=0d385de2194dfe791c6b394f61863edc4c2cc936643aa84ea5bc099fb33bef15
scope.9.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI2dvdmVybm1lbnRCYWxhbmNlKDApOjI3
scope.9.kind=method
scope.9.startLine=27
scope.9.endLine=29
scope.9.semanticHash=d8bc138d727a3e5b764b1b831b98422bc7f6205bdc40ce3d0f6cec1395d86e8f
scope.10.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI3NldEdvdmVybm1lbnRCYWxhbmNlKDEpOjMx
scope.10.kind=method
scope.10.startLine=31
scope.10.endLine=33
scope.10.semanticHash=6423c2771330eacd8a690849279bf136d4d3141c1a95b0a7e57454b4fa6d9983
scope.11.id=bWV0aG9kOldhclByb2ZpdHNUYXhCb29rI3RheEZvcigyKTo0OQ
scope.11.kind=method
scope.11.startLine=49
scope.11.endLine=51
scope.11.semanticHash=d2cca8a95cec89471e7d752b4311f93527027ee02ef52f56ae31e98c8a8a02ec
*/
