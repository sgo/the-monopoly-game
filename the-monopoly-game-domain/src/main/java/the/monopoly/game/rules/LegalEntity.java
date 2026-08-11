package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.finance.Bank.Account;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
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
    if (!hasThreeDistinctShareholders(shareholders)) return Optional.empty();
    if (!boardFullyOwned(rules, deeds)) return Optional.empty();
    List<ColourStreet> streets = streetsOf(colour, rules);
    if (colourGroupIneligible(streets, highestPriority)) return Optional.empty();
    if (!splitAcrossThreeDistinctOwners(streets, deeds)) return Optional.empty();
    if (!everyShareholderOwnsAStreet(shareholders, streets, deeds)) return Optional.empty();
    return Optional.of(new LegalEntity(name, colour, shareholders, streets, rules.bank()));
  }

  /** Creates an entity from already-set-up scenario state. */
  public static LegalEntity formed(String name, Street.Colour colour, List<Player> shareholders, Rule.Set rules) {
    return new LegalEntity(name, colour, shareholders, streetsOf(colour, rules), rules.bank());
  }

  private static boolean hasThreeDistinctShareholders(List<Player> shareholders) {
    return shareholders.size() == 3 && shareholders.stream().distinct().count() == 3;
  }

  private static boolean boardFullyOwned(Rule.Set rules, Deeds deeds) {
    return rules.streets().filter(Ownable.class::isInstance).map(Ownable.class::cast)
        .noneMatch(it -> deeds.isUnowned(it.type()));
  }

  public static List<ColourStreet> streetsOf(Street.Colour colour, Rule.Set rules) {
    return rules.streets().filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast).filter(it -> it.colourGroup() == colour).toList();
  }

  /** Empty, or led by a street the Greedo priority always trades toward rather than split. */
  private static boolean colourGroupIneligible(List<ColourStreet> streets,
                                               Predicate<ColourStreet> highestPriority) {
    return streets.isEmpty() || streets.stream().anyMatch(highestPriority);
  }

  private static boolean splitAcrossThreeDistinctOwners(List<ColourStreet> streets, Deeds deeds) {
    return streets.stream().map(it -> deeds.ownerOf(it.type()).orElse(null)).distinct().count() == 3;
  }

  private static boolean everyShareholderOwnsAStreet(List<Player> shareholders, List<ColourStreet> streets, Deeds deeds) {
    return shareholders.stream().allMatch(player -> streets.stream()
        .anyMatch(street -> deeds.ownerOf(street.type()).filter(player.id()::equals).isPresent()));
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
moduleHash=519da6f9456e4ea41ced1c27624949568966a077935a79bee6db66ef4b1a40c4
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHkjTGVnYWxFbnRpdHk6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=316
scope.0.semanticHash=d066002b85490a0673459e8803129925eb580d01b570575d6e99802f148d6436
scope.1.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uI09wZXJhdGlvbjoyOTk
scope.1.kind=class
scope.1.startLine=299
scope.1.endLine=314
scope.1.semanticHash=5da44acac7f171f13bd3dfa275ae8d256bb3b943679f0063da5fff6afa5207e3
scope.2.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNEaXZpZGVuZFBhaWQ6MzA5
scope.2.kind=class
scope.2.startLine=309
scope.2.endLine=310
scope.2.semanticHash=0b69ccd7ea09521e3e3e9d298c96117637dca3d237c184584c4669483b2e8f03
scope.3.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjSG91c2VCdWlsdDozMDM
scope.3.kind=class
scope.3.startLine=303
scope.3.endLine=304
scope.3.semanticHash=dcae13bb81221b6370517b84b7a45208cd96ea547ef72005527f03adf68b013a
scope.4.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I0xvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0OjMwNg
scope.4.kind=class
scope.4.startLine=306
scope.4.endLine=307
scope.4.semanticHash=4b3efa345da2f3fa37efafd54344939903debb6d14136a8cf05631336c841b74
scope.5.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjTG9hblJlcGFpZDozMDA
scope.5.kind=class
scope.5.startLine=300
scope.5.endLine=301
scope.5.semanticHash=41a047a961472207700654f4741eee2a240ba4cb361357fb6c137a712574c6a0
scope.6.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLk5vQWN0aW9uI05vQWN0aW9uOjMxMg
scope.6.kind=class
scope.6.startLine=312
scope.6.endLine=313
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
scope.19.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNhbW91bnQ6MzA5
scope.19.kind=field
scope.19.startLine=309
scope.19.endLine=309
scope.19.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.20.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjc3RyZWV0OjMwMw
scope.20.kind=field
scope.20.startLine=303
scope.20.endLine=303
scope.20.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.21.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I2xvYW46MzA2
scope.21.kind=field
scope.21.startLine=306
scope.21.endLine=306
scope.21.semanticHash=d4d84c63caeacb4f9272dffed543044a61c8b0b5c4af3764e868090776e65294
scope.22.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I3N0cmVldDozMDY
scope.22.kind=field
scope.22.startLine=306
scope.22.endLine=306
scope.22.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.23.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcHJpbmNpcGFsOjMwMA
scope.23.kind=field
scope.23.startLine=300
scope.23.endLine=300
scope.23.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.24.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcmVwYXltZW50OjMwMA
scope.24.kind=field
scope.24.startLine=300
scope.24.endLine=300
scope.24.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.25.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjc2hhcmVob2xkZXI6MzAw
scope.25.kind=field
scope.25.startLine=300
scope.25.endLine=300
scope.25.semanticHash=a67773ac74374bf297c8b046f4a036b7b383f81231c7b87d05151145d4006783
scope.26.id=bWV0aG9kOkxlZ2FsRW50aXR5I2FmZm9yZGFibGVCdWlsZFBsYW4oMSk6MTg5
scope.26.kind=method
scope.26.startLine=189
scope.26.endLine=205
scope.26.semanticHash=5f791da057096ad9df6b8483635f7d3e6aa28942974bd2c2e5e976e48e0cd8de
scope.27.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JhbmtCYWxhbmNlKDApOjExOA
scope.27.kind=method
scope.27.startLine=118
scope.27.endLine=118
scope.27.semanticHash=a444af7ebeadf69d34ed0f13bc8ad1f6d2d6978af13b5b0260e3763b23a60ec6
scope.28.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvYXJkRnVsbHlPd25lZCgyKTo2NQ
scope.28.kind=method
scope.28.startLine=65
scope.28.endLine=68
scope.28.semanticHash=f1356c828dc58032afd29882bdbf3bcedd40c0b501507da684fe18f9a2db8250
scope.29.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvcnJvd1Nob3J0ZmFsbCgxKToyNDM
scope.29.kind=method
scope.29.startLine=243
scope.29.endLine=256
scope.29.semanticHash=73e85bcd139a112256aef5cd113184cfcb43fe461f2a6fab3cc0a9df6434eb45
scope.30.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkQXNNdWNoQXNBZmZvcmRhYmxlKDEpOjE3Mg
scope.30.kind=method
scope.30.startLine=172
scope.30.endLine=187
scope.30.semanticHash=2c7ff3159d6b5bf98024a251f97a4a480dae61cb34359ed42b68ec5ba256ec7d
scope.31.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkT25lSW1wcm92ZW1lbnQoMik6MjY3
scope.31.kind=method
scope.31.startLine=267
scope.31.endLine=272
scope.31.semanticHash=24880e84564fc041de398746285922b8148542b38600b80d0cfeb56239764989
scope.32.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NhbkJvcnJvd0ZvckJ1aWxkaW5nKDEpOjIzMw
scope.32.kind=method
scope.32.startLine=233
scope.32.endLine=241
scope.32.semanticHash=666677a850d29638034040de5ec2e20ab02e3d0139578ef832b2c4925c81199b
scope.33.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Nhbm5vdEV4dGVuZFBsYW4oMyk6MjA3
scope.33.kind=method
scope.33.startLine=207
scope.33.endLine=211
scope.33.semanticHash=ddc4036ee483a8adc5e302b2cf52563bc127a8a730237848ac94c72edcc6d974
scope.34.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NoZWFwZXN0QnVpbGRhYmxlU3RyZWV0KDMpOjIxOA
scope.34.kind=method
scope.34.startLine=218
scope.34.endLine=231
scope.34.semanticHash=63050f5ddccea0df7d510fdded5007b91e7cbffbfb9c50303348617695ffaa41
scope.35.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91cigwKTo5MQ
scope.35.kind=method
scope.35.startLine=91
scope.35.endLine=91
scope.35.semanticHash=61fa4ee3a95e764e4c9372fff2696b5e9e3c5aeb0dd7407567c74e28017b11cd
scope.36.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91ckdyb3VwSW5lbGlnaWJsZSgyKTo3Ng
scope.36.kind=method
scope.36.startLine=76
scope.36.endLine=79
scope.36.semanticHash=43daeaa19e539a65cb621c6584fc7509343a73cdf3f0767f07219b7c8cd10d79
scope.37.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbW1pdFRvQnVpbGQoMik6MTM0
scope.37.kind=method
scope.37.startLine=134
scope.37.endLine=137
scope.37.semanticHash=58fe1bcf3cbcefa5c9dd43eb3c3133c5aad38236343a29ba5b1c5ab39a9e00bf
scope.38.id=bWV0aG9kOkxlZ2FsRW50aXR5I2N0b3IoNSk6MzM
scope.38.kind=method
scope.38.startLine=33
scope.38.endLine=42
scope.38.semanticHash=8a95db0cf9a16ce09a4486d937bf397276b3dc155d6a81199d3fb69f8191d0e4
scope.39.id=bWV0aG9kOkxlZ2FsRW50aXR5I2RlcG9zaXRUb0JhbmsoMSk6MTE5
scope.39.kind=method
scope.39.startLine=119
scope.39.endLine=119
scope.39.semanticHash=9ea79f364cb5cd19fa125183bfc835974c88f4e0e5ea75a2448dbe9acf8d6352
scope.40.id=bWV0aG9kOkxlZ2FsRW50aXR5I2V2ZXJ5U2hhcmVob2xkZXJPd25zQVN0cmVldCgzKTo4NQ
scope.40.kind=method
scope.40.startLine=85
scope.40.endLine=88
scope.40.semanticHash=ccafcb1c2b7075297ef35f00d1b8d817de2f5ba594606694c2e32468ba2e9abd
scope.41.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm0oNik6NDQ
scope.41.kind=method
scope.41.startLine=44
scope.41.endLine=54
scope.41.semanticHash=ae939cda8741093e7492f04072f17d29a6076e98c3d12f4c3d00e6e4feb65932
scope.42.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm1lZCg0KTo1Nw
scope.42.kind=method
scope.42.startLine=57
scope.42.endLine=59
scope.42.semanticHash=02caf564807a3a12b98f61cf13fe5b91cd42832a32b5c6ec8726c5bd00f52ad9
scope.43.id=bWV0aG9kOkxlZ2FsRW50aXR5I2hhc1RocmVlRGlzdGluY3RTaGFyZWhvbGRlcnMoMSk6NjE
scope.43.kind=method
scope.43.startLine=61
scope.43.endLine=63
scope.43.semanticHash=a9e99322b63c32a1e02399009013cd9d9a74c94ce67d42a5b62cef7639db9b8d
scope.44.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVyKDApOjE0MQ
scope.44.kind=method
scope.44.startLine=141
scope.44.endLine=141
scope.44.semanticHash=3ce02856488a64e8028cf542db1562aef51faba20444f04364213b3f68a00252
scope.45.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xvYW4oMCk6MTE3
scope.45.kind=method
scope.45.startLine=117
scope.45.endLine=117
scope.45.semanticHash=5aea94847a2d312e9b1926d1160d1eba775015b671451f05a52a4d3d5d989fe4
scope.46.id=bWV0aG9kOkxlZ2FsRW50aXR5I21hcmtPcGVyYXRlZCgwKToxMjI
scope.46.kind=method
scope.46.startLine=122
scope.46.endLine=122
scope.46.semanticHash=45f1b8b1350b04e17da39d3b7caee90e3c4c619b64d10b022653fdc007a00b4a
scope.47.id=bWV0aG9kOkxlZ2FsRW50aXR5I25hbWUoMCk6OTA
scope.47.kind=method
scope.47.startLine=90
scope.47.endLine=90
scope.47.semanticHash=49add184feea67e02d8ac137f88d4c5ecd32bfddf5f28841a4ae58f4edb91125
scope.48.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMSk6MTYx
scope.48.kind=method
scope.48.startLine=161
scope.48.endLine=170
scope.48.semanticHash=b00cc7a8e415a57bc79fc0c40744c343e7e92f517059fe3ed5f9c6af716471d7
scope.49.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkKDApOjEyMQ
scope.49.kind=method
scope.49.startLine=121
scope.49.endLine=121
scope.49.semanticHash=3f1616aac94d6299300ade7b2a8c5e8e5af5f3254fc9ef247bc940342fb5a800
scope.50.id=bWV0aG9kOkxlZ2FsRW50aXR5I3BheURpdmlkZW5kKDApOjI5MQ
scope.50.kind=method
scope.50.startLine=291
scope.50.endLine=297
scope.50.semanticHash=b354efede1e8cc0b5c7e2a9a20dbb2ebb3b6cf77bb617f5c2e601baca8ace847
scope.51.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTG9hbigxKToxMjM
scope.51.kind=method
scope.51.startLine=123
scope.51.endLine=126
scope.51.semanticHash=9302e05d76ba53ee9df1ba3af855016595cb6a96e20e8679fac8236d29f96afc
scope.52.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVSZW50KDEpOjEyOA
scope.52.kind=method
scope.52.startLine=128
scope.52.endLine=128
scope.52.semanticHash=084d46aeb96ce70030969c6cd3b601b985aa6095bdacba9c1534798b3c8392c0
scope.53.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVkUmVudCgwKToxMjk
scope.53.kind=method
scope.53.startLine=129
scope.53.endLine=129
scope.53.semanticHash=42ed660456ec75ab515bcc5bde3d0dedd244534fa2cbe0292b253f16659b52fa
scope.54.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZENhcGl0YWxpemF0aW9uKDEpOjE0Mg
scope.54.kind=method
scope.54.startLine=142
scope.54.endLine=146
scope.54.semanticHash=74e49f12443432ff774257c94325a25b9d3a0dc6d6d1843aa0437df7cd5c32f6
scope.55.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZExvYW4oMSk6MTI3
scope.55.kind=method
scope.55.startLine=127
scope.55.endLine=127
scope.55.semanticHash=f9380a92dcf167189dc26571308891b41951d3798240b560abe0dd3f03436476
scope.56.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZFNoYXJlaG9sZGVyUGF5bWVudCgyKToxMzA
scope.56.kind=method
scope.56.startLine=130
scope.56.endLine=133
scope.56.semanticHash=e7698fcb7889addba31b1795a141d5881c05796205acbe0bc3484d24346f4213
scope.57.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlbW92ZVNoYXJlcygxKToxMTM
scope.57.kind=method
scope.57.startLine=113
scope.57.endLine=115
scope.57.semanticHash=6d94cd79a1e2dd7a138fead9fbc4e37edf952a97b0d68a8f5dac1c507bc164fc
scope.58.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbigxKToxNTQ
scope.58.kind=method
scope.58.startLine=154
scope.58.endLine=158
scope.58.semanticHash=bca73a22a40320d53439c46d437e577c239f2bc8676b17fc65fd38cc68cb5bd8
scope.59.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbklmQWZmb3JkYWJsZSgwKToyODE
scope.59.kind=method
scope.59.startLine=281
scope.59.endLine=289
scope.59.semanticHash=42e7b15877b2a956865b9ec2b0182e808717a93f9444bc76df3c9e3df594ed68
scope.60.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbk9yUGF5RGl2aWRlbmQoMCk6Mjc0
scope.60.kind=method
scope.60.startLine=274
scope.60.endLine=279
scope.60.semanticHash=c50c376a037fae59691e768c82e59d510e7b7d5a9da55dfd7b559615e5baf614
scope.61.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NlbGxTaGFyZSgzKToxMDU
scope.61.kind=method
scope.61.startLine=105
scope.61.endLine=111
scope.61.semanticHash=1cf45c384ea9e900be5705cc57016f800bedeb94b6c71adfda773dbd8e1f8a58
scope.62.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlT2YoMSk6OTQ
scope.62.kind=method
scope.62.startLine=94
scope.62.endLine=96
scope.62.semanticHash=baadc43f86cba3a9578a75df855a313e5f99d3141d8d76ff0cdfa0ef724c1a67
scope.63.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlVmFsdWUoMCk6OTk
scope.63.kind=method
scope.63.startLine=99
scope.63.endLine=102
scope.63.semanticHash=37a511857c9a08fa9966f8a9c1dce238b1993f87190f0efebdb76ad7f60d7494
scope.64.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyR3Jld09sZGVyKDEpOjE0Nw
scope.64.kind=method
scope.64.startLine=147
scope.64.endLine=153
scope.64.semanticHash=604340798714514aa9826968fa254d93f14aa404ef00853d15215e837a321056
scope.65.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyUGF5bWVudCgxKToxMzg
scope.65.kind=method
scope.65.startLine=138
scope.65.endLine=140
scope.65.semanticHash=9e0678ac6cffb008f1f70a4e58d8037ec501ffee7064c70004f9f52766bf3d2f
scope.66.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVycygwKTo5Mg
scope.66.kind=method
scope.66.startLine=92
scope.66.endLine=92
scope.66.semanticHash=887a0bbecd58fd1ad113f0a80f2359dbcea8e301b6ffb6dcf11c7509e796a66a
scope.67.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlc09mKDEpOjI1OQ
scope.67.kind=method
scope.67.startLine=259
scope.67.endLine=265
scope.67.semanticHash=b8e4d380e8f3a39a01883ae8093e838ec31528b1e8aabfdbb72fd1e3030b58ac
scope.68.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NwbGl0QWNyb3NzVGhyZWVEaXN0aW5jdE93bmVycygyKTo4MQ
scope.68.kind=method
scope.68.startLine=81
scope.68.endLine=83
scope.68.semanticHash=e1dc43db1d652d4ca15adb46375d2e18b50cd99ada2d53edb65d468a6ee090b2
scope.69.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHMoMCk6OTM
scope.69.kind=method
scope.69.startLine=93
scope.69.endLine=93
scope.69.semanticHash=7020ffe61f8cc9dd780c62717a353212389033396cdf981f3d88c1ac3f5a1b72
scope.70.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHNPZigyKTo3MA
scope.70.kind=method
scope.70.startLine=70
scope.70.endLine=73
scope.70.semanticHash=20dcba0a9dc440b6eaa72b374c3cdb05c172e301c9db4e6843e9b438c0854040
scope.71.id=bWV0aG9kOkxlZ2FsRW50aXR5I3RvdGFsQ29uc3RydWN0aW9uQ29zdCgxKToyMTM
scope.71.kind=method
scope.71.startLine=213
scope.71.endLine=216
scope.71.semanticHash=ee0474cafdf5134ccc1483edbc6727c23d530e28a2be9db8bdfa85f6a5a53f1c
scope.72.id=bWV0aG9kOkxlZ2FsRW50aXR5I3dpdGhkcmF3RnJvbUJhbmsoMSk6MTIw
scope.72.kind=method
scope.72.startLine=120
scope.72.endLine=120
scope.72.semanticHash=31d998ce1ee917e685e973fdf2e171d8c0c2811c90e87c145788206cb3dc679d
scope.73.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5EaXZpZGVuZFBhaWQjY3RvcigxKTozMDk
scope.73.kind=method
scope.73.startLine=1
scope.73.endLine=316
scope.73.semanticHash=30070c9f5673eef2941b4734ae0ffb0357238b739270d59129b2501fb6510524
scope.74.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ib3VzZUJ1aWx0I2N0b3IoMSk6MzAz
scope.74.kind=method
scope.74.startLine=1
scope.74.endLine=316
scope.74.semanticHash=30070c9f5673eef2941b4734ae0ffb0357238b739270d59129b2501fb6510524
scope.75.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmFpc2VkQW5kSG91c2VCdWlsdCNjdG9yKDIpOjMwNg
scope.75.kind=method
scope.75.startLine=1
scope.75.endLine=316
scope.75.semanticHash=30070c9f5673eef2941b4734ae0ffb0357238b739270d59129b2501fb6510524
scope.76.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmVwYWlkI2N0b3IoMyk6MzAw
scope.76.kind=method
scope.76.startLine=1
scope.76.endLine=316
scope.76.semanticHash=30070c9f5673eef2941b4734ae0ffb0357238b739270d59129b2501fb6510524
scope.77.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ob0FjdGlvbiNjdG9yKDApOjMxMg
scope.77.kind=method
scope.77.startLine=1
scope.77.endLine=316
scope.77.semanticHash=30070c9f5673eef2941b4734ae0ffb0357238b739270d59129b2501fb6510524
*/
