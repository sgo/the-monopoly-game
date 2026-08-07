package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.Rule;

import java.util.List;

/**
 * How a player decides. The game asks a strategy wherever a player has a
 * choice, and every choice has a default of leaving well alone, so a strategy
 * answers only the questions it has an opinion about and a new question can be
 * asked without every strategy being rewritten.
 */
public interface Strategy {
  /** A player who leaves every choice alone, which is what the defaults amount to. */
  Strategy UNDECIDED = new Strategy() {
  };

  /** Whether to buy the land on offer at the price on the board. */
  default boolean accepts(Offer offer) {
    return false;
  }

  /** Whether the trader should exchange one owned property for another player's property. */
  default boolean accepts(TradeOffer offer, Rule.Set rules, Deeds deeds) {
    return false;
  }

  default DeclineReason declineReason(Offer offer) {
    return DeclineReason.NO_BUYING_POLICY;
  }

  /** What to bid for the land at auction. Nothing is not a bid. */
  default Money bidFor(Offer offer) {
    return Money.ZERO;
  }

  /** Whether to collect the rent owed by a visitor to land this player owns. */
  default boolean claims(RentClaim claim) {
    return false;
  }

  /** Whether to buy the next house or hotel offered for an owned street. */
  default boolean builds(BuildOffer offer) {
    return false;
  }

  /** Whether to pay the fine to begin this turn outside jail. */
  default boolean pays(JailFine fine) {
    return false;
  }

  /** A visitor, the land they stopped on, and the rent its owner may claim. */
  record RentClaim(Player tenant, Ownable land, Money amount) {
  }

  /** The land a player is being asked about, and what they have to spend on it. */
  record Offer(Ownable land, Money available, Money reserve, boolean utilityMonopolyOpportunity) {
    public Offer(Ownable land, Money available) {
      this(land, available, Money.ZERO, false);
    }

    public boolean isAffordable() {
      return available.covers(land.price());
    }
  }

  record TradeOffer(Player trader, Player partner, Ownable offered, Ownable wanted) {
  }

  enum DeclineReason {
    CANNOT_AFFORD,
    CASH_RESERVE,
    NO_BUYING_POLICY
  }

  default Money cashReserve() {
    return Money.ZERO;
  }

  /** Reserve calculated with the player's current holdings, when a strategy has one. */
  default Money cashReserve(Player player, Rule.Set rules, Deeds deeds) {
    return cashReserve();
  }

  enum Priority {
    HIGHEST, MIDDLE, LOWEST
  }

  default Priority priority(Ownable land) {
    return Priority.LOWEST;
  }

  /** Maximum offer for a property sold by a debtor trying to avoid bankruptcy. */
  default Money bidForDistressed(Offer offer, Player bidder, Player debtor,
                                 List<Player> players, Rule.Set rules, Deeds deeds) {
    return bidFor(offer);
  }

  /** The improvement a player is being asked to buy for a street they already own. */
  record BuildOffer(ColourStreet land, Money price, Money available, boolean hotel) {
    public boolean isAffordable() {
      return available.covers(price);
    }
  }

  record JailFine(Money amount, Money available) {
    public boolean isAffordable() {
      return available.covers(amount);
    }
  }

  /** Which strategy each player at the table is playing. */
  @FunctionalInterface
  interface OfPlayers {
    /** Everyone leaving every choice alone, for a game where nobody decides. */
    OfPlayers NOBODY_DECIDES = player -> UNDECIDED;

    Strategy forPlayer(Player player);
  }
}

/* mutate4java-manifest
version=1
moduleHash=713077835097f73f16688c5d5d4f55c683dbac73b6648d515f686b83a12a9fc4
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=125
scope.0.semanticHash=bd492eb2f3b07a7b21ccbc897d2fba8812392c24554cfac714a7507ef4771bf8
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoyMA
scope.1.kind=class
scope.1.startLine=20
scope.1.endLine=21
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuQnVpbGRPZmZlciNCdWlsZE9mZmVyOjEwNQ
scope.2.kind=class
scope.2.startLine=105
scope.2.endLine=109
scope.2.semanticHash=ed3ceeeb156aea50b62f3f6599d6d4144af1f8d29586c01c219259c33849bb19
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNEZWNsaW5lUmVhc29uOjc1
scope.3.kind=class
scope.3.startLine=75
scope.3.endLine=79
scope.3.semanticHash=d8497c5c1662d16ecb6ba17e836492b159af8f5e497a9da2ffc6af14d5dd4322
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuSmFpbEZpbmUjSmFpbEZpbmU6MTEx
scope.4.kind=class
scope.4.startLine=111
scope.4.endLine=115
scope.4.semanticHash=475585c5238183cdae22a88d6cf475cbf0fb23e46d258ade7757621811afeda0
scope.5.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczoxMTg
scope.5.kind=class
scope.5.startLine=118
scope.5.endLine=124
scope.5.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.6.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6NjI
scope.6.kind=class
scope.6.startLine=62
scope.6.endLine=70
scope.6.semanticHash=7f2cd56eda30901389ecac24e05691990687d70540c4e9be4470d751f7e8452c
scope.7.id=Y2xhc3M6U3RyYXRlZ3kuUHJpb3JpdHkjUHJpb3JpdHk6OTA
scope.7.kind=class
scope.7.startLine=90
scope.7.endLine=92
scope.7.semanticHash=a2a404b2f42d68270f65d74737b7fb841f3116105222137422b6c68e3b2c5b2c
scope.8.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTo1OA
scope.8.kind=class
scope.8.startLine=58
scope.8.endLine=59
scope.8.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.9.id=Y2xhc3M6U3RyYXRlZ3kuVHJhZGVPZmZlciNUcmFkZU9mZmVyOjcy
scope.9.kind=class
scope.9.startLine=72
scope.9.endLine=73
scope.9.semanticHash=2059acf82196db4175dc32e0bbb0927bd10e949950b23c77c695517103c2a370
scope.10.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjIw
scope.10.kind=field
scope.10.startLine=20
scope.10.endLine=21
scope.10.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.11.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNhdmFpbGFibGU6MTA1
scope.11.kind=field
scope.11.startLine=105
scope.11.endLine=105
scope.11.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.12.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNob3RlbDoxMDU
scope.12.kind=field
scope.12.startLine=105
scope.12.endLine=105
scope.12.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.13.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNsYW5kOjEwNQ
scope.13.kind=field
scope.13.startLine=105
scope.13.endLine=105
scope.13.semanticHash=35f9733d561459c6d821f37d194db007baffa148da826ae273a60a12e0e55476
scope.14.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNwcmljZToxMDU
scope.14.kind=field
scope.14.startLine=105
scope.14.endLine=105
scope.14.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.15.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQU5OT1RfQUZGT1JEOjc2
scope.15.kind=field
scope.15.startLine=76
scope.15.endLine=76
scope.15.semanticHash=0562a15b653fe383269ebd77aa29e1c50390797b07f7a8166e9de3b296c3fc21
scope.16.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQVNIX1JFU0VSVkU6Nzc
scope.16.kind=field
scope.16.startLine=77
scope.16.endLine=77
scope.16.semanticHash=c36ca0607e5d5b006445adf3206eeb33ccc63ab058d0b09a3b5a0f1580409514
scope.17.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNOT19CVVlJTkdfUE9MSUNZOjc4
scope.17.kind=field
scope.17.startLine=78
scope.17.endLine=78
scope.17.semanticHash=573981a3034acaf2d9db7c0ea32b36b35c43345919b99c88bb91e7feb003d593
scope.18.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYW1vdW50OjExMQ
scope.18.kind=field
scope.18.startLine=111
scope.18.endLine=111
scope.18.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.19.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYXZhaWxhYmxlOjExMQ
scope.19.kind=field
scope.19.startLine=111
scope.19.endLine=111
scope.19.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.20.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjEyMQ
scope.20.kind=field
scope.20.startLine=121
scope.20.endLine=121
scope.20.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.21.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjYy
scope.21.kind=field
scope.21.startLine=62
scope.21.endLine=62
scope.21.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.22.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDo2Mg
scope.22.kind=field
scope.22.startLine=62
scope.22.endLine=62
scope.22.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.23.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjcmVzZXJ2ZTo2Mg
scope.23.kind=field
scope.23.startLine=62
scope.23.endLine=62
scope.23.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.24.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjdXRpbGl0eU1vbm9wb2x5T3Bwb3J0dW5pdHk6NjI
scope.24.kind=field
scope.24.startLine=62
scope.24.endLine=62
scope.24.semanticHash=4827abe12096fb0602e87255e1235bbd76c30e6faad4101e14d7c13a22b76c8b
scope.25.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjSElHSEVTVDo5MQ
scope.25.kind=field
scope.25.startLine=91
scope.25.endLine=91
scope.25.semanticHash=f18742671a135cae02cfdafc9c39c14bcbb737bac94d266b118d444b3a81fa62
scope.26.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjTE9XRVNUOjkx
scope.26.kind=field
scope.26.startLine=91
scope.26.endLine=91
scope.26.semanticHash=466a4131f1e6ce62c929316d2b7fefa9339c0c6cc0df60c6db1c10407fd2892c
scope.27.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjTUlERExFOjkx
scope.27.kind=field
scope.27.startLine=91
scope.27.endLine=91
scope.27.semanticHash=b0db1518f3cdfd4db883854f40d1c2b32f687162d0ffb4a5c70637de157a67b7
scope.28.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDo1OA
scope.28.kind=field
scope.28.startLine=58
scope.28.endLine=58
scope.28.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.29.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6NTg
scope.29.kind=field
scope.29.startLine=58
scope.29.endLine=58
scope.29.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.30.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDo1OA
scope.30.kind=field
scope.30.startLine=58
scope.30.endLine=58
scope.30.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.31.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciNvZmZlcmVkOjcy
scope.31.kind=field
scope.31.startLine=72
scope.31.endLine=72
scope.31.semanticHash=4a3c1a8e149716d4eff641f25e52ed346b5eaa070d3c26a4f73dea037494197f
scope.32.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciNwYXJ0bmVyOjcy
scope.32.kind=field
scope.32.startLine=72
scope.32.endLine=72
scope.32.semanticHash=1c4b328c26083116ccd92908d023d04b5d1387bc0feed450661bd017d6917a10
scope.33.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciN0cmFkZXI6NzI
scope.33.kind=field
scope.33.startLine=72
scope.33.endLine=72
scope.33.semanticHash=ee0f1344de9034bfc0e6c93ac47dc1267e98d4e6a099bec0d588c3945e9c54b1
scope.34.id=ZmllbGQ6U3RyYXRlZ3kuVHJhZGVPZmZlciN3YW50ZWQ6NzI
scope.34.kind=field
scope.34.startLine=72
scope.34.endLine=72
scope.34.semanticHash=2ed62d75695cc7464e1b52a329be624426651cc688cd32491385c68e2124a780
scope.35.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MjQ
scope.35.kind=method
scope.35.startLine=24
scope.35.endLine=26
scope.35.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.36.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMyk6Mjk
scope.36.kind=method
scope.36.startLine=29
scope.36.endLine=31
scope.36.semanticHash=697de314e8f022d7c259a387f9a057b565d86261593362bee48ec7722d888109
scope.37.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKTozOA
scope.37.kind=method
scope.37.startLine=38
scope.37.endLine=40
scope.37.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.38.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvckRpc3RyZXNzZWQoNik6OTk
scope.38.kind=method
scope.38.startLine=99
scope.38.endLine=102
scope.38.semanticHash=e4ab55653e41003e36f416113d350aa58b42e0c3740660f35a59ecbac92d2a71
scope.39.id=bWV0aG9kOlN0cmF0ZWd5I2J1aWxkcygxKTo0OA
scope.39.kind=method
scope.39.startLine=48
scope.39.endLine=50
scope.39.semanticHash=f7d3cc98f5a772d00d27eb7defef23be2865369310099a11a3c5b00ca3f3cae6
scope.40.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDApOjgx
scope.40.kind=method
scope.40.startLine=81
scope.40.endLine=83
scope.40.semanticHash=ad759992f3e478a058748ed385db64fd90320432890c3e55b970807c7b591a56
scope.41.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDMpOjg2
scope.41.kind=method
scope.41.startLine=86
scope.41.endLine=88
scope.41.semanticHash=ef54b4fa5744bd18de2343911b27ab8095fcfb809b8bdf5fa90f1644d9e6e640
scope.42.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKTo0Mw
scope.42.kind=method
scope.42.startLine=43
scope.42.endLine=45
scope.42.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.43.id=bWV0aG9kOlN0cmF0ZWd5I2RlY2xpbmVSZWFzb24oMSk6MzM
scope.43.kind=method
scope.43.startLine=33
scope.43.endLine=35
scope.43.semanticHash=b21810ac08d8bf76c5f02fc132ba14d1bc098521837310c97dbe43ebacc7dae6
scope.44.id=bWV0aG9kOlN0cmF0ZWd5I3BheXMoMSk6NTM
scope.44.kind=method
scope.44.startLine=53
scope.44.endLine=55
scope.44.semanticHash=9665aac958731b28c9d13787f8f077161467c026d849a454887fc1614714e7b1
scope.45.id=bWV0aG9kOlN0cmF0ZWd5I3ByaW9yaXR5KDEpOjk0
scope.45.kind=method
scope.45.startLine=94
scope.45.endLine=96
scope.45.semanticHash=926a6dd97684bd51f06e7ef41dee3d103d5130ff0c8513083cf4042fefdab937
scope.46.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjIw
scope.46.kind=method
scope.46.startLine=1
scope.46.endLine=125
scope.46.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.47.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjY3Rvcig0KToxMDU
scope.47.kind=method
scope.47.startLine=1
scope.47.endLine=125
scope.47.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.48.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjaXNBZmZvcmRhYmxlKDApOjEwNg
scope.48.kind=method
scope.48.startLine=106
scope.48.endLine=108
scope.48.semanticHash=e14c79b46e24f2513da2fac747d83f31ba0673fea6ee146454f89007dcf0d6a9
scope.49.id=bWV0aG9kOlN0cmF0ZWd5LkRlY2xpbmVSZWFzb24jY3RvcigwKTo3NQ
scope.49.kind=method
scope.49.startLine=1
scope.49.endLine=125
scope.49.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.50.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2N0b3IoMik6MTEx
scope.50.kind=method
scope.50.startLine=1
scope.50.endLine=125
scope.50.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.51.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2lzQWZmb3JkYWJsZSgwKToxMTI
scope.51.kind=method
scope.51.startLine=112
scope.51.endLine=114
scope.51.semanticHash=dff0ac3387fd0017fbe557e0ceb9169463894f542c375ca972afc49bc2a777e7
scope.52.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6MTIz
scope.52.kind=method
scope.52.startLine=123
scope.52.endLine=123
scope.52.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.53.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6NjM
scope.53.kind=method
scope.53.startLine=63
scope.53.endLine=65
scope.53.semanticHash=e3ad9be7fc0e9555f8686dffdf5eb9f7835da5074bb34b923f39f2c52eddc72d
scope.54.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoNCk6NjI
scope.54.kind=method
scope.54.startLine=1
scope.54.endLine=125
scope.54.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.55.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTo2Nw
scope.55.kind=method
scope.55.startLine=67
scope.55.endLine=69
scope.55.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.56.id=bWV0aG9kOlN0cmF0ZWd5LlByaW9yaXR5I2N0b3IoMCk6OTA
scope.56.kind=method
scope.56.startLine=1
scope.56.endLine=125
scope.56.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.57.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjU4
scope.57.kind=method
scope.57.startLine=1
scope.57.endLine=125
scope.57.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
scope.58.id=bWV0aG9kOlN0cmF0ZWd5LlRyYWRlT2ZmZXIjY3Rvcig0KTo3Mg
scope.58.kind=method
scope.58.startLine=1
scope.58.endLine=125
scope.58.semanticHash=1920d2be742916241cfa0dbc147a1881d2da306013fba236bea63fabb6a2a2f2
*/
