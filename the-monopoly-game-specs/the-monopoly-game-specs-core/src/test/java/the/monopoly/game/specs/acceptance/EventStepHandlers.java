package the.monopoly.game.specs.acceptance;

import java.util.List;
import java.util.Map;

import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.MONEY;
import static the.monopoly.game.specs.acceptance.StepHandler.given;
import static the.monopoly.game.specs.acceptance.StepHandler.step;
import static the.monopoly.game.specs.acceptance.StepHandler.then;

/** Step vocabulary for scenarios comparing a logged event's text to its report rendering. */
final class EventStepHandlers {
  private EventStepHandlers() {
  }

  static List<StepHandler> handlers() {
    return List.of(
        given("^a game with an event of type \"" + NAME + "\"$",
            (world, arguments) -> world.selectEvent(arguments.text(1))),

        given("^a game with a bought event for pawn \"" + NAME + "\" on \"" + NAME
                + "\" for \\$" + MONEY + "$",
            (world, arguments) -> world.selectEvent("player_buys_property", Map.of(
                "player", arguments.text(1), "land", arguments.text(2), "price", arguments.text(3)))),

        given("^a game with a rent-paid event for pawn \"" + NAME + "\" to pawn \"" + NAME
                + "\" on \"" + NAME + "\" for \\$" + MONEY + "$",
            (world, arguments) -> world.selectEvent("player_pays_rent", Map.of(
                "tenant", arguments.text(1), "owner", arguments.text(2), "land", arguments.text(3),
                "rent", arguments.text(4)))),

        given("^a game with a salary-collected event for pawn \"" + NAME + "\" of \\$" + MONEY + "$",
            (world, arguments) -> world.selectEvent("player_passes_go", Map.of(
                "player", arguments.text(1), "salary", arguments.text(2)))),

        given("^a game with a chance-card-drawn event for pawn \"" + NAME + "\" named \"" + NAME + "\"$",
            (world, arguments) -> world.selectEvent("player_draws_card", Map.of(
                "player", arguments.text(1), "card", arguments.text(2)))),

        step("^the event is rendered for the report$",
            (world, arguments) -> world.renderSelectedEventForReport()),

        step("^the event is logged to the Journal$",
            (world, arguments) -> world.logSelectedEventToJournal()),

        then("^the logged message text is identical to the report's rendered text$",
            (world, arguments) -> world.assertLoggedEventTextMatchesReportRendering())
    );
  }
}
