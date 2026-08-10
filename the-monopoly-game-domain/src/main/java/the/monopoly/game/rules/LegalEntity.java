package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.finance.Bank.Account;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.List;
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
  private Player lastCapitalizedShareholder;
  private boolean lastCapitalizedShareholderGrewOlder;
  private boolean operated;

  private LegalEntity(String name, Street.Colour colour, List<Player> shareholders,
                      List<ColourStreet> streets, Bank bank) {
    this.name = name;
    this.colour = colour;
    this.shareholders = List.copyOf(shareholders);
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
  public List<Player> shareholders() { return shareholders; }
  public List<ColourStreet> streets() { return streets; }
  public double shareOf(Player shareholder) { return shareholders.contains(shareholder) ? 1.0 / shareholders.size() : 0.0; }

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
  public Money shareholderPayment(Player shareholder) {
    return shareholderPayments.getOrDefault(shareholder.id(), Money.ZERO);
  }
  Player lastCapitalizedShareholder() { return lastCapitalizedShareholder; }
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
    boolean canReachHotels = bankBalance().equals(Money.ZERO)
        && shareholders.size() == 3
        && shareholders.stream().allMatch(it -> it.account().balance().amount().amount() <= 500)
        && loan.equals(Money.ZERO);
    while (true) {
      ColourStreet next = cheapestBuildableStreet(deeds, plan, canReachHotels);
      if (next == null) break;
      Money candidateCost = totalCost.plus(next.houseConstructionCost());
      Money shortfall = candidateCost.minus(bankBalance());
      if (shortfall.amount() > 0 && startedWithTreasuryFunds && !plan.isEmpty()) break;
      if (shortfall.amount() > 0 && !canBorrowForBuilding(shortfall)) break;
      plan.add(next);
      totalCost = candidateCost;
    }
    return plan;
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
    if (!loan.equals(Money.ZERO))
      return false;
    if (bankBalance().amount() > 0) return true;
    if (shareholders.size() != 3
        || shareholders.stream().anyMatch(it -> it.account().balance().amount().amount() > 500))
      return false;
    List<Money> shares = sharesOf(shortfall);
    return java.util.stream.IntStream.range(0, shareholders.size()).allMatch(index ->
        shareholders.get(index).account().balance().amount().amount() >= shares.get(index).amount());
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
    return shortfall;
  }

  /** Splits an amount across shareholders as evenly as possible; earlier shareholders absorb any remainder. */
  private List<Money> sharesOf(Money amount) {
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
    Money repayment = new Money(loan.amount() + loan.amount() * 5 / 100);
    if (bankBalance().amount() < repayment.amount()) return null;
    withdrawFromBank(repayment);
    Money principal = loan;
    Money paid = repayLoan(principal);
    shareholders.getFirst().account().deposit(paid);
    return new Operation.LoanRepaid(shareholders.getFirst(), principal, paid);
  }

  private Operation payDividend() {
    Money dividend = new Money(50);
    withdrawFromBank(new Money(150));
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
moduleHash=4e02a4ea6823a3d3e3b0ddf562c1361aacf37061b939579adff2eb83ce0755a5
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHkjTGVnYWxFbnRpdHk6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=264
scope.0.semanticHash=fd55c9175fd1b51be065eada41018cd202b2eb55c3b42289e3cc1235233bf281
scope.1.id=Y2xhc3M6TGVnYWxFbnRpdHkuQnVpbGRTdGVwI0J1aWxkU3RlcDoxNzE
scope.1.kind=class
scope.1.startLine=171
scope.1.endLine=172
scope.1.semanticHash=9ba5c6835a9c664d6753b710a4bea2fc24d93886da6c085cf5ef771a25d51319
scope.2.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uI09wZXJhdGlvbjoyNDc
scope.2.kind=class
scope.2.startLine=247
scope.2.endLine=262
scope.2.semanticHash=5da44acac7f171f13bd3dfa275ae8d256bb3b943679f0063da5fff6afa5207e3
scope.3.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNEaXZpZGVuZFBhaWQ6MjU3
scope.3.kind=class
scope.3.startLine=257
scope.3.endLine=258
scope.3.semanticHash=0b69ccd7ea09521e3e3e9d298c96117637dca3d237c184584c4669483b2e8f03
scope.4.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjSG91c2VCdWlsdDoyNTE
scope.4.kind=class
scope.4.startLine=251
scope.4.endLine=252
scope.4.semanticHash=dcae13bb81221b6370517b84b7a45208cd96ea547ef72005527f03adf68b013a
scope.5.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I0xvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0OjI1NA
scope.5.kind=class
scope.5.startLine=254
scope.5.endLine=255
scope.5.semanticHash=4b3efa345da2f3fa37efafd54344939903debb6d14136a8cf05631336c841b74
scope.6.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjTG9hblJlcGFpZDoyNDg
scope.6.kind=class
scope.6.startLine=248
scope.6.endLine=249
scope.6.semanticHash=41a047a961472207700654f4741eee2a240ba4cb361357fb6c137a712574c6a0
scope.7.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLk5vQWN0aW9uI05vQWN0aW9uOjI2MA
scope.7.kind=class
scope.7.startLine=260
scope.7.endLine=261
scope.7.semanticHash=8be20b41827c09498e59853af7a4dd560b2d4810feef6997ec5ba90608a1c494
scope.8.id=ZmllbGQ6TGVnYWxFbnRpdHkjYmFua0FjY291bnQ6MjQ
scope.8.kind=field
scope.8.startLine=24
scope.8.endLine=24
scope.8.semanticHash=7bbf3a82e28f3c6efcdc54e30a5e45ff2d7aca1ff947b2ae29242496960ca65d
scope.9.id=ZmllbGQ6TGVnYWxFbnRpdHkjY29sb3VyOjIw
scope.9.kind=field
scope.9.startLine=20
scope.9.endLine=20
scope.9.semanticHash=8bcc7ad2b0ce320016118422cec6012345e08fddb913b4b1f638adfde08910c7
scope.10.id=ZmllbGQ6TGVnYWxFbnRpdHkjbGFzdENhcGl0YWxpemVkU2hhcmVob2xkZXI6Mjc
scope.10.kind=field
scope.10.startLine=27
scope.10.endLine=27
scope.10.semanticHash=f2fe3954b516a50ada49af2785268d16be5e45b7593829918647cfdcd4cd251b
scope.11.id=ZmllbGQ6TGVnYWxFbnRpdHkjbGFzdENhcGl0YWxpemVkU2hhcmVob2xkZXJHcmV3T2xkZXI6Mjg
scope.11.kind=field
scope.11.startLine=28
scope.11.endLine=28
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
scope.14.id=ZmllbGQ6TGVnYWxFbnRpdHkjb3BlcmF0ZWQ6Mjk
scope.14.kind=field
scope.14.startLine=29
scope.14.endLine=29
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
scope.19.id=ZmllbGQ6TGVnYWxFbnRpdHkuQnVpbGRTdGVwI2xvYW5SYWlzZWQ6MTcx
scope.19.kind=field
scope.19.startLine=171
scope.19.endLine=171
scope.19.semanticHash=bad9430ecc3fe4566a81dfc6188b907d1fa63b1e9252ae9d3a325ea735a9e985
scope.20.id=ZmllbGQ6TGVnYWxFbnRpdHkuQnVpbGRTdGVwI3N0cmVldDoxNzE
scope.20.kind=field
scope.20.startLine=171
scope.20.endLine=171
scope.20.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.21.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNhbW91bnQ6MjU3
scope.21.kind=field
scope.21.startLine=257
scope.21.endLine=257
scope.21.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.22.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjc3RyZWV0OjI1MQ
scope.22.kind=field
scope.22.startLine=251
scope.22.endLine=251
scope.22.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.23.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I2xvYW46MjU0
scope.23.kind=field
scope.23.startLine=254
scope.23.endLine=254
scope.23.semanticHash=d4d84c63caeacb4f9272dffed543044a61c8b0b5c4af3764e868090776e65294
scope.24.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I3N0cmVldDoyNTQ
scope.24.kind=field
scope.24.startLine=254
scope.24.endLine=254
scope.24.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.25.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcHJpbmNpcGFsOjI0OA
scope.25.kind=field
scope.25.startLine=248
scope.25.endLine=248
scope.25.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.26.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcmVwYXltZW50OjI0OA
scope.26.kind=field
scope.26.startLine=248
scope.26.endLine=248
scope.26.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.27.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjc2hhcmVob2xkZXI6MjQ4
scope.27.kind=field
scope.27.startLine=248
scope.27.endLine=248
scope.27.semanticHash=a67773ac74374bf297c8b046f4a036b7b383f81231c7b87d05151145d4006783
scope.28.id=bWV0aG9kOkxlZ2FsRW50aXR5I2F0dGVtcHRUb0J1aWxkT25lSG91c2UoMSk6MTYx
scope.28.kind=method
scope.28.startLine=161
scope.28.endLine=169
scope.28.semanticHash=be4a497020a0f211900f830454f5496ccce41cfd20cd3a2d83cd98aa55b35d31
scope.29.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JhbmtCYWxhbmNlKDApOjk1
scope.29.kind=method
scope.29.startLine=95
scope.29.endLine=95
scope.29.semanticHash=a444af7ebeadf69d34ed0f13bc8ad1f6d2d6978af13b5b0260e3763b23a60ec6
scope.30.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvYXJkRnVsbHlPd25lZCgyKTo2Mw
scope.30.kind=method
scope.30.startLine=63
scope.30.endLine=66
scope.30.semanticHash=f1356c828dc58032afd29882bdbf3bcedd40c0b501507da684fe18f9a2db8250
scope.31.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvcnJvd1Nob3J0ZmFsbCgxKToxOTM
scope.31.kind=method
scope.31.startLine=193
scope.31.endLine=206
scope.31.semanticHash=d0fd85bcc527808280275cad1890a0232ebc6fa4baf63e543cac3542f825abd8
scope.32.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkQXNNdWNoQXNBZmZvcmRhYmxlKDEpOjE0NQ
scope.32.kind=method
scope.32.startLine=145
scope.32.endLine=159
scope.32.semanticHash=cb037113bfda5e6c90b38d22b02c04e0cf34a7743ed18fbfbef4ec23d34fbb55
scope.33.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkT25lSG91c2UoMik6MjE3
scope.33.kind=method
scope.33.startLine=217
scope.33.endLine=220
scope.33.semanticHash=05209be9fdea30cc6e5445b868727933816b907c1f4b1a7e07759e1f0dcae483
scope.34.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NhbkJvcnJvd0ZvckJ1aWxkaW5nKDEpOjE4NQ
scope.34.kind=method
scope.34.startLine=185
scope.34.endLine=191
scope.34.semanticHash=d5a7ef11504913d482264d14065267bf56d3d71db3fc2e77c7c65346e370f37c
scope.35.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NoZWFwZXN0QnVpbGRhYmxlU3RyZWV0KDEpOjE3NA
scope.35.kind=method
scope.35.startLine=174
scope.35.endLine=179
scope.35.semanticHash=0abd999ecbfce7014959b52830479b6692dfea0d5b3155b8022a67637198fb9a
scope.36.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91cigwKTo4OQ
scope.36.kind=method
scope.36.startLine=89
scope.36.endLine=89
scope.36.semanticHash=61fa4ee3a95e764e4c9372fff2696b5e9e3c5aeb0dd7407567c74e28017b11cd
scope.37.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91ckdyb3VwSW5lbGlnaWJsZSgyKTo3NA
scope.37.kind=method
scope.37.startLine=74
scope.37.endLine=77
scope.37.semanticHash=43daeaa19e539a65cb621c6584fc7509343a73cdf3f0767f07219b7c8cd10d79
scope.38.id=bWV0aG9kOkxlZ2FsRW50aXR5I2N0b3IoNSk6MzE
scope.38.kind=method
scope.38.startLine=31
scope.38.endLine=40
scope.38.semanticHash=b13b77edaf4e2b93e96ab442410afcf59868e1faee119e68c45016a60db6a2d5
scope.39.id=bWV0aG9kOkxlZ2FsRW50aXR5I2RlcG9zaXRUb0JhbmsoMSk6OTY
scope.39.kind=method
scope.39.startLine=96
scope.39.endLine=96
scope.39.semanticHash=9ea79f364cb5cd19fa125183bfc835974c88f4e0e5ea75a2448dbe9acf8d6352
scope.40.id=bWV0aG9kOkxlZ2FsRW50aXR5I2V2ZXJ5U2hhcmVob2xkZXJPd25zQVN0cmVldCgzKTo4Mw
scope.40.kind=method
scope.40.startLine=83
scope.40.endLine=86
scope.40.semanticHash=ccafcb1c2b7075297ef35f00d1b8d817de2f5ba594606694c2e32468ba2e9abd
scope.41.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm0oNik6NDI
scope.41.kind=method
scope.41.startLine=42
scope.41.endLine=52
scope.41.semanticHash=ae939cda8741093e7492f04072f17d29a6076e98c3d12f4c3d00e6e4feb65932
scope.42.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm1lZCg0KTo1NQ
scope.42.kind=method
scope.42.startLine=55
scope.42.endLine=57
scope.42.semanticHash=02caf564807a3a12b98f61cf13fe5b91cd42832a32b5c6ec8726c5bd00f52ad9
scope.43.id=bWV0aG9kOkxlZ2FsRW50aXR5I2hhc1RocmVlRGlzdGluY3RTaGFyZWhvbGRlcnMoMSk6NTk
scope.43.kind=method
scope.43.startLine=59
scope.43.endLine=61
scope.43.semanticHash=a9e99322b63c32a1e02399009013cd9d9a74c94ce67d42a5b62cef7639db9b8d
scope.44.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xhc3RDYXBpdGFsaXplZFNoYXJlaG9sZGVyKDApOjExNA
scope.44.kind=method
scope.44.startLine=114
scope.44.endLine=114
scope.44.semanticHash=c5a7fba8a018ebcef2d60a3a88d253e1a7dbc2b9262199f54167053f70fd3f3b
scope.45.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xvYW4oMCk6OTQ
scope.45.kind=method
scope.45.startLine=94
scope.45.endLine=94
scope.45.semanticHash=5aea94847a2d312e9b1926d1160d1eba775015b671451f05a52a4d3d5d989fe4
scope.46.id=bWV0aG9kOkxlZ2FsRW50aXR5I21hcmtPcGVyYXRlZCgwKTo5OQ
scope.46.kind=method
scope.46.startLine=99
scope.46.endLine=99
scope.46.semanticHash=45f1b8b1350b04e17da39d3b7caee90e3c4c619b64d10b022653fdc007a00b4a
scope.47.id=bWV0aG9kOkxlZ2FsRW50aXR5I25hbWUoMCk6ODg
scope.47.kind=method
scope.47.startLine=88
scope.47.endLine=88
scope.47.semanticHash=49add184feea67e02d8ac137f88d4c5ecd32bfddf5f28841a4ae58f4edb91125
scope.48.id=bWV0aG9kOkxlZ2FsRW50aXR5I25lZWRzTG9hblRvQWZmb3JkKDEpOjE4MQ
scope.48.kind=method
scope.48.startLine=181
scope.48.endLine=183
scope.48.semanticHash=4f45e5158af558bc405a3e7766f9db05026e75d8ade53169017a38b12eea1d3b
scope.49.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMSk6MTM0
scope.49.kind=method
scope.49.startLine=134
scope.49.endLine=143
scope.49.semanticHash=b00cc7a8e415a57bc79fc0c40744c343e7e92f517059fe3ed5f9c6af716471d7
scope.50.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkKDApOjk4
scope.50.kind=method
scope.50.startLine=98
scope.50.endLine=98
scope.50.semanticHash=3f1616aac94d6299300ade7b2a8c5e8e5af5f3254fc9ef247bc940342fb5a800
scope.51.id=bWV0aG9kOkxlZ2FsRW50aXR5I3BheURpdmlkZW5kKDApOjIzOQ
scope.51.kind=method
scope.51.startLine=239
scope.51.endLine=245
scope.51.semanticHash=5746e31a1e3e2ad10dd2d51781c6caddcbbc5f18a441db8048138f27f599ede0
scope.52.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTG9hbigxKToxMDA
scope.52.kind=method
scope.52.startLine=100
scope.52.endLine=103
scope.52.semanticHash=9302e05d76ba53ee9df1ba3af855016595cb6a96e20e8679fac8236d29f96afc
scope.53.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVSZW50KDEpOjEwNQ
scope.53.kind=method
scope.53.startLine=105
scope.53.endLine=105
scope.53.semanticHash=084d46aeb96ce70030969c6cd3b601b985aa6095bdacba9c1534798b3c8392c0
scope.54.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVkUmVudCgwKToxMDY
scope.54.kind=method
scope.54.startLine=106
scope.54.endLine=106
scope.54.semanticHash=42ed660456ec75ab515bcc5bde3d0dedd244534fa2cbe0292b253f16659b52fa
scope.55.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZENhcGl0YWxpemF0aW9uKDEpOjExNQ
scope.55.kind=method
scope.55.startLine=115
scope.55.endLine=119
scope.55.semanticHash=74e49f12443432ff774257c94325a25b9d3a0dc6d6d1843aa0437df7cd5c32f6
scope.56.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZExvYW4oMSk6MTA0
scope.56.kind=method
scope.56.startLine=104
scope.56.endLine=104
scope.56.semanticHash=f9380a92dcf167189dc26571308891b41951d3798240b560abe0dd3f03436476
scope.57.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZFNoYXJlaG9sZGVyUGF5bWVudCgyKToxMDc
scope.57.kind=method
scope.57.startLine=107
scope.57.endLine=110
scope.57.semanticHash=e7698fcb7889addba31b1795a141d5881c05796205acbe0bc3484d24346f4213
scope.58.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbigxKToxMjc
scope.58.kind=method
scope.58.startLine=127
scope.58.endLine=131
scope.58.semanticHash=bca73a22a40320d53439c46d437e577c239f2bc8676b17fc65fd38cc68cb5bd8
scope.59.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbklmQWZmb3JkYWJsZSgwKToyMjk
scope.59.kind=method
scope.59.startLine=229
scope.59.endLine=237
scope.59.semanticHash=42e7b15877b2a956865b9ec2b0182e808717a93f9444bc76df3c9e3df594ed68
scope.60.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbk9yUGF5RGl2aWRlbmQoMCk6MjIy
scope.60.kind=method
scope.60.startLine=222
scope.60.endLine=227
scope.60.semanticHash=c50c376a037fae59691e768c82e59d510e7b7d5a9da55dfd7b559615e5baf614
scope.61.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlT2YoMSk6OTI
scope.61.kind=method
scope.61.startLine=92
scope.61.endLine=92
scope.61.semanticHash=25d203bbd5cdf5438bcccf38e4648753003e28426259f5ba82640f9e4b097ef6
scope.62.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyR3Jld09sZGVyKDEpOjEyMA
scope.62.kind=method
scope.62.startLine=120
scope.62.endLine=126
scope.62.semanticHash=604340798714514aa9826968fa254d93f14aa404ef00853d15215e837a321056
scope.63.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyUGF5bWVudCgxKToxMTE
scope.63.kind=method
scope.63.startLine=111
scope.63.endLine=113
scope.63.semanticHash=9e0678ac6cffb008f1f70a4e58d8037ec501ffee7064c70004f9f52766bf3d2f
scope.64.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVycygwKTo5MA
scope.64.kind=method
scope.64.startLine=90
scope.64.endLine=90
scope.64.semanticHash=d9a8760d1a732b16322c7299131b79ef8db5d6738f6203053a795d21167f9b16
scope.65.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlc09mKDEpOjIwOQ
scope.65.kind=method
scope.65.startLine=209
scope.65.endLine=215
scope.65.semanticHash=b8e4d380e8f3a39a01883ae8093e838ec31528b1e8aabfdbb72fd1e3030b58ac
scope.66.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NwbGl0QWNyb3NzVGhyZWVEaXN0aW5jdE93bmVycygyKTo3OQ
scope.66.kind=method
scope.66.startLine=79
scope.66.endLine=81
scope.66.semanticHash=e1dc43db1d652d4ca15adb46375d2e18b50cd99ada2d53edb65d468a6ee090b2
scope.67.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHMoMCk6OTE
scope.67.kind=method
scope.67.startLine=91
scope.67.endLine=91
scope.67.semanticHash=7020ffe61f8cc9dd780c62717a353212389033396cdf981f3d88c1ac3f5a1b72
scope.68.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHNPZigyKTo2OA
scope.68.kind=method
scope.68.startLine=68
scope.68.endLine=71
scope.68.semanticHash=20dcba0a9dc440b6eaa72b374c3cdb05c172e301c9db4e6843e9b438c0854040
scope.69.id=bWV0aG9kOkxlZ2FsRW50aXR5I3dpdGhkcmF3RnJvbUJhbmsoMSk6OTc
scope.69.kind=method
scope.69.startLine=97
scope.69.endLine=97
scope.69.semanticHash=31d998ce1ee917e685e973fdf2e171d8c0c2811c90e87c145788206cb3dc679d
scope.70.id=bWV0aG9kOkxlZ2FsRW50aXR5LkJ1aWxkU3RlcCNjdG9yKDIpOjE3MQ
scope.70.kind=method
scope.70.startLine=1
scope.70.endLine=264
scope.70.semanticHash=f031bcb9e1dc96b69071c03c660c7ffdabb680cbde286c599dfcc88559c049a2
scope.71.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5EaXZpZGVuZFBhaWQjY3RvcigxKToyNTc
scope.71.kind=method
scope.71.startLine=1
scope.71.endLine=264
scope.71.semanticHash=f031bcb9e1dc96b69071c03c660c7ffdabb680cbde286c599dfcc88559c049a2
scope.72.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ib3VzZUJ1aWx0I2N0b3IoMSk6MjUx
scope.72.kind=method
scope.72.startLine=1
scope.72.endLine=264
scope.72.semanticHash=f031bcb9e1dc96b69071c03c660c7ffdabb680cbde286c599dfcc88559c049a2
scope.73.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmFpc2VkQW5kSG91c2VCdWlsdCNjdG9yKDIpOjI1NA
scope.73.kind=method
scope.73.startLine=1
scope.73.endLine=264
scope.73.semanticHash=f031bcb9e1dc96b69071c03c660c7ffdabb680cbde286c599dfcc88559c049a2
scope.74.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmVwYWlkI2N0b3IoMyk6MjQ4
scope.74.kind=method
scope.74.startLine=1
scope.74.endLine=264
scope.74.semanticHash=f031bcb9e1dc96b69071c03c660c7ffdabb680cbde286c599dfcc88559c049a2
scope.75.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ob0FjdGlvbiNjdG9yKDApOjI2MA
scope.75.kind=method
scope.75.startLine=1
scope.75.endLine=264
scope.75.semanticHash=f031bcb9e1dc96b69071c03c660c7ffdabb680cbde286c599dfcc88559c049a2
*/
