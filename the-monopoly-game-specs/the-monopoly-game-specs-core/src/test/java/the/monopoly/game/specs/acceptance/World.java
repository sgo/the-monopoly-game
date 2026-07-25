package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;

/**
 * The state shared by the steps of a single scenario execution. Each execution
 * gets a fresh instance.
 */
public class World {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private Street space;

  public void select(Street.Type type) {
    space = ruleSet.create(type);
  }

  public Street space() {
    if (space == null)
      throw new AssertionError("No space has been selected yet.");
    return space;
  }
}
