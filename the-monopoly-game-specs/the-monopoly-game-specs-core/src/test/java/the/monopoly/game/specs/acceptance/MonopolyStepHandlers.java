package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.components.streets.Utility;
import the.monopoly.game.components.dice.Roll;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;
import static the.monopoly.game.specs.acceptance.GameAccount.Claim;
import static the.monopoly.game.specs.acceptance.GameAccount.logRecords;
import static the.monopoly.game.specs.acceptance.GameAccount.logRecordsNoWinner;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.VALUE;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.colourGroupOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.diceFaceCount;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.facesOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.money;
import static the.monopoly.game.specs.acceptance.StepHandler.given;
import static the.monopoly.game.specs.acceptance.StepHandler.then;
import static the.monopoly.game.specs.acceptance.StepHandler.step;

/**
 * The step vocabulary of the component features. Steps that differ only in
 * their example values share one handler.
 * <p>
 * Each step asks the world for the kind of space it needs, so a step written
 * against the wrong kind of space fails saying so.
 * <p>
 * The full vocabulary is split across this class and {@link JournalStepHandlers} /
 * {@link GameLogStepHandlers} / {@link PomStepHandlers} / {@link EventStepHandlers} so
 * no single file carries too many mutation sites; {@link #handlers()} is the one entry
 * point callers use.
 */
public final class MonopolyStepHandlers {
  private MonopolyStepHandlers() {
  }

  public static List<StepHandler> handlers() {
    return Stream.of(boardAndSetupSteps(), JournalStepHandlers.handlers(), GameLogStepHandlers.handlers(),
            PomStepHandlers.handlers(), EventStepHandlers.handlers())
        .flatMap(List::stream)
        .toList();
  }

  private static List<StepHandler> boardAndSetupSteps() {
    return List.of(
        step("^the (?:street|station|utility|tax space) \"" + NAME + "\"$",
            (world, arguments) -> world.select(SpaceNames.of(arguments.text(1)))),

        step("^your salary is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(StartSpace.class).salary())
                .isEqualTo(money(arguments.number(1)))),

        step("^the (?:street|station|utility) value is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(Ownable.class).price())
                .isEqualTo(money(arguments.number(1)))),

        step("^the tax is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(TaxSpace.class).tax())
                .isEqualTo(money(arguments.number(1)))),

        step("^vacant rent is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).vacantRent())
                .isEqualTo(money(arguments.number(1)))),

        step("^rent for ([0-9]+) houses? is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).rentForHouses(arguments.number(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^rent for ([0-9]+) hotel is \\$" + VALUE + "$",
            (world, arguments) -> {
              assertThat(arguments.number(1)).isEqualTo(1);
              assertThat(world.space(ColourStreet.class).rentForOneHotel())
                  .isEqualTo(money(arguments.number(2)));
            }),

        step("^house construction cost is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).houseConstructionCost())
                .isEqualTo(money(arguments.number(1)))),

        step("^hotel construction cost is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).hotelConstructionCost())
                .isEqualTo(money(arguments.number(1)))),

        step("^hotel construction requires ([0-9]+) existing houses$",
            (world, arguments) -> assertThat(world.space(ColourStreet.class).hotelConstructionRequiresNumberOfHouses())
                .isEqualTo(arguments.number(1))),

        step("^mortgage value of the land is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(Ownable.class).landMortgageValue())
                .isEqualTo(money(arguments.number(1)))),

        step("^rent for owning ([0-9]+) stations? is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.space(Station.class).rentForOwning(arguments.number(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^rent for owning ([0-9]+) utilit(?:y|ies) is ([0-9]+) times the dice roll$",
            (world, arguments) -> assertThat(world.space(Utility.class).rentDiceMultiplierForOwning(arguments.number(1)))
                .isEqualTo(arguments.number(2))),

        step("^the " + NAME + " rule set$",
            (world, arguments) -> world.selectRuleSet(Vocabulary.ruleSet(arguments.text(1)))),

        step("^dice " + VALUE + " is " + NAME + "$",
            (world, arguments) -> assertThat(diceFaceCount(world, arguments.number(1)))
                .isEqualTo(facesOf(arguments.text(2)))),

        step("^we play with " + VALUE + " to " + VALUE + " players$",
            (world, arguments) -> {
              assertThat(world.ruleSet().players().min()).isEqualTo(arguments.number(1));
              assertThat(world.ruleSet().players().max()).isEqualTo(arguments.number(2));
            }),

        step("^space " + VALUE + " is \"" + NAME + "\" of type " + NAME
                + " and colour group " + NAME + "$",
            (world, arguments) -> {
              Street space = world.spaceAt(arguments.number(1));
              assertThat(space.type()).isEqualTo(SpaceNames.of(arguments.text(2)));
              assertThat(space.kind()).isEqualTo(Vocabulary.kind(arguments.text(3)));
              assertThat(colourGroupOf(space)).isEqualTo(Vocabulary.colour(arguments.text(4)));
            }),

        step("^we select ([0-9]+) players$",
            (world, arguments) -> world.selectPlayers(arguments.number(1))),

        step("^the standard game setup$", (world, arguments) -> world.selectStandardGameSetup()),

        then("^every selected player starts at position " + VALUE + "$",
            (world, arguments) -> assertThat(world.selectedPlayers())
                .extracting(player -> player.position().index()).containsOnly(arguments.number(1))),

        then("^every selected player has \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.selectedPlayers())
                .extracting(player -> player.account().balance().amount()).containsOnly(money(arguments.number(1)))),

        then("^no selected player owns any street$",
            (world, arguments) -> assertThat(world.bankOwnsEveryOwnableSpace()).isTrue()),

        then("^no selected player has any house or hotel$",
            (world, arguments) -> assertThat(world.bankHasAllImprovements()).isTrue()),

        then("^the bank owns every ownable space$",
            (world, arguments) -> assertThat(world.bankOwnsEveryOwnableSpace()).isTrue()),

        then("^the bank has all houses$", (world, arguments) -> assertThat(world.bankHasAllImprovements()).isTrue()),
        then("^the bank has all hotels$", (world, arguments) -> assertThat(world.bankHasAllImprovements()).isTrue()),
        then("^all Chance cards are available in the Chance deck$",
            (world, arguments) -> assertThat(world.cardDecksAreComplete()).isTrue()),
        then("^all Community Chest cards are available in the Community Chest deck$",
            (world, arguments) -> assertThat(world.cardDecksAreComplete()).isTrue()),
        then("^no selected player holds a Get Out of Jail Free card$",
            (world, arguments) -> assertThat(world.noSelectedPlayerHoldsGetOutOfJailFreeCard()).isTrue()),

        step("^we select <players> players$",
            (world, arguments) -> world.selectPlayers(8)),

        step("^the simulator is configured for (.+) players without strategy choices$",
            (world, arguments) -> world.configureSimulator(arguments.number(1), false)),

        step("^the simulator is configured for (.+) players$",
            (world, arguments) -> world.configureSimulator(arguments.number(1), false)),

        step("^every player selects the \"Agree if affordable\" strategy$",
            (world, arguments) -> world.configureSimulatorWithAgreeIfAffordable()),

        step("^I run the simulator$", (world, arguments) -> world.runSimulator()),

        step("^I start the simulator$", (world, arguments) -> world.startSimulator()),

        step("^I stop the simulator before the game ends$", (world, arguments) -> world.stopSimulator()),

        then("^the simulator process ends$", (world, arguments) -> world.awaitSimulatorEnd()),

        then("^the simulator is still playing when the game log has recorded " + VALUE + " rolls$",
            (world, arguments) -> {
              world.awaitGameLog(arguments.number(1), Entry.Rolled.class::isInstance,
                  "at least " + arguments.number(1) + " rolls");
              assertThat(world.simulatorIsPlaying()).isTrue();
            }),

        then("^the game log records that the game starts$",
            (world, arguments) -> logRecords(world, Claim.ofAny(Entry.Start.class))),

        then("^the game log records at least " + VALUE + " rolls$",
            (world, arguments) -> world.awaitGameLog(arguments.number(1), Entry.Rolled.class::isInstance,
                "at least " + arguments.number(1) + " rolls")),

        then("^the game log records at least " + VALUE + " rolls of a total between 2 and 12$",
            (world, arguments) -> world.awaitGameLog(arguments.number(1),
                entry -> entry instanceof Entry.Rolled rolled
                    && rolled.total() >= 2 && rolled.total() <= 12,
                "at least " + arguments.number(1) + " rolls of a total between 2 and 12")),

        then("^the game log records at least two different roll totals$",
            (world, arguments) -> {
              Set<Integer> totals = Set.copyOf(world.gameLog().stream()
                  .filter(Entry.Rolled.class::isInstance)
                  .map(Entry.Rolled.class::cast)
                  .map(Entry.Rolled::total)
                  .toList());
              assertThat(totals.size()).isGreaterThanOrEqualTo(2);
            }),

        then("^the game log records no winner$",
            (world, arguments) -> logRecordsNoWinner(world)),

        then("^the simulator exits successfully$",
            (world, arguments) -> assertThat(world.simulatorResult().succeeded()).isTrue()),

        then("^the simulator exits unsuccessfully$",
            (world, arguments) -> assertThat(world.simulatorResult().succeeded()).isFalse()),

        then("^the output contains a human-readable game report$",
            (world, arguments) -> assertThat(world.simulatorResult().output()).contains("The game starts")),

        then("^the report contains a bankruptcy before the game's winner$",
            (world, arguments) -> {
              String report = world.simulatorResult().output();
              assertThat(report).contains("goes bankrupt").contains("wins the game");
              assertThat(report.indexOf("goes bankrupt")).isLessThan(report.indexOf("wins the game"));
            }),

        then("^the report contains the game's winner$",
            (world, arguments) -> assertThat(world.simulatorResult().output()).contains("wins the game")),

        then("^the output explains that the number of players must be between 2 and 8 and received (.+) players$",
            (world, arguments) -> assertThat(world.simulatorResult().output())
                .contains("number of players must be between 2 and 8")
                .contains("received " + arguments.number(1) + " players")),

        step("^we play " + VALUE + " times$",
            (world, arguments) -> world.playMonopolyGames(arguments.number(1))),

        then("^the game ends every time with a monopoly$",
            (world, arguments) -> assertThat(world.monopolyRunsCompleted()).isTrue()),

        step("^pawn \"" + NAME + "\" is at play$",
            (world, arguments) -> assertThat(world.pawn(arguments.text(1))).isNotNull()),

        step("^pawn \"" + NAME + "\"'s account balance is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.pawn(arguments.text(1)).account().balance())
                .isEqualTo(Balance.of(arguments.number(2)))),

        step("^a player$",
            (world, arguments) -> world.startPlayer()),

        step("^with \\$" + VALUE + " in his account$",
            (world, arguments) -> world.fundPlayer(money(arguments.number(1)))),

        step("^with optional double salary when landing on Start rule$",
            (world, arguments) -> world.ruleSet().activate(double_salary_when_landing_on_start)),

        step("^the player passes the street \"" + NAME + "\"$",
            (world, arguments) -> {
              world.select(SpaceNames.of(arguments.text(1)));
              world.player().pass(world.space(StartSpace.class));
            }),

        step("^the player lands on the street \"" + NAME + "\"$",
            (world, arguments) -> {
              world.select(SpaceNames.of(arguments.text(1)));
              world.player().land(world.space(StartSpace.class));
            }),

        step("^the player's account balance is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.player().account().balance())
                .isEqualTo(Balance.of(arguments.number(1)))),

        step("^a ([0-9]+) faced dice$",
            (world, arguments) -> world.selectDice(arguments.number(1))),

        step("^I roll the dice " + VALUE + " times$",
            (world, arguments) -> world.rollDice(arguments.number(1))),

        given("^the player is at position " + VALUE + "$",
            (world, arguments) -> world.player().position().moveTo(arguments.number(1))),

        then("^the player is at position " + VALUE + "$",
            (world, arguments) -> assertThat(world.player().position().index())
                .isEqualTo(arguments.number(1))),

        step("^the next roll will be " + VALUE + " and " + VALUE + "$",
            (world, arguments) -> world.queueRoll(new Roll(arguments.number(1), arguments.number(2)))),

        step("^the player takes a turn$",
            (world, arguments) -> world.takeTurn()),

        given("^the player is in jail$",
            (world, arguments) -> world.startPlayerInJail()),

        step("^pawn \"" + NAME + "\" will roll " + VALUE + " for initiative$",
            (world, arguments) -> world.queueInitiativeRoll(arguments.text(1), arguments.number(2))),

        step("^pawn \"" + NAME + "\" will roll " + VALUE + " and " + VALUE + " for their turn$",
            (world, arguments) -> world.queuePawnRoll(
                arguments.text(1), new Roll(arguments.number(2), arguments.number(3)))),

        step("^the next chance card will be \"" + NAME + "\"$",
            (world, arguments) -> world.queueChanceCard(arguments.text(1))),

        step("^the next community chest card will be \"" + NAME + "\"$",
            (world, arguments) -> world.queueCommunityChestCard(arguments.text(1))),

        step("^with \\$" + VALUE + " in pawn \"" + NAME + "\"'s account$",
            (world, arguments) -> world.arrangePawnBalance(arguments.text(2), money(arguments.number(1)))),

        step("^we roll for initiative$",
            (world, arguments) -> world.rollForInitiative())
    );
  }
}
