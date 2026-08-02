package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static the.monopoly.game.specs.acceptance.GameAccount.Claim;
import static the.monopoly.game.specs.acceptance.GameAccount.logRecords;
import static the.monopoly.game.specs.acceptance.GameAccount.logRecordsInOrder;
import static the.monopoly.game.specs.acceptance.GameAccount.logRecordsStartWith;
import static the.monopoly.game.specs.acceptance.GameAccount.records;
import static the.monopoly.game.specs.acceptance.GameAccount.recordsInOrder;
import static the.monopoly.game.specs.acceptance.GameAccount.says;
import static the.monopoly.game.specs.acceptance.GameAccount.saysInOrder;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.VALUE;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.auctionWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankPaidLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bought;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.buildingRefused;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.buildingRefusedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.builtAHouse;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.chanceCardDrawn;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.chanceCardDrawnLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.communityChestCardDrawn;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.communityChestCardDrawnLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.dollars;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.houseBuilt;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.houseSold;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.idOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.initiativeRoll;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.initiativeWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailEntered;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailEnteredLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailFinePaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailFinePaidLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSaleRefused;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSaleRefusedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSold;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSoldLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgaged;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgagedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgageLifted;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgageLiftedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.moved;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.moves;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.movesAnywhere;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.movesFromPosition;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.playerPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.playerPaidLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rentPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rolled;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.salaryCollected;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.soldAHouse;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.turnStarted;
import static the.monopoly.game.specs.acceptance.StepHandler.step;
import static the.monopoly.game.specs.acceptance.StepHandler.then;

/**
 * Journal/game-log/report assertions for the property, trading, building, mortgage,
 * card, and bankruptcy family of events, plus dice fairness.
 * Split out of {@link MonopolyStepHandlers} to keep its mutation-site count down;
 * {@link MonopolyStepHandlers#handlers()} is the one entry point callers use.
 */
final class GameLogStepHandlers {
  private GameLogStepHandlers() {
  }

  static List<StepHandler> handlers() {
    return List.of(
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

        then("^the game journal records that pawn \"" + NAME + "\" mortgages \"" + NAME + "\" for \\$"
                + VALUE + "$",
            (world, arguments) -> records(world, mortgaged(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" lifts the mortgage on \"" + NAME
                + "\" for \\$" + VALUE + " including \\$" + VALUE + " interest$",
            (world, arguments) -> records(world, mortgageLifted(
                arguments.text(1), arguments.text(2), arguments.number(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" sells \"" + NAME + "\" to pawn \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> records(world, landSold(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" is refused selling \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \\$" + VALUE
                + " because the colour group has houses built$",
            (world, arguments) -> records(world, landSaleRefused(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" is refused building a house on \"" + NAME
                + "\" for \\$" + VALUE + " because a street in the colour group is mortgaged$",
            (world, arguments) -> records(world, buildingRefused(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws the chance card \"" + NAME + "\"$",
            (world, arguments) -> records(world, chanceCardDrawn(arguments.text(1), arguments.text(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws the community chest card \"" + NAME + "\"$",
            (world, arguments) -> records(world, communityChestCardDrawn(arguments.text(1), arguments.text(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws a chance card$",
            (world, arguments) -> records(world, chanceCardDrawn(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws a community chest card$",
            (world, arguments) -> records(world, communityChestCardDrawn(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                playerPaid(arguments.text(3), arguments.text(4), arguments.number(5)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws the community chest card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                communityChestCardDrawn(arguments.text(1), arguments.text(2)),
                playerPaid(arguments.text(3), arguments.text(4), arguments.number(5)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" pays the bank \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                bankPaid(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME + "\" pays the bank \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                moves(arguments.text(1)), bankPaid(arguments.text(2), arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME
                + "\" is sent to jail from landing on \"" + NAME + "\"$",
            (world, arguments) -> recordsInOrder(world,
                moves(arguments.text(1)), jailEntered(arguments.text(2), arguments.text(3)))),

        then("^the game journal records that pawn \"" + NAME
                + "\" leaves jail by paying the \\$" + VALUE + " fine$",
            (world, arguments) -> records(world, jailFinePaid(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" goes bankrupt to the bank$",
            (world, arguments) -> records(world, Claim.of(new Entry.Bankrupt(idOf(arguments.text(1)), null)))),

        then("^the game journal records that pawn \"" + NAME + "\" goes bankrupt to pawn \"" + NAME + "\"$",
            (world, arguments) -> records(world, Claim.of(new Entry.Bankrupt(idOf(arguments.text(1)), idOf(arguments.text(2)))))),

        then("^the game journal records that pawn \"" + NAME + "\" wins the game$",
            (world, arguments) -> records(world, Claim.of(new Entry.Won(idOf(arguments.text(1)))))),

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

        then("^the game log records that the game starts with pawn \"" + NAME
                + "\" before pawn \"" + NAME + "\"$",
            (world, arguments) -> logRecordsStartWith(world, arguments.text(1), arguments.text(2))),

        then("^the game log records that pawn \"" + NAME + "\" rolls " + VALUE + " for initiative$",
            (world, arguments) -> logRecords(world, initiativeRoll(arguments.text(1), arguments.number(2)))),

        then("^the game log records that pawn \"" + NAME + "\" wins initiative$",
            (world, arguments) -> logRecords(world, initiativeWon(arguments.text(1)))),

        then("^the game log records that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> logRecords(world, turnStarted(arguments.text(1)))),

        then("^the game log records that pawn \"" + NAME + "\" starts a turn with balance \"" + NAME + "\"$",
            (world, arguments) -> {
              String pawnName = arguments.text(1);
              int balance = dollars(arguments.text(2));
              logRecords(world, turnStarted(pawnName, balance));
              // log-1 guarantees the logged text is the report's rendered text, so
              // checking the played game's own report proves what the real log line
              // says, not just that the structured entry carries the right balance.
              says(world, pawnName + " starts a turn with $" + balance);
            }),

        then("^the game log records that pawn \"" + NAME + "\" rolls a total of " + VALUE + "$",
            (world, arguments) -> logRecords(world, rolled(arguments.text(1), arguments.number(2)))),

        then("^the game log records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> logRecords(world,
                moved(arguments.text(1), arguments.number(2), arguments.text(3), arguments.number(4), arguments.text(5)))),

        then("^the game log records that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, salaryCollected(arguments.text(1), arguments.number(2)))),

        then("^the game log records game start before it records that pawn \"" + NAME
                + "\" rolls " + VALUE + " for initiative$",
            (world, arguments) -> logRecordsInOrder(world,
                Claim.ofAny(Entry.Start.class),
                initiativeRoll(arguments.text(1), arguments.number(2)))),

        then("^the game log records that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative before it records that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative$",
            (world, arguments) -> logRecordsInOrder(world,
                initiativeRoll(arguments.text(1), arguments.number(2)),
                initiativeRoll(arguments.text(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME + "\" rolls " + VALUE
                + " for initiative before it records that pawn \"" + NAME + "\" wins initiative$",
            (world, arguments) -> logRecordsInOrder(world,
                initiativeRoll(arguments.text(1), arguments.number(2)),
                initiativeWon(arguments.text(3)))),

        then("^the game log records that pawn \"" + NAME
                + "\" wins initiative before starting a turn$",
            (world, arguments) -> logRecordsInOrder(world,
                initiativeWon(arguments.text(1)),
                Claim.ofAny(Entry.TurnStarted.class))),

        then("^the game log records that pawn \"" + NAME
                + "\" starts a turn before it records that pawn \"" + NAME + "\" rolls a total of "
                + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                turnStarted(arguments.text(1)),
                rolled(arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" rolls a total of " + VALUE
                + " before it records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> logRecordsInOrder(world,
                rolled(arguments.text(1), arguments.number(2)),
                moved(arguments.text(3), arguments.number(4), arguments.text(5), arguments.number(6), arguments.text(7)))),

        then("^the game log records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\) before it records that pawn \"" + NAME + "\" collects a salary of \\$"
                + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                moved(arguments.text(1), arguments.number(2), arguments.text(3), arguments.number(4), arguments.text(5)),
                salaryCollected(arguments.text(6), arguments.number(7)))),

        then("^the game log records that pawn \"" + NAME
                + "\" starts its turn before pawn \"" + NAME + "\"$",
            (world, arguments) -> logRecordsInOrder(world,
                turnStarted(arguments.text(1)), turnStarted(arguments.text(2)))),

        then("^the game log records that pawn \"" + NAME + "\" buys \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world,
                bought(arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" wins the auction for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world,
                auctionWon(arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" pays pawn \"" + NAME
                + "\" \\$" + VALUE + " rent for \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, rentPaid(
                arguments.text(1), arguments.text(2), arguments.text(4), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" builds a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, houseBuilt(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" sells a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, houseSold(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" mortgages \"" + NAME + "\" for \\$"
                + VALUE + "$",
            (world, arguments) -> logRecords(world, mortgaged(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" lifts the mortgage on \"" + NAME
                + "\" for \\$" + VALUE + " including \\$" + VALUE + " interest$",
            (world, arguments) -> logRecords(world, mortgageLifted(
                arguments.text(1), arguments.text(2), arguments.number(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME + "\" sells \"" + NAME + "\" to pawn \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, landSold(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME + "\" is refused selling \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \\$" + VALUE
                + " because the colour group has houses built$",
            (world, arguments) -> logRecords(world, landSaleRefused(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME + "\" is refused building a house on \"" + NAME
                + "\" for \\$" + VALUE + " because a street in the colour group is mortgaged$",
            (world, arguments) -> logRecords(world, buildingRefused(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                playerPaid(arguments.text(3), arguments.text(4), arguments.number(5)))),

        then("^the game log records that pawn \"" + NAME + "\" draws the community chest card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                communityChestCardDrawn(arguments.text(1), arguments.text(2)),
                playerPaid(arguments.text(3), arguments.text(4), arguments.number(5)))),

        then("^the game log records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" pays the bank \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                bankPaid(arguments.text(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> logRecordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                moved(arguments.text(3), arguments.number(4), arguments.text(5),
                    arguments.number(6), arguments.text(7)))),

        then("^the game log records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME + "\" pays the bank \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                moves(arguments.text(1)), bankPaid(arguments.text(2), arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME
                + "\" is sent to jail from landing on \"" + NAME + "\"$",
            (world, arguments) -> logRecordsInOrder(world,
                moves(arguments.text(1)), jailEntered(arguments.text(2), arguments.text(3)))),

        then("^the game log records that pawn \"" + NAME
                + "\" leaves jail by paying the \\$" + VALUE + " fine$",
            (world, arguments) -> logRecords(world, jailFinePaid(arguments.text(1), arguments.number(2)))),

        then("^the game log records that pawn \"" + NAME + "\" goes bankrupt to the bank$",
            (world, arguments) -> logRecords(world,
                Claim.of(new Entry.Bankrupt(idOf(arguments.text(1)), null)))),

        then("^the game log records that pawn \"" + NAME + "\" goes bankrupt to pawn \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world,
                Claim.of(new Entry.Bankrupt(idOf(arguments.text(1)), idOf(arguments.text(2)))))),

        then("^the game log records that pawn \"" + NAME + "\" wins the game$",
            (world, arguments) -> logRecords(world, Claim.of(new Entry.Won(idOf(arguments.text(1)))))),

        then("^the game log records that pawn \"" + NAME + "\" moves before it records that pawn \""
                + NAME + "\" buys \"" + NAME + "\" for \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                moves(arguments.text(1)),
                bought(arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME + "\" wins the auction for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                moves(arguments.text(1)),
                auctionWon(arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game log records that pawn \"" + NAME
                + "\" moves before it records that pawn \"" + NAME + "\" pays pawn \"" + NAME
                + "\" \\$" + VALUE + " rent for \"" + NAME + "\"$",
            (world, arguments) -> logRecordsInOrder(world,
                moves(arguments.text(1)),
                rentPaid(arguments.text(2), arguments.text(3), arguments.text(5), arguments.number(4)))),

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

        then("^the game report says that pawn \"" + NAME + "\" mortgages \"" + NAME + "\" for \\$"
                + VALUE + "$",
            (world, arguments) -> says(world, mortgagedLine(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME + "\" lifts the mortgage on \"" + NAME
                + "\" for \\$" + VALUE + " including \\$" + VALUE + " interest$",
            (world, arguments) -> says(world, mortgageLiftedLine(
                arguments.text(1), arguments.text(2), arguments.number(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" sells \"" + NAME + "\" to pawn \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> says(world, landSoldLine(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" is refused selling \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \\$" + VALUE
                + " because the colour group has houses built$",
            (world, arguments) -> says(world, landSaleRefusedLine(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" is refused building a house on \"" + NAME
                + "\" for \\$" + VALUE + " because a street in the colour group is mortgaged$",
            (world, arguments) -> says(world, buildingRefusedLine(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                chanceCardDrawnLine(arguments.text(1), arguments.text(2)),
                playerPaidLine(arguments.text(3), arguments.text(4), arguments.number(5)))),

        then("^the game report says that pawn \"" + NAME + "\" draws the community chest card \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                communityChestCardDrawnLine(arguments.text(1), arguments.text(2)),
                playerPaidLine(arguments.text(3), arguments.text(4), arguments.number(5)))),

        then("^the game report says that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" pays the bank \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                chanceCardDrawnLine(arguments.text(1), arguments.text(2)),
                bankPaidLine(arguments.text(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> saysInOrder(world,
                chanceCardDrawnLine(arguments.text(1), arguments.text(2)),
                movesFromPosition(arguments.text(3), arguments.number(4), arguments.text(5),
                    arguments.number(6), arguments.text(7)))),

        then("^the game report says that pawn \"" + NAME
                + "\" moves before it says that pawn \"" + NAME + "\" pays the bank \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                movesAnywhere(arguments.text(1)), bankPaidLine(arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME
                + "\" moves before it says that pawn \"" + NAME
                + "\" is sent to jail from landing on \"" + NAME + "\"$",
            (world, arguments) -> saysInOrder(world,
                movesAnywhere(arguments.text(1)), jailEnteredLine(arguments.text(2), arguments.text(3)))),

        then("^the game report says that pawn \"" + NAME
                + "\" leaves jail by paying the \\$" + VALUE + " fine$",
            (world, arguments) -> says(world, jailFinePaidLine(arguments.text(1), arguments.number(2)))),

        then("^the game report says that pawn \"" + NAME + "\" goes bankrupt to the bank$",
            (world, arguments) -> says(world, arguments.text(1) + " goes bankrupt to the bank")),

        then("^the game report says that pawn \"" + NAME + "\" goes bankrupt to pawn \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " goes bankrupt to " + arguments.text(2))),

        then("^the game report says that pawn \"" + NAME + "\" wins the game$",
            (world, arguments) -> says(world, arguments.text(1) + " wins the game")),

        step("^each face was rolled about " + VALUE + " times within a " + VALUE + "% margin$",
            (world, arguments) -> {
              int expected = arguments.number(1);
              double margin = expected * (arguments.number(2) / 100.0);
              assertThat(world.rolls().values())
                  .allSatisfy(seen -> assertThat(seen).isCloseTo(expected, within((int) margin)));
            })
    );
  }
}
