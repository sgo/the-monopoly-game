package the.monopoly.game.specs.acceptance;

import org.junit.jupiter.api.Test;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.streets.Street;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.core.api.Assertions.assertThat;

class TargetedLandingStepTest {
  @Test
  void targetedLandingVocabularyRunsARealSelectedTurn() {
    World world = new World();
    world.selectRuleSet(the.monopoly.game.rules.Rule.Set.Type.official);
    world.selectPlayers(2);
    world.queueInitiativeRoll("dog", 10);
    world.queueInitiativeRoll("high hat", 4);

    StepHandler handler = MonopolyStepHandlers.handlers().stream()
        .filter(candidate -> candidate.matches("And",
            "pawn \"dog\" takes a targeted landing on \"Diestsestraat Leuven\""))
        .findFirst()
        .orElseThrow();

    assertDoesNotThrow(() -> handler.execute(world,
        "pawn \"dog\" takes a targeted landing on \"Diestsestraat Leuven\"", Map.of()));
    assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.Moved moved
        && moved.player().value().equals("dog")
        && moved.toSpace() == Street.Type.DiestsestraatLeuven);
  }
}
