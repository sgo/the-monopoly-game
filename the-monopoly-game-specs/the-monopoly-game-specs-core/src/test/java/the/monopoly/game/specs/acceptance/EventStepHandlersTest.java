package the.monopoly.game.specs.acceptance;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventStepHandlersTest {
  private static final List<Step> DIRECT_STEPS = List.of(
      new Step("Given", "a game with a bought event for pawn \"dog\" on \"Diestsestraat Leuven\" for $60"),
      new Step("Given", "a game with a rent-paid event for pawn \"dog\" to pawn \"high hat\" on \"Diestsestraat Leuven\" for $4"),
      new Step("Given", "a game with a salary-collected event for pawn \"dog\" of $200"),
      new Step("Given", "a game with a chance-card-drawn event for pawn \"dog\" named \"Advance to Go\""));

  @Test
  void directEventVocabularyBuildsEachSupportedEntryShape() {
    for (Step step : DIRECT_STEPS) {
      StepHandler handler = EventStepHandlers.handlers().stream()
          .filter(candidate -> candidate.matches(step.keyword(), step.text()))
          .findFirst()
          .orElseThrow(() -> new AssertionError("No handler for: " + step.text()));
      World world = new World();

      assertDoesNotThrow(() -> handler.execute(world, step.text(), Map.of()));
      assertDoesNotThrow(world::renderSelectedEventForReport);
    }
  }

  @Test
  void directEventVocabularyKeepsFieldsIndependentlyCapturable() {
    StepHandler handler = EventStepHandlers.handlers().stream()
        .filter(candidate -> candidate.matches("Given",
            "a game with a rent-paid event for pawn \"<tenant>\" to pawn \"<owner>\" on \"<land>\" for $<rent>"))
        .findFirst()
        .orElseThrow();

    assertTrue(handler.matches("Given",
        "a game with a rent-paid event for pawn \"<tenant>\" to pawn \"<owner>\" on \"<land>\" for $<rent>"));
  }

  private record Step(String keyword, String text) {
  }
}
