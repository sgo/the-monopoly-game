package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;

/**
 * The state shared by the steps of a single scenario execution. Each execution
 * gets a fresh instance.
 * <p>
 * A feature names a space in prose, so which kind of space it is only becomes
 * known at run time. {@link #space(Class)} is where that stays: it turns a
 * step asking a space for something it cannot do into a readable failure.
 */
public class World {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();
  private Street space;

  public void select(Street.Type type) {
    space = ruleSet.create(type);
  }

  public <T extends Street> T space(Class<T> kind) {
    if (space == null)
      throw new AssertionError("No space has been selected yet.");
    if (!kind.isInstance(space))
      throw new AssertionError(
          "This step needs a " + kind.getSimpleName() + " but \"" + space.type()
              + "\" is a " + space.kind() + " space."
      );
    return kind.cast(space);
  }
}
