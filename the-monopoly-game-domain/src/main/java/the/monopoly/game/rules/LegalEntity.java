package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.finance.Bank.Account;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

/** A company that consolidates a three-player split of an eligible colour group. */
public final class LegalEntity {
  private final String name;
  private final Street.Colour colour;
  private final List<Player> shareholders;
  private final List<ColourStreet> streets;
  private Money loan = Money.ZERO;
  private final Account bankAccount;
  private ColourStreet rentReceivedOn;
  private final Map<Player.ID, Money> shareholderPayments = new HashMap<>();
  private final Map<Player.ID, Money> buildCommitments = new HashMap<>();
  private Player lastCapitalizedShareholder;
  private boolean lastCapitalizedShareholderGrewOlder;
  private boolean operated;

  private LegalEntity(String name, Street.Colour colour, List<Player> shareholders,
                      List<ColourStreet> streets, Bank bank) {
    this.name = name;
    this.colour = colour;
    this.shareholders = new ArrayList<>(shareholders);
    this.streets = List.copyOf(streets);
    Account.Owner owner = new Account.Owner(name);
    bank.createAccountFor(owner);
    this.bankAccount = bank.accountOf(owner);
  }

  public static Optional<LegalEntity> form(String name, Street.Colour colour,
                                           List<Player> shareholders, Rule.Set rules, Deeds deeds,
                                           Predicate<ColourStreet> highestPriority) {
    return LegalEntityFormation.eligibleStreets(shareholders, colour, rules, deeds, highestPriority)
        .map(streets -> new LegalEntity(name, colour, shareholders, streets, rules.bank()));
  }

  /** Creates an entity from already-set-up scenario state. */
  public static LegalEntity formed(String name, Street.Colour colour, List<Player> shareholders, Rule.Set rules) {
    return new LegalEntity(name, colour, shareholders, streetsOf(colour, rules), rules.bank());
  }

  public static List<ColourStreet> streetsOf(Street.Colour colour, Rule.Set rules) {
    return rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == colour).toList();
  }

  public String name() { return name; }
  public Street.Colour colour() { return colour; }
  public List<Player> shareholders() { return List.copyOf(shareholders); }
  public boolean hasShareholders() { return !shareholders.isEmpty(); }
  public List<ColourStreet> streets() { return streets; }
  public double shareOf(Player shareholder) {
    if (!hasShareholders()) return 0.0;
    return shareholders.stream().filter(shareholder::equals).count() / (double) shareholders.size();
  }

  /** The value of one share, based on the maximum developed rent of every entity street. */
  public Money shareValue() {
    if (!hasShareholders()) return Money.ZERO;
    int total = streets.stream().mapToInt(street -> street.rentForOneHotel().amount()).sum();
    return new Money(total / shareholders.size());
  }

  /** Transfers exactly one share from a distressed shareholder to a fellow shareholder. */
  public void sellShare(Player seller, Player buyer, Money price) {
    int share = shareholders.indexOf(seller);
    if (share < 0 || !shareholders.contains(buyer)) throw new IllegalArgumentException("Share sale requires two shareholders.");
    seller.account().deposit(price);
    buyer.account().withdraw(price);
    shareholders.set(share, buyer);
  }

  public void removeShares(Player shareholder) {
    shareholders.removeIf(shareholder::equals);
  }

  /** Dissolves this entity and transfers its remaining treasury to its final shareholder. */
  public Money liquidateTo(Player shareholder) {
    if (shareholders.size() != 1 || !shareholders.contains(shareholder))
      throw new IllegalArgumentException("Liquidation requires the final shareholder.");
    Money balance = bankAccount.balance().amount();
    if (balance.amount() > 0) {
      bankAccount.withdraw(balance);
      shareholder.account().deposit(balance);
    }
    shareholders.clear();
    return balance;
  }

  public Money loan() { return loan; }
  public Money bankBalance() { return bankAccount.balance().amount(); }
  public void depositToBank(Money amount) { bankAccount.deposit(amount); }
  public void withdrawFromBank(Money amount) { bankAccount.withdraw(amount); }
  public boolean operated() { return operated; }
  public void markOperated() { operated = true; }
  public void raiseLoan(Money amount) {
    loan = loan.plus(amount);
    bankAccount.deposit(amount);
  }
  public void recordLoan(Money amount) { loan = loan.plus(amount); }
  public void receiveRent(ColourStreet street) { rentReceivedOn = street; }
  public boolean receivedRent() { return rentReceivedOn != null; }
  public void recordShareholderPayment(Player shareholder, Money amount) {
    if (!shareholders.contains(shareholder)) throw new IllegalArgumentException("Not a shareholder.");
    shareholderPayments.merge(shareholder.id(), amount, Money::plus);
  }
  public void commitToBuild(Player shareholder, Money amount) {
    if (!shareholders.contains(shareholder)) throw new IllegalArgumentException("Not a shareholder.");
    buildCommitments.put(shareholder.id(), amount);
  }
  boolean buildCommitmentsEmpty() { return buildCommitments.isEmpty(); }
  Money buildCommitmentOf(Player shareholder) { return buildCommitments.getOrDefault(shareholder.id(), Money.ZERO); }
  void clearBuildCommitments() { buildCommitments.clear(); }
  public Money shareholderPayment(Player shareholder) {
    return shareholderPayments.getOrDefault(shareholder.id(), Money.ZERO);
  }
  public Player lastCapitalizedShareholder() { return lastCapitalizedShareholder; }
  public void recordCapitalization(Player shareholder) {
    if (!shareholders.contains(shareholder)) throw new IllegalArgumentException("Not a shareholder.");
    lastCapitalizedShareholder = shareholder;
    lastCapitalizedShareholderGrewOlder = false;
  }
  public void shareholderGrewOlder(Player shareholder) {
    if (lastCapitalizedShareholder != null
        && lastCapitalizedShareholder.id().equals(shareholder.id())) {
      lastCapitalizedShareholderGrewOlder = true;
      operated = false;
    }
  }
  public Money repayLoan(Money principal) {
    Money repayment = new Money(principal.amount() + principal.amount() * 5 / 100);
    loan = loan.minus(principal);
    return repayment;
  }

  /** Applies the entity's end-of-turn priority: build as much as affordable, then service debt, then pay a dividend. */
  public Operation operate(Deeds deeds) {
    return operate(deeds, null, null);
  }

  public Operation operate(Deeds deeds, Strategy.OfPlayers strategies, Rule.Set rules) {
    if (!hasShareholders()) return new Operation.NoAction();
    Operation building = LegalEntityBuilding.buildAsMuchAsAffordable(this, deeds, strategies, rules);
    return building != null ? operatedAs(building) : settled(deeds);
  }

  /** Whether every shareholder would fund their share of this entity's next standard-cost improvement. */
  public boolean canFundNextImprovement(Strategy.OfPlayers strategies, Rule.Set rules, Deeds deeds) {
    return LegalEntityBuilding.canFundNextImprovement(this, strategies, rules, deeds);
  }

  private Operation settled(Deeds deeds) {
    Operation settlement = repayLoanOrPayDividend(deeds);
    return operatedAs(settlement != null ? settlement : new Operation.NoAction());
  }

  private Operation operatedAs(Operation operation) {
    markOperated();
    return operation;
  }

  private Operation repayLoanOrPayDividend(Deeds deeds) {
    if (!loan.equals(Money.ZERO)) return repayLoanIfAffordable();
    return bankBalance().amount() >= 150 && fullyDeveloped(deeds)
        && (lastCapitalizedShareholder == null || lastCapitalizedShareholderGrewOlder)
        ? payDividend() : null;
  }

  private boolean fullyDeveloped(Deeds deeds) {
    return streets.stream().allMatch(deeds::hasHotelOn);
  }

  private Operation repayLoanIfAffordable() {
    if (!hasShareholders()) return new Operation.NoAction();
    Money repayment = new Money(loan.amount() + loan.amount() * 5 / 100);
    if (bankBalance().amount() < repayment.amount()) return null;
    withdrawFromBank(repayment);
    Money principal = loan;
    Money paid = repayLoan(principal);
    shareholders.getFirst().account().deposit(paid);
    return new Operation.LoanRepaid(shareholders.getFirst(), principal, paid);
  }

  private Operation payDividend() {
    if (!hasShareholders()) return new Operation.NoAction();
    Money dividend = new Money(bankBalance().amount() / shareholders.size());
    withdrawFromBank(new Money(dividend.amount() * shareholders.size()));
    shareholders.forEach(player -> player.account().deposit(dividend));
    lastCapitalizedShareholderGrewOlder = false;
    return new Operation.DividendPaid(dividend);
  }

  public sealed interface Operation {
    record LoanRepaid(Player shareholder, Money principal, Money repayment) implements Operation {
    }

    record HouseBuilt(ColourStreet street) implements Operation {
    }

    record LoanRaisedAndHouseBuilt(Money loan, ColourStreet street) implements Operation {
    }

    record DividendPaid(Money amount) implements Operation {
    }

    record NoAction() implements Operation {
    }
  }

}

/* mutate4java-manifest
version=1
moduleHash=22a36e2bf5d71c9bff39c7d20ec5e2c21398f16b8ea50470d42fa70308a14820
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHkjTGVnYWxFbnRpdHk6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=224
scope.0.semanticHash=f1f0b617307e7879fcbbbaff9657dd9faf0d4a8bea0ee6773514dcd104311157
scope.1.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uI09wZXJhdGlvbjoyMDc
scope.1.kind=class
scope.1.startLine=207
scope.1.endLine=222
scope.1.semanticHash=5da44acac7f171f13bd3dfa275ae8d256bb3b943679f0063da5fff6afa5207e3
scope.2.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNEaXZpZGVuZFBhaWQ6MjE3
scope.2.kind=class
scope.2.startLine=217
scope.2.endLine=218
scope.2.semanticHash=0b69ccd7ea09521e3e3e9d298c96117637dca3d237c184584c4669483b2e8f03
scope.3.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjSG91c2VCdWlsdDoyMTE
scope.3.kind=class
scope.3.startLine=211
scope.3.endLine=212
scope.3.semanticHash=dcae13bb81221b6370517b84b7a45208cd96ea547ef72005527f03adf68b013a
scope.4.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I0xvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0OjIxNA
scope.4.kind=class
scope.4.startLine=214
scope.4.endLine=215
scope.4.semanticHash=4b3efa345da2f3fa37efafd54344939903debb6d14136a8cf05631336c841b74
scope.5.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjTG9hblJlcGFpZDoyMDg
scope.5.kind=class
scope.5.startLine=208
scope.5.endLine=209
scope.5.semanticHash=41a047a961472207700654f4741eee2a240ba4cb361357fb6c137a712574c6a0
scope.6.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLk5vQWN0aW9uI05vQWN0aW9uOjIyMA
scope.6.kind=class
scope.6.startLine=220
scope.6.endLine=221
scope.6.semanticHash=8be20b41827c09498e59853af7a4dd560b2d4810feef6997ec5ba90608a1c494
scope.7.id=ZmllbGQ6TGVnYWxFbnRpdHkjYmFua0FjY291bnQ6MjU
scope.7.kind=field
scope.7.startLine=25
scope.7.endLine=25
scope.7.semanticHash=7bbf3a82e28f3c6efcdc54e30a5e45ff2d7aca1ff947b2ae29242496960ca65d
scope.8.id=ZmllbGQ6TGVnYWxFbnRpdHkjYnVpbGRDb21taXRtZW50czoyOA
scope.8.kind=field
scope.8.startLine=28
scope.8.endLine=28
scope.8.semanticHash=b17aa2b7b71051c48a8c4ee2eb696ac8281ff4619604db1d41d58c5317080d30
scope.9.id=ZmllbGQ6TGVnYWxFbnRpdHkjY29sb3VyOjIx
scope.9.kind=field
scope.9.startLine=21
scope.9.endLine=21
scope.9.semanticHash=8bcc7ad2b0ce320016118422cec6012345e08fddb913b4b1f638adfde08910c7
scope.10.id=ZmllbGQ6TGVnYWxFbnRpdHkjbGFzdENhcGl0YWxpemVkU2hhcmVob2xkZXI6Mjk
scope.10.kind=field
scope.10.startLine=29
scope.10.endLine=29
scope.10.semanticHash=f2fe3954b516a50ada49af2785268d16be5e45b7593829918647cfdcd4cd251b
scope.11.id=ZmllbGQ6TGVnYWxFbnRpdHkjbGFzdENhcGl0YWxpemVkU2hhcmVob2xkZXJHcmV3T2xkZXI6MzA
scope.11.kind=field
scope.11.startLine=30
scope.11.endLine=30
scope.11.semanticHash=e66adcfe75bd3269f1765c48ff5a684d3b1e7d67a00f77b6515b5eb9b19084bb
scope.12.id=ZmllbGQ6TGVnYWxFbnRpdHkjbG9hbjoyNA
scope.12.kind=field
scope.12.startLine=24
scope.12.endLine=24
scope.12.semanticHash=0eb11d1b549cd698514ba826ca398238645a5bc40f5232d6985d33c99420428a
scope.13.id=ZmllbGQ6TGVnYWxFbnRpdHkjbmFtZToyMA
scope.13.kind=field
scope.13.startLine=20
scope.13.endLine=20
scope.13.semanticHash=50911222d6c01838cb594ba4fe8b2b9fe6c9ec53e268036b98aceda32dad771c
scope.14.id=ZmllbGQ6TGVnYWxFbnRpdHkjb3BlcmF0ZWQ6MzE
scope.14.kind=field
scope.14.startLine=31
scope.14.endLine=31
scope.14.semanticHash=b3efe17a01dba6b4c344144f77ddb94b637e76bc47c0aa2853ffbcef7b22286a
scope.15.id=ZmllbGQ6TGVnYWxFbnRpdHkjcmVudFJlY2VpdmVkT246MjY
scope.15.kind=field
scope.15.startLine=26
scope.15.endLine=26
scope.15.semanticHash=5aa48ef8fc285c873817fe42af22d3c53f69ecf2ac3f9baa3a57ecda337c3407
scope.16.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJQYXltZW50czoyNw
scope.16.kind=field
scope.16.startLine=27
scope.16.endLine=27
scope.16.semanticHash=6010b82ace6eae187e7c7ee35e3a3ba7b06b99e9abf998984cbec3d957daa0f9
scope.17.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJzOjIy
scope.17.kind=field
scope.17.startLine=22
scope.17.endLine=22
scope.17.semanticHash=a7cf30c47f8e4c7c871fc45960987ec8670a446fafde737fbd72d51ea5be206f
scope.18.id=ZmllbGQ6TGVnYWxFbnRpdHkjc3RyZWV0czoyMw
scope.18.kind=field
scope.18.startLine=23
scope.18.endLine=23
scope.18.semanticHash=df36006d25c9c7f2913b137bc7e547909a0df36eafdcbcda0145384e5046758b
scope.19.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNhbW91bnQ6MjE3
scope.19.kind=field
scope.19.startLine=217
scope.19.endLine=217
scope.19.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.20.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjc3RyZWV0OjIxMQ
scope.20.kind=field
scope.20.startLine=211
scope.20.endLine=211
scope.20.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.21.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I2xvYW46MjE0
scope.21.kind=field
scope.21.startLine=214
scope.21.endLine=214
scope.21.semanticHash=d4d84c63caeacb4f9272dffed543044a61c8b0b5c4af3764e868090776e65294
scope.22.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I3N0cmVldDoyMTQ
scope.22.kind=field
scope.22.startLine=214
scope.22.endLine=214
scope.22.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.23.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcHJpbmNpcGFsOjIwOA
scope.23.kind=field
scope.23.startLine=208
scope.23.endLine=208
scope.23.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.24.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcmVwYXltZW50OjIwOA
scope.24.kind=field
scope.24.startLine=208
scope.24.endLine=208
scope.24.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.25.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjc2hhcmVob2xkZXI6MjA4
scope.25.kind=field
scope.25.startLine=208
scope.25.endLine=208
scope.25.semanticHash=a67773ac74374bf297c8b046f4a036b7b383f81231c7b87d05151145d4006783
scope.26.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JhbmtCYWxhbmNlKDApOjEwNQ
scope.26.kind=method
scope.26.startLine=105
scope.26.endLine=105
scope.26.semanticHash=a444af7ebeadf69d34ed0f13bc8ad1f6d2d6978af13b5b0260e3763b23a60ec6
scope.27.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkQ29tbWl0bWVudE9mKDEpOjEyNg
scope.27.kind=method
scope.27.startLine=126
scope.27.endLine=126
scope.27.semanticHash=b352c0bbe37b3169c14caa0c83f5c80f6c6fd8ceb15600b77d657126d4855a89
scope.28.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkQ29tbWl0bWVudHNFbXB0eSgwKToxMjU
scope.28.kind=method
scope.28.startLine=125
scope.28.endLine=125
scope.28.semanticHash=3740b9e3703f334f7e5bb3e6981076ecb88230f6128a6a3d867cd03e641832f1
scope.29.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NhbkZ1bmROZXh0SW1wcm92ZW1lbnQoMyk6MTYy
scope.29.kind=method
scope.29.startLine=162
scope.29.endLine=164
scope.29.semanticHash=3e227c43d688cf41b34802286602a8cde326cafbbfb1a48d69b5beb850c39ce8
scope.30.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NsZWFyQnVpbGRDb21taXRtZW50cygwKToxMjc
scope.30.kind=method
scope.30.startLine=127
scope.30.endLine=127
scope.30.semanticHash=3bcabe7396b12e71921c81776abb11f6252697a5710da2c757390c99f716ce8b
scope.31.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91cigwKTo2Mg
scope.31.kind=method
scope.31.startLine=62
scope.31.endLine=62
scope.31.semanticHash=61fa4ee3a95e764e4c9372fff2696b5e9e3c5aeb0dd7407567c74e28017b11cd
scope.32.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbW1pdFRvQnVpbGQoMik6MTIx
scope.32.kind=method
scope.32.startLine=121
scope.32.endLine=124
scope.32.semanticHash=58fe1bcf3cbcefa5c9dd43eb3c3133c5aad38236343a29ba5b1c5ab39a9e00bf
scope.33.id=bWV0aG9kOkxlZ2FsRW50aXR5I2N0b3IoNSk6MzM
scope.33.kind=method
scope.33.startLine=33
scope.33.endLine=42
scope.33.semanticHash=8a95db0cf9a16ce09a4486d937bf397276b3dc155d6a81199d3fb69f8191d0e4
scope.34.id=bWV0aG9kOkxlZ2FsRW50aXR5I2RlcG9zaXRUb0JhbmsoMSk6MTA2
scope.34.kind=method
scope.34.startLine=106
scope.34.endLine=106
scope.34.semanticHash=9ea79f364cb5cd19fa125183bfc835974c88f4e0e5ea75a2448dbe9acf8d6352
scope.35.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm0oNik6NDQ
scope.35.kind=method
scope.35.startLine=44
scope.35.endLine=49
scope.35.semanticHash=607256097a596f30f33de7e4d965fe74fb79b65877ff15e15b3ab39bb8c27bb6
scope.36.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm1lZCg0KTo1Mg
scope.36.kind=method
scope.36.startLine=52
scope.36.endLine=54
scope.36.semanticHash=02caf564807a3a12b98f61cf13fe5b91cd42832a32b5c6ec8726c5bd00f52ad9
scope.37.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Z1bGx5RGV2ZWxvcGVkKDEpOjE4Mw
scope.37.kind=method
scope.37.startLine=183
scope.37.endLine=185
scope.37.semanticHash=c36a1f93d4f60f21923beeb05774bd828655c8c3cb00dbe111814d896e910a12
scope.38.id=bWV0aG9kOkxlZ2FsRW50aXR5I2hhc1NoYXJlaG9sZGVycygwKTo2NA
scope.38.kind=method
scope.38.startLine=64
scope.38.endLine=64
scope.38.semanticHash=c4efbffa6302f7743bd487e01cfc46fe852abbefa88e5e22b409044d4716bee9
scope.39.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVyKDApOjEzMQ
scope.39.kind=method
scope.39.startLine=131
scope.39.endLine=131
scope.39.semanticHash=3ce02856488a64e8028cf542db1562aef51faba20444f04364213b3f68a00252
scope.40.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xpcXVpZGF0ZVRvKDEpOjky
scope.40.kind=method
scope.40.startLine=92
scope.40.endLine=102
scope.40.semanticHash=7f0e1fc6f7941a41ef9e36fea3b4a54cf2aa4b995d7ef1a871bc23f0bb0a4d60
scope.41.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xvYW4oMCk6MTA0
scope.41.kind=method
scope.41.startLine=104
scope.41.endLine=104
scope.41.semanticHash=5aea94847a2d312e9b1926d1160d1eba775015b671451f05a52a4d3d5d989fe4
scope.42.id=bWV0aG9kOkxlZ2FsRW50aXR5I21hcmtPcGVyYXRlZCgwKToxMDk
scope.42.kind=method
scope.42.startLine=109
scope.42.endLine=109
scope.42.semanticHash=45f1b8b1350b04e17da39d3b7caee90e3c4c619b64d10b022653fdc007a00b4a
scope.43.id=bWV0aG9kOkxlZ2FsRW50aXR5I25hbWUoMCk6NjE
scope.43.kind=method
scope.43.startLine=61
scope.43.endLine=61
scope.43.semanticHash=49add184feea67e02d8ac137f88d4c5ecd32bfddf5f28841a4ae58f4edb91125
scope.44.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMSk6MTUx
scope.44.kind=method
scope.44.startLine=151
scope.44.endLine=153
scope.44.semanticHash=0eacc59a2e0e2848a84e12d08b2f574804780494ba9d370a6cb45df7372dc9b6
scope.45.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMyk6MTU1
scope.45.kind=method
scope.45.startLine=155
scope.45.endLine=159
scope.45.semanticHash=319e47effb8702abe1c5c5ae30f807788e3b3d83fb5e6b5dd52069bced10b920
scope.46.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkKDApOjEwOA
scope.46.kind=method
scope.46.startLine=108
scope.46.endLine=108
scope.46.semanticHash=3f1616aac94d6299300ade7b2a8c5e8e5af5f3254fc9ef247bc940342fb5a800
scope.47.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkQXMoMSk6MTcx
scope.47.kind=method
scope.47.startLine=171
scope.47.endLine=174
scope.47.semanticHash=11af9141fa017d985ffbd2738e95988764f6e4e4ff684bdef05b4a647258d197
scope.48.id=bWV0aG9kOkxlZ2FsRW50aXR5I3BheURpdmlkZW5kKDApOjE5OA
scope.48.kind=method
scope.48.startLine=198
scope.48.endLine=205
scope.48.semanticHash=0d1434b63d42a52a2674245e9e05268d7d562fae06ea44fd97601f600c5e4af1
scope.49.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTG9hbigxKToxMTA
scope.49.kind=method
scope.49.startLine=110
scope.49.endLine=113
scope.49.semanticHash=9302e05d76ba53ee9df1ba3af855016595cb6a96e20e8679fac8236d29f96afc
scope.50.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVSZW50KDEpOjExNQ
scope.50.kind=method
scope.50.startLine=115
scope.50.endLine=115
scope.50.semanticHash=084d46aeb96ce70030969c6cd3b601b985aa6095bdacba9c1534798b3c8392c0
scope.51.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVkUmVudCgwKToxMTY
scope.51.kind=method
scope.51.startLine=116
scope.51.endLine=116
scope.51.semanticHash=42ed660456ec75ab515bcc5bde3d0dedd244534fa2cbe0292b253f16659b52fa
scope.52.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZENhcGl0YWxpemF0aW9uKDEpOjEzMg
scope.52.kind=method
scope.52.startLine=132
scope.52.endLine=136
scope.52.semanticHash=74e49f12443432ff774257c94325a25b9d3a0dc6d6d1843aa0437df7cd5c32f6
scope.53.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZExvYW4oMSk6MTE0
scope.53.kind=method
scope.53.startLine=114
scope.53.endLine=114
scope.53.semanticHash=f9380a92dcf167189dc26571308891b41951d3798240b560abe0dd3f03436476
scope.54.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZFNoYXJlaG9sZGVyUGF5bWVudCgyKToxMTc
scope.54.kind=method
scope.54.startLine=117
scope.54.endLine=120
scope.54.semanticHash=e7698fcb7889addba31b1795a141d5881c05796205acbe0bc3484d24346f4213
scope.55.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlbW92ZVNoYXJlcygxKTo4Nw
scope.55.kind=method
scope.55.startLine=87
scope.55.endLine=89
scope.55.semanticHash=6d94cd79a1e2dd7a138fead9fbc4e37edf952a97b0d68a8f5dac1c507bc164fc
scope.56.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbigxKToxNDQ
scope.56.kind=method
scope.56.startLine=144
scope.56.endLine=148
scope.56.semanticHash=bca73a22a40320d53439c46d437e577c239f2bc8676b17fc65fd38cc68cb5bd8
scope.57.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbklmQWZmb3JkYWJsZSgwKToxODc
scope.57.kind=method
scope.57.startLine=187
scope.57.endLine=196
scope.57.semanticHash=cf970ee08da8f9b72950ff543784afd98666cd88b581335b8b8b7e352b8d0b6a
scope.58.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbk9yUGF5RGl2aWRlbmQoMSk6MTc2
scope.58.kind=method
scope.58.startLine=176
scope.58.endLine=181
scope.58.semanticHash=7457cb4ccf98f4da8e2032374df7c175588285a9c1a25b355ef3bab4e5f5e71c
scope.59.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NlbGxTaGFyZSgzKTo3OQ
scope.59.kind=method
scope.59.startLine=79
scope.59.endLine=85
scope.59.semanticHash=1cf45c384ea9e900be5705cc57016f800bedeb94b6c71adfda773dbd8e1f8a58
scope.60.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NldHRsZWQoMSk6MTY2
scope.60.kind=method
scope.60.startLine=166
scope.60.endLine=169
scope.60.semanticHash=82e081dcc3108533d2539bac17e1798a60e9e4327e0bea24598a3e8455c04d40
scope.61.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlT2YoMSk6NjY
scope.61.kind=method
scope.61.startLine=66
scope.61.endLine=69
scope.61.semanticHash=ef5dad659482703a6d3f81b90ee327eea86f1ff96a2842ff970660dc80cb4178
scope.62.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlVmFsdWUoMCk6NzI
scope.62.kind=method
scope.62.startLine=72
scope.62.endLine=76
scope.62.semanticHash=cd3f6ea6db2618a781b9cdc863a8b6f80a11d74fc9e2874a82566a06ed3472fb
scope.63.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyR3Jld09sZGVyKDEpOjEzNw
scope.63.kind=method
scope.63.startLine=137
scope.63.endLine=143
scope.63.semanticHash=604340798714514aa9826968fa254d93f14aa404ef00853d15215e837a321056
scope.64.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyUGF5bWVudCgxKToxMjg
scope.64.kind=method
scope.64.startLine=128
scope.64.endLine=130
scope.64.semanticHash=9e0678ac6cffb008f1f70a4e58d8037ec501ffee7064c70004f9f52766bf3d2f
scope.65.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVycygwKTo2Mw
scope.65.kind=method
scope.65.startLine=63
scope.65.endLine=63
scope.65.semanticHash=887a0bbecd58fd1ad113f0a80f2359dbcea8e301b6ffb6dcf11c7509e796a66a
scope.66.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHMoMCk6NjU
scope.66.kind=method
scope.66.startLine=65
scope.66.endLine=65
scope.66.semanticHash=7020ffe61f8cc9dd780c62717a353212389033396cdf981f3d88c1ac3f5a1b72
scope.67.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHNPZigyKTo1Ng
scope.67.kind=method
scope.67.startLine=56
scope.67.endLine=59
scope.67.semanticHash=20dcba0a9dc440b6eaa72b374c3cdb05c172e301c9db4e6843e9b438c0854040
scope.68.id=bWV0aG9kOkxlZ2FsRW50aXR5I3dpdGhkcmF3RnJvbUJhbmsoMSk6MTA3
scope.68.kind=method
scope.68.startLine=107
scope.68.endLine=107
scope.68.semanticHash=31d998ce1ee917e685e973fdf2e171d8c0c2811c90e87c145788206cb3dc679d
scope.69.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5EaXZpZGVuZFBhaWQjY3RvcigxKToyMTc
scope.69.kind=method
scope.69.startLine=1
scope.69.endLine=224
scope.69.semanticHash=9fe7f4b47e270691c783fc225e9474f388601662db939a0d6517c8ddf523f310
scope.70.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ib3VzZUJ1aWx0I2N0b3IoMSk6MjEx
scope.70.kind=method
scope.70.startLine=1
scope.70.endLine=224
scope.70.semanticHash=9fe7f4b47e270691c783fc225e9474f388601662db939a0d6517c8ddf523f310
scope.71.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmFpc2VkQW5kSG91c2VCdWlsdCNjdG9yKDIpOjIxNA
scope.71.kind=method
scope.71.startLine=1
scope.71.endLine=224
scope.71.semanticHash=9fe7f4b47e270691c783fc225e9474f388601662db939a0d6517c8ddf523f310
scope.72.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmVwYWlkI2N0b3IoMyk6MjA4
scope.72.kind=method
scope.72.startLine=1
scope.72.endLine=224
scope.72.semanticHash=9fe7f4b47e270691c783fc225e9474f388601662db939a0d6517c8ddf523f310
scope.73.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ob0FjdGlvbiNjdG9yKDApOjIyMA
scope.73.kind=method
scope.73.startLine=1
scope.73.endLine=224
scope.73.semanticHash=9fe7f4b47e270691c783fc225e9474f388601662db939a0d6517c8ddf523f310
*/
