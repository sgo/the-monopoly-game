package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.strategies.Greedo;
import the.monopoly.game.strategies.Billionaire;
import the.monopoly.game.strategies.Strategy;

/**
 * Turns the words a feature uses into the domain's own names. Features spell
 * multi-word names with spaces where the domain uses underscores.
 */
final class Vocabulary {
  private Vocabulary() {
  }

  static Street.Kind kind(String name) {
    return constant(Street.Kind.class, name, "space type");
  }

  /** A colour group, or {@code null} for the "-" a colourless space carries. */
  static Street.Colour colour(String name) {
    return name.equals("-") ? null : constant(Street.Colour.class, name, "colour group");
  }

  static Rule.Set.Type ruleSet(String name) {
    return constant(Rule.Set.Type.class, name, "rule set");
  }

  /** A strategy by the name the features call it, which is the name it is published under. */
  static Strategy strategy(String name) {
    return switch (name) {
      case "Greedo" -> new Greedo();
      case "billionaire", "Billionaire" -> new Billionaire();
      default -> throw new AssertionError("Unknown strategy \"" + name + "\".");
    };
  }

  /** A strategy by name, keeping a reserve, as the features call it. */
  static Strategy strategyWithReserve(String name, Money reserve,
                                      boolean stalemateTrading, boolean legalEntityTrading) {
    return switch (name) {
      case "Greedo" -> new Greedo(reserve, stalemateTrading, legalEntityTrading);
      case "billionaire", "Billionaire" -> new Billionaire(reserve, stalemateTrading, legalEntityTrading);
      default -> throw new AssertionError("Unknown strategy \"" + name + "\".");
    };
  }

  private static <T extends Enum<T>> T constant(Class<T> type, String name, String description) {
    try {
      return Enum.valueOf(type, name.replace(' ', '_'));
    } catch (IllegalArgumentException cause) {
      throw new AssertionError("Unknown " + description + " \"" + name + "\".", cause);
    }
  }
}
