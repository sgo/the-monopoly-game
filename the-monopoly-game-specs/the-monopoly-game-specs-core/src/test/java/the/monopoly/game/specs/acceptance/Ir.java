package the.monopoly.game.specs.acceptance;

import java.util.List;
import java.util.Map;

/**
 * The parser-produced feature representation, handed to the runtime by the
 * generated acceptance entry points. It mirrors the JSON IR described by the
 * Acceptance Pipeline Specification.
 */
public record Ir(String name, List<Step> background, List<Scenario> scenarios) {
  public record Step(String keyword, String text) {
  }

  public record Scenario(String name, List<Step> steps, List<Map<String, String>> examples) {
  }
}
