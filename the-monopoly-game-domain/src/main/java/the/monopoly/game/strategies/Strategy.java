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
moduleHash=32073846d952435605a4723b21faab84eda8ba987e915d2e669e4e616ff34930
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=117
scope.0.semanticHash=40fe18d7224c04df869b1aa0c8f7e0a32aa4a1a8564cf0198a60a4f950645f6e
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoyMA
scope.1.kind=class
scope.1.startLine=20
scope.1.endLine=21
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuQnVpbGRPZmZlciNCdWlsZE9mZmVyOjk3
scope.2.kind=class
scope.2.startLine=97
scope.2.endLine=101
scope.2.semanticHash=ed3ceeeb156aea50b62f3f6599d6d4144af1f8d29586c01c219259c33849bb19
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNEZWNsaW5lUmVhc29uOjY3
scope.3.kind=class
scope.3.startLine=67
scope.3.endLine=71
scope.3.semanticHash=d8497c5c1662d16ecb6ba17e836492b159af8f5e497a9da2ffc6af14d5dd4322
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuSmFpbEZpbmUjSmFpbEZpbmU6MTAz
scope.4.kind=class
scope.4.startLine=103
scope.4.endLine=107
scope.4.semanticHash=475585c5238183cdae22a88d6cf475cbf0fb23e46d258ade7757621811afeda0
scope.5.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczoxMTA
scope.5.kind=class
scope.5.startLine=110
scope.5.endLine=116
scope.5.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.6.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6NTc
scope.6.kind=class
scope.6.startLine=57
scope.6.endLine=65
scope.6.semanticHash=7f2cd56eda30901389ecac24e05691990687d70540c4e9be4470d751f7e8452c
scope.7.id=Y2xhc3M6U3RyYXRlZ3kuUHJpb3JpdHkjUHJpb3JpdHk6ODI
scope.7.kind=class
scope.7.startLine=82
scope.7.endLine=84
scope.7.semanticHash=a2a404b2f42d68270f65d74737b7fb841f3116105222137422b6c68e3b2c5b2c
scope.8.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTo1Mw
scope.8.kind=class
scope.8.startLine=53
scope.8.endLine=54
scope.8.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.9.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjIw
scope.9.kind=field
scope.9.startLine=20
scope.9.endLine=21
scope.9.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.10.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNhdmFpbGFibGU6OTc
scope.10.kind=field
scope.10.startLine=97
scope.10.endLine=97
scope.10.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.11.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNob3RlbDo5Nw
scope.11.kind=field
scope.11.startLine=97
scope.11.endLine=97
scope.11.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.12.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNsYW5kOjk3
scope.12.kind=field
scope.12.startLine=97
scope.12.endLine=97
scope.12.semanticHash=35f9733d561459c6d821f37d194db007baffa148da826ae273a60a12e0e55476
scope.13.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNwcmljZTo5Nw
scope.13.kind=field
scope.13.startLine=97
scope.13.endLine=97
scope.13.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.14.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQU5OT1RfQUZGT1JEOjY4
scope.14.kind=field
scope.14.startLine=68
scope.14.endLine=68
scope.14.semanticHash=0562a15b653fe383269ebd77aa29e1c50390797b07f7a8166e9de3b296c3fc21
scope.15.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQVNIX1JFU0VSVkU6Njk
scope.15.kind=field
scope.15.startLine=69
scope.15.endLine=69
scope.15.semanticHash=c36ca0607e5d5b006445adf3206eeb33ccc63ab058d0b09a3b5a0f1580409514
scope.16.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNOT19CVVlJTkdfUE9MSUNZOjcw
scope.16.kind=field
scope.16.startLine=70
scope.16.endLine=70
scope.16.semanticHash=573981a3034acaf2d9db7c0ea32b36b35c43345919b99c88bb91e7feb003d593
scope.17.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYW1vdW50OjEwMw
scope.17.kind=field
scope.17.startLine=103
scope.17.endLine=103
scope.17.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.18.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYXZhaWxhYmxlOjEwMw
scope.18.kind=field
scope.18.startLine=103
scope.18.endLine=103
scope.18.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.19.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjExMw
scope.19.kind=field
scope.19.startLine=113
scope.19.endLine=113
scope.19.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.20.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjU3
scope.20.kind=field
scope.20.startLine=57
scope.20.endLine=57
scope.20.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.21.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDo1Nw
scope.21.kind=field
scope.21.startLine=57
scope.21.endLine=57
scope.21.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.22.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjcmVzZXJ2ZTo1Nw
scope.22.kind=field
scope.22.startLine=57
scope.22.endLine=57
scope.22.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.23.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjdXRpbGl0eU1vbm9wb2x5T3Bwb3J0dW5pdHk6NTc
scope.23.kind=field
scope.23.startLine=57
scope.23.endLine=57
scope.23.semanticHash=4827abe12096fb0602e87255e1235bbd76c30e6faad4101e14d7c13a22b76c8b
scope.24.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjSElHSEVTVDo4Mw
scope.24.kind=field
scope.24.startLine=83
scope.24.endLine=83
scope.24.semanticHash=f18742671a135cae02cfdafc9c39c14bcbb737bac94d266b118d444b3a81fa62
scope.25.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjTE9XRVNUOjgz
scope.25.kind=field
scope.25.startLine=83
scope.25.endLine=83
scope.25.semanticHash=466a4131f1e6ce62c929316d2b7fefa9339c0c6cc0df60c6db1c10407fd2892c
scope.26.id=ZmllbGQ6U3RyYXRlZ3kuUHJpb3JpdHkjTUlERExFOjgz
scope.26.kind=field
scope.26.startLine=83
scope.26.endLine=83
scope.26.semanticHash=b0db1518f3cdfd4db883854f40d1c2b32f687162d0ffb4a5c70637de157a67b7
scope.27.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDo1Mw
scope.27.kind=field
scope.27.startLine=53
scope.27.endLine=53
scope.27.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.28.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6NTM
scope.28.kind=field
scope.28.startLine=53
scope.28.endLine=53
scope.28.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.29.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDo1Mw
scope.29.kind=field
scope.29.startLine=53
scope.29.endLine=53
scope.29.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.30.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MjQ
scope.30.kind=method
scope.30.startLine=24
scope.30.endLine=26
scope.30.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.31.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKTozMw
scope.31.kind=method
scope.31.startLine=33
scope.31.endLine=35
scope.31.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.32.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvckRpc3RyZXNzZWQoNik6OTE
scope.32.kind=method
scope.32.startLine=91
scope.32.endLine=94
scope.32.semanticHash=e4ab55653e41003e36f416113d350aa58b42e0c3740660f35a59ecbac92d2a71
scope.33.id=bWV0aG9kOlN0cmF0ZWd5I2J1aWxkcygxKTo0Mw
scope.33.kind=method
scope.33.startLine=43
scope.33.endLine=45
scope.33.semanticHash=f7d3cc98f5a772d00d27eb7defef23be2865369310099a11a3c5b00ca3f3cae6
scope.34.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDApOjcz
scope.34.kind=method
scope.34.startLine=73
scope.34.endLine=75
scope.34.semanticHash=ad759992f3e478a058748ed385db64fd90320432890c3e55b970807c7b591a56
scope.35.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDMpOjc4
scope.35.kind=method
scope.35.startLine=78
scope.35.endLine=80
scope.35.semanticHash=ef54b4fa5744bd18de2343911b27ab8095fcfb809b8bdf5fa90f1644d9e6e640
scope.36.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKTozOA
scope.36.kind=method
scope.36.startLine=38
scope.36.endLine=40
scope.36.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.37.id=bWV0aG9kOlN0cmF0ZWd5I2RlY2xpbmVSZWFzb24oMSk6Mjg
scope.37.kind=method
scope.37.startLine=28
scope.37.endLine=30
scope.37.semanticHash=b21810ac08d8bf76c5f02fc132ba14d1bc098521837310c97dbe43ebacc7dae6
scope.38.id=bWV0aG9kOlN0cmF0ZWd5I3BheXMoMSk6NDg
scope.38.kind=method
scope.38.startLine=48
scope.38.endLine=50
scope.38.semanticHash=9665aac958731b28c9d13787f8f077161467c026d849a454887fc1614714e7b1
scope.39.id=bWV0aG9kOlN0cmF0ZWd5I3ByaW9yaXR5KDEpOjg2
scope.39.kind=method
scope.39.startLine=86
scope.39.endLine=88
scope.39.semanticHash=926a6dd97684bd51f06e7ef41dee3d103d5130ff0c8513083cf4042fefdab937
scope.40.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjIw
scope.40.kind=method
scope.40.startLine=1
scope.40.endLine=117
scope.40.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
scope.41.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjY3Rvcig0KTo5Nw
scope.41.kind=method
scope.41.startLine=1
scope.41.endLine=117
scope.41.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
scope.42.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjaXNBZmZvcmRhYmxlKDApOjk4
scope.42.kind=method
scope.42.startLine=98
scope.42.endLine=100
scope.42.semanticHash=e14c79b46e24f2513da2fac747d83f31ba0673fea6ee146454f89007dcf0d6a9
scope.43.id=bWV0aG9kOlN0cmF0ZWd5LkRlY2xpbmVSZWFzb24jY3RvcigwKTo2Nw
scope.43.kind=method
scope.43.startLine=1
scope.43.endLine=117
scope.43.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
scope.44.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2N0b3IoMik6MTAz
scope.44.kind=method
scope.44.startLine=1
scope.44.endLine=117
scope.44.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
scope.45.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2lzQWZmb3JkYWJsZSgwKToxMDQ
scope.45.kind=method
scope.45.startLine=104
scope.45.endLine=106
scope.45.semanticHash=dff0ac3387fd0017fbe557e0ceb9169463894f542c375ca972afc49bc2a777e7
scope.46.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6MTE1
scope.46.kind=method
scope.46.startLine=115
scope.46.endLine=115
scope.46.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.47.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6NTg
scope.47.kind=method
scope.47.startLine=58
scope.47.endLine=60
scope.47.semanticHash=e3ad9be7fc0e9555f8686dffdf5eb9f7835da5074bb34b923f39f2c52eddc72d
scope.48.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoNCk6NTc
scope.48.kind=method
scope.48.startLine=1
scope.48.endLine=117
scope.48.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
scope.49.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTo2Mg
scope.49.kind=method
scope.49.startLine=62
scope.49.endLine=64
scope.49.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.50.id=bWV0aG9kOlN0cmF0ZWd5LlByaW9yaXR5I2N0b3IoMCk6ODI
scope.50.kind=method
scope.50.startLine=1
scope.50.endLine=117
scope.50.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
scope.51.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjUz
scope.51.kind=method
scope.51.startLine=1
scope.51.endLine=117
scope.51.semanticHash=86e26d916f15396e685702e4dbbb8a1f96cdeb4a756b0863ea58482786ff88a3
*/
