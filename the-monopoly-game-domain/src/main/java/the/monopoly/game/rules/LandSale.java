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
