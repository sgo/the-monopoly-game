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
moduleHash=801dc271fe82ed3b96ea919b9004ab3dd23cf5b1e126e71d7b4d8f9bc9caf739
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTQ
scope.0.kind=class
scope.0.startLine=14
scope.0.endLine=65
scope.0.semanticHash=b1cfe18dec265f9f629b5d1b140beb6c7af8acf040b86f734e032ddff39a3936
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoxNg
scope.1.kind=class
scope.1.startLine=16
scope.1.endLine=17
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuQnVpbGRPZmZlciNCdWlsZE9mZmVyOjUx
scope.2.kind=class
scope.2.startLine=51
scope.2.endLine=55
scope.2.semanticHash=ed3ceeeb156aea50b62f3f6599d6d4144af1f8d29586c01c219259c33849bb19
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczo1OA
scope.3.kind=class
scope.3.startLine=58
scope.3.endLine=64
scope.3.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6NDQ
scope.4.kind=class
scope.4.startLine=44
scope.4.endLine=48
scope.4.semanticHash=43c8d98d308f61f25683ad7027f6a73257b39dda5f69034e6248e64fa80d5ae0
scope.5.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTo0MA
scope.5.kind=class
scope.5.startLine=40
scope.5.endLine=41
scope.5.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.6.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjE2
scope.6.kind=field
scope.6.startLine=16
scope.6.endLine=17
scope.6.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.7.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNhdmFpbGFibGU6NTE
scope.7.kind=field
scope.7.startLine=51
scope.7.endLine=51
scope.7.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.8.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNob3RlbDo1MQ
scope.8.kind=field
scope.8.startLine=51
scope.8.endLine=51
scope.8.semanticHash=c21fcfc97b1a6da4cb2f0e69272a837902ba8c9a1c749e9e9a52d66d2c3f5006
scope.9.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNsYW5kOjUx
scope.9.kind=field
scope.9.startLine=51
scope.9.endLine=51
scope.9.semanticHash=35f9733d561459c6d821f37d194db007baffa148da826ae273a60a12e0e55476
scope.10.id=ZmllbGQ6U3RyYXRlZ3kuQnVpbGRPZmZlciNwcmljZTo1MQ
scope.10.kind=field
scope.10.startLine=51
scope.10.endLine=51
scope.10.semanticHash=09b57537b3450740954004cfbb30b3928ee2ab7435093259934c13b6f0529ea0
scope.11.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjYx
scope.11.kind=field
scope.11.startLine=61
scope.11.endLine=61
scope.11.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.12.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjQ0
scope.12.kind=field
scope.12.startLine=44
scope.12.endLine=44
scope.12.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.13.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDo0NA
scope.13.kind=field
scope.13.startLine=44
scope.13.endLine=44
scope.13.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.14.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDo0MA
scope.14.kind=field
scope.14.startLine=40
scope.14.endLine=40
scope.14.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.15.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6NDA
scope.15.kind=field
scope.15.startLine=40
scope.15.endLine=40
scope.15.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.16.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDo0MA
scope.16.kind=field
scope.16.startLine=40
scope.16.endLine=40
scope.16.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.17.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MjA
scope.17.kind=method
scope.17.startLine=20
scope.17.endLine=22
scope.17.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.18.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKToyNQ
scope.18.kind=method
scope.18.startLine=25
scope.18.endLine=27
scope.18.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.19.id=bWV0aG9kOlN0cmF0ZWd5I2J1aWxkcygxKTozNQ
scope.19.kind=method
scope.19.startLine=35
scope.19.endLine=37
scope.19.semanticHash=f7d3cc98f5a772d00d27eb7defef23be2865369310099a11a3c5b00ca3f3cae6
scope.20.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKTozMA
scope.20.kind=method
scope.20.startLine=30
scope.20.endLine=32
scope.20.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.21.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjE2
scope.21.kind=method
scope.21.startLine=1
scope.21.endLine=65
scope.21.semanticHash=00663ef0f70403f8ee0aa7cbf05838a18d4c5023611724c6548d5e02f1670ee4
scope.22.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjY3Rvcig0KTo1MQ
scope.22.kind=method
scope.22.startLine=1
scope.22.endLine=65
scope.22.semanticHash=00663ef0f70403f8ee0aa7cbf05838a18d4c5023611724c6548d5e02f1670ee4
scope.23.id=bWV0aG9kOlN0cmF0ZWd5LkJ1aWxkT2ZmZXIjaXNBZmZvcmRhYmxlKDApOjUy
scope.23.kind=method
scope.23.startLine=52
scope.23.endLine=54
scope.23.semanticHash=e14c79b46e24f2513da2fac747d83f31ba0673fea6ee146454f89007dcf0d6a9
scope.24.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6NjM
scope.24.kind=method
scope.24.startLine=63
scope.24.endLine=63
scope.24.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.25.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6NDQ
scope.25.kind=method
scope.25.startLine=1
scope.25.endLine=65
scope.25.semanticHash=00663ef0f70403f8ee0aa7cbf05838a18d4c5023611724c6548d5e02f1670ee4
scope.26.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTo0NQ
scope.26.kind=method
scope.26.startLine=45
scope.26.endLine=47
scope.26.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.27.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjQw
scope.27.kind=method
scope.27.startLine=1
scope.27.endLine=65
scope.27.semanticHash=00663ef0f70403f8ee0aa7cbf05838a18d4c5023611724c6548d5e02f1670ee4
*/
