package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Who owns which space. The rules lay the board out afresh every time they are
 * asked, so a space is a value rather than a thing that can be marked; the
 * title to it is kept here instead, against the space's type, for as long as a
 * game lasts.
 */
public class Deeds {
  private final Map<Street.Type, Player.ID> owners = new HashMap<>();
  private final Map<Street.Type, Improvement> improvements = new HashMap<>();

  public boolean isUnowned(Street.Type land) {
    return !owners.containsKey(land);
  }

  /** Who holds the title to this land, if anyone does. */
  public Optional<Player.ID> ownerOf(Street.Type land) {
    return Optional.ofNullable(owners.get(land));
  }

  /**
   * Hands the title to a buyer, who pays the bank what the land went for. That
   * is the price on the board when it is bought, and the winning bid when it is
   * auctioned, so the sale is told what it fetched rather than working it out.
   */
  public void sell(Ownable land, Player buyer, Money price) {
    buyer.account().withdraw(price);
    owners.put(land.type(), buyer.id());
  }

  public int housesBuiltOn(ColourStreet land) {
    return improvementOn(land).houses();
  }

  public boolean hasHotelOn(ColourStreet land) {
    return improvementOn(land).hotel();
  }

  public void arrangeHouses(ColourStreet land, int houses) {
    improvements.put(land.type(), Improvement.withHouses(validatedHouses(land, houses)));
  }

  public void arrangeHotel(ColourStreet land) {
    improvements.put(land.type(), Improvement.withHotel());
  }

  public void buildHouse(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    Improvement current = improvementOn(land);
    if (current.hotel())
      throw new IllegalStateException(land.type() + " already has a hotel.");
    if (current.houses() >= land.hotelConstructionRequiresNumberOfHouses())
      throw new IllegalStateException(land.type() + " already has enough houses for a hotel.");
    owner.account().withdraw(land.houseConstructionCost());
    improvements.put(land.type(), current.withAnotherHouse());
  }

  public void buildHotel(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    Improvement current = improvementOn(land);
    if (current.hotel())
      throw new IllegalStateException(land.type() + " already has a hotel.");
    if (current.houses() != land.hotelConstructionRequiresNumberOfHouses())
      throw new IllegalStateException(land.type() + " needs "
          + land.hotelConstructionRequiresNumberOfHouses() + " houses before a hotel.");
    owner.account().withdraw(hotelValueOf(land));
    improvements.put(land.type(), Improvement.withHotel());
  }

  public Money sellHouse(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    Improvement current = improvementOn(land);
    if (current.hotel())
      throw new IllegalStateException(land.type() + " has a hotel, not a house to sell.");
    if (current.houses() <= 0)
      throw new IllegalStateException(land.type() + " has no house to sell.");
    return refund(land, owner, current.withOneLessHouse(), land.houseConstructionCost());
  }

  public Money exchangeHotelForHouses(ColourStreet land, Player owner) {
    verifyOwner(land, owner);
    if (!improvementOn(land).hotel())
      throw new IllegalStateException(land.type() + " has no hotel to exchange.");
    return refund(
        land, owner,
        Improvement.withHouses(land.hotelConstructionRequiresNumberOfHouses()), hotelValueOf(land)
    );
  }

  private Money refund(ColourStreet land, Player owner, Improvement newImprovement, Money fullPrice) {
    improvements.put(land.type(), newImprovement);
    Money price = half(fullPrice);
    owner.account().deposit(price);
    return price;
  }

  private Improvement improvementOn(ColourStreet land) {
    return improvements.getOrDefault(land.type(), Improvement.UNIMPROVED);
  }

  private void verifyOwner(ColourStreet land, Player owner) {
    if (ownerOf(land.type()).filter(owner.id()::equals).isEmpty())
      throw new IllegalStateException(owner.id().value() + " does not own " + land.type() + ".");
  }

  private static int validatedHouses(ColourStreet land, int houses) {
    if (houses < 0 || houses > land.hotelConstructionRequiresNumberOfHouses())
      throw new IllegalArgumentException(
          land.type() + " can hold between 0 and " + land.hotelConstructionRequiresNumberOfHouses() + " houses."
      );
    return houses;
  }

  private static Money hotelValueOf(ColourStreet land) {
    return land.rentForOneHotel();
  }

  private static Money half(Money price) {
    return new Money(price.amount() / 2);
  }

  private record Improvement(int houses, boolean hotel) {
    private static final Improvement UNIMPROVED = new Improvement(0, false);

    private static Improvement withHouses(int houses) {
      return new Improvement(houses, false);
    }

    private static Improvement withHotel() {
      return new Improvement(0, true);
    }

    private Improvement withAnotherHouse() {
      return new Improvement(houses + 1, false);
    }

    private Improvement withOneLessHouse() {
      return new Improvement(houses - 1, false);
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=7c7770f99409fe50e64259a4116c7bcdd93b4fcf0531814163e0fec75374da6c
scope.0.id=Y2xhc3M6RGVlZHMjRGVlZHM6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=152
scope.0.semanticHash=19b8d7a96c2ca046645c49c88f19c02426f380d920abf54da5b3bec9d2d8a2a5
scope.1.id=Y2xhc3M6RGVlZHMuSW1wcm92ZW1lbnQjSW1wcm92ZW1lbnQ6MTMz
scope.1.kind=class
scope.1.startLine=133
scope.1.endLine=151
scope.1.semanticHash=01d0094bf71dac744851a913ddee763c3c3c7270a15bac54a6587b0424f6a3f1
scope.2.id=ZmllbGQ6RGVlZHMjaW1wcm92ZW1lbnRzOjIx
scope.2.kind=field
scope.2.startLine=21
scope.2.endLine=21
scope.2.semanticHash=248563e375a2333a1a1a09ebf4e6d7a80893fe06abf2ef118ea5c93ebf9d8d30
scope.3.id=ZmllbGQ6RGVlZHMjb3duZXJzOjIw
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=20
scope.3.semanticHash=96142c84799464504dabd909915e6daee49568f895a220f99cbe848745aa4492
scope.4.id=ZmllbGQ6RGVlZHMuSW1wcm92ZW1lbnQjVU5JTVBST1ZFRDoxMzQ
scope.4.kind=field
scope.4.startLine=134
scope.4.endLine=134
scope.4.semanticHash=f0aabaeaa4507a21bdf759a93cead663a2a15e9d3b48af6fb2be2fca45c3c74a
scope.5.id=ZmllbGQ6RGVlZHMuSW1wcm92ZW1lbnQjaG90ZWw6MTMz
scope.5.kind=field
scope.5.startLine=133
scope.5.endLine=133
scope.5.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.6.id=ZmllbGQ6RGVlZHMuSW1wcm92ZW1lbnQjaG91c2VzOjEzMw
scope.6.kind=field
scope.6.startLine=133
scope.6.endLine=133
scope.6.semanticHash=f3605400ce7efcaa86a03709c82ce920d989c2ce73df04a47273c2a160a0a961
scope.7.id=bWV0aG9kOkRlZWRzI2FycmFuZ2VIb3RlbCgxKTo1NA
scope.7.kind=method
scope.7.startLine=54
scope.7.endLine=56
scope.7.semanticHash=8b24706421e800ee181c65855c27d99ecf1b70738a2c4fdd86c73c11463a44fb
scope.8.id=bWV0aG9kOkRlZWRzI2FycmFuZ2VIb3VzZXMoMik6NTA
scope.8.kind=method
scope.8.startLine=50
scope.8.endLine=52
scope.8.semanticHash=bb800569810e5d522fa5f24dc376d3c7e584ba877b194c02dc3519f69a3148cc
scope.9.id=bWV0aG9kOkRlZWRzI2J1aWxkSG90ZWwoMik6Njk
scope.9.kind=method
scope.9.startLine=69
scope.9.endLine=79
scope.9.semanticHash=2da41aaa74cefd0831e5974c78eb5dbd91ae4b6de304ecbb005bf0bf5a26c24a
scope.10.id=bWV0aG9kOkRlZWRzI2J1aWxkSG91c2UoMik6NTg
scope.10.kind=method
scope.10.startLine=58
scope.10.endLine=67
scope.10.semanticHash=656387603c42d226108e7127892a5f08f79aac37ebe2d91afd369b23a0520805
scope.11.id=bWV0aG9kOkRlZWRzI2N0b3IoMCk6MTk
scope.11.kind=method
scope.11.startLine=1
scope.11.endLine=152
scope.11.semanticHash=227b3596650eaacd963390c64f15178532a16f7de341e3038a32dff586f1224c
scope.12.id=bWV0aG9kOkRlZWRzI2V4Y2hhbmdlSG90ZWxGb3JIb3VzZXMoMik6OTE
scope.12.kind=method
scope.12.startLine=91
scope.12.endLine=99
scope.12.semanticHash=5ede006e43b2581f0c25bd30b1f54f519a7f9c30e9864c974c3f9b32eb4f5286
scope.13.id=bWV0aG9kOkRlZWRzI2hhbGYoMSk6MTI5
scope.13.kind=method
scope.13.startLine=129
scope.13.endLine=131
scope.13.semanticHash=c909dcf6862f8b16c3cd83b5a42828d9dcfadf101853f5762e52ec55d2124178
scope.14.id=bWV0aG9kOkRlZWRzI2hhc0hvdGVsT24oMSk6NDY
scope.14.kind=method
scope.14.startLine=46
scope.14.endLine=48
scope.14.semanticHash=61bef379caccf7d18c58bae9b23a788d5240afc76734ebde4b8b34d087a73df9
scope.15.id=bWV0aG9kOkRlZWRzI2hvdGVsVmFsdWVPZigxKToxMjU
scope.15.kind=method
scope.15.startLine=125
scope.15.endLine=127
scope.15.semanticHash=17546a78cc0ec25d3efcd092ef179706ecc772bb83d84e11e5423ead267814dd
scope.16.id=bWV0aG9kOkRlZWRzI2hvdXNlc0J1aWx0T24oMSk6NDI
scope.16.kind=method
scope.16.startLine=42
scope.16.endLine=44
scope.16.semanticHash=b2fbbccd4bd1878e49d603b61ce15370954a077b055f30130c69b055bda29c1a
scope.17.id=bWV0aG9kOkRlZWRzI2ltcHJvdmVtZW50T24oMSk6MTA4
scope.17.kind=method
scope.17.startLine=108
scope.17.endLine=110
scope.17.semanticHash=cd0e78e7fa1d412b08774be36ba231db3e7fcf3c45f719fd394a19007c8d4d44
scope.18.id=bWV0aG9kOkRlZWRzI2lzVW5vd25lZCgxKToyMw
scope.18.kind=method
scope.18.startLine=23
scope.18.endLine=25
scope.18.semanticHash=b5fbaa45464bf90d554ea7add1f945f6f5027a3dbc4518f95d9f0a5ce602edfc
scope.19.id=bWV0aG9kOkRlZWRzI293bmVyT2YoMSk6Mjg
scope.19.kind=method
scope.19.startLine=28
scope.19.endLine=30
scope.19.semanticHash=5b5d48e94bbee42661fe2a00386b3585e4b5f1ebe10a5df06318cbc242b22090
scope.20.id=bWV0aG9kOkRlZWRzI3JlZnVuZCg0KToxMDE
scope.20.kind=method
scope.20.startLine=101
scope.20.endLine=106
scope.20.semanticHash=0364d4a62db6e7e2d7469c62bf8fa5d1abae8967a68eda1c30095afbfe4f5e5d
scope.21.id=bWV0aG9kOkRlZWRzI3NlbGwoMyk6Mzc
scope.21.kind=method
scope.21.startLine=37
scope.21.endLine=40
scope.21.semanticHash=6c391fe5295e11609838f608937f07d26992a19798b259b55391a12ca146adc5
scope.22.id=bWV0aG9kOkRlZWRzI3NlbGxIb3VzZSgyKTo4MQ
scope.22.kind=method
scope.22.startLine=81
scope.22.endLine=89
scope.22.semanticHash=bf3daa2318c939e6d93c832955e8ee2059f8c6d2be0e7737569d912f95f65546
scope.23.id=bWV0aG9kOkRlZWRzI3ZhbGlkYXRlZEhvdXNlcygyKToxMTc
scope.23.kind=method
scope.23.startLine=117
scope.23.endLine=123
scope.23.semanticHash=9309e8bf2bb58fa70bf578fba62f8cf28d989ede6699d54129c4ee33729c1ede
scope.24.id=bWV0aG9kOkRlZWRzI3ZlcmlmeU93bmVyKDIpOjExMg
scope.24.kind=method
scope.24.startLine=112
scope.24.endLine=115
scope.24.semanticHash=b465348978268a8261b2e71757395023057f59e907f11d5f60dd60e106ccdeb5
scope.25.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I2N0b3IoMik6MTMz
scope.25.kind=method
scope.25.startLine=1
scope.25.endLine=152
scope.25.semanticHash=227b3596650eaacd963390c64f15178532a16f7de341e3038a32dff586f1224c
scope.26.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhBbm90aGVySG91c2UoMCk6MTQ0
scope.26.kind=method
scope.26.startLine=144
scope.26.endLine=146
scope.26.semanticHash=8838380645dd6c08e5de7f23787df82faeb4143838a344fafe82f9df1632f4b8
scope.27.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhIb3RlbCgwKToxNDA
scope.27.kind=method
scope.27.startLine=140
scope.27.endLine=142
scope.27.semanticHash=4cde0f7b4831427979e669228a89230c579cc98ba9184f1aefbd599caf4b16a0
scope.28.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhIb3VzZXMoMSk6MTM2
scope.28.kind=method
scope.28.startLine=136
scope.28.endLine=138
scope.28.semanticHash=d087abc2c595fcd745b7b682cd3818a2aa45685a7c3f45124d6b78641b5703c5
scope.29.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhPbmVMZXNzSG91c2UoMCk6MTQ4
scope.29.kind=method
scope.29.startLine=148
scope.29.endLine=150
scope.29.semanticHash=c1aa8bb6677403c86a49184b50675bc7f2321598c948df496629446d0a245710
*/
