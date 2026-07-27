package the.monopoly.game.components.dice;

/**
 * What a player throws in one go: both dice, kept apart because the rules care
 * whether they came up the same.
 */
public record Roll(int die1, int die2) {
  public int total() {
    return die1 + die2;
  }

  public boolean isDouble() {
    return die1 == die2;
  }
}
