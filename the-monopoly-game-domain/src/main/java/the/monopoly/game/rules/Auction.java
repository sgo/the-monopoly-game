package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;

import java.util.ArrayList;
import java.util.List;

/**
 * An auction that ascends from the land's mortgage value in $5 steps until
 * only one bidder is still willing to raise. Each bidder supplies a ceiling; a
 * sole bidder pays only the opening bid and nobody pays above their own
 * ceiling. Shared by the landing auction (an ownerless street the table can
 * buy) and the bank-forced auction of a bankrupt debtor's land.
 */
final class Auction {
  private Auction() {
  }

  /** Bidders whose ceiling meets the opening bid, with their ceilings in order. */
  static Bidders qualified(List<Player> players, Ownable land, CeilingFor ceilingFor) {
    return qualified(players, land, ceilingFor, true);
  }

  /** Bidders willing to bid at or above the opening bid (or, if {@code floorOpening}, any positive bid). */
  static Bidders qualified(List<Player> players, Ownable land, CeilingFor ceilingFor, boolean floorOpening) {
    Money opening = land.landMortgageValue();
    List<Player> bidders = new ArrayList<>();
    List<Money> ceilings = new ArrayList<>();
    for (Player candidate : players) {
      Money ceiling = ceilingFor.ceiling(candidate);
      boolean qualified = floorOpening ? ceiling.amount() >= opening.amount() : ceiling.amount() > 0;
      if (qualified) {
        bidders.add(candidate);
        ceilings.add(ceiling);
      }
    }
    return new Bidders(bidders, ceilings, opening);
  }

  @FunctionalInterface
  interface CeilingFor {
    Money ceiling(Player player);
  }

  /** Result of an ascending-rule auction among qualified bidders. A sole bidder pays the opening bid, capped at their ceiling. */
  static Result ascend(Bidders bidders) {
    if (bidders.players.size() == 1) {
      Money payment = bidders.ceilings.getFirst().amount() < bidders.opening.amount()
          ? bidders.ceilings.getFirst() : bidders.opening;
      return new Result(bidders.players.getFirst(), payment);
    }
    return ascendMany(bidders);
  }

  private static Result ascendMany(Bidders bidders) {
    Player winner = null;
    Money bid = new Money(bidders.opening.amount());
    boolean firstOffer = true;
    boolean raised;
    boolean settled;
    do {
      raised = false;
      settled = false;
      for (int index = 0; index < bidders.players.size(); index++) {
        Money offer = firstOffer ? bid : new Money(Math.max(bid.amount(), 0) + 5);
        if (offer.exceeds(bidders.ceilings.get(index))) continue;
        winner = bidders.players.get(index);
        bid = offer;
        firstOffer = false;
        raised = true;
        if (cannotRaise(bidders.ceilings, index, bid)) {
          settled = true;
          break;
        }
      }
    } while (raised && !settled);
    return new Result(winner, bid);
  }

  private static boolean cannotRaise(List<Money> ceilings, int currentBidder, Money bid) {
    for (int index = 0; index < ceilings.size(); index++) {
      if (index != currentBidder && ceilings.get(index).amount() >= bid.amount()) return false;
    }
    return true;
  }

  record Bidders(List<Player> players, List<Money> ceilings, Money opening) {
  }

  record Result(Player winner, Money bid) {
  }
}