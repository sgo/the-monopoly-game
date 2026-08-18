package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.strategies.Strategy;

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
import static the.monopoly.game.specs.acceptance.GameAccount.saysTurnStartedWith;
import static the.monopoly.game.specs.acceptance.GameAccount.saysTurnStartedWithBalance;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.UNQUOTED_NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.UNQUOTED_NAME_WITHOUT_ORDERING;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.VALUE;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.MONEY;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.money;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.auctionWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.auctionWonLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankPaidLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankReceived;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankReceivedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bought;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.buildingRefused;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.buildingRefusedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.builtAHouse;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.chanceCardDrawn;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.chanceCardDrawnLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.communityChestCardDrawn;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.communityChestCardDrawnLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.distressedOffer;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.distressedNoBidder;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.distressedStarted;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.distressedWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.dollars;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.developmentLoans;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.houseBuilt;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.houseSold;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.idOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.inherited;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.inheritedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.initiativeRoll;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.initiativeWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.finalBalance;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.finalAge;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailEntered;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailEnteredLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailCardUsed;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailCardUsedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailDoublesRolled;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailDoublesRolledLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailFinePaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailFinePaidLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailStayed;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailStayedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSaleRefused;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSaleRefusedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSold;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.landSoldLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgaged;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgagedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgageKept;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgageKeptLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgageLifted;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.mortgageLiftedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.moved;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.moves;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.movesAnywhere;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.movesFromPosition;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.playerPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.playerPaidLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.purchaseDeclined;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.peerTrade;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.purchaseDeclinedForReserveLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.purchaseDeclinedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rentPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rolled;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.salaryCollected;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.stalemateTrading;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.splitMonopolyPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.splitMonopolyWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.soldAHouse;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.turnStarted;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.turnStartedAtAge;
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
        then("^the game log records that full-draw development loans are " + NAME + "$",
            (world, arguments) -> logRecords(world, developmentLoans(arguments.text(1), true))),
        then("^the game log records that " + NAME + " is formed, held in equal thirds by pawn \"" + NAME
                + "\", pawn \"" + NAME + "\", and pawn \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.LegalEntityFormed it
                && it.name().equals(arguments.text(1)), "entity formed"))),
        then("^the game log records that " + NAME + " raises a loan of \\$" + VALUE
                + " from pawn \"" + NAME + "\", pawn \"" + NAME + "\", and pawn \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRaised it
                && it.name().equals(arguments.text(1)) && it.amount().amount() == arguments.number(2), "loan raised"))),
        then("^the game log records that " + NAME + " repays pawn \"" + NAME + "\" \\$" + VALUE
                + " for the loan$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRepaid it
                && it.name().equals(arguments.text(1)) && it.repayment().amount() == arguments.number(3), "loan repaid"))),
        then("^the game log records that " + NAME + " pays each of pawn \"" + NAME + "\", pawn \""
                + NAME + "\", and pawn \"" + NAME + "\" an equal dividend$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.LegalEntityDividendPaid it
                && it.name().equals(arguments.text(1)), "equal dividend"))),
        then("^the game log records that pawn \"" + NAME + "\" pays \\$" + VALUE
                + " rent to " + NAME + " for \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.LegalEntityRentPaid it
                && it.tenant().equals(idOf(arguments.text(1))) && it.rent().amount() == arguments.number(2)
                && it.name().equals(arguments.text(3)) && it.land().equals(SpaceNames.of(arguments.text(4))),
                "entity rent paid"))),
        then("^the game log records that pawn \"" + NAME + "\" raises a development loan of \\$" + MONEY
                + " from the bank, secured by \"" + NAME + "\", funded by pawn \"" + NAME + "\"'s bond purchase$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanRaised it
                && it.borrower().equals(idOf(arguments.text(1))) && it.amount().equals(money(arguments.text(2)))
                && it.collateral() == SpaceNames.of(arguments.text(3))
                && it.bondholder().value().equals(arguments.text(4)), "player development loan raised"))),
        then("^the game log records that pawn \"" + NAME + "\" pays the bank \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanPayment it
                && it.borrower().equals(idOf(arguments.text(1))) && it.interest().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3))) && it.collateral() == SpaceNames.of(arguments.text(4)),
                "player development loan payment"))),
        then("^the game log records that pawn \"" + NAME + "\"'s development loan on \"" + NAME
                + "\" has been fully repaid$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanRepaid it
                && it.borrower().equals(idOf(arguments.text(1))) && it.collateral() == SpaceNames.of(arguments.text(2)),
                "player development loan repaid"))),
        then("^the game log records that pawn \"" + NAME + "\" receives \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan bond secured by \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.DevelopmentBondPayment it
                && it.bondholder().equals(idOf(arguments.text(1))) && it.yield().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3))) && it.collateral() == SpaceNames.of(arguments.text(4)),
                "player development bond payment"))),
        then("^the game log records that development loans are " + NAME + "$",
            (world, arguments) -> logRecords(world, developmentLoans(arguments.text(1), false))),
        then("^the game log records that " + NAME + " raises a development loan of \\$" + MONEY
                + " from the bank, secured by \"" + NAME + "\", funded by pawn \"" + NAME + "\"'s bond purchase$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.EntityDevelopmentLoanRaised it
                && it.name().equals(arguments.text(1)) && it.amount().equals(money(arguments.text(2)))
                && it.collateral() == SpaceNames.of(arguments.text(3))
                && it.bondholder().value().equals(arguments.text(4)), "entity development loan raised"))),
        then("^the game log records that " + NAME + " builds a house on \"" + NAME + "\" for \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.LegalEntityHouseBuilt it
                && it.name().equals(arguments.text(1)) && it.land() == SpaceNames.of(arguments.text(2))
                && it.price().amount() == arguments.number(3), "entity house built"))),
        then("^the game log records that " + NAME + " pays the bank \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.EntityDevelopmentLoanPayment it
                && it.name().equals(arguments.text(1)) && it.interest().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3)))
                && it.collateral() == SpaceNames.of(arguments.text(4)), "entity development loan payment"))),
        then("^the game log records that " + NAME + " defaults on the development loan secured by \"" + NAME
                + "\"; the bank forecloses$",
            (world, arguments) -> logRecords(world, new Claim(entry ->
                (entry instanceof Entry.EntityDevelopmentLoanDefaulted it
                    && it.name().equals(arguments.text(1)) && it.collateral() == SpaceNames.of(arguments.text(2)))
                    || (entry instanceof Entry.DevelopmentLoanDefaulted playerDefaulted
                    && playerDefaulted.borrower().equals(idOf(arguments.text(1)))
                    && playerDefaulted.collateral() == SpaceNames.of(arguments.text(2))),
                "development loan default"))),
        then("^the game log records that pawn \"" + NAME + "\" defaults on the development loan secured by \""
                + NAME + "\"; the bank forecloses$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanDefaulted it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.collateral() == SpaceNames.of(arguments.text(2)), "player development loan default"))),
        then("^the game log records that the bank recovers \\$" + MONEY + " from the foreclosure of \"" + NAME
                + "\", added to its own account$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanRecovered it
                && it.collateral() == SpaceNames.of(arguments.text(2))
                && it.amount().equals(money(arguments.text(1))), "development loan recovery"))),
        then("^the game log records that " + NAME + "'s development loan on \"" + NAME
                + "\" has been fully repaid$",
            (world, arguments) -> logRecords(world, new Claim(entry -> entry instanceof Entry.EntityDevelopmentLoanRepaid it
                && it.name().equals(arguments.text(1)) && it.collateral() == SpaceNames.of(arguments.text(2)),
                "entity development loan repaid"))),
        then("^the game report says that " + NAME + " is formed, held in equal thirds by pawn \"" + NAME
                + "\", pawn \"" + NAME + "\", and pawn \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " is formed, held in equal thirds by")),
        then("^the game report says that pawn \"" + NAME + "\" raises a development loan of \\$" + MONEY
                + " from the bank, secured by \"" + NAME + "\", funded by pawn \"" + NAME + "\"'s bond purchase$",
            (world, arguments) -> says(world, arguments.text(1) + " raises a development loan of $"
                + money(arguments.text(2)).amount() + " from the bank, secured by "
                + reportSpace(arguments.text(3)) + ", funded by " + arguments.text(4) + "'s bond purchase")),
        then("^the game report says that pawn \"" + NAME + "\" pays the bank \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " pays the bank $"
                + money(arguments.text(2)).amount() + " interest and $" + money(arguments.text(3)).amount()
                + " principal on the development loan secured by " + reportSpace(arguments.text(4)))), 
        then("^the game report says that pawn \"" + NAME + "\"'s development loan on \"" + NAME
                + "\" has been fully repaid$",
            (world, arguments) -> says(world, arguments.text(1) + "'s development loan on "
                + reportSpace(arguments.text(2)) + " has been fully repaid")),
        then("^the game report says that pawn \"" + NAME + "\" receives \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan bond secured by \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " receives $"
                + money(arguments.text(2)).amount() + " interest and $" + money(arguments.text(3)).amount()
                + " principal on the development loan bond secured by " + reportSpace(arguments.text(4)))), 
        then("^the game report says that development loans are " + NAME + "$",
            (world, arguments) -> says(world, "development loans are " + arguments.text(1))),
        then("^the game report says that " + NAME + " raises a development loan of \\$" + MONEY
                + " from the bank, secured by \"" + NAME + "\", funded by pawn \"" + NAME + "\"'s bond purchase$",
            (world, arguments) -> says(world, arguments.text(1) + " raises a development loan of $"
                + money(arguments.text(2)).amount() + " from the bank, secured by "
                + reportSpace(arguments.text(3)) + ", funded by " + arguments.text(4) + "'s bond purchase")), 
        then("^the game report says that " + NAME + " pays the bank \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " pays the bank $" + money(arguments.text(2)).amount()
                + " interest and $" + money(arguments.text(3)).amount() + " principal on the development loan secured by "
                + reportSpace(arguments.text(4)))), 
        then("^the game report says that " + NAME + " defaults on the development loan secured by \"" + NAME
                + "\"; the bank forecloses$",
            (world, arguments) -> says(world, arguments.text(1) + " defaults on the development loan secured by "
                + reportSpace(arguments.text(2)) + "; the bank forecloses")),
        then("^the game report says that pawn \"" + NAME + "\" defaults on the development loan secured by \""
                + NAME + "\"; the bank forecloses$",
            (world, arguments) -> says(world, arguments.text(1) + " defaults on the development loan secured by "
                + reportSpace(arguments.text(2)) + "; the bank forecloses")),
        then("^the game report says that " + NAME + " raises a loan of \\$" + VALUE
                + " from pawn \"dog\", pawn \"high hat\", and pawn \"iron box\"$",
            (world, arguments) -> says(world, arguments.text(1) + " raises a loan of $"
                + arguments.number(2) + " from dog, high hat, iron box")), 
        then("^the game report says that the bank recovers \\$" + MONEY + " from the foreclosure of \"" + NAME
                + "\", added to its own account$",
            (world, arguments) -> says(world, "The bank recovers $" + money(arguments.text(1)).amount()
                + " from the foreclosure of " + reportSpace(arguments.text(2)) + ", added to its own account")),
        then("^the game report says that " + NAME + "'s development loan on \"" + NAME
                + "\" has been fully repaid$",
            (world, arguments) -> says(world, arguments.text(1) + "'s development loan on "
                + reportSpace(arguments.text(2)) + " has been fully repaid")),
        then("^the game report says that " + NAME + " pays pawn \"" + NAME + "\" \\$" + VALUE
                + " for the loan$", 
            (world, arguments) -> says(world, arguments.text(1) + " repays " + arguments.text(2)
                + " $" + arguments.number(3) + " for the loan")),
        then("^the game report says that " + NAME + " pays each of pawn \"" + NAME + "\", pawn \""
                + NAME + "\", and pawn \"" + NAME + "\" an equal dividend$",
            (world, arguments) -> says(world, arguments.text(1) + " pays each of")),
        then("^the game report says that pawn \"" + NAME + "\" pays \\$" + VALUE
                + " rent to " + NAME + " for \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " pays $"
                + arguments.number(2) + " rent to " + arguments.text(3) + " for " + arguments.text(4))),
        then("^the game report says that " + NAME + " builds a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> says(world, builtAHouse(arguments.text(1),
                SpaceNames.of(arguments.text(2)).name().replaceAll("(?<=[a-z])(?=[A-Z])", " "),
                arguments.number(3)))),
        then("^the game log records that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years before it records that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                turnStartedAtAge(arguments.text(1), arguments.number(2)),
                salaryCollected(arguments.text(3), arguments.number(4)))),
        then("^the game log records that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE
                + " before it records that pawn \"" + NAME + "\" starts a turn aged " + VALUE + " years$",
            (world, arguments) -> logRecordsInOrder(world,
                salaryCollected(arguments.text(1), arguments.number(2)),
                turnStartedAtAge(arguments.text(3), arguments.number(4)))),
        then("^the game log records that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years before it records that pawn \"" + NAME + "\" is sent to jail from landing on \"" + NAME + "\"$",
            (world, arguments) -> logRecordsInOrder(world,
                turnStartedAtAge(arguments.text(1), arguments.number(2)),
                jailEntered(arguments.text(3), arguments.text(4)))),
        then("^the game log records that pawn \"" + NAME + "\" is sent to jail from landing on \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" starts a turn aged " + VALUE + " years$",
            (world, arguments) -> logRecordsInOrder(world,
                jailEntered(arguments.text(1), arguments.text(2)),
                turnStartedAtAge(arguments.text(3), arguments.number(4)))),
        then("^the game log records that pawn \"" + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> logRecords(world, finalAge(arguments.text(1), arguments.number(2)))),
        then("^the game log records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE
                + " before it records that pawn \"" + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> logRecordsInOrder(world,
                finalBalance(arguments.text(1), arguments.number(2)),
                finalAge(arguments.text(3), arguments.number(4)))),
        then("^the game log records that pawn \"" + NAME + "\"'s final age is " + VALUE
                + " years before it records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                finalAge(arguments.text(1), arguments.number(2)),
                finalBalance(arguments.text(3), arguments.number(4)))),
        then("^the game report says that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years before it says that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " starts a turn aged " + arguments.number(2) + " years",
                arguments.text(3) + " collects a salary of $" + arguments.number(4))),
        then("^the game report says that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE
                + " before it says that pawn \"" + NAME + "\" starts a turn aged " + VALUE + " years$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " collects a salary of $" + arguments.number(2),
                arguments.text(3) + " starts a turn aged " + arguments.number(4) + " years")),
        then("^the game report says that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years before it says that pawn \"" + NAME + "\" is sent to jail from landing on \"" + NAME + "\"$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " starts a turn aged " + arguments.number(2) + " years",
                arguments.text(3) + " is sent to jail from landing on " + arguments.text(4))),
        then("^the game report says that pawn \"" + NAME + "\" is sent to jail from landing on \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" starts a turn aged " + VALUE + " years$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " is sent to jail from landing on " + arguments.text(2),
                arguments.text(3) + " starts a turn aged " + arguments.number(4) + " years")),
        then("^the game report says that pawn \"" + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> says(world, arguments.text(1) + "'s final age is " + arguments.number(2) + " years")),
        then("^the game report says that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE
                + " before it says that pawn \"" + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + "'s final balance is $" + arguments.number(2),
                arguments.text(3) + "'s final age is " + arguments.number(4) + " years")),
        then("^the game report says that pawn \"" + NAME + "\"'s final age is " + VALUE
                + " years before it says that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + "'s final age is " + arguments.number(2) + " years",
                arguments.text(3) + "'s final balance is $" + arguments.number(4))),
        then("^the game log records that pawn \"" + NAME + "\" wins the game before it records that pawn \""
                + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> logRecordsInOrder(world,
                Claim.of(new Entry.Won(idOf(arguments.text(1)))),
                finalAge(arguments.text(2), arguments.number(3)))),
        then("^the game report says that pawn \"" + NAME + "\" wins the game before it says that pawn \""
                + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " wins the game",
                arguments.text(2) + "'s final age is " + arguments.number(3) + " years")),
        then("^the game journal records that pawn \"" + NAME + "\" wins the split monopoly$",
            (world, arguments) -> records(world, splitMonopolyWon(arguments.text(1)))),
        then("^the game journal records that pawn \"" + NAME + "\" wins the split monopoly before it records that pawn \""
                + NAME + "\" starts a turn$",
            (world, arguments) -> recordsInOrder(world, splitMonopolyWon(arguments.text(1)),
                turnStarted(arguments.text(2)))),
        then("^the game journal records that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE
                + " for the split monopoly$",
            (world, arguments) -> records(world, splitMonopolyPaid(
                arguments.text(1), arguments.text(2), arguments.number(3)))),
        then("^the game log records that pawn \"" + NAME + "\" wins the split monopoly before it records that pawn \""
                + NAME + "\" starts a turn$",
            (world, arguments) -> logRecordsInOrder(world, splitMonopolyWon(arguments.text(1)),
                turnStarted(arguments.text(2)))),
        then("^the game log records that pawn \"" + NAME + "\" wins the split monopoly$",
            (world, arguments) -> logRecords(world, splitMonopolyWon(arguments.text(1)))),
        then("^the game log records that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE
                + " for the split monopoly$",
            (world, arguments) -> logRecords(world, splitMonopolyPaid(
                arguments.text(1), arguments.text(2), arguments.number(3)))),
        then("^the game report says that pawn \"" + NAME + "\" wins the split monopoly before it says that pawn \""
                + NAME + "\" starts a turn$",
            (world, arguments) -> saysInOrder(world, arguments.text(1) + " wins the split monopoly",
                arguments.text(2) + " starts a turn")),
        then("^the game report says that pawn \"" + NAME + "\" wins the split monopoly$",
            (world, arguments) -> says(world, arguments.text(1) + " wins the split monopoly")),
        then("^the game report says that pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE
                + " for the split monopoly$",
            (world, arguments) -> says(world, arguments.text(1) + " pays " + arguments.text(2) + " $"
                + arguments.number(3) + " for the split monopoly")),
        then("^the game journal records that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME + "\"$",
            (world, arguments) -> records(world, peerTrade(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.text(4)))),
        then("^the game log records that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, peerTrade(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.text(4)))),
        then("^the game report says that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " trades " + arguments.text(2)
                + " to " + arguments.text(3) + " for " + arguments.text(4))),
        then("^the game journal does not record that pawn \"" + NAME + "\" wins the split monopoly$",
            (world, arguments) -> assertThat(world.journal()).noneMatch(
                splitMonopolyWon(arguments.text(1)).matches())),
        then("^the game log does not record that pawn \"" + NAME + "\" wins the split monopoly$",
            (world, arguments) -> assertThat(world.gameLog()).noneMatch(
                splitMonopolyWon(arguments.text(1)).matches())),
        then("^the game report does not say that pawn \"" + NAME + "\" wins the split monopoly$",
            (world, arguments) -> assertThat(world.report()).doesNotContain(
                arguments.text(1) + " wins the split monopoly")),
        then("^the game log records that stalemate trading is " + NAME + "$",
            (world, arguments) -> logRecords(world, stalemateTrading(arguments.text(1)))),
        then("^the game report says that stalemate trading is " + NAME + "$",
            (world, arguments) -> says(world, "stalemate trading is " + arguments.text(1))),
        then("^the game journal records that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> recordsInOrder(world,
                peerTrade(arguments.text(1), arguments.text(2), arguments.text(3), arguments.text(4)),
                turnStarted(arguments.text(5)))),
        then("^the game journal does not record that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.journal()).noneMatch(peerTrade(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.text(4)).matches())),
        then("^the game log records that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> logRecordsInOrder(world,
                peerTrade(arguments.text(1), arguments.text(2), arguments.text(3), arguments.text(4)),
                turnStarted(arguments.text(5)))),
        then("^the game log does not record that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.gameLog()).noneMatch(peerTrade(
                arguments.text(1), arguments.text(2), arguments.text(3), arguments.text(4)).matches())),
        then("^the game report says that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " trades " + arguments.text(2) + " to " + arguments.text(3)
                    + " for " + arguments.text(4),
                arguments.text(5) + " starts a turn")),
        then("^the game report does not say that pawn \"" + NAME + "\" trades \"" + NAME
                + "\" to pawn \"" + NAME + "\" for \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.report()).doesNotContain(arguments.text(1) + " trades "
                + arguments.text(2) + " to " + arguments.text(3) + " for " + arguments.text(4))),
        then("^the game log records that the game ends in a stalemate before it records that pawn \""
                + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                new Claim(entry -> entry instanceof Entry.Stalemate, "game ends in a stalemate"),
                finalBalance(arguments.text(1), arguments.number(2)))),

        then("^the game log records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, finalBalance(arguments.text(1), arguments.number(2)))),

        then("^the game log records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE
                + " before it records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                finalBalance(arguments.text(1), arguments.number(2)),
                finalBalance(arguments.text(3), arguments.number(4)))),

        then("^the game report says that the game ends in a stalemate before it says that pawn \""
                + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world, "The game ends in a stalemate",
                arguments.text(1) + "'s final balance is $" + arguments.number(2))),

        then("^the game report says that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> says(world, arguments.text(1) + "'s final balance is $" + arguments.number(2))),

        then("^the game report says that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE
                + " before it says that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + "'s final balance is $" + arguments.number(2),
                arguments.text(3) + "'s final balance is $" + arguments.number(4))),

        then("^the game journal records that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\" before it records that pawn \"" + NAME
                + "\" offers \\$" + VALUE + " for \"" + NAME + "\"$",
            (world, arguments) -> recordsInOrder(world,
                distressedOffer(arguments.text(1), arguments.text(3), arguments.number(2)),
                distressedOffer(arguments.text(4), arguments.text(6), arguments.number(5)))),
        then("^the game log records that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\" before it records that pawn \"" + NAME
                + "\" offers \\$" + VALUE + " for \"" + NAME + "\"$",
            (world, arguments) -> logRecordsInOrder(world,
                distressedOffer(arguments.text(1), arguments.text(3), arguments.number(2)),
                distressedOffer(arguments.text(4), arguments.text(6), arguments.number(5)))),
        then("^the game report says that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\" before it says that pawn \"" + NAME
                + "\" offers \\$" + VALUE + " for \"" + NAME + "\"$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " offers $" + arguments.number(2) + " for " + arguments.text(3),
                arguments.text(4) + " offers $" + arguments.number(5) + " for " + arguments.text(6))),
        then("^the game journal records that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\" before it records that pawn \"" + NAME
                + "\" wins the distressed sale for \"" + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                distressedOffer(arguments.text(1), arguments.text(3), arguments.number(2)),
                distressedWon(arguments.text(4), arguments.text(5), arguments.number(6)))),
        then("^the game log records that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\" before it records that pawn \"" + NAME
                + "\" wins the distressed sale for \"" + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                distressedOffer(arguments.text(1), arguments.text(3), arguments.number(2)),
                distressedWon(arguments.text(4), arguments.text(5), arguments.number(6)))),
        then("^the game report says that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\" before it says that pawn \"" + NAME
                + "\" wins the distressed sale for \"" + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " offers $" + arguments.number(2) + " for " + arguments.text(3),
                arguments.text(4) + " wins the distressed sale for " + arguments.text(5)
                    + " at $" + arguments.number(6))),
        then("^the game journal records that pawn \"" + NAME + "\" puts \"" + NAME
                + "\" up for sale to avoid bankruptcy$",
            (world, arguments) -> records(world, distressedStarted(arguments.text(1), arguments.text(2)))),
        then("^the game journal records that pawn \"" + NAME + "\" finds no bidder for "
                + UNQUOTED_NAME_WITHOUT_ORDERING + "$",
            (world, arguments) -> records(world, distressedNoBidder(arguments.text(1), arguments.text(2)))),
        then("^the game journal records that pawn \"" + NAME + "\" puts " + UNQUOTED_NAME
                + " up for sale to avoid bankruptcy before it records that pawn \"" + NAME
                + "\" finds no bidder for " + UNQUOTED_NAME + "$",
            (world, arguments) -> recordsInOrder(world,
                distressedStarted(arguments.text(1), arguments.text(2)),
                distressedNoBidder(arguments.text(3), arguments.text(4)))),
        then("^the game journal records that pawn \"" + NAME + "\" finds no bidder for " + UNQUOTED_NAME
                + " before it records that pawn \"" + NAME + "\" mortgages " + UNQUOTED_NAME
                + " for \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                distressedNoBidder(arguments.text(1), arguments.text(2)),
                mortgaged(arguments.text(3), arguments.text(4), arguments.number(5)))),
        then("^the game journal records that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\"$",
            (world, arguments) -> records(world, distressedOffer(
                arguments.text(1), arguments.text(3), arguments.number(2)))),
        then("^the game journal records that pawn \"" + NAME + "\" wins the distressed sale for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> records(world, distressedWon(
                arguments.text(1), arguments.text(2), arguments.number(3)))),
        then("^the game log records that pawn \"" + NAME + "\" puts \"" + NAME
                + "\" up for sale to avoid bankruptcy$",
            (world, arguments) -> logRecords(world, distressedStarted(arguments.text(1), arguments.text(2)))),
        then("^the game log records that pawn \"" + NAME + "\" finds no bidder for "
                + UNQUOTED_NAME_WITHOUT_ORDERING + "$",
            (world, arguments) -> logRecords(world, distressedNoBidder(arguments.text(1), arguments.text(2)))),
        then("^the game log records that pawn \"" + NAME + "\" puts " + UNQUOTED_NAME
                + " up for sale to avoid bankruptcy before it records that pawn \"" + NAME
                + "\" finds no bidder for " + UNQUOTED_NAME + "$",
            (world, arguments) -> logRecordsInOrder(world,
                distressedStarted(arguments.text(1), arguments.text(2)),
                distressedNoBidder(arguments.text(3), arguments.text(4)))),
        then("^the game log records that pawn \"" + NAME + "\" finds no bidder for " + UNQUOTED_NAME
                + " before it records that pawn \"" + NAME + "\" mortgages " + UNQUOTED_NAME
                + " for \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                distressedNoBidder(arguments.text(1), arguments.text(2)),
                mortgaged(arguments.text(3), arguments.text(4), arguments.number(5)))),
        then("^the game log records that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, distressedOffer(
                arguments.text(1), arguments.text(3), arguments.number(2)))),
        then("^the game log records that pawn \"" + NAME + "\" wins the distressed sale for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> logRecords(world, distressedWon(
                arguments.text(1), arguments.text(2), arguments.number(3)))),
        then("^the game report says that pawn \"" + NAME + "\" puts \"" + NAME
                + "\" up for sale to avoid bankruptcy$",
            (world, arguments) -> says(world, arguments.text(1) + " puts " + arguments.text(2)
                + " up for sale to avoid bankruptcy")),
        then("^the game report says that pawn \"" + NAME + "\" finds no bidder for "
                + UNQUOTED_NAME_WITHOUT_ORDERING + "$",
            (world, arguments) -> says(world, arguments.text(1) + " finds no bidder for " + arguments.text(2))),
        then("^the game report says that pawn \"" + NAME + "\" puts " + UNQUOTED_NAME
                + " up for sale to avoid bankruptcy before it says that pawn \"" + NAME
                + "\" finds no bidder for " + UNQUOTED_NAME + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " puts " + arguments.text(2) + " up for sale to avoid bankruptcy",
                arguments.text(3) + " finds no bidder for " + arguments.text(4))),
        then("^the game report says that pawn \"" + NAME + "\" finds no bidder for " + UNQUOTED_NAME
                + " before it says that pawn \"" + NAME + "\" mortgages " + UNQUOTED_NAME
                + " for \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                arguments.text(1) + " finds no bidder for " + arguments.text(2),
                arguments.text(3) + " mortgages " + arguments.text(4) + " for $" + arguments.number(5))),
        then("^the game report says that pawn \"" + NAME + "\" offers \\$" + VALUE
                + " for \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " offers $" + arguments.number(2)
                + " for " + arguments.text(3))),
        then("^the game report says that pawn \"" + NAME + "\" wins the distressed sale for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> says(world, arguments.text(1) + " wins the distressed sale for "
                + arguments.text(2) + " at $" + arguments.number(3))),
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

        then("^the game journal records that pawn \"" + NAME + "\" inherits \"" + NAME
                + "\" from pawn \"" + NAME + "\"$",
            (world, arguments) -> records(world, inherited(
                arguments.text(1), arguments.text(2), arguments.text(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" pays \\$" + VALUE
                + " interest to keep the mortgage on \"" + NAME + "\"$",
            (world, arguments) -> records(world, mortgageKept(
                arguments.text(1), arguments.text(3), arguments.number(2)))),

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

        then("^the game journal records that pawn \"" + NAME + "\" declines to buy \"" + NAME
                + "\" because it cannot afford the \\$" + VALUE + " price$",
            (world, arguments) -> records(world, purchaseDeclined(
                arguments.text(1), arguments.text(2), arguments.number(3),
                Strategy.DeclineReason.CANNOT_AFFORD, 0))),

        then("^the game journal records that pawn \"" + NAME + "\" declines to buy \"" + NAME
                + "\" because it would drop the balance below the \\$" + VALUE + " reserve$",
            (world, arguments) -> records(world, purchaseDeclined(
                arguments.text(1), arguments.text(2), 0,
                Strategy.DeclineReason.CASH_RESERVE, arguments.number(3)))),

        then("^the game journal records that pawn \"" + NAME + "\" declines to buy \"" + NAME + "\"$",
            (world, arguments) -> records(world, purchaseDeclined(
                arguments.text(1), arguments.text(2), 0,
                Strategy.DeclineReason.NO_BUYING_POLICY, 0))),

        then("^the game journal records that pawn \"" + NAME + "\" starts a turn with \\$" + VALUE
                + " and a \\$" + VALUE + " reserve$",
            (world, arguments) -> records(world, turnStarted(
                arguments.text(1), arguments.number(2), arguments.number(3)))),

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

        then("^the game journal records that pawn \"" + NAME + "\" stays in jail$",
            (world, arguments) -> records(world, jailStayed(arguments.text(1)))),
        then("^the game journal records that pawn \"" + NAME + "\" leaves jail by rolling doubles$",
            (world, arguments) -> records(world, jailDoublesRolled(arguments.text(1)))),
        then("^the game journal records that pawn \"" + NAME
                + "\" leaves jail using the Get Out of Jail Free card$",
            (world, arguments) -> records(world, jailCardUsed(arguments.text(1)))),

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
              saysTurnStartedWithBalance(world, pawnName, balance);
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

        then("^the game log records that pawn \"" + NAME + "\" inherits \"" + NAME
                + "\" from pawn \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, inherited(
                arguments.text(1), arguments.text(2), arguments.text(3)))),

        then("^the game log records that pawn \"" + NAME + "\" pays \\$" + VALUE
                + " interest to keep the mortgage on \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, mortgageKept(
                arguments.text(1), arguments.text(3), arguments.number(2)))),

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

        then("^the game log records that pawn \"" + NAME + "\" declines to buy \"" + NAME
                + "\" because it cannot afford the \\$" + VALUE + " price$",
            (world, arguments) -> logRecords(world, purchaseDeclined(
                arguments.text(1), arguments.text(2), arguments.number(3),
                Strategy.DeclineReason.CANNOT_AFFORD, 0))),

        then("^the game log records that pawn \"" + NAME + "\" declines to buy \"" + NAME
                + "\" because it would drop the balance below the \\$" + VALUE + " reserve$",
            (world, arguments) -> logRecords(world, purchaseDeclined(
                arguments.text(1), arguments.text(2), 0,
                Strategy.DeclineReason.CASH_RESERVE, arguments.number(3)))),

        then("^the game log records that pawn \"" + NAME + "\" declines to buy \"" + NAME + "\"$",
            (world, arguments) -> logRecords(world, purchaseDeclined(
                arguments.text(1), arguments.text(2), 0,
                Strategy.DeclineReason.NO_BUYING_POLICY, 0))),

        then("^the game log records that pawn \"" + NAME + "\" starts a turn with \\$" + VALUE
                + " and a \\$" + VALUE + " reserve$",
            (world, arguments) -> logRecords(world, turnStarted(
                arguments.text(1), arguments.number(2), arguments.number(3)))),

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

        then("^the game log records that pawn \"" + NAME + "\" stays in jail$",
            (world, arguments) -> logRecords(world, jailStayed(arguments.text(1)))),
        then("^the game log records that pawn \"" + NAME + "\" leaves jail by rolling doubles$",
            (world, arguments) -> logRecords(world, jailDoublesRolled(arguments.text(1)))),
        then("^the game log records that pawn \"" + NAME
                + "\" leaves jail using the Get Out of Jail Free card$",
            (world, arguments) -> logRecords(world, jailCardUsed(arguments.text(1)))),

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

        then("^the game report says that pawn \"" + NAME + "\" wins the auction for \""
                + NAME + "\" at \\$" + VALUE + "$",
            (world, arguments) -> says(world, auctionWonLine(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

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

        then("^the game report says that pawn \"" + NAME + "\" inherits \"" + NAME
                + "\" from pawn \"" + NAME + "\"$",
            (world, arguments) -> says(world, inheritedLine(
                arguments.text(1), arguments.text(2), arguments.text(3)))),

        then("^the game report says that pawn \"" + NAME + "\" pays \\$" + VALUE
                + " interest to keep the mortgage on \"" + NAME + "\"$",
            (world, arguments) -> says(world, mortgageKeptLine(
                arguments.text(1), arguments.text(3), arguments.number(2)))),

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

        then("^the game report says that pawn \"" + NAME + "\" declines to buy \"" + NAME
                + "\" because it cannot afford the \\$" + VALUE + " price$",
            (world, arguments) -> says(world, purchaseDeclinedLine(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME + "\" declines to buy \"" + NAME
                + "\" because it would drop the balance below the \\$" + VALUE + " reserve$",
            (world, arguments) -> says(world, purchaseDeclinedForReserveLine(
                arguments.text(1), arguments.text(2), arguments.number(3)))),

        then("^the game report says that pawn \"" + NAME + "\" declines to buy \"" + NAME + "\"$",
            (world, arguments) -> says(world, purchaseDeclinedLine(
                arguments.text(1), arguments.text(2)))),

        then("^the game report says that pawn \"" + NAME + "\" starts a turn with \\$" + VALUE
                + " and a \\$" + VALUE + " reserve$",
            (world, arguments) -> saysTurnStartedWith(world, arguments.text(1),
                arguments.number(2), arguments.number(3))),

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

        then("^the game report says that pawn \"" + NAME + "\" stays in jail$",
            (world, arguments) -> says(world, jailStayedLine(arguments.text(1)))),
        then("^the game report says that pawn \"" + NAME + "\" leaves jail by rolling doubles$",
            (world, arguments) -> says(world, jailDoublesRolledLine(arguments.text(1)))),
        then("^the game report says that pawn \"" + NAME
                + "\" leaves jail using the Get Out of Jail Free card$",
            (world, arguments) -> says(world, jailCardUsedLine(arguments.text(1)))),

        then("^the game report says that pawn \"" + NAME + "\" goes bankrupt to the bank$",
            (world, arguments) -> says(world, arguments.text(1) + " goes bankrupt to the bank")),

        then("^the game report says that pawn \"" + NAME + "\" goes bankrupt to pawn \"" + NAME + "\"$",
            (world, arguments) -> says(world, arguments.text(1) + " goes bankrupt to " + arguments.text(2))),

        then("^the game report says that pawn \"" + NAME + "\" wins the game$",
            (world, arguments) -> says(world, arguments.text(1) + " wins the game")),

        then("^the game log records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" receives \\$" + VALUE + " from the bank$",
            (world, arguments) -> logRecordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                bankReceived(arguments.text(3), arguments.number(4)))),

        then("^the game report says that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it says that pawn \"" + NAME + "\" receives \\$" + VALUE + " from the bank$",
            (world, arguments) -> saysInOrder(world,
                chanceCardDrawnLine(arguments.text(1), arguments.text(2)),
                bankReceivedLine(arguments.text(3), arguments.number(4)))),

        step("^each face was rolled about " + VALUE + " times within a " + VALUE + "% margin$",
            (world, arguments) -> {
              int expected = arguments.number(1);
              double margin = expected * (arguments.number(2) / 100.0);
              assertThat(world.rolls().values())
                  .allSatisfy(seen -> assertThat(seen).isCloseTo(expected, within((int) margin)));
            }),
        then("^the game log records that the game ends because the year limit was reached before it records that pawn \"" + NAME
                + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> logRecordsInOrder(world,
                new Claim(entry -> entry instanceof Entry.YearLimitReached, "game ends because the year limit was reached"),
                finalBalance(arguments.text(1), arguments.number(2)))),

        then("^the game report says that the game ends because the year limit was reached before it says that pawn \"" + NAME
                + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> saysInOrder(world, "The year limit was reached",
                arguments.text(1) + "'s final balance is $" + arguments.number(2)))
    );
  }

  private static String reportSpace(String featureName) {
    return switch (SpaceNames.of(featureName)) {
      case RueDeDiekirchArlon -> "Rue de Diekirch Arlon";
      default -> SpaceNames.of(featureName).name().replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    };
  }
}
