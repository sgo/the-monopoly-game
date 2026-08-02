package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;

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

    public DeclineReason declineReason() {
      return isAffordable() ? DeclineReason.CASH_RESERVE : DeclineReason.CANNOT_AFFORD;
    }
  }

  enum DeclineReason {
    CANNOT_AFFORD,
    CASH_RESERVE
  }

  default Money cashReserve() {
    return Money.ZERO;
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
moduleHash=b5df6398fd07ba13f24314c8395feb5715113c85359edfc2f7d804cf2460e110
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTQ
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=93
scope.0.semanticHash=5a29227a0365c90e49f052e2b70bcb017f9720e5e8b66dc9bf05bf814cb0c0d9
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoxNg
scope.1.kind=class
scope.1.startLine=16
scope.1.endLine=17
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuQnVpbGRPZmZlciNCdWlsZE9mZmVyOjcz
scope.2.kind=class
scope.2.startLine=73
scope.2.endLine=77
scope.2.semanticHash=ed3ceeeb156aea50b62f3f6599d6d4144af1f8d29586c01c219259c33849bb19
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNEZWNsaW5lUmVhc29uOjYz
scope.3.kind=class
scope.3.startLine=63
scope.3.endLine=66
scope.3.semanticHash=07db87da0881578ea61ac516ccffb8d308e62066fa8273a591b32aa570abd4d7
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuSmFpbEZpbmUjSmFpbEZpbmU6Nzk
scope.4.kind=class
scope.4.startLine=79
scope.4.endLine=83
scope.4.semanticHash=475585c5238183cdae22a88d6cf475cbf0fb23e46d258ade7757621811afeda0
scope.5.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczo4Ng
scope.5.kind=class
scope.5.startLine=86
scope.5.endLine=92
scope.5.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.6.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6NDk
scope.6.kind=class
scope.6.startLine=49
scope.6.endLine=61
scope.6.semanticHash=1ff610b05d801b1bbdaebf5c8dc04ac1fa1f07890e831c95c5b63977af6885d1
scope.7.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTo0NQ
scope.7.kind=class
scope.7.startLine=45
scope.7.endLine=46
scope.7.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.8.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjE2
scope.8.kind=field
scope.8.startLine=16
scope.8.endLine=17
scope.8.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.9.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNhdmFpbGFibGU6NzM
scope.9.kind=field
scope.9.startLine=73
scope.9.endLine=73
scope.9.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.10.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNob3RlbDo3Mw
scope.10.kind=field
scope.10.startLine=73
scope.10.endLine=73
scope.10.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.11.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNsYW5kOjcz
scope.11.kind=field
scope.11.startLine=73
scope.11.endLine=73
scope.11.semanticHash=35f9733d561459c6d821f37d194db007baffa148da826ae273a60a12e0e55476
scope.12.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNwcmljZTo3Mw
scope.12.kind=field
scope.12.startLine=73
scope.12.endLine=73
scope.12.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.13.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQU5OT1RfQUZGT1JEOjY0
scope.13.kind=field
scope.13.startLine=64
scope.13.endLine=64
scope.13.semanticHash=0562a15b653fe383269ebd77aa29e1c50390797b07f7a8166e9de3b296c3fc21
scope.14.id=ZmllbGQ6U3RyYXRlZ3kuRGVjbGluZVJlYXNvbiNDQVNIX1JFU0VSVkU6NjU
scope.14.kind=field
scope.14.startLine=65
scope.14.endLine=65
scope.14.semanticHash=c36ca0607e5d5b006445adf3206eeb33ccc63ab058d0b09a3b5a0f1580409514
scope.15.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYW1vdW50Ojc5
scope.15.kind=field
scope.15.startLine=79
scope.15.endLine=79
scope.15.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.16.id=ZmllbGQ6U3RyYXRlZ3kuSmFpbEZpbmUjYXZhaWxhYmxlOjc5
scope.16.kind=field
scope.16.startLine=79
scope.16.endLine=79
scope.16.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.17.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjg5
scope.17.kind=field
scope.17.startLine=89
scope.17.endLine=89
scope.17.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.18.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjQ5
scope.18.kind=field
scope.18.startLine=49
scope.18.endLine=49
scope.18.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.19.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDo0OQ
scope.19.kind=field
scope.19.startLine=49
scope.19.endLine=49
scope.19.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.20.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjcmVzZXJ2ZTo0OQ
scope.20.kind=field
scope.20.startLine=49
scope.20.endLine=49
scope.20.semanticHash=b5109b92832f380cb970fb1ecc65b11f7e4f319de473f9a8c1543f483bbab100
scope.21.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjdXRpbGl0eU1vbm9wb2x5T3Bwb3J0dW5pdHk6NDk
scope.21.kind=field
scope.21.startLine=49
scope.21.endLine=49
scope.21.semanticHash=4827abe12096fb0602e87255e1235bbd76c30e6faad4101e14d7c13a22b76c8b
scope.22.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDo0NQ
scope.22.kind=field
scope.22.startLine=45
scope.22.endLine=45
scope.22.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.23.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6NDU
scope.23.kind=field
scope.23.startLine=45
scope.23.endLine=45
scope.23.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.24.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDo0NQ
scope.24.kind=field
scope.24.startLine=45
scope.24.endLine=45
scope.24.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.25.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MjA
scope.25.kind=method
scope.25.startLine=20
scope.25.endLine=22
scope.25.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.26.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKToyNQ
scope.26.kind=method
scope.26.startLine=25
scope.26.endLine=27
scope.26.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.27.id=bWV0aG9kOlN0cmF0ZWd5I2J1aWxkcygxKTozNQ
scope.27.kind=method
scope.27.startLine=35
scope.27.endLine=37
scope.27.semanticHash=f7d3cc98f5a772d00d27eb7defef23be2865369310099a11a3c5b00ca3f3cae6
scope.28.id=bWV0aG9kOlN0cmF0ZWd5I2Nhc2hSZXNlcnZlKDApOjY4
scope.28.kind=method
scope.28.startLine=68
scope.28.endLine=70
scope.28.semanticHash=ad759992f3e478a058748ed385db64fd90320432890c3e55b970807c7b591a56
scope.29.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKTozMA
scope.29.kind=method
scope.29.startLine=30
scope.29.endLine=32
scope.29.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.30.id=bWV0aG9kOlN0cmF0ZWd5I3BheXMoMSk6NDA
scope.30.kind=method
scope.30.startLine=40
scope.30.endLine=42
scope.30.semanticHash=9665aac958731b28c9d13787f8f077161467c026d849a454887fc1614714e7b1
scope.31.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjE2
scope.31.kind=method
scope.31.startLine=1
scope.31.endLine=93
scope.31.semanticHash=3649eab2314a463560150fff8ab169f5855751494dbf88752ecdb143cb44a802
scope.32.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjY3Rvcig0KTo3Mw
scope.32.kind=method
scope.32.startLine=1
scope.32.endLine=93
scope.32.semanticHash=3649eab2314a463560150fff8ab169f5855751494dbf88752ecdb143cb44a802
scope.33.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjaXNBZmZvcmRhYmxlKDApOjc0
scope.33.kind=method
scope.33.startLine=74
scope.33.endLine=76
scope.33.semanticHash=e14c79b46e24f2513da2fac747d83f31ba0673fea6ee146454f89007dcf0d6a9
scope.34.id=bWV0aG9kOlN0cmF0ZWd5LkRlY2xpbmVSZWFzb24jY3RvcigwKTo2Mw
scope.34.kind=method
scope.34.startLine=1
scope.34.endLine=93
scope.34.semanticHash=3649eab2314a463560150fff8ab169f5855751494dbf88752ecdb143cb44a802
scope.35.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2N0b3IoMik6Nzk
scope.35.kind=method
scope.35.startLine=1
scope.35.endLine=93
scope.35.semanticHash=3649eab2314a463560150fff8ab169f5855751494dbf88752ecdb143cb44a802
scope.36.id=bWV0aG9kOlN0cmF0ZWd5LkphaWxGaW5lI2lzQWZmb3JkYWJsZSgwKTo4MA
scope.36.kind=method
scope.36.startLine=80
scope.36.endLine=82
scope.36.semanticHash=dff0ac3387fd0017fbe557e0ceb9169463894f542c375ca972afc49bc2a777e7
scope.37.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6OTE
scope.37.kind=method
scope.37.startLine=91
scope.37.endLine=91
scope.37.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.38.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6NTA
scope.38.kind=method
scope.38.startLine=50
scope.38.endLine=52
scope.38.semanticHash=e3ad9be7fc0e9555f8686dffdf5eb9f7835da5074bb34b923f39f2c52eddc72d
scope.39.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoNCk6NDk
scope.39.kind=method
scope.39.startLine=1
scope.39.endLine=93
scope.39.semanticHash=3649eab2314a463560150fff8ab169f5855751494dbf88752ecdb143cb44a802
scope.40.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2RlY2xpbmVSZWFzb24oMCk6NTg
scope.40.kind=method
scope.40.startLine=58
scope.40.endLine=60
scope.40.semanticHash=929f004702bc67b93eb8d8d3a57a53804d832ca8fc8197e9d2bef9735df53099
scope.41.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTo1NA
scope.41.kind=method
scope.41.startLine=54
scope.41.endLine=56
scope.41.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.42.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjQ1
scope.42.kind=method
scope.42.startLine=1
scope.42.endLine=93
scope.42.semanticHash=3649eab2314a463560150fff8ab169f5855751494dbf88752ecdb143cb44a802
*/
