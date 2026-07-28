package the.monopoly.game.rules;

import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
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
  private final Rule.Set rules;
  private final List<Player> table;
  private final Strategy.OfPlayers strategies;
  private final Events events;

  public LandSale(Deeds deeds, Rule.Set rules, List<Player> table, Strategy.OfPlayers strategies, Events events) {
    this.deeds = deeds;
    this.rules = rules;
    this.table = table;
    this.strategies = strategies;
    this.events = events;
  }

  @Override
  public void resolve(Player player, Street space, Roll roll) {
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

  public void sell(Player seller, Ownable land, Player buyer, Money price) {
    if (saleIsRefused(land)) {
      events.saleRefused(seller, land, buyer, price);
      return;
    }
    deeds.transfer(land, seller, buyer, price);
    events.sold(seller, land, buyer, price);
  }

  private boolean saleIsRefused(Ownable land) {
    if (!(land instanceof ColourStreet street)) return false;
    return rules.streets()
        .filter(ColourStreet.class::isInstance)
        .map(ColourStreet.class::cast)
        .filter(it -> it.colourGroup() == street.colourGroup())
        .anyMatch(it -> deeds.housesBuiltOn(it) > 0 || deeds.hasHotelOn(it));
  }

  /** What a sale did, for whoever is keeping an account of the game. */
  public interface Events {
    void bought(Player buyer, Ownable land, Money price);

    void wonAtAuction(Player winner, Ownable land, Money price);

    default void sold(Player seller, Ownable land, Player buyer, Money price) {
    }

    default void saleRefused(Player seller, Ownable land, Player buyer, Money price) {
    }
  }
}

/* mutate4java-manifest
version=1
moduleHash=dcabc56961604c52fefa340d436f5bb41544fd085f755f3b1c1cd3b3fdd932f3
scope.0.id=Y2xhc3M6TGFuZFNhbGUjTGFuZFNhbGU6MTg
scope.0.kind=class
scope.0.startLine=18
scope.0.endLine=100
scope.0.semanticHash=7953edeb9c1e4911e82e916bba23ea234640d778b83d1080860176664592db76
scope.1.id=Y2xhc3M6TGFuZFNhbGUuRXZlbnRzI0V2ZW50czo4OQ
scope.1.kind=class
scope.1.startLine=89
scope.1.endLine=99
scope.1.semanticHash=c16eb33e674445a2342b9a24dfe3b1b5d69a0ca597765e62ea3d49029edc0c27
scope.2.id=ZmllbGQ6TGFuZFNhbGUjZGVlZHM6MTk
scope.2.kind=field
scope.2.startLine=19
scope.2.endLine=19
scope.2.semanticHash=c4e4ff9ea2a9a11186aece2a14009bf304e1f2857b471dea2d44eaf7828ab299
scope.3.id=ZmllbGQ6TGFuZFNhbGUjZXZlbnRzOjIz
scope.3.kind=field
scope.3.startLine=23
scope.3.endLine=23
scope.3.semanticHash=f45bca3c564ae0e8fad5f337b7ef7c92e650884f3a09bdc6888537075ec7b4bf
scope.4.id=ZmllbGQ6TGFuZFNhbGUjcnVsZXM6MjA
scope.4.kind=field
scope.4.startLine=20
scope.4.endLine=20
scope.4.semanticHash=dfcabe320a2f3509b46a3ddf63ecfed9f444220f95d95bdec8848f2dbab07105
scope.5.id=ZmllbGQ6TGFuZFNhbGUjc3RyYXRlZ2llczoyMg
scope.5.kind=field
scope.5.startLine=22
scope.5.endLine=22
scope.5.semanticHash=8f90b89a0f4f61ab31d1d05eb010b34195c3e0a9cffef75c49d25c1fcf7e77c8
scope.6.id=ZmllbGQ6TGFuZFNhbGUjdGFibGU6MjE
scope.6.kind=field
scope.6.startLine=21
scope.6.endLine=21
scope.6.semanticHash=0c984fdd21bb92570e4346cbc3a211c2477f5af1f79acf6ef85c7fa510a2ede8
scope.7.id=bWV0aG9kOkxhbmRTYWxlI2F1Y3Rpb24oMSk6NTA
scope.7.kind=method
scope.7.startLine=50
scope.7.endLine=64
scope.7.semanticHash=c222f34ceaf8afb13bb08236524eb477a950eb44005ffc6ff7fbe322a58b50d0
scope.8.id=bWV0aG9kOkxhbmRTYWxlI2N0b3IoNSk6MjU
scope.8.kind=method
scope.8.startLine=25
scope.8.endLine=31
scope.8.semanticHash=1075dbd9e06d685d2ca99595811be9d53d2c7143cc98da14af56c3d34de16829
scope.9.id=bWV0aG9kOkxhbmRTYWxlI29mZmVyVG8oMik6NjY
scope.9.kind=method
scope.9.startLine=66
scope.9.endLine=68
scope.9.semanticHash=e8ed8ed9567f9646a101ebdc32a68317033632664dde8dcc1c33703f30ccdfab
scope.10.id=bWV0aG9kOkxhbmRTYWxlI3Jlc29sdmUoMyk6MzM
scope.10.kind=method
scope.10.startLine=33
scope.10.endLine=43
scope.10.semanticHash=6b9b2af1f6f47f86e213978e43b2f5c401b52852e42b895e9b32b9f6da21856f
scope.11.id=bWV0aG9kOkxhbmRTYWxlI3NhbGVJc1JlZnVzZWQoMSk6Nzk
scope.11.kind=method
scope.11.startLine=79
scope.11.endLine=86
scope.11.semanticHash=a4925299c685a29b2c550c3bca51ca39435e2b63fc2f0633bec52e4f21cfbc6e
scope.12.id=bWV0aG9kOkxhbmRTYWxlI3NlbGwoNCk6NzA
scope.12.kind=method
scope.12.startLine=70
scope.12.endLine=77
scope.12.semanticHash=00fbe19ffe6662a6feca30b2b4b2860aa00a49df46d1a3883e3f21e0c7801eae
scope.13.id=bWV0aG9kOkxhbmRTYWxlLkV2ZW50cyNib3VnaHQoMyk6OTA
scope.13.kind=method
scope.13.startLine=90
scope.13.endLine=90
scope.13.semanticHash=dd17383fd825e39dfa7a9ebd759a59553e677c9f5485a2caff87685ab750485a
scope.14.id=bWV0aG9kOkxhbmRTYWxlLkV2ZW50cyNzYWxlUmVmdXNlZCg0KTo5Nw
scope.14.kind=method
scope.14.startLine=97
scope.14.endLine=98
scope.14.semanticHash=9e4ac71777b2a74a2b221e445d318fdf729d2763d20b39fd474ce2bfc79ea50f
scope.15.id=bWV0aG9kOkxhbmRTYWxlLkV2ZW50cyNzb2xkKDQpOjk0
scope.15.kind=method
scope.15.startLine=94
scope.15.endLine=95
scope.15.semanticHash=81155de16a5b4f1ecbc6fd66671a05a05740cb9874b82417eb5b7baef8054c87
scope.16.id=bWV0aG9kOkxhbmRTYWxlLkV2ZW50cyN3b25BdEF1Y3Rpb24oMyk6OTI
scope.16.kind=method
scope.16.startLine=92
scope.16.endLine=92
scope.16.semanticHash=2fedea6d0f37cb8261582b65a40ff24a537f46f8bfca22da42212a5e514662d7
*/
