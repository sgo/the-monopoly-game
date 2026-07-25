package the.monopoly.game.components.streets;

import java.util.List;

/**
 * Several spaces look a figure up in a table indexed by a count: how many
 * stations an owner holds, how many houses stand on a street. The table decides
 * which counts exist, so the bounds check belongs next to the lookup.
 */
final class OwnedCount {
  private OwnedCount() {
  }

  static int checked(int count, List<?> valuesByCount, Street.Type type, String counted) {
    if (count < 0 || count >= valuesByCount.size())
      throw new IllegalArgumentException(
          "Cannot have " + count + " " + counted + " on " + type
              + "; the board allows at most " + (valuesByCount.size() - 1) + "."
      );
    return count;
  }
}
