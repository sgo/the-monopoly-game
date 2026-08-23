package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;

import java.util.Map;
import java.util.Set;

import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.idOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.money;

/** A concrete journal entry for each event type a scenario names by its identifier. */
final class SampleEvents {
  private SampleEvents() {
  }

  static Entry of(String eventType) {
    return switch (eventType) {
      case "player_buys_property" -> of(eventType, Map.of(
          "player", "dog", "land", "Diestsestraat Leuven", "price", "60"));
      case "player_pays_rent" -> of(eventType, Map.of(
          "tenant", "dog", "owner", "high hat", "land", "Diestsestraat Leuven", "rent", "4"));
      case "player_passes_go" -> of(eventType, Map.of("player", "dog", "salary", "200"));
      case "player_draws_card" -> of(eventType, Map.of(
          "player", "dog", "card", "Advance to Go"));
      default -> throw unsupported(eventType);
    };
  }

  /**
   * Builds an entry for a logging fixture with values supplied by the scenario.
   * This is deliberately a test adapter: it constructs an entry for rendering
   * tests and does not exercise any game rule or journal-emission path.
   */
  static Entry of(String eventType, Map<String, String> values) {
    return switch (eventType) {
      case "player_buys_property" -> {
        shape(values, "player", "land", "price");
        yield new Entry.Bought(id(values, "player"), land(values, "land"), amount(values, "price"));
      }
      case "player_pays_rent" -> {
        shape(values, "tenant", "owner", "land", "rent");
        yield new Entry.RentPaid(
            id(values, "tenant"), id(values, "owner"), land(values, "land"), amount(values, "rent"));
      }
      case "player_passes_go" -> {
        shape(values, "player", "salary");
        yield new Entry.SalaryCollected(id(values, "player"), amount(values, "salary"));
      }
      case "player_draws_card" -> {
        shape(values, "player", "card");
        yield new Entry.ChanceCardDrawn(id(values, "player"), value(values, "card"));
      }
      default -> throw unsupported(eventType);
    };
  }

  private static Player.ID id(Map<String, String> values, String field) {
    return idOf(value(values, field));
  }

  private static Street.Type land(Map<String, String> values, String field) {
    return SpaceNames.of(value(values, field));
  }

  private static the.monopoly.game.components.finance.Money amount(
      Map<String, String> values, String field) {
    return money(value(values, field));
  }

  private static String value(Map<String, String> values, String field) {
    String value = values.get(field);
    if (value == null || value.isBlank())
      throw new AssertionError("Event field \"" + field + "\" is required.");
    return value;
  }

  private static void shape(Map<String, String> values, String... fields) {
    Set<String> expected = Set.of(fields);
    if (!values.keySet().equals(expected))
      throw new AssertionError("Event fields " + values.keySet() + " do not match required fields " + expected + ".");
  }

  private static AssertionError unsupported(String eventType) {
    return new AssertionError("No sample event for type \"" + eventType + "\".");
  }
}
