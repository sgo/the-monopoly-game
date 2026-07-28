package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

/**
 * Land nobody owns is offered to whoever stops on it at the price on the board,
 * and auctioned to the whole table when they turn it down. The player who
 * turned it down bids at that auction like anyone else.
 */
public class LandSale implements Landings {
  private final Deeds deeds;
  private final List<Player> table;
  private final Strategy.OfPlayers strategies;
  private final Events events;

  public LandSale(Deeds deeds, List<Player> table, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.table = table;
    this.strategies = strategies;
    this.events = events;
  }

  @Override
  public void resolve(Player player, Street space) {
    if (!(space instanceof Ownable land) || !deeds.isUnowned(land.type())) return;

    if (strategies.forPlayer(player).accepts(offerTo(player, land))) {
      deeds.sell(land, player, land.price());
      events.bought(player, land, land.price());
      return;
    }
    auction(land);
  }

  /**
   * Everyone says what the land is worth to them and the best bid takes it. A
   * bid has to beat the one before it to win, so nothing is not a bid and two
   * players wanting it equally leaves it with whoever spoke first.
   */
  private void auction(Ownable land) {
    Player winner = null;
    Money winningBid = Money.ZERO;
    for (Player bidder : table) {
      Money bid = strategies.forPlayer(bidder).bidFor(offerTo(bidder, land));
      if (bid.exceeds(winningBid)) {
        winner = bidder;
        winningBid = bid;
      }
    }
    if (winner == null) return;

    deeds.sell(land, winner, winningBid);
    events.wonAtAuction(winner, land, winningBid);
  }

  private Strategy.Offer offerTo(Player player, Ownable land) {
    return new Strategy.Offer(land, player.account().balance().amount());
  }

  /** What a sale did, for whoever is keeping an account of the game. */
  public interface Events {
    void bought(Player buyer, Ownable land, Money price);

    void wonAtAuction(Player winner, Ownable land, Money price);
  }
}

/* mutate4java-manifest
version=1
moduleHash=4afb7be5070f5556d77c62c6918f0365ba0700683166b0b42f7c50c66f4ffcde
scope.0.id=Y2xhc3M6TGFuZFNhbGUjTGFuZFNhbGU6MTY
scope.0.kind=class
scope.0.startLine=16
scope.0.endLine=72
scope.0.semanticHash=82e2d22ea790b23bd9d0989202667bff513111fb597f036e011e757b66ecf72c
scope.1.id=Y2xhc3M6TGFuZFNhbGUuRXZlbnRzI0V2ZW50czo2Nw
scope.1.kind=class
scope.1.startLine=67
scope.1.endLine=71
scope.1.semanticHash=de808758da5352b42fbe291c77b6882be142260ad8bfe396c1483fff07cbbce3
scope.2.id=ZmllbGQ6TGFuZFNhbGUjZGVlZHM6MTc
scope.2.kind=field
scope.2.startLine=17
scope.2.endLine=17
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6TGFuZFNhbGUjZXZlbnRzOjIw
scope.3.kind=field
scope.3.startLine=20
scope.3.endLine=20
scope.3.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.4.id=ZmllbGQ6TGFuZFNhbGUjc3RyYXRlZ2llczoxOQ
scope.4.kind=field
scope.4.startLine=19
scope.4.endLine=19
scope.4.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.5.id=ZmllbGQ6TGFuZFNhbGUjdGFibGU6MTg
scope.5.kind=field
scope.5.startLine=18
scope.5.endLine=18
scope.5.semanticHash=0c984fdd21bb92570e4346cbc3a211c2477f5af1f79acf6ef85c7fa510a2ede8
scope.6.id=bWV0aG9kOkxhbmRTYWxlI2F1Y3Rpb24oMSk6NDY
scope.6.kind=method
scope.6.startLine=46
scope.6.endLine=60
scope.6.semanticHash=c222f34ceaf8afb13bb08236524eb477a950eb44005ffc6ff7fbe322a58b50d0
scope.7.id=bWV0aG9kOkxhbmRTYWxlI2N0b3IoNCk6MjI
scope.7.kind=method
scope.7.startLine=22
scope.7.endLine=27
scope.7.semanticHash=b70785917180f32ab0716e668422117fcbd772ec55e1f6c3e14f0b6553bea783
scope.8.id=bWV0aG9kOkxhbmRTYWxlI29mZmVyVG8oMik6NjI
scope.8.kind=method
scope.8.startLine=62
scope.8.endLine=64
scope.8.semanticHash=e8ed8ed9567f9646a101ebdc32a68317033632664dde8dcc1c33703f30ccdfab
scope.9.id=bWV0aG9kOkxhbmRTYWxlI3Jlc29sdmUoMik6Mjk
scope.9.kind=method
scope.9.startLine=29
scope.9.endLine=39
scope.9.semanticHash=766b1bdbe798ee954b2b5450b99683d175f47c2c3a4301af168d441ece991c48
scope.10.id=bWV0aG9kOkxhbmRTYWxlLkV2ZW50cyNib3VnaHQoMyk6Njg
scope.10.kind=method
scope.10.startLine=68
scope.10.endLine=68
scope.10.semanticHash=dd17383fd825e39dfa7a9ebd759a59553e677c9f5485a2caff87685ab750485a
scope.11.id=bWV0aG9kOkxhbmRTYWxlLkV2ZW50cyN3b25BdEF1Y3Rpb24oMyk6NzA
scope.11.kind=method
scope.11.startLine=70
scope.11.endLine=70
scope.11.semanticHash=2fedea6d0f37cb8261582b65a40ff24a537f46f8bfca22da42212a5e514662d7
*/
