package the.monopoly.game.specs.acceptance;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connects one shape of step text to project behaviour.
 * <p>
 * Patterns are matched against the raw step text, so a captured argument is
 * either a literal value or a {@code <placeholder>} naming a column of the
 * current example row.
 */
public record StepHandler(Pattern pattern, Body body) {
  public static StepHandler step(String pattern, Body body) {
    return new StepHandler(Pattern.compile(pattern), body);
  }

  boolean matches(String text) {
    return pattern.matcher(text).matches();
  }

  void execute(World world, String text, Map<String, String> example) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.matches())
      throw new IllegalStateException("Step text does not match: " + text);
    body.execute(world, new Arguments(matcher, example));
  }

  @FunctionalInterface
  public interface Body {
    void execute(World world, Arguments arguments);
  }

  /**
   * The captured arguments of a matched step, resolved against the example row.
   */
  public static final class Arguments {
    private static final Pattern PLACEHOLDER = Pattern.compile("<([^<>]+)>");

    private final Matcher matcher;
    private final Map<String, String> example;

    private Arguments(Matcher matcher, Map<String, String> example) {
      this.matcher = matcher;
      this.example = example;
    }

    public String text(int group) {
      String captured = matcher.group(group);
      Matcher placeholder = PLACEHOLDER.matcher(captured);
      if (!placeholder.matches()) return captured;

      String column = placeholder.group(1);
      String value = example.get(column);
      if (value == null)
        throw new AssertionError(
            "Step wants <" + column + "> but the example row only has " + example.keySet() + "."
        );
      return value;
    }

    public int number(int group) {
      String value = text(group);
      try {
        return Integer.parseInt(value.replace(",", ""));
      } catch (NumberFormatException cause) {
        throw new AssertionError("Expected a number but the step supplied \"" + value + "\".", cause);
      }
    }
  }
}
