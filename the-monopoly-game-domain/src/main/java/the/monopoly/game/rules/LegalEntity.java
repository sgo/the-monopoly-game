package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
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
  private ColourStreet rentReceivedOn;
  private final Map<Player.ID, Money> shareholderPayments = new HashMap<>();
  private boolean operated;

  private LegalEntity(String name, Street.Colour colour, List<Player> shareholders,
                      List<ColourStreet> streets) {
    this.name = name;
    this.colour = colour;
    this.shareholders = List.copyOf(shareholders);
    this.streets = List.copyOf(streets);
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
    return Optional.of(new LegalEntity(name, colour, shareholders, streets));
  }

  /** Creates an entity from already-set-up scenario state. */
  public static LegalEntity formed(String name, Street.Colour colour, List<Player> shareholders, Rule.Set rules) {
    return new LegalEntity(name, colour, shareholders, streetsOf(colour, rules));
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
  public boolean operated() { return operated; }
  public void markOperated() { operated = true; }
  public void raiseLoan(Money amount) { loan = loan.plus(amount); }
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

  /** Raises a starting loan with a dividend, or repays an outstanding one at five percent interest. */
  public Operation operate() {
    Operation operation = loan.equals(Money.ZERO)
        ? new Operation.LoanRaisedWithDividend(raiseNewLoan(), new Money(50))
        : new Operation.LoanRepaid(shareholders.getFirst(), loan, repayLoan(loan));
    markOperated();
    return operation;
  }

  /** Applies the entity's end-of-turn priority: reinvest rent before debt service. */
  public Operation operate(Deeds deeds) {
    if (rentReceivedOn != null && deeds.housesBuiltOn(rentReceivedOn) == 0) {
      ColourStreet street = rentReceivedOn;
      deeds.arrangeHouses(street, 1);
      rentReceivedOn = null;
      markOperated();
      return new Operation.HouseBuilt(street);
    }
    return operate();
  }

  private Money raiseNewLoan() {
    Money amount = new Money(150);
    raiseLoan(amount);
    return amount;
  }

  public sealed interface Operation {
    record LoanRaisedWithDividend(Money loan, Money dividend) implements Operation {
    }

    record LoanRepaid(Player shareholder, Money principal, Money repayment) implements Operation {
    }

    record HouseBuilt(ColourStreet street) implements Operation {
    }
  }

}

/* mutate4java-manifest
version=1
moduleHash=cddcf74464bd72d1b35e543dfe750abefea6b91dd7dcaa12b9ecf99ece8d2232
scope.0.id=Y2xhc3M6TGVnYWxFbnRpdHkjTGVnYWxFbnRpdHk6MTY
scope.0.kind=class
scope.0.startLine=16
scope.0.endLine=143
scope.0.semanticHash=e008b605f117dc554b08bcf85c27d49c2e1f2f058b168f531cdef439b0a1527f
scope.1.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uI09wZXJhdGlvbjoxMzI
scope.1.kind=class
scope.1.startLine=132
scope.1.endLine=141
scope.1.semanticHash=d27dc137c0d505630dd7bf675707453c164ee3925b270fa353eb24945ce068b8
scope.2.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjSG91c2VCdWlsdDoxMzk
scope.2.kind=class
scope.2.startLine=139
scope.2.endLine=140
scope.2.semanticHash=dcae13bb81221b6370517b84b7a45208cd96ea547ef72005527f03adf68b013a
scope.3.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRXaXRoRGl2aWRlbmQjTG9hblJhaXNlZFdpdGhEaXZpZGVuZDoxMzM
scope.3.kind=class
scope.3.startLine=133
scope.3.endLine=134
scope.3.semanticHash=e09ee627be645f8176867fc8f6038cbdf2c441a95eae22731b0a92fb4486cd0b
scope.4.id=Y2xhc3M6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjTG9hblJlcGFpZDoxMzY
scope.4.kind=class
scope.4.startLine=136
scope.4.endLine=137
scope.4.semanticHash=41a047a961472207700654f4741eee2a240ba4cb361357fb6c137a712574c6a0
scope.5.id=ZmllbGQ6TGVnYWxFbnRpdHkjY29sb3VyOjE4
scope.5.kind=field
scope.5.startLine=18
scope.5.endLine=18
scope.5.semanticHash=8bcc7ad2b0ce320016118422cec6012345e08fddb913b4b1f638adfde08910c7
scope.6.id=ZmllbGQ6TGVnYWxFbnRpdHkjbG9hbjoyMQ
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=0eb11d1b549cd698514ba826ca398238645a5bc40f5232d6985d33c99420428a
scope.7.id=ZmllbGQ6TGVnYWxFbnRpdHkjbmFtZToxNw
scope.7.kind=field
scope.7.startLine=17
scope.7.endLine=17
scope.7.semanticHash=50911222d6c01838cb594ba4fe8b2b9fe6c9ec53e268036b98aceda32dad771c
scope.8.id=ZmllbGQ6TGVnYWxFbnRpdHkjb3BlcmF0ZWQ6MjQ
scope.8.kind=field
scope.8.startLine=24
scope.8.endLine=24
scope.8.semanticHash=b3efe17a01dba6b4c344144f77ddb94b637e76bc47c0aa2853ffbcef7b22286a
scope.9.id=ZmllbGQ6TGVnYWxFbnRpdHkjcmVudFJlY2VpdmVkT246MjI
scope.9.kind=field
scope.9.startLine=22
scope.9.endLine=22
scope.9.semanticHash=5aa48ef8fc285c873817fe42af22d3c53f69ecf2ac3f9baa3a57ecda337c3407
scope.10.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJQYXltZW50czoyMw
scope.10.kind=field
scope.10.startLine=23
scope.10.endLine=23
scope.10.semanticHash=6010b82ace6eae187e7c7ee35e3a3ba7b06b99e9abf998984cbec3d957daa0f9
scope.11.id=ZmllbGQ6TGVnYWxFbnRpdHkjc2hhcmVob2xkZXJzOjE5
scope.11.kind=field
scope.11.startLine=19
scope.11.endLine=19
scope.11.semanticHash=a7cf30c47f8e4c7c871fc45960987ec8670a446fafde737fbd72d51ea5be206f
scope.12.id=ZmllbGQ6TGVnYWxFbnRpdHkjc3RyZWV0czoyMA
scope.12.kind=field
scope.12.startLine=20
scope.12.endLine=20
scope.12.semanticHash=df36006d25c9c7f2913b137bc7e547909a0df36eafdcbcda0145384e5046758b
scope.13.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkhvdXNlQnVpbHQjc3RyZWV0OjEzOQ
scope.13.kind=field
scope.13.startLine=139
scope.13.endLine=139
scope.13.semanticHash=4e13845f444644da235928e888bcae225da431d392640575f8bc1c3098178c91
scope.14.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRXaXRoRGl2aWRlbmQjZGl2aWRlbmQ6MTMz
scope.14.kind=field
scope.14.startLine=133
scope.14.endLine=133
scope.14.semanticHash=bb6e6fa0a6f2c09380d135a94d61b56dacf33d60089242a442e8d7f2a2314cae
scope.15.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SYWlzZWRXaXRoRGl2aWRlbmQjbG9hbjoxMzM
scope.15.kind=field
scope.15.startLine=133
scope.15.endLine=133
scope.15.semanticHash=d4d84c63caeacb4f9272dffed543044a61c8b0b5c4af3764e868090776e65294
scope.16.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcHJpbmNpcGFsOjEzNg
scope.16.kind=field
scope.16.startLine=136
scope.16.endLine=136
scope.16.semanticHash=42011677e9987cd0425cb9f024ccc825029d866940759c240e79ead8157727d4
scope.17.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjcmVwYXltZW50OjEzNg
scope.17.kind=field
scope.17.startLine=136
scope.17.endLine=136
scope.17.semanticHash=ee2c17b270c575911db7bf1b03966485dc745102a918f7837b6484b1a669a30c
scope.18.id=ZmllbGQ6TGVnYWxFbnRpdHkuT3BlcmF0aW9uLkxvYW5SZXBhaWQjc2hhcmVob2xkZXI6MTM2
scope.18.kind=field
scope.18.startLine=136
scope.18.endLine=136
scope.18.semanticHash=a67773ac74374bf297c8b046f4a036b7b383f81231c7b87d05151145d4006783
scope.19.id=bWV0aG9kOkxlZ2FsRW50aXR5I2JvYXJkRnVsbHlPd25lZCgyKTo1NQ
scope.19.kind=method
scope.19.startLine=55
scope.19.endLine=58
scope.19.semanticHash=f1356c828dc58032afd29882bdbf3bcedd40c0b501507da684fe18f9a2db8250
scope.20.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91cigwKTo4MQ
scope.20.kind=method
scope.20.startLine=81
scope.20.endLine=81
scope.20.semanticHash=61fa4ee3a95e764e4c9372fff2696b5e9e3c5aeb0dd7407567c74e28017b11cd
scope.21.id=bWV0aG9kOkxlZ2FsRW50aXR5I2NvbG91ckdyb3VwSW5lbGlnaWJsZSgyKTo2Ng
scope.21.kind=method
scope.21.startLine=66
scope.21.endLine=69
scope.21.semanticHash=43daeaa19e539a65cb621c6584fc7509343a73cdf3f0767f07219b7c8cd10d79
scope.22.id=bWV0aG9kOkxlZ2FsRW50aXR5I2N0b3IoNCk6MjY
scope.22.kind=method
scope.22.startLine=26
scope.22.endLine=32
scope.22.semanticHash=35048dc481de5360c107a2bc6377b6db401f1904ae3994de74bff3b886a32d0e
scope.23.id=bWV0aG9kOkxlZ2FsRW50aXR5I2V2ZXJ5U2hhcmVob2xkZXJPd25zQVN0cmVldCgzKTo3NQ
scope.23.kind=method
scope.23.startLine=75
scope.23.endLine=78
scope.23.semanticHash=ccafcb1c2b7075297ef35f00d1b8d817de2f5ba594606694c2e32468ba2e9abd
scope.24.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm0oNik6MzQ
scope.24.kind=method
scope.24.startLine=34
scope.24.endLine=44
scope.24.semanticHash=e8224a43ac44815301d50b136c395fc12ba4a71d95e2e648817bbe225fa11905
scope.25.id=bWV0aG9kOkxlZ2FsRW50aXR5I2Zvcm1lZCg0KTo0Nw
scope.25.kind=method
scope.25.startLine=47
scope.25.endLine=49
scope.25.semanticHash=722994165ca1975fd0ca6020858158760ad30d52aeda8ec5aeb126eb40408292
scope.26.id=bWV0aG9kOkxlZ2FsRW50aXR5I2hhc1RocmVlRGlzdGluY3RTaGFyZWhvbGRlcnMoMSk6NTE
scope.26.kind=method
scope.26.startLine=51
scope.26.endLine=53
scope.26.semanticHash=a9e99322b63c32a1e02399009013cd9d9a74c94ce67d42a5b62cef7639db9b8d
scope.27.id=bWV0aG9kOkxlZ2FsRW50aXR5I2xvYW4oMCk6ODY
scope.27.kind=method
scope.27.startLine=86
scope.27.endLine=86
scope.27.semanticHash=5aea94847a2d312e9b1926d1160d1eba775015b671451f05a52a4d3d5d989fe4
scope.28.id=bWV0aG9kOkxlZ2FsRW50aXR5I21hcmtPcGVyYXRlZCgwKTo4OA
scope.28.kind=method
scope.28.startLine=88
scope.28.endLine=88
scope.28.semanticHash=45f1b8b1350b04e17da39d3b7caee90e3c4c619b64d10b022653fdc007a00b4a
scope.29.id=bWV0aG9kOkxlZ2FsRW50aXR5I25hbWUoMCk6ODA
scope.29.kind=method
scope.29.startLine=80
scope.29.endLine=80
scope.29.semanticHash=49add184feea67e02d8ac137f88d4c5ecd32bfddf5f28841a4ae58f4edb91125
scope.30.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMCk6MTA2
scope.30.kind=method
scope.30.startLine=106
scope.30.endLine=112
scope.30.semanticHash=d4132651b4d676ea3453d1dde8e0c36363298e0b08eccc4b91664bc1d4f409d4
scope.31.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGUoMSk6MTE1
scope.31.kind=method
scope.31.startLine=115
scope.31.endLine=124
scope.31.semanticHash=59651822388fd72d3bc32e6bd07a8d78e0b5aecccbab8d71cd88ede36fdf42f0
scope.32.id=bWV0aG9kOkxlZ2FsRW50aXR5I29wZXJhdGVkKDApOjg3
scope.32.kind=method
scope.32.startLine=87
scope.32.endLine=87
scope.32.semanticHash=3f1616aac94d6299300ade7b2a8c5e8e5af5f3254fc9ef247bc940342fb5a800
scope.33.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTG9hbigxKTo4OQ
scope.33.kind=method
scope.33.startLine=89
scope.33.endLine=89
scope.33.semanticHash=300b5362efb1dbf15c7dcd294341c48aeb0c2adba1b77cd1bdc0f2b42e67d1b9
scope.34.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JhaXNlTmV3TG9hbigwKToxMjY
scope.34.kind=method
scope.34.startLine=126
scope.34.endLine=130
scope.34.semanticHash=453ae70ab79be382a4de741ac9bd3afe2ae40601ac0a1f8c8c159175304aa550
scope.35.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVSZW50KDEpOjkw
scope.35.kind=method
scope.35.startLine=90
scope.35.endLine=90
scope.35.semanticHash=084d46aeb96ce70030969c6cd3b601b985aa6095bdacba9c1534798b3c8392c0
scope.36.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY2VpdmVkUmVudCgwKTo5MQ
scope.36.kind=method
scope.36.startLine=91
scope.36.endLine=91
scope.36.semanticHash=42ed660456ec75ab515bcc5bde3d0dedd244534fa2cbe0292b253f16659b52fa
scope.37.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlY29yZFNoYXJlaG9sZGVyUGF5bWVudCgyKTo5Mg
scope.37.kind=method
scope.37.startLine=92
scope.37.endLine=95
scope.37.semanticHash=e7698fcb7889addba31b1795a141d5881c05796205acbe0bc3484d24346f4213
scope.38.id=bWV0aG9kOkxlZ2FsRW50aXR5I3JlcGF5TG9hbigxKTo5OQ
scope.38.kind=method
scope.38.startLine=99
scope.38.endLine=103
scope.38.semanticHash=bca73a22a40320d53439c46d437e577c239f2bc8676b17fc65fd38cc68cb5bd8
scope.39.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlT2YoMSk6ODQ
scope.39.kind=method
scope.39.startLine=84
scope.39.endLine=84
scope.39.semanticHash=25d203bbd5cdf5438bcccf38e4648753003e28426259f5ba82640f9e4b097ef6
scope.40.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVyUGF5bWVudCgxKTo5Ng
scope.40.kind=method
scope.40.startLine=96
scope.40.endLine=98
scope.40.semanticHash=9e0678ac6cffb008f1f70a4e58d8037ec501ffee7064c70004f9f52766bf3d2f
scope.41.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NoYXJlaG9sZGVycygwKTo4Mg
scope.41.kind=method
scope.41.startLine=82
scope.41.endLine=82
scope.41.semanticHash=d9a8760d1a732b16322c7299131b79ef8db5d6738f6203053a795d21167f9b16
scope.42.id=bWV0aG9kOkxlZ2FsRW50aXR5I3NwbGl0QWNyb3NzVGhyZWVEaXN0aW5jdE93bmVycygyKTo3MQ
scope.42.kind=method
scope.42.startLine=71
scope.42.endLine=73
scope.42.semanticHash=e1dc43db1d652d4ca15adb46375d2e18b50cd99ada2d53edb65d468a6ee090b2
scope.43.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHMoMCk6ODM
scope.43.kind=method
scope.43.startLine=83
scope.43.endLine=83
scope.43.semanticHash=7020ffe61f8cc9dd780c62717a353212389033396cdf981f3d88c1ac3f5a1b72
scope.44.id=bWV0aG9kOkxlZ2FsRW50aXR5I3N0cmVldHNPZigyKTo2MA
scope.44.kind=method
scope.44.startLine=60
scope.44.endLine=63
scope.44.semanticHash=20dcba0a9dc440b6eaa72b374c3cdb05c172e301c9db4e6843e9b438c0854040
scope.45.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Ib3VzZUJ1aWx0I2N0b3IoMSk6MTM5
scope.45.kind=method
scope.45.startLine=1
scope.45.endLine=143
scope.45.semanticHash=aa9159526d1909c576f08247e50a4e14f9eb970ace7aa4f77477f4c0c916839e
scope.46.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmFpc2VkV2l0aERpdmlkZW5kI2N0b3IoMik6MTMz
scope.46.kind=method
scope.46.startLine=1
scope.46.endLine=143
scope.46.semanticHash=aa9159526d1909c576f08247e50a4e14f9eb970ace7aa4f77477f4c0c916839e
scope.47.id=bWV0aG9kOkxlZ2FsRW50aXR5Lk9wZXJhdGlvbi5Mb2FuUmVwYWlkI2N0b3IoMyk6MTM2
scope.47.kind=method
scope.47.startLine=1
scope.47.endLine=143
scope.47.semanticHash=aa9159526d1909c576f08247e50a4e14f9eb970ace7aa4f77477f4c0c916839e
*/
