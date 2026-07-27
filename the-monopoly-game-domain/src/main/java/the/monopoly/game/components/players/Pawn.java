package the.monopoly.game.components.players;

import java.util.Arrays;

/**
 * The pieces players are represented by. A player is identified by the pawn
 * they play, so the pawn's name is the player's identifier.
 */
public enum Pawn {
  dog,
  high_hat,
  iron_box,
  racecar,
  ship,
  shoe,
  thimble,
  wheelbarrow;

  /** The name the rules use, which spells multi-word pawns with a space. */
  public String pawnName() {
    return name().replace('_', ' ');
  }

  public Player.ID id() {
    return new Player.ID(pawnName());
  }

  /** The pawn of that name, or {@code null} when no pawn is called that. */
  public static Pawn named(String name) {
    return Arrays.stream(values())
        .filter(it -> it.pawnName().equals(name))
        .findFirst()
        .orElse(null);
  }
}
