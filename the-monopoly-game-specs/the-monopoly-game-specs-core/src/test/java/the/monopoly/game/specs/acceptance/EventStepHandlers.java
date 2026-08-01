package the.monopoly.game.specs.acceptance;

import java.util.List;

import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
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

        step("^the event is rendered for the report$",
            (world, arguments) -> world.renderSelectedEventForReport()),

        step("^the event is logged to the Journal$",
            (world, arguments) -> world.logSelectedEventToJournal()),

        then("^the logged message text is identical to the report's rendered text$",
            (world, arguments) -> world.assertLoggedEventTextMatchesReportRendering())
    );
  }
}
