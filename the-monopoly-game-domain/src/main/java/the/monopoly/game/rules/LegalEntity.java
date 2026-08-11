package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.finance.Bank.Account;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;

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
  private Money liquidationRecipientBalance;
  private Money liquidationRecipientBalanceAfter;
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
    liquidationRecipientBalance = shareholder.account().balance().amount();
    if (balance.amount() > 0) {
      bankAccount.withdraw(balance);
      shareholder.account().deposit(balance);
    }
    liquidationRecipientBalanceAfter = shareholder.account().balance().amount();
    shareholders.clear();
    return balance;
  }

  public Money liquidationRecipientBalance() { return liquidationRecipientBalance; }
  public Money liquidationRecipientBalanceAfter() { return liquidationRecipientBalanceAfter; }

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
    if (!hasShareholders()) return new Operation.NoAction();
    Operation building = buildAsMuchAsAffordable(deeds);
    if (building != null) {
      markOperated();
      return building;
    }
    Operation settlement = repayLoanOrPayDividend();
    markOperated();
    return settlement != null ? settlement : new Operation.NoAction();
  }

  private Operation buildAsMuchAsAffordable(Deeds deeds) {
    List<ColourStreet> plan = affordableBuildPlan(deeds);
    if (plan.isEmpty()) return null;

    Money shortfall = totalConstructionCost(plan).minus(bankBalance());
    Money loanRaised = Money.ZERO;
    if (shortfall.amount() > 0) {
      loanRaised = borrowShortfall(shortfall);
    }
    plan.forEach(street -> buildOneImprovement(deeds, street));

    ColourStreet firstBuilt = plan.getFirst();
    return loanRaised.equals(Money.ZERO)
        ? new Operation.HouseBuilt(firstBuilt)
        : new Operation.LoanRaisedAndHouseBuilt(loanRaised, firstBuilt);
  }

  private List<ColourStreet> affordableBuildPlan(Deeds deeds) {
    List<ColourStreet> plan = new java.util.ArrayList<>();
    Money totalCost = Money.ZERO;
    boolean startedWithTreasuryFunds = bankBalance().amount() > 0;
    boolean canReachHotels = bankBalance().equals(Money.ZERO) && loan.equals(Money.ZERO)
        && !buildCommitments.isEmpty();
    while (true) {
      ColourStreet next = cheapestBuildableStreet(deeds, plan, canReachHotels);
      if (next == null) break;
      Money candidateCost = totalCost.plus(next.houseConstructionCost());
      Money shortfall = candidateCost.minus(bankBalance());
      if (cannotExtendPlan(shortfall, startedWithTreasuryFunds, plan)) break;
      plan.add(next);
      totalCost = candidateCost;
    }
    return plan;
  }

  private boolean cannotExtendPlan(Money shortfall, boolean startedWithTreasuryFunds, List<ColourStreet> plan) {
    if (shortfall.amount() <= 0) return false;
    if (startedWithTreasuryFunds && !plan.isEmpty()) return true;
    return !canBorrowForBuilding(shortfall);
  }

  private Money totalConstructionCost(List<ColourStreet> plan) {
    return plan.stream().map(ColourStreet::houseConstructionCost)
        .reduce(Money.ZERO, Money::plus);
  }

  private ColourStreet cheapestBuildableStreet(Deeds deeds, List<ColourStreet> plan,
                                               boolean canReachHotels) {
    return streets.stream()
        .filter(street -> !deeds.hasHotelOn(street)
            && ((deeds.housesBuiltOn(street)
                + (int) plan.stream().filter(street::equals).count())
                < street.hotelConstructionRequiresNumberOfHouses()
                || (canReachHotels && deeds.housesBuiltOn(street)
                    + (int) plan.stream().filter(street::equals).count()
                    == street.hotelConstructionRequiresNumberOfHouses())))
        .min(java.util.Comparator.comparingInt(street -> deeds.housesBuiltOn(street)
            + (int) plan.stream().filter(street::equals).count()))
        .orElse(null);
  }

  private boolean canBorrowForBuilding(Money shortfall) {
    if (!hasShareholders() || !loan.equals(Money.ZERO))
      return false;
    List<Money> shares = sharesOf(shortfall);
    return java.util.stream.IntStream.range(0, shareholders.size()).allMatch(index ->
        shareholders.get(index).account().balance().amount().amount() >= shares.get(index).amount()
            && buildCommitments.getOrDefault(shareholders.get(index).id(), Money.ZERO).amount()
                >= shares.get(index).amount());
  }

  private Money borrowShortfall(Money shortfall) {
    recordLoan(shortfall);
    depositToBank(shortfall);
    List<Money> shares = sharesOf(shortfall);
    for (int index = 0; index < shareholders.size(); index++) {
      Player shareholder = shareholders.get(index);
      Money share = shares.get(index);
      shareholder.account().withdraw(share);
      recordShareholderPayment(shareholder, share);
      if (!share.equals(Money.ZERO)) recordCapitalization(shareholder);
    }
    buildCommitments.clear();
    return shortfall;
  }

  /** Splits an amount across shareholders as evenly as possible; earlier shareholders absorb any remainder. */
  private List<Money> sharesOf(Money amount) {
    if (!hasShareholders()) return List.of();
    int base = amount.amount() / shareholders.size();
    int remainder = amount.amount() % shareholders.size();
    return java.util.stream.IntStream.range(0, shareholders.size())
        .mapToObj(index -> new Money(base + (index < remainder ? 1 : 0)))
        .toList();
  }

  private void buildOneImprovement(Deeds deeds, ColourStreet street) {
    withdrawFromBank(street.houseConstructionCost());
    if (deeds.housesBuiltOn(street) == street.hotelConstructionRequiresNumberOfHouses())
      deeds.arrangeHotel(street);
    else deeds.arrangeHouses(street, deeds.housesBuiltOn(street) + 1);
  }

  private Operation repayLoanOrPayDividend() {
    if (!loan.equals(Money.ZERO)) return repayLoanIfAffordable();
    return bankBalance().amount() >= 150
        && (lastCapitalizedShareholder == null || lastCapitalizedShareholderGrewOlder)
        ? payDividend() : null;
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
moduleHash=94e4ea2f2cfae407555a7f8e2d6935ceababda293726ffeb9ff2f4a8f3365e35
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHkjTGVnYWxFbnRpdHk6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=293
scope.0.semanticHash=f2e533b0e110e854fae20271062cf318250f9fe9cd21418646766a1c5ad3dffb
scope.1.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uI09wZXJhdGlvbjoyNzY
scope.1.kind=class
scope.1.startLine=276
scope.1.endLine=291
scope.1.semanticHash=5da44acac7f171f13bd3dfa275ae8d256bb3b943679f0063da5fff6afa5207e3
scope.2.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNEaXZpZGVuZFBhaWQ6Mjg2
scope.2.kind=class
scope.2.startLine=286
scope.2.endLine=287
scope.2.semanticHash=0b69ccd7ea09521e3e3e9d298c96117637dca3d237c184584c4669483b2e8f03
scope.3.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjSG91c2VCdWlsdDoyODA
scope.3.kind=class
scope.3.startLine=280
scope.3.endLine=281
scope.3.semanticHash=dcae13bb81221b6370517b84b7a45208cd96ea547ef72005527f03adf68b013a
scope.4.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I0xvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0OjI4Mw
scope.4.kind=class
scope.4.startLine=283
scope.4.endLine=284
scope.4.semanticHash=4b3efa345da2f3fa37efafd54344939903debb6d14136a8cf05631336c841b74
scope.5.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjTG9hblJlcGFpZDoyNzc
scope.5.kind=class
scope.5.startLine=277
scope.5.endLine=278
scope.5.semanticHash=41a047a961472207700654f4741eee2a240ba4cb361357fb6c137a712574c6a0
scope.6.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLk5vQWN0aW9uI05vQWN0aW9uOjI4OQ
scope.6.kind=class
scope.6.startLine=289
scope.6.endLine=290
scope.6.semanticHash=8be20b41827c09498e59853af7a4dd560b2d4810feef6997ec5ba90608a1c494
scope.7.id=ZmllbGQ6TGVnYWxFbnRpdHkjYmFua0FjY291bnQ6MjQ
scope.7.kind=field
scope.7.startLine=24
scope.7.endLine=24
scope.7.semanticHash=7bbf3a82e28f3c6efcdc54e30a5e45ff2d7aca1ff947b2ae29242496960ca65d
scope.8.id=ZmllbGQ6TGVnYWxFbnRpdHkjYnVpbGRDb21taXRtZW50czoyNw
scope.8.kind=field
scope.8.startLine=27
scope.8.endLine=27
scope.8.semanticHash=b17aa2b7b71051c48a8c4ee2eb696ac8281ff4619604db1d41d58c5317080d30
scope.9.id=ZmllbGQ6TGVnYWxFbnRpdHkjY29sb3VyOjIw
scope.9.kind=field
scope.9.startLine=20
scope.9.endLine=20
scope.9.semanticHash=8bcc7ad2b0ce320016118422cec6012345e08fddb913b4b1f638adfde08910c7
scope.10.id=ZmllbGQ6TGVnYWxFbnRpdHkjbGFzdENhcGl0YWxpemVkU2hhcmVob2xkZXI6Mjg
scope.10.kind=field
scope.10.startLine=28
scope.10.endLine=28
scope.10.semanticHash=f2fe3954b516a50ada49af2785268d16be5e45b7593829918647cfdcd4cd251b
scope.11.id=ZmllbGQ6TGVnYWxFbnRpdHkjbGFzdENhcGl0YWxpemVkU2hhcmVob2xkZXJHcmV3T2xkZXI6Mjk
scope.11.kind=field
scope.11.startLine=29
scope.11.endLine=29
scope.11.semanticHash=e66adcfe75bd3269f1765c48ff5a684d3b1e7d67a00f77b6515b5eb9b19084bb
scope.12.id=ZmllbGQ6TGVnYWxFbnRpdHkjbG9hbjoyMw
scope.12.kind=field
scope.12.startLine=23
scope.12.endLine=23
scope.12.semanticHash=0eb11d1b549cd698514ba826ca398238645a5bc40f5232d6985d33c99420428a
scope.13.id=ZmllbGQ6TGVnYWxFbnRpdHkjbmFtZToxOQ
scope.13.kind=field
scope.13.startLine=19
scope.13.endLine=19
scope.13.semanticHash=50911222d6c01838cb594ba4fe8b2b9fe6c9ec53e268036b98aceda32dad771c
scope.14.id=ZmllbGQ6TGVnYWxFbnRpdHkjb3BlcmF0ZWQ6MzA
scope.14.kind=field
scope.14.startLine=30
scope.14.endLine=30
scope.14.semanticHash=b3efe17a01dba6b4c344144f77ddb94b637e76bc47c0aa2853ffbcef7b22286a
scope.15.id=ZmllbGQ6TGVnYWxFbnRpdHkjcmVudFJlY2VpdmVkT246MjU
scope.15.kind=field
scope.15.startLine=25
scope.15.endLine=25
scope.15.semanticHash=5aa48ef8fc285c873817fe42af22d3c53f69ecf2ac3f9baa3a57ecda337c3407
scope.16.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJQYXltZW50czoyNg
scope.16.kind=field
scope.16.startLine=26
scope.16.endLine=26
scope.16.semanticHash=6010b82ace6eae187e7c7ee35e3a3ba7b06b99e9abf998984cbec3d957daa0f9
scope.17.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJzOjIx
scope.17.kind=field
scope.17.startLine=21
scope.17.endLine=21
scope.17.semanticHash=a7cf30c47f8e4c7c871fc45960987ec8670a446fafde737fbd72d51ea5be206f
scope.18.id=ZmllbGQ6TGVnYWxFbnRpdHkjc3RyZWV0czoyMg
scope.18.kind=field
scope.18.startLine=22
scope.18.endLine=22
scope.18.semanticHash=df36006d25c9c7f2913b137bc7e547909a0df36eafdcbcda0145384e5046758b
scope.19.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNhbW91bnQ6Mjg2
scope.19.kind=field
scope.19.startLine=286
scope.19.endLine=286
scope.19.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.20.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjc3RyZWV0OjI4MA
scope.20.kind=field
scope.20.startLine=280
scope.20.endLine=280
scope.20.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.21.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I2xvYW46Mjgz
scope.21.kind=field
scope.21.startLine=283
scope.21.endLine=283
scope.21.semanticHash=d4d84c63caeacb4f9272dffed543044a61c8b0b5c4af3764e868090776e65294
scope.22.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I3N0cmVldDoyODM
scope.22.kind=field
scope.22.startLine=283
scope.22.endLine=283
scope.22.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.23.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcHJpbmNpcGFsOjI3Nw
scope.23.kind=field
scope.23.startLine=277
scope.23.endLine=277
scope.23.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.24.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcmVwYXltZW50OjI3Nw
scope.24.kind=field
scope.24.startLine=277
scope.24.endLine=277
scope.24.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.25.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjc2hhcmVob2xkZXI6Mjc3
scope.25.kind=field
scope.25.startLine=277
scope.25.endLine=277
scope.25.semanticHash=a67773ac74374bf297c8b046f4a036b7b383f81231c7b87d05151145d4006783
scope.26.id=bWV0aG9kOkxlZ2FsRW50aXR5I2FmZm9yZGFibGVCdWlsZFBsYW4oMSk6MTYz
scope.26.kind=method
scope.26.startLine=163
scope.26.endLine=179
scope.26.semanticHash=5f791da057096ad9df6b8483635f7d3e6aa28942974bd2c2e5e976e48e0cd8de
scope.27.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JhbmtCYWxhbmNlKDApOjkx
scope.27.kind=method
scope.27.startLine=91
scope.27.endLine=91
scope.27.semanticHash=a444af7ebeadf69d34ed0f13bc8ad1f6d2d6978af13b5b0260e3763b23a60ec6
scope.28.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvcnJvd1Nob3J0ZmFsbCgxKToyMTc
scope.28.kind=method
scope.28.startLine=217
scope.28.endLine=230
scope.28.semanticHash=73e85bcd139a112256aef5cd113184cfcb43fe461f2a6fab3cc0a9df6434eb45
scope.29.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkQXNNdWNoQXNBZmZvcmRhYmxlKDEpOjE0Ng
scope.29.kind=method
scope.29.startLine=146
scope.29.endLine=161
scope.29.semanticHash=2c7ff3159d6b5bf98024a251f97a4a480dae61cb34359ed42b68ec5ba256ec7d
scope.30.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkT25lSW1wcm92ZW1lbnQoMik6MjQy
scope.30.kind=method
scope.30.startLine=242
scope.30.endLine=247
scope.30.semanticHash=24880e84564fc041de398746285922b8148542b38600b80d0cfeb56239764989
scope.31.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NhbkJvcnJvd0ZvckJ1aWxkaW5nKDEpOjIwNw
scope.31.kind=method
scope.31.startLine=207
scope.31.endLine=215
scope.31.semanticHash=46f3e9be22db33f7982069871f87d63f7652e2adc4e45d09d2a927e3b56bd66e
scope.32.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Nhbm5vdEV4dGVuZFBsYW4oMyk6MTgx
scope.32.kind=method
scope.32.startLine=181
scope.32.endLine=185
scope.32.semanticHash=ddc4036ee483a8adc5e302b2cf52563bc127a8a730237848ac94c72edcc6d974
scope.33.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NoZWFwZXN0QnVpbGRhYmxlU3RyZWV0KDMpOjE5Mg
scope.33.kind=method
scope.33.startLine=192
scope.33.endLine=205
scope.33.semanticHash=63050f5ddccea0df7d510fdded5007b91e7cbffbfb9c50303348617695ffaa41
scope.34.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91cigwKTo2MQ
scope.34.kind=method
scope.34.startLine=61
scope.34.endLine=61
scope.34.semanticHash=61fa4ee3a95e764e4c9372fff2696b5e9e3c5aeb0dd7407567c74e28017b11cd
scope.35.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbW1pdFRvQnVpbGQoMik6MTA3
scope.35.kind=method
scope.35.startLine=107
scope.35.endLine=110
scope.35.semanticHash=58fe1bcf3cbcefa5c9dd43eb3c3133c5aad38236343a29ba5b1c5ab39a9e00bf
scope.36.id=bWV0aG9kOkxlZ2FsRW50aXR5I2N0b3IoNSk6MzI
scope.36.kind=method
scope.36.startLine=32
scope.36.endLine=41
scope.36.semanticHash=8a95db0cf9a16ce09a4486d937bf397276b3dc155d6a81199d3fb69f8191d0e4
scope.37.id=bWV0aG9kOkxlZ2FsRW50aXR5I2RlcG9zaXRUb0JhbmsoMSk6OTI
scope.37.kind=method
scope.37.startLine=92
scope.37.endLine=92
scope.37.semanticHash=9ea79f364cb5cd19fa125183bfc835974c88f4e0e5ea75a2448dbe9acf8d6352
scope.38.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm0oNik6NDM
scope.38.kind=method
scope.38.startLine=43
scope.38.endLine=48
scope.38.semanticHash=607256097a596f30f33de7e4d965fe74fb79b65877ff15e15b3ab39bb8c27bb6
scope.39.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm1lZCg0KTo1MQ
scope.39.kind=method
scope.39.startLine=51
scope.39.endLine=53
scope.39.semanticHash=02caf564807a3a12b98f61cf13fe5b91cd42832a32b5c6ec8726c5bd00f52ad9
scope.40.id=bWV0aG9kOkxlZ2FsRW50aXR5I2hhc1NoYXJlaG9sZGVycygwKTo2Mw
scope.40.kind=method
scope.40.startLine=63
scope.40.endLine=63
scope.40.semanticHash=c4efbffa6302f7743bd487e01cfc46fe852abbefa88e5e22b409044d4716bee9
scope.41.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVyKDApOjExNA
scope.41.kind=method
scope.41.startLine=114
scope.41.endLine=114
scope.41.semanticHash=3ce02856488a64e8028cf542db1562aef51faba20444f04364213b3f68a00252
scope.42.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xvYW4oMCk6OTA
scope.42.kind=method
scope.42.startLine=90
scope.42.endLine=90
scope.42.semanticHash=5aea94847a2d312e9b1926d1160d1eba775015b671451f05a52a4d3d5d989fe4
scope.43.id=bWV0aG9kOkxlZ2FsRW50aXR5I21hcmtPcGVyYXRlZCgwKTo5NQ
scope.43.kind=method
scope.43.startLine=95
scope.43.endLine=95
scope.43.semanticHash=45f1b8b1350b04e17da39d3b7caee90e3c4c619b64d10b022653fdc007a00b4a
scope.44.id=bWV0aG9kOkxlZ2FsRW50aXR5I25hbWUoMCk6NjA
scope.44.kind=method
scope.44.startLine=60
scope.44.endLine=60
scope.44.semanticHash=49add184feea67e02d8ac137f88d4c5ecd32bfddf5f28841a4ae58f4edb91125
scope.45.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMSk6MTM0
scope.45.kind=method
scope.45.startLine=134
scope.45.endLine=144
scope.45.semanticHash=0ad7d32677278dd3c3a3eae2f036b6c58e257df45abdffe50cc4ffe20baa6bdf
scope.46.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkKDApOjk0
scope.46.kind=method
scope.46.startLine=94
scope.46.endLine=94
scope.46.semanticHash=3f1616aac94d6299300ade7b2a8c5e8e5af5f3254fc9ef247bc940342fb5a800
scope.47.id=bWV0aG9kOkxlZ2FsRW50aXR5I3BheURpdmlkZW5kKDApOjI2Nw
scope.47.kind=method
scope.47.startLine=267
scope.47.endLine=274
scope.47.semanticHash=0d1434b63d42a52a2674245e9e05268d7d562fae06ea44fd97601f600c5e4af1
scope.48.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTG9hbigxKTo5Ng
scope.48.kind=method
scope.48.startLine=96
scope.48.endLine=99
scope.48.semanticHash=9302e05d76ba53ee9df1ba3af855016595cb6a96e20e8679fac8236d29f96afc
scope.49.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVSZW50KDEpOjEwMQ
scope.49.kind=method
scope.49.startLine=101
scope.49.endLine=101
scope.49.semanticHash=084d46aeb96ce70030969c6cd3b601b985aa6095bdacba9c1534798b3c8392c0
scope.50.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVkUmVudCgwKToxMDI
scope.50.kind=method
scope.50.startLine=102
scope.50.endLine=102
scope.50.semanticHash=42ed660456ec75ab515bcc5bde3d0dedd244534fa2cbe0292b253f16659b52fa
scope.51.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZENhcGl0YWxpemF0aW9uKDEpOjExNQ
scope.51.kind=method
scope.51.startLine=115
scope.51.endLine=119
scope.51.semanticHash=74e49f12443432ff774257c94325a25b9d3a0dc6d6d1843aa0437df7cd5c32f6
scope.52.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZExvYW4oMSk6MTAw
scope.52.kind=method
scope.52.startLine=100
scope.52.endLine=100
scope.52.semanticHash=f9380a92dcf167189dc26571308891b41951d3798240b560abe0dd3f03436476
scope.53.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZFNoYXJlaG9sZGVyUGF5bWVudCgyKToxMDM
scope.53.kind=method
scope.53.startLine=103
scope.53.endLine=106
scope.53.semanticHash=e7698fcb7889addba31b1795a141d5881c05796205acbe0bc3484d24346f4213
scope.54.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlbW92ZVNoYXJlcygxKTo4Ng
scope.54.kind=method
scope.54.startLine=86
scope.54.endLine=88
scope.54.semanticHash=6d94cd79a1e2dd7a138fead9fbc4e37edf952a97b0d68a8f5dac1c507bc164fc
scope.55.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbigxKToxMjc
scope.55.kind=method
scope.55.startLine=127
scope.55.endLine=131
scope.55.semanticHash=bca73a22a40320d53439c46d437e577c239f2bc8676b17fc65fd38cc68cb5bd8
scope.56.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbklmQWZmb3JkYWJsZSgwKToyNTY
scope.56.kind=method
scope.56.startLine=256
scope.56.endLine=265
scope.56.semanticHash=cf970ee08da8f9b72950ff543784afd98666cd88b581335b8b8b7e352b8d0b6a
scope.57.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbk9yUGF5RGl2aWRlbmQoMCk6MjQ5
scope.57.kind=method
scope.57.startLine=249
scope.57.endLine=254
scope.57.semanticHash=c50c376a037fae59691e768c82e59d510e7b7d5a9da55dfd7b559615e5baf614
scope.58.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NlbGxTaGFyZSgzKTo3OA
scope.58.kind=method
scope.58.startLine=78
scope.58.endLine=84
scope.58.semanticHash=1cf45c384ea9e900be5705cc57016f800bedeb94b6c71adfda773dbd8e1f8a58
scope.59.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlT2YoMSk6NjU
scope.59.kind=method
scope.59.startLine=65
scope.59.endLine=68
scope.59.semanticHash=ef5dad659482703a6d3f81b90ee327eea86f1ff96a2842ff970660dc80cb4178
scope.60.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlVmFsdWUoMCk6NzE
scope.60.kind=method
scope.60.startLine=71
scope.60.endLine=75
scope.60.semanticHash=cd3f6ea6db2618a781b9cdc863a8b6f80a11d74fc9e2874a82566a06ed3472fb
scope.61.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyR3Jld09sZGVyKDEpOjEyMA
scope.61.kind=method
scope.61.startLine=120
scope.61.endLine=126
scope.61.semanticHash=604340798714514aa9826968fa254d93f14aa404ef00853d15215e837a321056
scope.62.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyUGF5bWVudCgxKToxMTE
scope.62.kind=method
scope.62.startLine=111
scope.62.endLine=113
scope.62.semanticHash=9e0678ac6cffb008f1f70a4e58d8037ec501ffee7064c70004f9f52766bf3d2f
scope.63.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVycygwKTo2Mg
scope.63.kind=method
scope.63.startLine=62
scope.63.endLine=62
scope.63.semanticHash=887a0bbecd58fd1ad113f0a80f2359dbcea8e301b6ffb6dcf11c7509e796a66a
scope.64.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlc09mKDEpOjIzMw
scope.64.kind=method
scope.64.startLine=233
scope.64.endLine=240
scope.64.semanticHash=04f17061eb9f508e709e3a4887cc4f12654f4031cdb1fa2bf49e6aa977fbadda
scope.65.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHMoMCk6NjQ
scope.65.kind=method
scope.65.startLine=64
scope.65.endLine=64
scope.65.semanticHash=7020ffe61f8cc9dd780c62717a353212389033396cdf981f3d88c1ac3f5a1b72
scope.66.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHNPZigyKTo1NQ
scope.66.kind=method
scope.66.startLine=55
scope.66.endLine=58
scope.66.semanticHash=20dcba0a9dc440b6eaa72b374c3cdb05c172e301c9db4e6843e9b438c0854040
scope.67.id=bWV0aG9kOkxlZ2FsRW50aXR5I3RvdGFsQ29uc3RydWN0aW9uQ29zdCgxKToxODc
scope.67.kind=method
scope.67.startLine=187
scope.67.endLine=190
scope.67.semanticHash=ee0474cafdf5134ccc1483edbc6727c23d530e28a2be9db8bdfa85f6a5a53f1c
scope.68.id=bWV0aG9kOkxlZ2FsRW50aXR5I3dpdGhkcmF3RnJvbUJhbmsoMSk6OTM
scope.68.kind=method
scope.68.startLine=93
scope.68.endLine=93
scope.68.semanticHash=31d998ce1ee917e685e973fdf2e171d8c0c2811c90e87c145788206cb3dc679d
scope.69.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5EaXZpZGVuZFBhaWQjY3RvcigxKToyODY
scope.69.kind=method
scope.69.startLine=1
scope.69.endLine=293
scope.69.semanticHash=a4a1d727bc98684b11d6ceb7c0e12ab92b9208a8039bdfb3e3ddc2c0c1e8a205
scope.70.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ib3VzZUJ1aWx0I2N0b3IoMSk6Mjgw
scope.70.kind=method
scope.70.startLine=1
scope.70.endLine=293
scope.70.semanticHash=a4a1d727bc98684b11d6ceb7c0e12ab92b9208a8039bdfb3e3ddc2c0c1e8a205
scope.71.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmFpc2VkQW5kSG91c2VCdWlsdCNjdG9yKDIpOjI4Mw
scope.71.kind=method
scope.71.startLine=1
scope.71.endLine=293
scope.71.semanticHash=a4a1d727bc98684b11d6ceb7c0e12ab92b9208a8039bdfb3e3ddc2c0c1e8a205
scope.72.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmVwYWlkI2N0b3IoMyk6Mjc3
scope.72.kind=method
scope.72.startLine=1
scope.72.endLine=293
scope.72.semanticHash=a4a1d727bc98684b11d6ceb7c0e12ab92b9208a8039bdfb3e3ddc2c0c1e8a205
scope.73.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ob0FjdGlvbiNjdG9yKDApOjI4OQ
scope.73.kind=method
scope.73.startLine=1
scope.73.endLine=293
scope.73.semanticHash=a4a1d727bc98684b11d6ceb7c0e12ab92b9208a8039bdfb3e3ddc2c0c1e8a205
*/
