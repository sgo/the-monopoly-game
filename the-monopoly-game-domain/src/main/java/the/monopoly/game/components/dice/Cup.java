package the.monopoly.game.components.dice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Where a roll comes from.
 * <p>
 * The rules never reach for a die directly, so replacing chance with a known
 * sequence of rolls is the one thing needed to play a game that repeats.
 */
@FunctionalInterface
public interface Cup {
  Roll roll();

  /** A cup of real dice, which needs exactly the two the rules are played with. */
  static Cup of(List<Dice> dice) {
    if (dice.size() != 2)
      throw new IllegalArgumentException("A roll is made with 2 dice, not " + dice.size() + ".");
    return () -> new Roll(faceOf(dice.get(0)), faceOf(dice.get(1)));
  }

  static Cup of(Dice first, Dice second) {
    return of(List.of(first, second));
  }

  /** A cup that yields exactly these rolls, for replaying a known game. */
  static Cup of(Roll... rolls) {
    Deque<Roll> remaining = new ArrayDeque<>(List.of(rolls));
    return () -> {
      if (remaining.isEmpty())
        throw new IllegalStateException("This cup has no more rolls left to give.");
      return remaining.removeFirst();
    };
  }

  private static int faceOf(Dice dice) {
    return Integer.parseInt(dice.roll().symbol());
  }
}
