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

  /** A visitor, the land they stopped on, and the rent its owner may claim. */
  record RentClaim(Player tenant, Ownable land, Money amount) {
  }

  /** The land a player is being asked about, and what they have to spend on it. */
  record Offer(Ownable land, Money available) {
    public boolean isAffordable() {
      return available.covers(land.price());
    }
  }

  /** The improvement a player is being asked to buy for a street they already own. */
  record BuildOffer(ColourStreet land, Money price, Money available, boolean hotel) {
    public boolean isAffordable() {
      return available.covers(price);
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
moduleHash=326e4f85993195b473bbd4bc239ec9d6017a1e50aa13be88761fa1ba2a4d32c7
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=52
scope.0.semanticHash=08c31f353d0e89e2f515fb75fdafb8e699a052437baa18e7f9ba416cbd93721c
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoxNQ
scope.1.kind=class
scope.1.startLine=15
scope.1.endLine=16
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczo0NQ
scope.2.kind=class
scope.2.startLine=45
scope.2.endLine=51
scope.2.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6Mzg
scope.3.kind=class
scope.3.startLine=38
scope.3.endLine=42
scope.3.semanticHash=43c8d98d308f61f25683ad7027f6a73257b39dda5f69034e6248e64fa80d5ae0
scope.4.id=Y2xhc3M6U3RyYXRlZ3kuUmVudENsYWltI1JlbnRDbGFpbTozNA
scope.4.kind=class
scope.4.startLine=34
scope.4.endLine=35
scope.4.semanticHash=be96e18726a00a7334fc1bb5c3555281345e5de8343d57b6c0acc978405306e3
scope.5.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjE1
scope.5.kind=field
scope.5.startLine=15
scope.5.endLine=16
scope.5.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.6.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjQ4
scope.6.kind=field
scope.6.startLine=48
scope.6.endLine=48
scope.6.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.7.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjM4
scope.7.kind=field
scope.7.startLine=38
scope.7.endLine=38
scope.7.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.8.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDozOA
scope.8.kind=field
scope.8.startLine=38
scope.8.endLine=38
scope.8.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.9.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2Ftb3VudDozNA
scope.9.kind=field
scope.9.startLine=34
scope.9.endLine=34
scope.9.semanticHash=b5cad57cdc32039150da77bc2b453ae0fd6c78c7c02bef90f5e430131335966a
scope.10.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI2xhbmQ6MzQ
scope.10.kind=field
scope.10.startLine=34
scope.10.endLine=34
scope.10.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.11.id=ZmllbGQ6U3RyYXRlZ3kuUmVudENsYWltI3RlbmFudDozNA
scope.11.kind=field
scope.11.startLine=34
scope.11.endLine=34
scope.11.semanticHash=f03ab23a67acec8e4db339095778b1e8cfea41719a41d12792d4d819cea02860
scope.12.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MTk
scope.12.kind=method
scope.12.startLine=19
scope.12.endLine=21
scope.12.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.13.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKToyNA
scope.13.kind=method
scope.13.startLine=24
scope.13.endLine=26
scope.13.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.14.id=bWV0aG9kOlN0cmF0ZWd5I2NsYWltcygxKToyOQ
scope.14.kind=method
scope.14.startLine=29
scope.14.endLine=31
scope.14.semanticHash=0de4b0d4da40a0870e5ed181c2e73a63e7ca91672ce5f14ac145936e8b2f7170
scope.15.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjE1
scope.15.kind=method
scope.15.startLine=1
scope.15.endLine=52
scope.15.semanticHash=f8a316995486a68a3351d7007890d1141244960506b190ac73e37e99ffbbf072
scope.16.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6NTA
scope.16.kind=method
scope.16.startLine=50
scope.16.endLine=50
scope.16.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.17.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6Mzg
scope.17.kind=method
scope.17.startLine=1
scope.17.endLine=52
scope.17.semanticHash=f8a316995486a68a3351d7007890d1141244960506b190ac73e37e99ffbbf072
scope.18.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTozOQ
scope.18.kind=method
scope.18.startLine=39
scope.18.endLine=41
scope.18.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
scope.19.id=bWV0aG9kOlN0cmF0ZWd5LlJlbnRDbGFpbSNjdG9yKDMpOjM0
scope.19.kind=method
scope.19.startLine=1
scope.19.endLine=52
scope.19.semanticHash=f8a316995486a68a3351d7007890d1141244960506b190ac73e37e99ffbbf072
*/
