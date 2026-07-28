package the.monopoly.game.rules;

import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

/**
 * What happens where a pawn stops. A turn moves the pawn and then hands the
 * space over, so what a space is worth stays out of the moving of pawns.
 */
@FunctionalInterface
public interface Landings {
  /** A board where stopping anywhere is worth nothing. */
  Landings UNEVENTFUL = (player, space) -> {
  };

  void resolve(Player player, Street space);
}
