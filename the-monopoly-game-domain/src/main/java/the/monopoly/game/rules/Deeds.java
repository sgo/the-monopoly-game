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
  private final Map<Street.Type, Mortgage> mortgages = new HashMap<>();
  private final Map<RetainedCard, Player.ID> retainedCards = new HashMap<>();

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

  public void transfer(Ownable land, Player seller, Player buyer, Money price) {
    verifyOwner(land, seller);
    seller.account().deposit(price);
    buyer.account().withdraw(price);
    owners.put(land.type(), buyer.id());
  }

  public void hold(RetainedCard card, Player owner) {
    retainedCards.put(card, owner.id());
  }

  public boolean holdsGetOutOfJailFreeCard(Player owner) {
    return retainedCards.containsValue(owner.id());
  }

  public void sellGetOutOfJailFreeCard(Player seller, Player buyer, Money price) {
    RetainedCard card = retainedCards.entrySet().stream()
        .filter(it -> it.getValue().equals(seller.id()))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(seller.id().value() + " holds no Get Out of Jail Free card."));
    seller.account().deposit(price);
    buyer.account().withdraw(price);
    retainedCards.put(card, buyer.id());
  }

  public boolean isMortgaged(Ownable land) {
    return mortgages.containsKey(land.type());
  }

  public void arrangeMortgaged(Ownable land) {
    mortgages.put(land.type(), Mortgage.on(land));
  }

  public Money mortgage(Ownable land, Player owner) {
    verifyOwner(land, owner);
    Mortgage mortgage = Mortgage.on(land);
    mortgages.put(land.type(), mortgage);
    owner.account().deposit(mortgage.value());
    return mortgage.value();
  }

  public Money keepMortgaged(Ownable land, Player owner) {
    verifyOwner(land, owner);
    Mortgage mortgage = mortgageOn(land);
    owner.account().withdraw(mortgage.interest());
    return mortgage.interest();
  }

  public MortgageCost liftMortgage(Ownable land, Player owner) {
    verifyOwner(land, owner);
    Mortgage mortgage = mortgageOn(land);
    owner.account().withdraw(mortgage.totalToLift());
    mortgages.remove(land.type());
    return new MortgageCost(mortgage.totalToLift(), mortgage.interest());
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

  private void verifyOwner(Ownable land, Player owner) {
    if (ownerOf(land.type()).filter(owner.id()::equals).isEmpty())
      throw new IllegalStateException(owner.id().value() + " does not own " + land.type() + ".");
  }

  private Mortgage mortgageOn(Ownable land) {
    Mortgage mortgage = mortgages.get(land.type());
    if (mortgage == null)
      throw new IllegalStateException(land.type() + " is not mortgaged.");
    return mortgage;
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

  private record Mortgage(Money value, Money interest, Money totalToLift) {
    private static Mortgage on(Ownable land) {
      Money value = land.landMortgageValue();
      Money interest = new Money((value.amount() + 9) / 10);
      return new Mortgage(value, interest, value.plus(interest));
    }
  }

  public record MortgageCost(Money total, Money interest) {
  }

  public enum RetainedCard {
    CHANCE_GET_OUT_OF_JAIL_FREE,
    COMMUNITY_CHEST_GET_OUT_OF_JAIL_FREE
  }
}

/* mutate4java-manifest
version=1
moduleHash=cd34a626d87d5c95337d09459cd31e1e650b50c77e5eb5eea95fbd2fa2b6d7d8
scope.0.id=Y2xhc3M6RGVlZHMjRGVlZHM6MTk
scope.0.kind=class
scope.0.startLine=19
scope.0.endLine=234
scope.0.semanticHash=4e44cf7e876f24125653fab7c6c28a7a90cd61297295dd5c8f6d9e12697a1fd4
scope.1.id=Y2xhc3M6RGVlZHMuSW1wcm92ZW1lbnQjSW1wcm92ZW1lbnQ6MTk5
scope.1.kind=class
scope.1.startLine=199
scope.1.endLine=217
scope.1.semanticHash=01d0094bf71dac744851a913ddee763c3c3c7270a15bac54a6587b0424f6a3f1
scope.2.id=Y2xhc3M6RGVlZHMuTW9ydGdhZ2UjTW9ydGdhZ2U6MjE5
scope.2.kind=class
scope.2.startLine=219
scope.2.endLine=225
scope.2.semanticHash=fee691259bfcb421e30a3ee9d471d92ec7ce123e93476977d5ce08635102648d
scope.3.id=Y2xhc3M6RGVlZHMuTW9ydGdhZ2VDb3N0I01vcnRnYWdlQ29zdDoyMjc
scope.3.kind=class
scope.3.startLine=227
scope.3.endLine=228
scope.3.semanticHash=5842da92fbced112e4abc8920d15689f948281ff5d0270a3386098328264350c
scope.4.id=Y2xhc3M6RGVlZHMuUmV0YWluZWRDYXJkI1JldGFpbmVkQ2FyZDoyMzA
scope.4.kind=class
scope.4.startLine=230
scope.4.endLine=233
scope.4.semanticHash=13caff382cfe96971f06f8397b3b976da22d92957c6ccd957cb6ad747879b119
scope.5.id=ZmllbGQ6RGVlZHMjaW1wcm92ZW1lbnRzOjIx
scope.5.kind=field
scope.5.startLine=21
scope.5.endLine=21
scope.5.semanticHash=248563e375a2333a1a1a09ebf4e6d7a80893fe06abf2ef118ea5c93ebf9d8d30
scope.6.id=ZmllbGQ6RGVlZHMjbW9ydGdhZ2VzOjIy
scope.6.kind=field
scope.6.startLine=22
scope.6.endLine=22
scope.6.semanticHash=f73644b237f8ccd650f92a7635d5ce95a2075c442baf95f4fc7760ac1c230e6a
scope.7.id=ZmllbGQ6RGVlZHMjb3duZXJzOjIw
scope.7.kind=field
scope.7.startLine=20
scope.7.endLine=20
scope.7.semanticHash=96142c84799464504dabd909915e6daee49568f895a220f99cbe848745aa4492
scope.8.id=ZmllbGQ6RGVlZHMjcmV0YWluZWRDYXJkczoyMw
scope.8.kind=field
scope.8.startLine=23
scope.8.endLine=23
scope.8.semanticHash=823fcc4e8f793ff82b331c872e3f2d1fe4e65eace12f41d0b40bc3de79ba455c
scope.9.id=ZmllbGQ6RGVlZHMuSW1wcm92ZW1lbnQjVU5JTVBST1ZFRDoyMDA
scope.9.kind=field
scope.9.startLine=200
scope.9.endLine=200
scope.9.semanticHash=f0aabaeaa4507a21bdf759a93cead663a2a15e9d3b48af6fb2be2fca45c3c74a
scope.10.id=ZmllbGQ6RGVlZHMuSW1wcm92ZW1lbnQjaG90ZWw6MTk5
scope.10.kind=field
scope.10.startLine=199
scope.10.endLine=199
scope.10.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.11.id=ZmllbGQ6RGVlZHMuSW1wcm92ZW1lbnQjaG91c2VzOjE5OQ
scope.11.kind=field
scope.11.startLine=199
scope.11.endLine=199
scope.11.semanticHash=f3605400ce7efcaa86a03709c82ce920d989c2ce73df04a47273c2a160a0a961
scope.12.id=ZmllbGQ6RGVlZHMuTW9ydGdhZ2UjaW50ZXJlc3Q6MjE5
scope.12.kind=field
scope.12.startLine=219
scope.12.endLine=219
scope.12.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.13.id=ZmllbGQ6RGVlZHMuTW9ydGdhZ2UjdG90YWxUb0xpZnQ6MjE5
scope.13.kind=field
scope.13.startLine=219
scope.13.endLine=219
scope.13.semanticHash=9f35f6212e828d68d05aea4add782827cbf9110faeabcb8f63123f8eeeea780a
scope.14.id=ZmllbGQ6RGVlZHMuTW9ydGdhZ2UjdmFsdWU6MjE5
scope.14.kind=field
scope.14.startLine=219
scope.14.endLine=219
scope.14.semanticHash=0d022e0e6113de33cad355012469518c3a21832e276a9c3f3a6893880214fa0a
scope.15.id=ZmllbGQ6RGVlZHMuTW9ydGdhZ2VDb3N0I2ludGVyZXN0OjIyNw
scope.15.kind=field
scope.15.startLine=227
scope.15.endLine=227
scope.15.semanticHash=f3f05b6907bf42f4e2df823504094b084a40d9c9027645a193dec296e207f973
scope.16.id=ZmllbGQ6RGVlZHMuTW9ydGdhZ2VDb3N0I3RvdGFsOjIyNw
scope.16.kind=field
scope.16.startLine=227
scope.16.endLine=227
scope.16.semanticHash=c19b9ea10da3846e1c37942dc47f59b13649fda020125cea6df5d3a470490575
scope.17.id=ZmllbGQ6RGVlZHMuUmV0YWluZWRDYXJkI0NIQU5DRV9HRVRfT1VUX09GX0pBSUxfRlJFRToyMzE
scope.17.kind=field
scope.17.startLine=231
scope.17.endLine=231
scope.17.semanticHash=b19cdeb110cf57ebc93ea82c8531432aff29fc7105e4a2bb84ae68c8bf8182cc
scope.18.id=ZmllbGQ6RGVlZHMuUmV0YWluZWRDYXJkI0NPTU1VTklUWV9DSEVTVF9HRVRfT1VUX09GX0pBSUxfRlJFRToyMzI
scope.18.kind=field
scope.18.startLine=232
scope.18.endLine=232
scope.18.semanticHash=7e408ed5038c2074f5aa3432251933fd75d3df9ee177a9305b14ebd33e85f53f
scope.19.id=bWV0aG9kOkRlZWRzI2FycmFuZ2VIb3RlbCgxKToxMTM
scope.19.kind=method
scope.19.startLine=113
scope.19.endLine=115
scope.19.semanticHash=8b24706421e800ee181c65855c27d99ecf1b70738a2c4fdd86c73c11463a44fb
scope.20.id=bWV0aG9kOkRlZWRzI2FycmFuZ2VIb3VzZXMoMik6MTA5
scope.20.kind=method
scope.20.startLine=109
scope.20.endLine=111
scope.20.semanticHash=bb800569810e5d522fa5f24dc376d3c7e584ba877b194c02dc3519f69a3148cc
scope.21.id=bWV0aG9kOkRlZWRzI2FycmFuZ2VNb3J0Z2FnZWQoMSk6NzQ
scope.21.kind=method
scope.21.startLine=74
scope.21.endLine=76
scope.21.semanticHash=6c8a39369f9f55d11d09defec4b5a59212210a0e91d1c18c9949733e46954a0c
scope.22.id=bWV0aG9kOkRlZWRzI2J1aWxkSG90ZWwoMik6MTI4
scope.22.kind=method
scope.22.startLine=128
scope.22.endLine=138
scope.22.semanticHash=2da41aaa74cefd0831e5974c78eb5dbd91ae4b6de304ecbb005bf0bf5a26c24a
scope.23.id=bWV0aG9kOkRlZWRzI2J1aWxkSG91c2UoMik6MTE3
scope.23.kind=method
scope.23.startLine=117
scope.23.endLine=126
scope.23.semanticHash=656387603c42d226108e7127892a5f08f79aac37ebe2d91afd369b23a0520805
scope.24.id=bWV0aG9kOkRlZWRzI2N0b3IoMCk6MTk
scope.24.kind=method
scope.24.startLine=1
scope.24.endLine=234
scope.24.semanticHash=3a4365fa9a87708866e793baabd215de3e1c05912f4d541e11e93d41401246af
scope.25.id=bWV0aG9kOkRlZWRzI2V4Y2hhbmdlSG90ZWxGb3JIb3VzZXMoMik6MTUw
scope.25.kind=method
scope.25.startLine=150
scope.25.endLine=158
scope.25.semanticHash=5ede006e43b2581f0c25bd30b1f54f519a7f9c30e9864c974c3f9b32eb4f5286
scope.26.id=bWV0aG9kOkRlZWRzI2hhbGYoMSk6MTk1
scope.26.kind=method
scope.26.startLine=195
scope.26.endLine=197
scope.26.semanticHash=c909dcf6862f8b16c3cd83b5a42828d9dcfadf101853f5762e52ec55d2124178
scope.27.id=bWV0aG9kOkRlZWRzI2hhc0hvdGVsT24oMSk6MTA1
scope.27.kind=method
scope.27.startLine=105
scope.27.endLine=107
scope.27.semanticHash=61bef379caccf7d18c58bae9b23a788d5240afc76734ebde4b8b34d087a73df9
scope.28.id=bWV0aG9kOkRlZWRzI2hvbGQoMik6NTE
scope.28.kind=method
scope.28.startLine=51
scope.28.endLine=53
scope.28.semanticHash=86bed028954daa507ce00c134bf397c759ebf864516a8e8a341da3c576ba3ec9
scope.29.id=bWV0aG9kOkRlZWRzI2hvbGRzR2V0T3V0T2ZKYWlsRnJlZUNhcmQoMSk6NTU
scope.29.kind=method
scope.29.startLine=55
scope.29.endLine=57
scope.29.semanticHash=f036bb16240c37cc477883a01b9dd453b442fe04fdee58a860c9c096cfde39e0
scope.30.id=bWV0aG9kOkRlZWRzI2hvdGVsVmFsdWVPZigxKToxOTE
scope.30.kind=method
scope.30.startLine=191
scope.30.endLine=193
scope.30.semanticHash=17546a78cc0ec25d3efcd092ef179706ecc772bb83d84e11e5423ead267814dd
scope.31.id=bWV0aG9kOkRlZWRzI2hvdXNlc0J1aWx0T24oMSk6MTAx
scope.31.kind=method
scope.31.startLine=101
scope.31.endLine=103
scope.31.semanticHash=b2fbbccd4bd1878e49d603b61ce15370954a077b055f30130c69b055bda29c1a
scope.32.id=bWV0aG9kOkRlZWRzI2ltcHJvdmVtZW50T24oMSk6MTY3
scope.32.kind=method
scope.32.startLine=167
scope.32.endLine=169
scope.32.semanticHash=cd0e78e7fa1d412b08774be36ba231db3e7fcf3c45f719fd394a19007c8d4d44
scope.33.id=bWV0aG9kOkRlZWRzI2lzTW9ydGdhZ2VkKDEpOjcw
scope.33.kind=method
scope.33.startLine=70
scope.33.endLine=72
scope.33.semanticHash=4f2d27e19ac35263ccd736d49d86ed550abff40d8ff7aa4db0f96cf4397be91c
scope.34.id=bWV0aG9kOkRlZWRzI2lzVW5vd25lZCgxKToyNQ
scope.34.kind=method
scope.34.startLine=25
scope.34.endLine=27
scope.34.semanticHash=b5fbaa45464bf90d554ea7add1f945f6f5027a3dbc4518f95d9f0a5ce602edfc
scope.35.id=bWV0aG9kOkRlZWRzI2tlZXBNb3J0Z2FnZWQoMik6ODY
scope.35.kind=method
scope.35.startLine=86
scope.35.endLine=91
scope.35.semanticHash=42cea4b5989369b365876d3b504910e2479c7bd751a28442fbea63272ef003a5
scope.36.id=bWV0aG9kOkRlZWRzI2xpZnRNb3J0Z2FnZSgyKTo5Mw
scope.36.kind=method
scope.36.startLine=93
scope.36.endLine=99
scope.36.semanticHash=0bfea644478ad4726b4c2cc8ffede0b24f169e77095b342dccd8ff0fabf95613
scope.37.id=bWV0aG9kOkRlZWRzI21vcnRnYWdlKDIpOjc4
scope.37.kind=method
scope.37.startLine=78
scope.37.endLine=84
scope.37.semanticHash=828efc28a7049365cdcca7c6d8e0a74f9e6bc1d41a4c3a495219c5afcf396feb
scope.38.id=bWV0aG9kOkRlZWRzI21vcnRnYWdlT24oMSk6MTc2
scope.38.kind=method
scope.38.startLine=176
scope.38.endLine=181
scope.38.semanticHash=090550749968f25e22c375577e598ac2cd182e48b9aabbd05b725886acb3eb78
scope.39.id=bWV0aG9kOkRlZWRzI293bmVyT2YoMSk6MzA
scope.39.kind=method
scope.39.startLine=30
scope.39.endLine=32
scope.39.semanticHash=5b5d48e94bbee42661fe2a00386b3585e4b5f1ebe10a5df06318cbc242b22090
scope.40.id=bWV0aG9kOkRlZWRzI3JlZnVuZCg0KToxNjA
scope.40.kind=method
scope.40.startLine=160
scope.40.endLine=165
scope.40.semanticHash=0364d4a62db6e7e2d7469c62bf8fa5d1abae8967a68eda1c30095afbfe4f5e5d
scope.41.id=bWV0aG9kOkRlZWRzI3NlbGwoMyk6Mzk
scope.41.kind=method
scope.41.startLine=39
scope.41.endLine=42
scope.41.semanticHash=6c391fe5295e11609838f608937f07d26992a19798b259b55391a12ca146adc5
scope.42.id=bWV0aG9kOkRlZWRzI3NlbGxHZXRPdXRPZkphaWxGcmVlQ2FyZCgzKTo1OQ
scope.42.kind=method
scope.42.startLine=59
scope.42.endLine=68
scope.42.semanticHash=2b10def0fc02edc4c6993ab800d420c6876d0d8964679a65536bb4a71240d011
scope.43.id=bWV0aG9kOkRlZWRzI3NlbGxIb3VzZSgyKToxNDA
scope.43.kind=method
scope.43.startLine=140
scope.43.endLine=148
scope.43.semanticHash=bf3daa2318c939e6d93c832955e8ee2059f8c6d2be0e7737569d912f95f65546
scope.44.id=bWV0aG9kOkRlZWRzI3RyYW5zZmVyKDQpOjQ0
scope.44.kind=method
scope.44.startLine=44
scope.44.endLine=49
scope.44.semanticHash=f5cfaf2d069a74ba8d3a3e351ba68fc07c953e92a8905d5719619e148c33828e
scope.45.id=bWV0aG9kOkRlZWRzI3ZhbGlkYXRlZEhvdXNlcygyKToxODM
scope.45.kind=method
scope.45.startLine=183
scope.45.endLine=189
scope.45.semanticHash=9309e8bf2bb58fa70bf578fba62f8cf28d989ede6699d54129c4ee33729c1ede
scope.46.id=bWV0aG9kOkRlZWRzI3ZlcmlmeU93bmVyKDIpOjE3MQ
scope.46.kind=method
scope.46.startLine=171
scope.46.endLine=174
scope.46.semanticHash=81c3bef9adc62f533c58754c244d51a5fa79fd9fbe9bc835ee402fa9ca3be0d0
scope.47.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I2N0b3IoMik6MTk5
scope.47.kind=method
scope.47.startLine=1
scope.47.endLine=234
scope.47.semanticHash=3a4365fa9a87708866e793baabd215de3e1c05912f4d541e11e93d41401246af
scope.48.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhBbm90aGVySG91c2UoMCk6MjEw
scope.48.kind=method
scope.48.startLine=210
scope.48.endLine=212
scope.48.semanticHash=8838380645dd6c08e5de7f23787df82faeb4143838a344fafe82f9df1632f4b8
scope.49.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhIb3RlbCgwKToyMDY
scope.49.kind=method
scope.49.startLine=206
scope.49.endLine=208
scope.49.semanticHash=4cde0f7b4831427979e669228a89230c579cc98ba9184f1aefbd599caf4b16a0
scope.50.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhIb3VzZXMoMSk6MjAy
scope.50.kind=method
scope.50.startLine=202
scope.50.endLine=204
scope.50.semanticHash=d087abc2c595fcd745b7b682cd3818a2aa45685a7c3f45124d6b78641b5703c5
scope.51.id=bWV0aG9kOkRlZWRzLkltcHJvdmVtZW50I3dpdGhPbmVMZXNzSG91c2UoMCk6MjE0
scope.51.kind=method
scope.51.startLine=214
scope.51.endLine=216
scope.51.semanticHash=c1aa8bb6677403c86a49184b50675bc7f2321598c948df496629446d0a245710
scope.52.id=bWV0aG9kOkRlZWRzLk1vcnRnYWdlI2N0b3IoMyk6MjE5
scope.52.kind=method
scope.52.startLine=1
scope.52.endLine=234
scope.52.semanticHash=3a4365fa9a87708866e793baabd215de3e1c05912f4d541e11e93d41401246af
scope.53.id=bWV0aG9kOkRlZWRzLk1vcnRnYWdlI29uKDEpOjIyMA
scope.53.kind=method
scope.53.startLine=220
scope.53.endLine=224
scope.53.semanticHash=08a70c49b7db216e2c1ca9ebfae1ccd4368f01d06e7c45fcedf8e394844f1bd3
scope.54.id=bWV0aG9kOkRlZWRzLk1vcnRnYWdlQ29zdCNjdG9yKDIpOjIyNw
scope.54.kind=method
scope.54.startLine=1
scope.54.endLine=234
scope.54.semanticHash=3a4365fa9a87708866e793baabd215de3e1c05912f4d541e11e93d41401246af
scope.55.id=bWV0aG9kOkRlZWRzLlJldGFpbmVkQ2FyZCNjdG9yKDApOjIzMA
scope.55.kind=method
scope.55.startLine=1
scope.55.endLine=234
scope.55.semanticHash=3a4365fa9a87708866e793baabd215de3e1c05912f4d541e11e93d41401246af
*/
