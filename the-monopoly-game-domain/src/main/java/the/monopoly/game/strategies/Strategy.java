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
moduleHash=07450a96360d761ac1f842fb0b09c2038b3c9461b0976a8a61c7030ec934dacd
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTQ
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=95
scope.0.semanticHash=12f2c41d6da08ec0a9f3e8f198f65418a2945a6c211f4e02726abb6600ba8ca1
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoxNg
scope.1.kind=class
scope.1.startLine=16
scope.1.endLine=17
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuQnVpbGRPZmZlciNCdWlsZE9mZmVyOjc1
scope.2.kind=class
scope.2.startLine=75
scope.2.endLine=79
scope.2.semanticHash=ed3ceeeb156aea50b62f3f6599d6d4144af1f8d29586c01c219259c33849bb19
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNEZWNsaW5lUmVhc29uOjY0
scope.3.kind=class
scope.3.startLine=64
scope.3.endLine=68
scope.3.semanticHash=d8497c5c1662d16ecb6ba17e836492b159af8f5e497a9da2ffc6af14d5dd4322
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuSmFpbEZpbmUjSmFpbEZpbmU6ODE
scope.4.kind=class
scope.4.startLine=81
scope.4.endLine=85
scope.4.semanticHash=475585c5238183cdae22a88d6cf475cbf0fb23e46d258ade7757621811afeda0
scope.5.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczo4OA
scope.5.kind=class
scope.5.startLine=88
scope.5.endLine=94
scope.5.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.6.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6NTM
scope.6.kind=class
scope.6.startLine=53
scope.6.endLine=62
scope.6.semanticHash=84e9b2de41da044a6a2d5c281bc217da5bf6289649e8d62fa2725128555cd090
scope.7.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTo0OQ
scope.7.kind=class
scope.7.startLine=49
scope.7.endLine=50
scope.7.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.8.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjE2
scope.8.kind=field
scope.8.startLine=16
scope.8.endLine=17
scope.8.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.9.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNhdmFpbGFibGU6NzU
scope.9.kind=field
scope.9.startLine=75
scope.9.endLine=75
scope.9.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.10.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNob3RlbDo3NQ
scope.10.kind=field
scope.10.startLine=75
scope.10.endLine=75
scope.10.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.11.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNsYW5kOjc1
scope.11.kind=field
scope.11.startLine=75
scope.11.endLine=75
scope.11.semanticHash=35f9733d561459c6d821f37d194db007baffa148da826ae273a60a12e0e55476
scope.12.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNwcmljZTo3NQ
scope.12.kind=field
scope.12.startLine=75
scope.12.endLine=75
scope.12.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.13.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQU5OT1RfQUZGT1JEOjY1
scope.13.kind=field
scope.13.startLine=65
scope.13.endLine=65
scope.13.semanticHash=0562a15b653fe383269ebd77aa29e1c50390797b07f7a8166e9de3b296c3fc21
scope.14.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQVNIX1JFU0VSVkU6NjY
scope.14.kind=field
scope.14.startLine=66
scope.14.endLine=66
scope.14.semanticHash=c36ca0607e5d5b006445adf3206eeb33ccc63ab058d0b09a3b5a0f1580409514
scope.15.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNOT19CVVlJTkdfUE9MSUNZOjY3
scope.15.kind=field
scope.15.startLine=67
scope.15.endLine=67
scope.15.semanticHash=573981a3034acaf2d9db7c0ea32b36b35c43345919b99c88bb91e7feb003d593
scope.16.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYW1vdW50Ojgx
scope.16.kind=field
scope.16.startLine=81
scope.16.endLine=81
scope.16.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.17.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYXZhaWxhYmxlOjgx
scope.17.kind=field
scope.17.startLine=81
scope.17.endLine=81
scope.17.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.18.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjkx
scope.18.kind=field
scope.18.startLine=91
scope.18.endLine=91
scope.18.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.19.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjUz
scope.19.kind=field
scope.19.startLine=53
scope.19.endLine=53
scope.19.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.20.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDo1Mw
scope.20.kind=field
scope.20.startLine=53
scope.20.endLine=53
scope.20.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.21.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjcmVzZXJ2ZTo1Mw
scope.21.kind=field
scope.21.startLine=53
scope.21.endLine=53
scope.21.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.22.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjdXRpbGl0eU1vbm9wb2x5T3Bwb3J0dW5pdHk6NTM
scope.22.kind=field
scope.22.startLine=53
scope.22.endLine=53
scope.22.semanticHash=4827abe12096fb0602e87255e1235bbd76c30e6faad4101e14d7c13a22b76c8b
scope.23.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDo0OQ
scope.23.kind=field
scope.23.startLine=49
scope.23.endLine=49
scope.23.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.24.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6NDk
scope.24.kind=field
scope.24.startLine=49
scope.24.endLine=49
scope.24.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.25.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDo0OQ
scope.25.kind=field
scope.25.startLine=49
scope.25.endLine=49
scope.25.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.26.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MjA
scope.26.kind=method
scope.26.startLine=20
scope.26.endLine=22
scope.26.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.27.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKToyOQ
scope.27.kind=method
scope.27.startLine=29
scope.27.endLine=31
scope.27.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.28.id=bWV0aG9kOlN0cmF0ZWd5I2J1aWxkcygxKTozOQ
scope.28.kind=method
scope.28.startLine=39
scope.28.endLine=41
scope.28.semanticHash=f7d3cc98f5a772d00d27eb7defef23be2865369310099a11a3c5b00ca3f3cae6
scope.29.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDApOjcw
scope.29.kind=method
scope.29.startLine=70
scope.29.endLine=72
scope.29.semanticHash=ad759992f3e478a058748ed385db64fd90320432890c3e55b970807c7b591a56
scope.30.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKTozNA
scope.30.kind=method
scope.30.startLine=34
scope.30.endLine=36
scope.30.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.31.id=bWV0aG9kOlN0cmF0ZWd5I2RlY2xpbmVSZWFzb24oMSk6MjQ
scope.31.kind=method
scope.31.startLine=24
scope.31.endLine=26
scope.31.semanticHash=b21810ac08d8bf76c5f02fc132ba14d1bc098521837310c97dbe43ebacc7dae6
scope.32.id=bWV0aG9kOlN0cmF0ZWd5I3BheXMoMSk6NDQ
scope.32.kind=method
scope.32.startLine=44
scope.32.endLine=46
scope.32.semanticHash=9665aac958731b28c9d13787f8f077161467c026d849a454887fc1614714e7b1
scope.33.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjE2
scope.33.kind=method
scope.33.startLine=1
scope.33.endLine=95
scope.33.semanticHash=751d636c52a2f0ad0dfee4f734ef0095dbca3af28672a307afec8913de3d994e
scope.34.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjY3Rvcig0KTo3NQ
scope.34.kind=method
scope.34.startLine=1
scope.34.endLine=95
scope.34.semanticHash=751d636c52a2f0ad0dfee4f734ef0095dbca3af28672a307afec8913de3d994e
scope.35.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjaXNBZmZvcmRhYmxlKDApOjc2
scope.35.kind=method
scope.35.startLine=76
scope.35.endLine=78
scope.35.semanticHash=e14c79b46e24f2513da2fac747d83f31ba0673fea6ee146454f89007dcf0d6a9
scope.36.id=bWV0aG9kOlN0cmF0ZWd5LkRlY2xpbmVSZWFzb24jY3RvcigwKTo2NA
scope.36.kind=method
scope.36.startLine=1
scope.36.endLine=95
scope.36.semanticHash=751d636c52a2f0ad0dfee4f734ef0095dbca3af28672a307afec8913de3d994e
scope.37.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2N0b3IoMik6ODE
scope.37.kind=method
scope.37.startLine=1
scope.37.endLine=95
scope.37.semanticHash=751d636c52a2f0ad0dfee4f734ef0095dbca3af28672a307afec8913de3d994e
scope.38.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2lzQWZmb3JkYWJsZSgwKTo4Mg
scope.38.kind=method
scope.38.startLine=82
scope.38.endLine=84
scope.38.semanticHash=dff0ac3387fd0017fbe557e0ceb9169463894f542c375ca972afc49bc2a777e7
scope.39.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6OTM
scope.39.kind=method
scope.39.startLine=93
scope.39.endLine=93
scope.39.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.40.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6NTQ
scope.40.kind=method
scope.40.startLine=54
scope.40.endLine=56
scope.40.semanticHash=e3ad9be7fc0e9555f8686dffdf5eb9f7835da5074bb34b923f39f2c52eddc72d
scope.41.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoNCk6NTM
scope.41.kind=method
scope.41.startLine=1
scope.41.endLine=95
scope.41.semanticHash=751d636c52a2f0ad0dfee4f734ef0095dbca3af28672a307afec8913de3d994e
scope.42.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTo1OA
scope.42.kind=method
scope.42.startLine=58
scope.42.endLine=60
scope.42.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.43.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjQ5
scope.43.kind=method
scope.43.startLine=1
scope.43.endLine=95
scope.43.semanticHash=751d636c52a2f0ad0dfee4f734ef0095dbca3af28672a307afec8913de3d994e
*/
