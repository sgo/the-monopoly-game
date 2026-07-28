package the.monopoly.game.strategies;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.rules.Rent;

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
  default boolean claims(Rent.Claim claim) {
    return false;
  }

  /** The land a player is being asked about, and what they have to spend on it. */
  record Offer(Ownable land, Money available) {
    public boolean isAffordable() {
      return available.covers(land.price());
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
moduleHash=44b5191f860f39ab5ec23011e357d39964af8c84f9853fea687eb4ac6ff38fba
scope.0.id=Y2xhc3M6U3RyYXRlZ3kjU3RyYXRlZ3k6MTM
scope.0.kind=class
scope.0.startLine=13
scope.0.endLine=43
scope.0.semanticHash=711f63c8a2208270aa5102eb25359cdd49ee5da30a65b08d154e3ed06a1ecfe7
scope.1.id=Y2xhc3M6U3RyYXRlZ3kuIzoxNQ
scope.1.kind=class
scope.1.startLine=15
scope.1.endLine=16
scope.1.semanticHash=f43c41c22e45eb1f9525f8be15e1c07728f4ca3b5268d4e1ae476949ee958ea2
scope.2.id=Y2xhc3M6U3RyYXRlZ3kuT2ZQbGF5ZXJzI09mUGxheWVyczozNg
scope.2.kind=class
scope.2.startLine=36
scope.2.endLine=42
scope.2.semanticHash=5f8f19ae06d88ffcfc16424a4863b524eeb7e0f5fd047520027bffe020887d9e
scope.3.id=Y2xhc3M6U3RyYXRlZ3kuT2ZmZXIjT2ZmZXI6Mjk
scope.3.kind=class
scope.3.startLine=29
scope.3.endLine=33
scope.3.semanticHash=43c8d98d308f61f25683ad7027f6a73257b39dda5f69034e6248e64fa80d5ae0
scope.4.id=ZmllbGQ6U3RyYXRlZ3kjVU5ERUNJREVEOjE1
scope.4.kind=field
scope.4.startLine=15
scope.4.endLine=16
scope.4.semanticHash=f00239ff23e22226b9cac10cc3cca46af5715c686a108e5ffe8fab4442477604
scope.5.id=ZmllbGQ6U3RyYXRlZ3kuT2ZQbGF5ZXJzI05PQk9EWV9ERUNJREVTOjM5
scope.5.kind=field
scope.5.startLine=39
scope.5.endLine=39
scope.5.semanticHash=c9a5335263a411687db98645598d3bb57e8e329f99505da857a008b62ddbc8b3
scope.6.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjYXZhaWxhYmxlOjI5
scope.6.kind=field
scope.6.startLine=29
scope.6.endLine=29
scope.6.semanticHash=e9c0aa89b25447462579d56f459d8c640121904e2741c1ee9773210e7ccd3f9d
scope.7.id=ZmllbGQ6U3RyYXRlZ3kuT2ZmZXIjbGFuZDoyOQ
scope.7.kind=field
scope.7.startLine=29
scope.7.endLine=29
scope.7.semanticHash=fb98346ff204a15373005d5136a3847fc2558923d36ff56682e9dc8f4cbee921
scope.8.id=bWV0aG9kOlN0cmF0ZWd5I2FjY2VwdHMoMSk6MTk
scope.8.kind=method
scope.8.startLine=19
scope.8.endLine=21
scope.8.semanticHash=db7e918b1301c0a5e1e4e5190b554cca47be22517eb285731cc1d83f4f509bbe
scope.9.id=bWV0aG9kOlN0cmF0ZWd5I2JpZEZvcigxKToyNA
scope.9.kind=method
scope.9.startLine=24
scope.9.endLine=26
scope.9.semanticHash=68b3807991ccbffe92c86209b89fae2df2340f29d0a497561cdcd3f78e46ce79
scope.10.id=bWV0aG9kOlN0cmF0ZWd5LiNjdG9yKDApOjE1
scope.10.kind=method
scope.10.startLine=1
scope.10.endLine=43
scope.10.semanticHash=f57688a184471d3f4e6e7b60da8e383175395d2dbdce0f861833a72b626c05f4
scope.11.id=bWV0aG9kOlN0cmF0ZWd5Lk9mUGxheWVycyNmb3JQbGF5ZXIoMSk6NDE
scope.11.kind=method
scope.11.startLine=41
scope.11.endLine=41
scope.11.semanticHash=66ad4c5c63cd26d01a2387bb0854f1d342eea8ef7fa68207a3fbca7556de35b6
scope.12.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2N0b3IoMik6Mjk
scope.12.kind=method
scope.12.startLine=1
scope.12.endLine=43
scope.12.semanticHash=f57688a184471d3f4e6e7b60da8e383175395d2dbdce0f861833a72b626c05f4
scope.13.id=bWV0aG9kOlN0cmF0ZWd5Lk9mZmVyI2lzQWZmb3JkYWJsZSgwKTozMA
scope.13.kind=method
scope.13.startLine=30
scope.13.endLine=32
scope.13.semanticHash=19099acd48bb08c42f5cd2a1a4a768468607fba899c3a9614997294aeff6661e
*/
