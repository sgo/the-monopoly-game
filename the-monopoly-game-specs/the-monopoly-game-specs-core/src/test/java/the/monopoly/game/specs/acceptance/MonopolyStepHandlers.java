package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.StartSpace;
import the.monopoly.game.components.streets.Station;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.components.streets.TaxSpace;
import the.monopoly.game.components.streets.Utility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;
import static the.monopoly.game.specs.acceptance.GameAccount.Claim;
import static the.monopoly.game.specs.acceptance.GameAccount.records;
import static the.monopoly.game.specs.acceptance.GameAccount.recordsInOrder;
import static the.monopoly.game.specs.acceptance.GameAccount.recordsStartWith;
import static the.monopoly.game.specs.acceptance.GameAccount.says;
import static the.monopoly.game.specs.acceptance.GameAccount.saysInOrder;
import static the.monopoly.game.specs.acceptance.GameAccount.saysStartWith;
import static the.monopoly.game.specs.acceptance.StepHandler.given;
import static the.monopoly.game.specs.acceptance.StepHandler.then;
import static the.monopoly.game.specs.acceptance.StepHandler.step;

/**
 * The step vocabulary of the component features. Steps that differ only in
 * their example values share one handler.
 * <p>
 * Each step asks the world for the kind of space it needs, so a step written
 * against the wrong kind of space fails saying so.
 */
public final class MonopolyStepHandlers {
  /** Either a literal amount or a {@code <placeholder>} naming an example column. */
  private static final String VALUE = "(<[^<>]+>|-?[0-9,]+)";
  private static final String NAME = "(<[^<>]+>|[^\"]+)";
  private static final Pattern DICE_DESCRIPTION = Pattern.compile("([0-9]+) faced");

  private MonopolyStepHandlers() {
  }

  public static List<StepHandler> handlers() {
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

        step("^pawn \"" + NAME + "\" will roll " + VALUE + " for initiative$",
            (world, arguments) -> world.queueInitiativeRoll(arguments.text(1), arguments.number(2))),

        step("^pawn \"" + NAME + "\" will roll " + VALUE + " and " + VALUE + " for their turn$",
            (world, arguments) -> world.queuePawnRoll(
                arguments.text(1), new Roll(arguments.number(2), arguments.number(3)))),

        step("^with \\$" + VALUE + " in pawn \"" + NAME + "\"'s account$",
            (world, arguments) -> world.arrangePawnBalance(arguments.text(2), money(arguments.number(1)))),

        step("^we roll for initiative$",
            (world, arguments) -> world.rollForInitiative()),

        step("^every other player can complete their turn$",
            (world, arguments) -> world.letTheOthersRollWhatTheyLike()),

        given("^pawn \"" + NAME + "\" starts at position " + VALUE + "$",
            (world, arguments) -> world.placePawn(arguments.text(1), arguments.number(2))),

        step("^we play the game$",
            (world, arguments) -> world.playGame()),

        then("^pawn \"" + NAME + "\" is at position " + VALUE + "$",
            (world, arguments) -> assertThat(world.pawn(arguments.text(1)).position().index())
                .isEqualTo(arguments.number(2))),

        step("^pawn \"" + NAME + "\" goes first$",
            (world, arguments) -> assertThat(world.turnOrder().getFirst().id().value())
                .isEqualTo(arguments.text(1))),

        step("^pawn \"" + NAME + "\" plays before pawn \"" + NAME + "\"$",
            (world, arguments) -> {
              List<String> order = world.turnOrder().stream().map(it -> it.id().value()).toList();
              assertThat(order).containsSubsequence(arguments.text(1), arguments.text(2));
            }),

        then("^the game journal records that the game starts with pawn \"" + NAME
                + "\" before pawn \"" + NAME + "\"$",
            (world, arguments) -> recordsStartWith(world, arguments.text(1), arguments.text(2))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls " + VALUE + " for initiative$",
            (world, arguments) -> records(world, initiativeRoll(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" wins initiative$",
            (world, arguments) -> records(world, initiativeWon(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> records(world, turnStarted(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls a total of " + VALUE + "$",
            (world, arguments) -> records(world, rolled(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " to " + VALUE + "$",
            (world, arguments) -> records(world,
                moved(arguments.text(1), arguments.number(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE + "$",
            (world, arguments) -> records(world, salaryCollected(arguments.text(1), arguments.number(2)))),

        then("^the game journal records game start before it records that pawn \"" + NAME
                + "\" rolls " + VALUE + " for initiative$",
            (world, arguments) -> recordsInOrder(world,
                Claim.ofAny(Entry.Start.class),
                initiativeRoll(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative before it records that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative$",
            (world, arguments) -> recordsInOrder(world,
                initiativeRoll(arguments.text(1), arguments.number(2)),
                initiativeRoll(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative before it records that pawn \"" + NAME + "\" wins initiative$",
            (world, arguments) -> recordsInOrder(world,
                initiativeRoll(arguments.text(1), arguments.number(2)),
                initiativeWon(arguments.text(3)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" wins initiative before starting a turn$",
            (world, arguments) -> recordsInOrder(world,
                initiativeWon(arguments.text(1)),
                Claim.ofAny(Entry.TurnStarted.class))),

        then("^the game journal records that pawn \"" + NAME
                + "\" starts a turn before it records that pawn \"" + NAME + "\" rolls a total of "
                + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                turnStarted(arguments.text(1)),
                rolled(arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls a total of " + VALUE
                + " before it records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " to " + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                rolled(arguments.text(1), arguments.number(2)),
                moved(arguments.text(3), arguments.number(4), arguments.number(5)))),

        then("^the game journal records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " to " + VALUE + " before it records that pawn \"" + NAME + "\" collects a salary of \\$"
                + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                moved(arguments.text(1), arguments.number(2), arguments.number(3)),
                salaryCollected(arguments.text(4), arguments.number(5)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" starts its turn before pawn \"" + NAME + "\"$",
            (world, arguments) -> recordsInOrder(world,
                turnStarted(arguments.text(1)), turnStarted(arguments.text(2)))),

        then("^the game report says that the game starts with pawn \"" + NAME
                + "\" before pawn \"" + NAME + "\"$",
            (world, arguments) -> saysStartWith(world, arguments.text(1), arguments.text(2))),

        then("^the game report says that the game starts before it says that pawn \"" + NAME
                + "\" rolls for initiative$",
            (world, arguments) -> saysInOrder(world,
                "The game starts with ", arguments.text(1) + " rolls ")),

        then("^the game report says that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative before it says that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative$",
            (world, arguments) -> saysInOrder(world,
                rollsForInitiative(arguments.text(1), arguments.number(2)),
                rollsForInitiative(arguments.text(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative before it says that pawn \"" + NAME + "\" wins initiative$",
            (world, arguments) -> saysInOrder(world,
                rollsForInitiative(arguments.text(1), arguments.number(2)),
                arguments.text(3) + " wins initiative")),

        then("^the game report says that pawn \"" + NAME
                + "\" wins initiative before it says that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " wins initiative", arguments.text(2) + " starts a turn")),

        then("^the game report says that pawn \"" + NAME
                + "\" starts a turn before it says that pawn \"" + NAME + "\" rolls a total of "
                + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " starts a turn",
                rollsATotalOf(arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME + "\" rolls a total of " + VALUE
                + " before it says that pawn \"" + NAME + "\" moves from position " + VALUE
                + " to " + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                rollsATotalOf(arguments.text(1), arguments.number(2)),
                movesFromPosition(arguments.text(3), arguments.number(4), arguments.number(5)))),

        then("^the game report says that pawn \"" + NAME + "\" moves from position " + VALUE
                + " to " + VALUE + " before it says that pawn \"" + NAME + "\" collects a salary of \\$"
                + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                movesFromPosition(arguments.text(1), arguments.number(2), arguments.number(3)),
                arguments.text(4) + " collects a salary of $" + arguments.number(5))),

        given("^pawn \"" + NAME + "\" follows the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.pawnFollows(arguments.text(1), Vocabulary.strategy(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" has \\$" + VALUE + " to spend$",
            (world, arguments) -> world.arrangePawnBalance(arguments.text(1), money(arguments.number(2)))),

        given("^pawn \"" + NAME + "\" declines the offer for \"" + NAME + "\"$",
            (world, arguments) -> world.pawnDeclines(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" will bid \\$" + VALUE + " for \"" + NAME + "\" at auction$",
            (world, arguments) -> world.pawnWillBid(
                arguments.text(1), SpaceNames.of(arguments.text(3)), money(arguments.number(2)))),

        given("^pawn \"" + NAME + "\" owns \"" + NAME + "\"$",
            (world, arguments) -> world.givePawnOwnership(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^the street \"" + NAME + "\" has " + VALUE + " house\\(s\\) built$",
            (world, arguments) -> world.arrangeHouses(SpaceNames.of(arguments.text(1)), arguments.number(2))),

        given("^the street \"" + NAME + "\" has a hotel built$",
            (world, arguments) -> world.arrangeHotel(SpaceNames.of(arguments.text(1)))),

        given("^pawn \"" + NAME + "\" declines to claim rent for \"" + NAME + "\"$",
            (world, arguments) -> world.pawnDeclinesRent(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" lands on \"" + NAME + "\"$",
            (world, arguments) -> world.landPawnOn(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" sells a house on \"" + NAME + "\" back to the bank$",
            (world, arguments) -> world.sellHouse(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" exchanges the hotel on \"" + NAME + "\" for houses$",
            (world, arguments) -> world.exchangeHotelForHouses(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        then("^pawn \"" + NAME + "\" owns \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.pawnOwns(arguments.text(1), SpaceNames.of(arguments.text(2))))
                .as("pawn \"%s\" owns \"%s\"", arguments.text(1), arguments.text(2))
                .isTrue()),

        then("^pawn \"" + NAME + "\" does not own \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.pawnOwns(arguments.text(1), SpaceNames.of(arguments.text(2))))
                .as("pawn \"%s\" owns \"%s\"", arguments.text(1), arguments.text(2))
                .isFalse()),

        then("^the street \"" + NAME + "\" has " + VALUE + " house\\(s\\) built$",
            (world, arguments) -> assertThat(world.housesBuiltOn(SpaceNames.of(arguments.text(1))))
                .isEqualTo(arguments.number(2))),

        then("^the street \"" + NAME + "\" has a hotel built$",
            (world, arguments) -> assertThat(world.hasHotelOn(SpaceNames.of(arguments.text(1)))).isTrue()),

        then("^the game journal records that pawn \"" + NAME + "\" pays pawn \"" + NAME
                + "\" \\$" + VALUE + " rent for \"" + NAME + "\"$",
            (world, arguments) -> records(world, rentPaid(
                arguments.text(1), arguments.text(2), arguments.text(4), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" builds a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> records(world, houseBuilt(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" sells a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> records(world, houseSold(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" moves before it records that pawn \""
                + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + " rent for \"" + NAME + "\"$",
            (world, arguments) -> recordsInOrder(world, moves(arguments.text(1)), rentPaid(
                arguments.text(2), arguments.text(3), arguments.text(5), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" buys \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> records(world,
                bought(arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" wins the auction for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> records(world,
                auctionWon(arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME + "\" buys \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                moves(arguments.text(1)),
                bought(arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME + "\" wins the auction for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                moves(arguments.text(1)),
                auctionWon(arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" moves before it says that pawn \""
                + NAME + "\" buys \"" + NAME + "\" for \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                movesAnywhere(arguments.text(1)),
                arguments.text(2) + " buys " + arguments.text(3) + " for $" + arguments.number(4))),

        then("^the game report says that pawn \"" + NAME + "\" moves before it says that pawn \""
                + NAME + "\" wins the auction for \"" + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                movesAnywhere(arguments.text(1)),
                arguments.text(2) + " wins the auction for " + arguments.text(3)
                    + " at $" + arguments.number(4))),

        then("^the game report says that pawn \"" + NAME + "\" moves before it says that pawn \""
                + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + " rent for \"" + NAME + "\"$",
            (world, arguments) -> saysInOrder(world, movesAnywhere(arguments.text(1)),
                arguments.text(2) + " pays " + arguments.text(3) + " $" + arguments.number(4)
                    + " rent for " + arguments.text(5))),

        then("^the game report says that pawn \"" + NAME + "\" builds a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> says(world, builtAHouse(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME + "\" sells a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> says(world, soldAHouse(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        step("^each face was rolled about " + VALUE + " times within a " + VALUE + "% margin$",
            (world, arguments) -> {
              int expected = arguments.number(1);
              double margin = expected * (arguments.number(2) / 100.0);
              assertThat(world.rolls().values())
                  .allSatisfy(seen -> assertThat(seen).isCloseTo(expected, within((int) margin)));
            })
    );
  }

  private static Claim initiativeRoll(String pawnName, int total) {
    return Claim.of(new Entry.InitiativeRoll(idOf(pawnName), total));
  }

  private static Claim initiativeWon(String pawnName) {
    return Claim.of(new Entry.InitiativeWon(idOf(pawnName)));
  }

  private static Claim turnStarted(String pawnName) {
    return Claim.of(new Entry.TurnStarted(idOf(pawnName)));
  }

  private static Claim rolled(String pawnName, int total) {
    return Claim.of(new Entry.Rolled(idOf(pawnName), total));
  }

  private static Claim moved(String pawnName, int from, int to) {
    return Claim.of(new Entry.Moved(idOf(pawnName), from, to));
  }

  private static Claim salaryCollected(String pawnName, int salary) {
    return Claim.of(new Entry.SalaryCollected(idOf(pawnName), money(salary)));
  }

  private static Claim bought(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.Bought(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  private static Claim auctionWon(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.AuctionWon(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  private static Claim rentPaid(String tenant, String owner, String spaceName, int rent) {
    return Claim.of(new Entry.RentPaid(idOf(tenant), idOf(owner), SpaceNames.of(spaceName), money(rent)));
  }

  private static Claim houseBuilt(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.HouseBuilt(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  private static Claim houseSold(String pawnName, String spaceName, int price) {
    return Claim.of(new Entry.HouseSold(idOf(pawnName), SpaceNames.of(spaceName), money(price)));
  }

  /** A pawn moving anywhere, for a step that says when it moved rather than where to. */
  private static Claim moves(String pawnName) {
    return new Claim(
        entry -> entry instanceof Entry.Moved it && it.player().equals(idOf(pawnName)),
        "move by " + pawnName
    );
  }

  /** The report reads as the features read, so a claim is words the report must carry. */
  private static String rollsForInitiative(String pawnName, int total) {
    return pawnName + " rolls " + total + " for initiative";
  }

  private static String rollsATotalOf(String pawnName, int total) {
    return pawnName + " rolls a total of " + total;
  }

  private static String movesFromPosition(String pawnName, int from, int to) {
    return pawnName + " moves from position " + from + " to " + to;
  }

  /** A pawn moving anywhere, for a step that says when it moved rather than where to. */
  private static String movesAnywhere(String pawnName) {
    return pawnName + " moves from position ";
  }

  private static String soldAHouse(String pawnName, String spaceName, int price) {
    return pawnName + " sells a house on " + spaceName + " for $" + price;
  }

  private static String builtAHouse(String pawnName, String spaceName, int price) {
    return pawnName + " builds a house on " + spaceName + " for $" + price;
  }

  private static Player.ID idOf(String pawnName) {
    return new Player.ID(pawnName);
  }

  /** A dice is described by how many faces it has, as in "6 faced". */
  private static int facesOf(String description) {
    Matcher faces = DICE_DESCRIPTION.matcher(description);
    if (!faces.matches())
      throw new AssertionError("A dice is described as \"<n> faced\", not \"" + description + "\".");
    return Integer.parseInt(faces.group(1));
  }

  private static long diceFaceCount(World world, int position) {
    List<Dice> dice = world.ruleSet().dice().toList();
    if (position < 1 || position > dice.size())
      throw new AssertionError("The rules use " + dice.size() + " dice, so there is no dice " + position + ".");
    return dice.get(position - 1).faces().count();
  }

  /** Only a colour street belongs to a colour group; every other space has none. */
  private static Street.Colour colourGroupOf(Street space) {
    return space instanceof ColourStreet street ? street.colourGroup() : null;
  }

  private static Money money(int amount) {
    return new Money(amount);
  }
}
