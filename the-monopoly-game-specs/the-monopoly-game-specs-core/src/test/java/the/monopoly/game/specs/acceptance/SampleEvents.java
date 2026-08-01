package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.streets.Street;

import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.idOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.money;

/** A concrete journal entry for each event type a scenario names by its identifier. */
final class SampleEvents {
  private SampleEvents() {
  }

  static Entry of(String eventType) {
    return switch (eventType) {
      case "player_buys_property" ->
          new Entry.Bought(idOf("dog"), Street.Type.DiestsestraatLeuven, money(60));
      case "player_pays_rent" ->
          new Entry.RentPaid(idOf("dog"), idOf("high hat"), Street.Type.DiestsestraatLeuven, money(4));
      case "player_passes_go" -> new Entry.SalaryCollected(idOf("dog"), money(200));
      case "player_draws_card" -> new Entry.ChanceCardDrawn(idOf("dog"), "Advance to Go");
      default -> throw new AssertionError("No sample event for type \"" + eventType + "\".");
    };
  }
}
