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
    List<BuildStep> steps = new java.util.ArrayList<>();
    while (bankBalance().amount() > 0) {
      Optional<BuildStep> step = attemptToBuildOneHouse(deeds);
      if (step.isEmpty()) break;
      steps.add(step.get());
    }
    if (steps.isEmpty()) return null;
    ColourStreet firstBuilt = steps.getFirst().street();
    Money loanRaised = steps.stream().map(BuildStep::loanRaised)
        .filter(it -> !it.equals(Money.ZERO)).findFirst().orElse(Money.ZERO);
    return loanRaised.equals(Money.ZERO)
        ? new Operation.HouseBuilt(firstBuilt)
        : new Operation.LoanRaisedAndHouseBuilt(loanRaised, firstBuilt);
  }

  private Optional<BuildStep> attemptToBuildOneHouse(Deeds deeds) {
    ColourStreet next = cheapestBuildableStreet(deeds);
    if (next == null) return Optional.empty();
    Money loanRaised = Money.ZERO;
    if (needsLoanToAfford(next) && canBorrowForBuilding(next)) loanRaised = borrowShortfall(next);
    if (needsLoanToAfford(next)) return Optional.empty();
    buildOneHouse(deeds, next);
    return Optional.of(new BuildStep(next, loanRaised));
  }

  private record BuildStep(ColourStreet street, Money loanRaised) {
  }

  private ColourStreet cheapestBuildableStreet(Deeds deeds) {
    return streets.stream()
        .filter(street -> deeds.housesBuiltOn(street) < street.hotelConstructionRequiresNumberOfHouses())
        .min(java.util.Comparator.comparingInt(deeds::housesBuiltOn))
        .orElse(null);
  }

  private boolean needsLoanToAfford(ColourStreet street) {
    return bankBalance().amount() < street.houseConstructionCost().amount();
  }

  private boolean canBorrowForBuilding(ColourStreet street) {
    if (!loan.equals(Money.ZERO)) return false;
    Money shortfall = street.houseConstructionCost().minus(bankBalance());
    int base = shortfall.amount() / shareholders.size();
    int remainder = shortfall.amount() % shareholders.size();
    for (int index = 0; index < shareholders.size(); index++) {
      int share = base + (index < remainder ? 1 : 0);
      if (shareholders.get(index).account().balance().amount().amount() < share) return false;
    }
    return true;
  }

  private Money borrowShortfall(ColourStreet street) {
    Money shortfall = street.houseConstructionCost().minus(bankBalance());
    recordLoan(shortfall);
    depositToBank(shortfall);
    int base = shortfall.amount() / shareholders.size();
    int remainder = shortfall.amount() % shareholders.size();
    for (int index = 0; index < shareholders.size(); index++) {
      Money share = new Money(base + (index < remainder ? 1 : 0));
      Player shareholder = shareholders.get(index);
      shareholder.account().withdraw(share);
      recordShareholderPayment(shareholder, share);
    }
    return shortfall;
  }

  private void buildOneHouse(Deeds deeds, ColourStreet street) {
    withdrawFromBank(street.houseConstructionCost());
    deeds.arrangeHouses(street, deeds.housesBuiltOn(street) + 1);
  }

  private Operation repayLoanOrPayDividend() {
    if (!loan.equals(Money.ZERO)) return repayLoanIfAffordable();
    return bankBalance().amount() >= 150 ? payDividend() : null;
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
moduleHash=91882429cc738d7a7b3e0e3efab8dc29ba7a5346c5a20ca1d88b6b0588819d49
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHkjTGVnYWxFbnRpdHk6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=227
scope.0.semanticHash=cb1add8f8e4d86e8911882f1a384f8ff34cb28400b34ed702c4e6f769903efc8
scope.1.id=Y2xhc3M6TGVnYWxFbnRpdHkuQnVpbGRTdGVwI0J1aWxkU3RlcDoxNTY
scope.1.kind=class
scope.1.startLine=156
scope.1.endLine=157
scope.1.semanticHash=9ba5c6835a9c664d6753b710a4bea2fc24d93886da6c085cf5ef771a25d51319
scope.2.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uI09wZXJhdGlvbjoyMTA
scope.2.kind=class
scope.2.startLine=210
scope.2.endLine=225
scope.2.semanticHash=5da44acac7f171f13bd3dfa275ae8d256bb3b943679f0063da5fff6afa5207e3
scope.3.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNEaXZpZGVuZFBhaWQ6MjIw
scope.3.kind=class
scope.3.startLine=220
scope.3.endLine=221
scope.3.semanticHash=0b69ccd7ea09521e3e3e9d298c96117637dca3d237c184584c4669483b2e8f03
scope.4.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjSG91c2VCdWlsdDoyMTQ
scope.4.kind=class
scope.4.startLine=214
scope.4.endLine=215
scope.4.semanticHash=dcae13bb81221b6370517b84b7a45208cd96ea547ef72005527f03adf68b013a
scope.5.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I0xvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0OjIxNw
scope.5.kind=class
scope.5.startLine=217
scope.5.endLine=218
scope.5.semanticHash=4b3efa345da2f3fa37efafd54344939903debb6d14136a8cf05631336c841b74
scope.6.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjTG9hblJlcGFpZDoyMTE
scope.6.kind=class
scope.6.startLine=211
scope.6.endLine=212
scope.6.semanticHash=41a047a961472207700654f4741eee2a240ba4cb361357fb6c137a712574c6a0
scope.7.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLk5vQWN0aW9uI05vQWN0aW9uOjIyMw
scope.7.kind=class
scope.7.startLine=223
scope.7.endLine=224
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
scope.10.id=ZmllbGQ6TGVnYWxFbnRpdHkjbG9hbjoyMw
scope.10.kind=field
scope.10.startLine=23
scope.10.endLine=23
scope.10.semanticHash=0eb11d1b549cd698514ba826ca398238645a5bc40f5232d6985d33c99420428a
scope.11.id=ZmllbGQ6TGVnYWxFbnRpdHkjbmFtZToxOQ
scope.11.kind=field
scope.11.startLine=19
scope.11.endLine=19
scope.11.semanticHash=50911222d6c01838cb594ba4fe8b2b9fe6c9ec53e268036b98aceda32dad771c
scope.12.id=ZmllbGQ6TGVnYWxFbnRpdHkjb3BlcmF0ZWQ6Mjc
scope.12.kind=field
scope.12.startLine=27
scope.12.endLine=27
scope.12.semanticHash=b3efe17a01dba6b4c344144f77ddb94b637e76bc47c0aa2853ffbcef7b22286a
scope.13.id=ZmllbGQ6TGVnYWxFbnRpdHkjcmVudFJlY2VpdmVkT246MjU
scope.13.kind=field
scope.13.startLine=25
scope.13.endLine=25
scope.13.semanticHash=5aa48ef8fc285c873817fe42af22d3c53f69ecf2ac3f9baa3a57ecda337c3407
scope.14.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJQYXltZW50czoyNg
scope.14.kind=field
scope.14.startLine=26
scope.14.endLine=26
scope.14.semanticHash=6010b82ace6eae187e7c7ee35e3a3ba7b06b99e9abf998984cbec3d957daa0f9
scope.15.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJzOjIx
scope.15.kind=field
scope.15.startLine=21
scope.15.endLine=21
scope.15.semanticHash=a7cf30c47f8e4c7c871fc45960987ec8670a446fafde737fbd72d51ea5be206f
scope.16.id=ZmllbGQ6TGVnYWxFbnRpdHkjc3RyZWV0czoyMg
scope.16.kind=field
scope.16.startLine=22
scope.16.endLine=22
scope.16.semanticHash=df36006d25c9c7f2913b137bc7e547909a0df36eafdcbcda0145384e5046758b
scope.17.id=ZmllbGQ6TGVnYWxFbnRpdHkuQnVpbGRTdGVwI2xvYW5SYWlzZWQ6MTU2
scope.17.kind=field
scope.17.startLine=156
scope.17.endLine=156
scope.17.semanticHash=bad9430ecc3fe4566a81dfc6188b907d1fa63b1e9252ae9d3a325ea735a9e985
scope.18.id=ZmllbGQ6TGVnYWxFbnRpdHkuQnVpbGRTdGVwI3N0cmVldDoxNTY
scope.18.kind=field
scope.18.startLine=156
scope.18.endLine=156
scope.18.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.19.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkRpdmlkZW5kUGFpZCNhbW91bnQ6MjIw
scope.19.kind=field
scope.19.startLine=220
scope.19.endLine=220
scope.19.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.20.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjc3RyZWV0OjIxNA
scope.20.kind=field
scope.20.startLine=214
scope.20.endLine=214
scope.20.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.21.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I2xvYW46MjE3
scope.21.kind=field
scope.21.startLine=217
scope.21.endLine=217
scope.21.semanticHash=d4d84c63caeacb4f9272dffed543044a61c8b0b5c4af3764e868090776e65294
scope.22.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRBbmRIb3VzZUJ1aWx0I3N0cmVldDoyMTc
scope.22.kind=field
scope.22.startLine=217
scope.22.endLine=217
scope.22.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.23.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcHJpbmNpcGFsOjIxMQ
scope.23.kind=field
scope.23.startLine=211
scope.23.endLine=211
scope.23.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.24.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcmVwYXltZW50OjIxMQ
scope.24.kind=field
scope.24.startLine=211
scope.24.endLine=211
scope.24.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.25.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjc2hhcmVob2xkZXI6MjEx
scope.25.kind=field
scope.25.startLine=211
scope.25.endLine=211
scope.25.semanticHash=a67773ac74374bf297c8b046f4a036b7b383f81231c7b87d05151145d4006783
scope.26.id=bWV0aG9kOkxlZ2FsRW50aXR5I2F0dGVtcHRUb0J1aWxkT25lSG91c2UoMSk6MTQ2
scope.26.kind=method
scope.26.startLine=146
scope.26.endLine=154
scope.26.semanticHash=a8c38dfa236c27333754be7338f30bdc7f3ae038bf058ffadbc66ac6232ffb50
scope.27.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JhbmtCYWxhbmNlKDApOjkz
scope.27.kind=method
scope.27.startLine=93
scope.27.endLine=93
scope.27.semanticHash=a444af7ebeadf69d34ed0f13bc8ad1f6d2d6978af13b5b0260e3763b23a60ec6
scope.28.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvYXJkRnVsbHlPd25lZCgyKTo2MQ
scope.28.kind=method
scope.28.startLine=61
scope.28.endLine=64
scope.28.semanticHash=f1356c828dc58032afd29882bdbf3bcedd40c0b501507da684fe18f9a2db8250
scope.29.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvcnJvd1Nob3J0ZmFsbCgxKToxNzQ
scope.29.kind=method
scope.29.startLine=174
scope.29.endLine=181
scope.29.semanticHash=69cc97eed5e8eaa9ee17531db9683132d3fee68f01b74bdba3ffcd27c9d2379e
scope.30.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkQXNNdWNoQXNBZmZvcmRhYmxlKDEpOjEzMA
scope.30.kind=method
scope.30.startLine=130
scope.30.endLine=144
scope.30.semanticHash=cb037113bfda5e6c90b38d22b02c04e0cf34a7743ed18fbfbef4ec23d34fbb55
scope.31.id=bWV0aG9kOkxlZ2FsRW50aXR5I2J1aWxkT25lSG91c2UoMik6MTgz
scope.31.kind=method
scope.31.startLine=183
scope.31.endLine=186
scope.31.semanticHash=05209be9fdea30cc6e5445b868727933816b907c1f4b1a7e07759e1f0dcae483
scope.32.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NhbkJvcnJvd0ZvckJ1aWxkaW5nKDApOjE3MA
scope.32.kind=method
scope.32.startLine=170
scope.32.endLine=172
scope.32.semanticHash=5d20b3722dffe1a13ee0571c1f45007ce663a925b2841bc71938178633bfa228
scope.33.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NoZWFwZXN0QnVpbGRhYmxlU3RyZWV0KDEpOjE1OQ
scope.33.kind=method
scope.33.startLine=159
scope.33.endLine=164
scope.33.semanticHash=0abd999ecbfce7014959b52830479b6692dfea0d5b3155b8022a67637198fb9a
scope.34.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91cigwKTo4Nw
scope.34.kind=method
scope.34.startLine=87
scope.34.endLine=87
scope.34.semanticHash=61fa4ee3a95e764e4c9372fff2696b5e9e3c5aeb0dd7407567c74e28017b11cd
scope.35.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91ckdyb3VwSW5lbGlnaWJsZSgyKTo3Mg
scope.35.kind=method
scope.35.startLine=72
scope.35.endLine=75
scope.35.semanticHash=43daeaa19e539a65cb621c6584fc7509343a73cdf3f0767f07219b7c8cd10d79
scope.36.id=bWV0aG9kOkxlZ2FsRW50aXR5I2N0b3IoNSk6Mjk
scope.36.kind=method
scope.36.startLine=29
scope.36.endLine=38
scope.36.semanticHash=b13b77edaf4e2b93e96ab442410afcf59868e1faee119e68c45016a60db6a2d5
scope.37.id=bWV0aG9kOkxlZ2FsRW50aXR5I2RlcG9zaXRUb0JhbmsoMSk6OTQ
scope.37.kind=method
scope.37.startLine=94
scope.37.endLine=94
scope.37.semanticHash=9ea79f364cb5cd19fa125183bfc835974c88f4e0e5ea75a2448dbe9acf8d6352
scope.38.id=bWV0aG9kOkxlZ2FsRW50aXR5I2V2ZXJ5U2hhcmVob2xkZXJPd25zQVN0cmVldCgzKTo4MQ
scope.38.kind=method
scope.38.startLine=81
scope.38.endLine=84
scope.38.semanticHash=ccafcb1c2b7075297ef35f00d1b8d817de2f5ba594606694c2e32468ba2e9abd
scope.39.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm0oNik6NDA
scope.39.kind=method
scope.39.startLine=40
scope.39.endLine=50
scope.39.semanticHash=ae939cda8741093e7492f04072f17d29a6076e98c3d12f4c3d00e6e4feb65932
scope.40.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm1lZCg0KTo1Mw
scope.40.kind=method
scope.40.startLine=53
scope.40.endLine=55
scope.40.semanticHash=02caf564807a3a12b98f61cf13fe5b91cd42832a32b5c6ec8726c5bd00f52ad9
scope.41.id=bWV0aG9kOkxlZ2FsRW50aXR5I2hhc1RocmVlRGlzdGluY3RTaGFyZWhvbGRlcnMoMSk6NTc
scope.41.kind=method
scope.41.startLine=57
scope.41.endLine=59
scope.41.semanticHash=a9e99322b63c32a1e02399009013cd9d9a74c94ce67d42a5b62cef7639db9b8d
scope.42.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xvYW4oMCk6OTI
scope.42.kind=method
scope.42.startLine=92
scope.42.endLine=92
scope.42.semanticHash=5aea94847a2d312e9b1926d1160d1eba775015b671451f05a52a4d3d5d989fe4
scope.43.id=bWV0aG9kOkxlZ2FsRW50aXR5I21hcmtPcGVyYXRlZCgwKTo5Nw
scope.43.kind=method
scope.43.startLine=97
scope.43.endLine=97
scope.43.semanticHash=45f1b8b1350b04e17da39d3b7caee90e3c4c619b64d10b022653fdc007a00b4a
scope.44.id=bWV0aG9kOkxlZ2FsRW50aXR5I25hbWUoMCk6ODY
scope.44.kind=method
scope.44.startLine=86
scope.44.endLine=86
scope.44.semanticHash=49add184feea67e02d8ac137f88d4c5ecd32bfddf5f28841a4ae58f4edb91125
scope.45.id=bWV0aG9kOkxlZ2FsRW50aXR5I25lZWRzTG9hblRvQWZmb3JkKDEpOjE2Ng
scope.45.kind=method
scope.45.startLine=166
scope.45.endLine=168
scope.45.semanticHash=4f45e5158af558bc405a3e7766f9db05026e75d8ade53169017a38b12eea1d3b
scope.46.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMSk6MTE5
scope.46.kind=method
scope.46.startLine=119
scope.46.endLine=128
scope.46.semanticHash=b00cc7a8e415a57bc79fc0c40744c343e7e92f517059fe3ed5f9c6af716471d7
scope.47.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkKDApOjk2
scope.47.kind=method
scope.47.startLine=96
scope.47.endLine=96
scope.47.semanticHash=3f1616aac94d6299300ade7b2a8c5e8e5af5f3254fc9ef247bc940342fb5a800
scope.48.id=bWV0aG9kOkxlZ2FsRW50aXR5I3BheURpdmlkZW5kKDApOjIwMw
scope.48.kind=method
scope.48.startLine=203
scope.48.endLine=208
scope.48.semanticHash=0fc895234227048a5555f82fd6f5413b74d40b0e3ecafded56624abd04be386e
scope.49.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTG9hbigxKTo5OA
scope.49.kind=method
scope.49.startLine=98
scope.49.endLine=101
scope.49.semanticHash=9302e05d76ba53ee9df1ba3af855016595cb6a96e20e8679fac8236d29f96afc
scope.50.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVSZW50KDEpOjEwMw
scope.50.kind=method
scope.50.startLine=103
scope.50.endLine=103
scope.50.semanticHash=084d46aeb96ce70030969c6cd3b601b985aa6095bdacba9c1534798b3c8392c0
scope.51.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVkUmVudCgwKToxMDQ
scope.51.kind=method
scope.51.startLine=104
scope.51.endLine=104
scope.51.semanticHash=42ed660456ec75ab515bcc5bde3d0dedd244534fa2cbe0292b253f16659b52fa
scope.52.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZExvYW4oMSk6MTAy
scope.52.kind=method
scope.52.startLine=102
scope.52.endLine=102
scope.52.semanticHash=f9380a92dcf167189dc26571308891b41951d3798240b560abe0dd3f03436476
scope.53.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZFNoYXJlaG9sZGVyUGF5bWVudCgyKToxMDU
scope.53.kind=method
scope.53.startLine=105
scope.53.endLine=108
scope.53.semanticHash=e7698fcb7889addba31b1795a141d5881c05796205acbe0bc3484d24346f4213
scope.54.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbigxKToxMTI
scope.54.kind=method
scope.54.startLine=112
scope.54.endLine=116
scope.54.semanticHash=bca73a22a40320d53439c46d437e577c239f2bc8676b17fc65fd38cc68cb5bd8
scope.55.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbklmQWZmb3JkYWJsZSgwKToxOTM
scope.55.kind=method
scope.55.startLine=193
scope.55.endLine=201
scope.55.semanticHash=42e7b15877b2a956865b9ec2b0182e808717a93f9444bc76df3c9e3df594ed68
scope.56.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbk9yUGF5RGl2aWRlbmQoMCk6MTg4
scope.56.kind=method
scope.56.startLine=188
scope.56.endLine=191
scope.56.semanticHash=e0f55d028479b5a23d6bac5d2aa2305c92b6daacb057f0cb8d6c16a1b994619e
scope.57.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlT2YoMSk6OTA
scope.57.kind=method
scope.57.startLine=90
scope.57.endLine=90
scope.57.semanticHash=25d203bbd5cdf5438bcccf38e4648753003e28426259f5ba82640f9e4b097ef6
scope.58.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyUGF5bWVudCgxKToxMDk
scope.58.kind=method
scope.58.startLine=109
scope.58.endLine=111
scope.58.semanticHash=9e0678ac6cffb008f1f70a4e58d8037ec501ffee7064c70004f9f52766bf3d2f
scope.59.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVycygwKTo4OA
scope.59.kind=method
scope.59.startLine=88
scope.59.endLine=88
scope.59.semanticHash=d9a8760d1a732b16322c7299131b79ef8db5d6738f6203053a795d21167f9b16
scope.60.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NwbGl0QWNyb3NzVGhyZWVEaXN0aW5jdE93bmVycygyKTo3Nw
scope.60.kind=method
scope.60.startLine=77
scope.60.endLine=79
scope.60.semanticHash=e1dc43db1d652d4ca15adb46375d2e18b50cd99ada2d53edb65d468a6ee090b2
scope.61.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHMoMCk6ODk
scope.61.kind=method
scope.61.startLine=89
scope.61.endLine=89
scope.61.semanticHash=7020ffe61f8cc9dd780c62717a353212389033396cdf981f3d88c1ac3f5a1b72
scope.62.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHNPZigyKTo2Ng
scope.62.kind=method
scope.62.startLine=66
scope.62.endLine=69
scope.62.semanticHash=20dcba0a9dc440b6eaa72b374c3cdb05c172e301c9db4e6843e9b438c0854040
scope.63.id=bWV0aG9kOkxlZ2FsRW50aXR5I3dpdGhkcmF3RnJvbUJhbmsoMSk6OTU
scope.63.kind=method
scope.63.startLine=95
scope.63.endLine=95
scope.63.semanticHash=31d998ce1ee917e685e973fdf2e171d8c0c2811c90e87c145788206cb3dc679d
scope.64.id=bWV0aG9kOkxlZ2FsRW50aXR5LkJ1aWxkU3RlcCNjdG9yKDIpOjE1Ng
scope.64.kind=method
scope.64.startLine=1
scope.64.endLine=227
scope.64.semanticHash=18378ab712f8e06fdcaae8c35d7ca19bf9b507f75595973a32bdf8936f585df1
scope.65.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5EaXZpZGVuZFBhaWQjY3RvcigxKToyMjA
scope.65.kind=method
scope.65.startLine=1
scope.65.endLine=227
scope.65.semanticHash=18378ab712f8e06fdcaae8c35d7ca19bf9b507f75595973a32bdf8936f585df1
scope.66.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ib3VzZUJ1aWx0I2N0b3IoMSk6MjE0
scope.66.kind=method
scope.66.startLine=1
scope.66.endLine=227
scope.66.semanticHash=18378ab712f8e06fdcaae8c35d7ca19bf9b507f75595973a32bdf8936f585df1
scope.67.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmFpc2VkQW5kSG91c2VCdWlsdCNjdG9yKDIpOjIxNw
scope.67.kind=method
scope.67.startLine=1
scope.67.endLine=227
scope.67.semanticHash=18378ab712f8e06fdcaae8c35d7ca19bf9b507f75595973a32bdf8936f585df1
scope.68.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmVwYWlkI2N0b3IoMyk6MjEx
scope.68.kind=method
scope.68.startLine=1
scope.68.endLine=227
scope.68.semanticHash=18378ab712f8e06fdcaae8c35d7ca19bf9b507f75595973a32bdf8936f585df1
scope.69.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ob0FjdGlvbiNjdG9yKDApOjIyMw
scope.69.kind=method
scope.69.startLine=1
scope.69.endLine=227
scope.69.semanticHash=18378ab712f8e06fdcaae8c35d7ca19bf9b507f75595973a32bdf8936f585df1
*/
