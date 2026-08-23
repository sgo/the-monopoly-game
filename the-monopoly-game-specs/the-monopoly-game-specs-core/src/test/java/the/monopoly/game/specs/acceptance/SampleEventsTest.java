package the.monopoly.game.specs.acceptance;

import org.junit.jupiter.api.Test;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.streets.Street;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleEventsTest {
  @Test
  void buildsAnEntryFromScenarioValues() {
    Entry entry = SampleEvents.of("player_pays_rent", Map.of(
        "tenant", "cat", "owner", "dog", "land", "Diestsestraat Leuven", "rent", "18"));

    assertEquals(new Entry.RentPaid(
        MonopolyStepHelpers.idOf("cat"), MonopolyStepHelpers.idOf("dog"),
        Street.Type.DiestsestraatLeuven, MonopolyStepHelpers.money(18)), entry);
  }

  @Test
  void reportsMissingFieldsClearly() {
    AssertionError error = assertThrows(AssertionError.class,
        () -> SampleEvents.of("player_buys_property", Map.of("player", "dog")));

    assertTrue(error.getMessage().contains("do not match required fields"));
  }

  @Test
  void reportsExtraFieldsClearly() {
    AssertionError error = assertThrows(AssertionError.class,
        () -> SampleEvents.of("player_passes_go", Map.of(
            "player", "dog", "salary", "200", "unexpected", "value")));

    assertTrue(error.getMessage().contains("do not match required fields"));
  }

  @Test
  void reportsNullFieldValuesClearly() {
    Map<String, String> values = new HashMap<>();
    values.put("player", null);
    values.put("salary", "200");

    AssertionError error = assertThrows(AssertionError.class,
        () -> SampleEvents.of("player_passes_go", values));

    assertEquals("Event field \"player\" is required.", error.getMessage());
  }

  @Test
  void reportsBlankFieldValuesClearly() {
    AssertionError error = assertThrows(AssertionError.class,
        () -> SampleEvents.of("player_passes_go", Map.of("player", " ", "salary", "200")));

    assertEquals("Event field \"player\" is required.", error.getMessage());
  }

  @Test
  void rejectsUnsupportedEventTypesClearly() {
    AssertionError error = assertThrows(AssertionError.class,
        () -> SampleEvents.of("unknown_event", Map.of()));

    assertEquals("No sample event for type \"unknown_event\".", error.getMessage());
  }
}
