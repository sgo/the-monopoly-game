package the.monopoly.game.rules;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Who owns which space. The rules lay the board out afresh every time they are
 * asked, so a space is a value rather than a thing that can be marked; the
 * title to it is kept here instead, against the space's type, for as long as a
 * game lasts.
 */
public class Deeds {
  private final Map<Street.Type, Player.ID> owners = new HashMap<>();

  public boolean isUnowned(Street.Type land) {
    return !owners.containsKey(land);
  }

  /** Who holds the title to this land, if anyone does. */
  public Optional<Player.ID> ownerOf(Street.Type land) {
    return Optional.ofNullable(owners.get(land));
  }

  /**
   * Hands the title to a buyer, who pays the bank what the land went for. That
   * is the price on the board when it is bought, and the winning bid when it is
   * auctioned, so the sale is told what it fetched rather than working it out.
   */
  public void sell(Ownable land, Player buyer, Money price) {
    buyer.account().credit(price);
    owners.put(land.type(), buyer.id());
  }
}
