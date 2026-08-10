package the.monopoly.game.specs.acceptance;

import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.streets.Street;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.specs.acceptance.GameAccount.Claim;
import static the.monopoly.game.specs.acceptance.GameAccount.records;
import static the.monopoly.game.specs.acceptance.GameAccount.recordsInOrder;
import static the.monopoly.game.specs.acceptance.GameAccount.recordsStartWith;
import static the.monopoly.game.specs.acceptance.GameAccount.says;
import static the.monopoly.game.specs.acceptance.GameAccount.saysInOrder;
import static the.monopoly.game.specs.acceptance.GameAccount.saysStartWith;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.NAME;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.VALUE;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.chanceCardDrawn;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.chanceCardDrawnLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankReceived;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.bankReceivedLine;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.initiativeRoll;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.initiativeWon;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.idOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.jailEntered;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.finalBalance;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.finalAge;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.money;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.moved;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.movesFromPosition;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rolled;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rollsATotalOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rollsForInitiative;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.salaryCollected;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.stalemateTrading;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.turnStarted;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.turnStartedAtAge;
import static the.monopoly.game.specs.acceptance.StepHandler.given;
import static the.monopoly.game.specs.acceptance.StepHandler.step;
import static the.monopoly.game.specs.acceptance.StepHandler.then;

/**
 * The turn-order, initiative, and per-property setup/state steps, plus the
 * journal/report assertions for the initiative-and-turn family of events.
 * Split out of {@link MonopolyStepHandlers} to keep its mutation-site count down;
 * {@link MonopolyStepHandlers#handlers()} is the one entry point callers use.
 */
final class JournalStepHandlers {
  private JournalStepHandlers() {
  }

  static List<StepHandler> handlers() {
    return List.of(
        step("^every other player can complete their turn$",
            (world, arguments) -> world.letTheOthersRollWhatTheyLike()),

        given("^legal-entity trading is enabled for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableLegalEntityTrading(arguments.text(1))),

        given("^<enabled_flag> trading is enabled for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableStalemateTrading(arguments.text(1))),

        step("^we select (<player_count>) players$",
            (world, arguments) -> world.selectPlayers(arguments.number(1))),

        given("^Pink Realty owes pawn \"dog\" \\$(<principal>|<loan>)$",
            (world, arguments) -> {
              world.entityOwes("Pink Realty", money(arguments.number(1)));
              world.letTheOthersRollWhatTheyLike();
            }),

        given("^" + NAME + " owes pawn \"dog\" \\$100$",
            (world, arguments) -> {
              world.entityOwes(arguments.text(1), money(100));
              world.letTheOthersRollWhatTheyLike();
            }),
        step("^pawn \"" + NAME + "\" will roll " + VALUE + " for their turn$",
            (world, arguments) -> {
              world.letTheOthersRollWhatTheyLike();
              world.queuePawnRoll(arguments.text(1), new the.monopoly.game.components.dice.Roll(6, 6));
            }),

        given("^" + NAME + " is formed$",
            (world, arguments) -> world.formNamedEntity(arguments.text(1))),

        given("^Pink Realty's loan has been fully repaid$",
            (world, arguments) -> world.entityLoanFullyRepaid("Pink Realty")),

        given("^Pink Realty's bank account holds \\$" + VALUE + "$",
            (world, arguments) -> world.entityBankHolds("Pink Realty", money(arguments.number(1)))),

        then("^Pink Realty's bank account holds \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.entityBankBalance("Pink Realty"))
                .isEqualTo(money(arguments.number(1)))),

        step("^Pink Realty raises a loan of \\$" + VALUE + "$",
            (world, arguments) -> world.entityRaisesLoan("Pink Realty", money(arguments.number(1)))),

        given("^Pink Realty owns no outstanding loan$",
            (world, arguments) -> assertThat(world.entityLoan("Pink Realty")).isEqualTo(money(0))),

        given("^Pink Realty's bank account is empty$",
            (world, arguments) -> world.entityBankHolds("Pink Realty", money(0))),

        then("^the pink colour group is developed up to at least (<houses_at_least>) houses$",
            (world, arguments) -> assertThat(world.totalHouses(Street.Colour.pink))
                .isGreaterThanOrEqualTo(arguments.number(1))),

        then("^Pink Realty raises no more than \\$(<max_loan>) in loans$",
            (world, arguments) -> assertThat(world.gameLog().stream()
                .filter(Entry.LegalEntityLoanRaised.class::isInstance)
                .map(Entry.LegalEntityLoanRaised.class::cast)
                .filter(entry -> entry.name().equals("Pink Realty"))
                .mapToInt(entry -> entry.amount().amount()).sum())
                .isLessThanOrEqualTo(arguments.number(1))),

        then("^each of pawn \"dog\", pawn \"high hat\", and pawn \"iron box\" receives a \\$(<dividend_share>) dividend from Pink Realty$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityDividendPaid it
                && it.name().equals("Pink Realty") && it.amount().amount() == arguments.number(1), "dividend amount"))),

        then("^the " + NAME + ", the " + NAME + ", and the " + NAME + " each have a house built$",
            (world, arguments) -> {
              assertThat(world.housesBuilt(SpaceNames.of(arguments.text(1)))).isGreaterThanOrEqualTo(1);
              assertThat(world.housesBuilt(SpaceNames.of(arguments.text(2)))).isGreaterThanOrEqualTo(1);
              assertThat(world.housesBuilt(SpaceNames.of(arguments.text(3)))).isGreaterThanOrEqualTo(1);
            }),

        then("^pawn \"" + NAME + "\" has paid \\$" + VALUE + " in rent$",
            (world, arguments) -> assertThat(world.pawnBalanceIsAfterRent(
                arguments.text(1), money(arguments.number(2)))).isTrue()),

        then("^Pink Realty issues no loan, repayment, or dividend$",
            (world, arguments) -> assertThat(world.gameLog().stream().anyMatch(entry ->
                entry instanceof Entry.LegalEntityLoanRaised
                    || entry instanceof Entry.LegalEntityLoanRepaid
                    || entry instanceof Entry.LegalEntityDividendPaid)).isFalse()),

        then("^Pink Realty still owes pawn \"dog\" \\$(<principal>)$",
            (world, arguments) -> assertThat(world.entityLoan("Pink Realty"))
                .isEqualTo(money(arguments.number(1)))),

        given("^pawn \"" + NAME + "\" has a balance that allows only \\$(<ceiling_share>) toward the entity$",
            (world, arguments) -> world.arrangePawnBalance(arguments.text(1), money(arguments.number(2)))),

        step("^pawn \"" + NAME + "\" considers forming a legal entity over the " + NAME + " colour group$",
            (world, arguments) -> world.considerFormingLegalEntity(arguments.text(1), arguments.text(2))),

        then("^the " + NAME + " colour group is owned by " + NAME + "$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity(arguments.text(1))).isTrue()),

        then("^the " + NAME + " colour group is not owned by a legal entity$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity(arguments.text(1))).isFalse()),

        then("^the pink colour group <outcome> a legal entity$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity("pink")).isFalse()),

        then("^each of pawn \"" + NAME + "\", pawn \"" + NAME + "\", and pawn \"" + NAME
                + "\" receives a dividend from " + NAME + "$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityDividendPaid,
                "entity dividend"))),

        then("^pawn \"dog\" receives no dividend$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRepaid,
                "loan repayment without dividend"))),

        then("^pawn \"dog\" receives no dividend from Pink Realty$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRepaid it
                && it.name().equals("Pink Realty"), "Pink Realty repayment without dividend"))),

        then("^each of pawn \"" + NAME + "\", pawn \"" + NAME + "\", and pawn \"" + NAME
                + "\" holds a third of " + NAME + "$",
            (world, arguments) -> assertThat(world.shareholdersHoldEqualThirds(arguments.text(4))).isTrue()),

        then("^the game journal records that " + NAME + " is formed, held in equal thirds by pawn \"" + NAME
                + "\", pawn \"" + NAME + "\", and pawn \"" + NAME + "\"$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityFormed it
                && it.name().equals(arguments.text(1)), arguments.text(1) + " is formed"))),

        then("^the game journal records that " + NAME + " raises a loan of \\$" + VALUE
                + " from pawn \"" + NAME + "\", pawn \"" + NAME + "\", and pawn \"" + NAME + "\"$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRaised it
                && it.name().equals(arguments.text(1)) && it.amount().amount() == arguments.number(2), "loan raised"))),

        then("^the game journal records that " + NAME + " repays pawn \"" + NAME + "\" \\$" + VALUE
                + " for the loan$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRepaid it
                && it.name().equals(arguments.text(1)) && it.shareholder().equals(idOf(arguments.text(2)))
                && it.repayment().amount() == arguments.number(3), "loan repaid"))),

        then("^Pink Realty repays pawn \"dog\" \\$<repayment> for the loan$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityLoanRepaid it
                && it.name().equals("Pink Realty") && it.repayment().amount() == 105, "loan repaid"))),

        then("^the game journal records that " + NAME + " pays each of pawn \"" + NAME + "\", pawn \""
                + NAME + "\", and pawn \"" + NAME + "\" an equal dividend$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityDividendPaid it
                && it.name().equals(arguments.text(1)), "equal dividend"))),

        step("^we play up to " + VALUE + " rounds$",
            (world, arguments) -> world.playUpToRounds(arguments.number(1))),

        step("^we play up to " + VALUE + " round$",
            (world, arguments) -> world.playUpToRounds(arguments.number(1))),

        given("^pawn \"" + NAME + "\"'s account holds \\$" + VALUE + "$",
            (world, arguments) -> world.holdPawnBalance(arguments.text(1), money(arguments.number(2)))),

        then("^the stalemate detection threshold is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.stalemateThreshold()).isEqualTo(money(arguments.number(1)))),

        then("^the game ends in a stalemate$",
            (world, arguments) -> assertThat(world.endedInStalemate()).isTrue()),

        then("^the game does not end in a stalemate$",
            (world, arguments) -> assertThat(world.endedInStalemate()).isFalse()),

        then("^the game journal records that the game ends in a stalemate only once$",
            (world, arguments) -> assertThat(world.journal().stream()
                .filter(Entry.Stalemate.class::isInstance).count()).isOne()),

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

        then("^the game journal records that stalemate trading is " + NAME + "$",
            (world, arguments) -> records(world, stalemateTrading(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls " + VALUE + " for initiative$",
            (world, arguments) -> records(world, initiativeRoll(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" wins initiative$",
            (world, arguments) -> records(world, initiativeWon(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" starts a turn$",
            (world, arguments) -> records(world, turnStarted(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years$",
            (world, arguments) -> records(world, turnStartedAtAge(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years before it records that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                turnStartedAtAge(arguments.text(1), arguments.number(2)),
                salaryCollected(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" collects a salary of \\$" + VALUE
                + " before it records that pawn \"" + NAME + "\" starts a turn aged " + VALUE + " years$",
            (world, arguments) -> recordsInOrder(world,
                salaryCollected(arguments.text(1), arguments.number(2)),
                turnStartedAtAge(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" starts a turn aged " + VALUE
                + " years before it records that pawn \"" + NAME + "\" is sent to jail from landing on \"" + NAME + "\"$",
            (world, arguments) -> recordsInOrder(world,
                turnStartedAtAge(arguments.text(1), arguments.number(2)),
                jailEntered(arguments.text(3), arguments.text(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" is sent to jail from landing on \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" starts a turn aged " + VALUE + " years$",
            (world, arguments) -> recordsInOrder(world,
                jailEntered(arguments.text(1), arguments.text(2)),
                turnStartedAtAge(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" rolls a total of " + VALUE + "$",
            (world, arguments) -> records(world, rolled(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> records(world,
                moved(arguments.text(1), arguments.number(2), arguments.text(3), arguments.number(4), arguments.text(5)))),

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
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> recordsInOrder(world,
                rolled(arguments.text(1), arguments.number(2)),
                moved(arguments.text(3), arguments.number(4), arguments.text(5), arguments.number(6), arguments.text(7)))),

        then("^the game journal records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> recordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                moved(arguments.text(3), arguments.number(4), arguments.text(5),
                    arguments.number(6), arguments.text(7)))),

        then("^the game journal records that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\) before it records that pawn \"" + NAME + "\" collects a salary of \\$"
                + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                moved(arguments.text(1), arguments.number(2), arguments.text(3), arguments.number(4), arguments.text(5)),
                salaryCollected(arguments.text(6), arguments.number(7)))),

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

        then("^the game report says that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> says(world,
                movesFromPosition(arguments.text(1), arguments.number(2), arguments.text(3),
                    arguments.number(4), arguments.text(5)))),

        then("^the game report says that pawn \"" + NAME + "\" rolls a total of " + VALUE
                + " before it says that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\)$",
            (world, arguments) -> saysInOrder(world,
                rollsATotalOf(arguments.text(1), arguments.number(2)),
                movesFromPosition(arguments.text(3), arguments.number(4), arguments.text(5),
                    arguments.number(6), arguments.text(7)))),

        then("^the game report says that pawn \"" + NAME + "\" moves from position " + VALUE
                + " \\(" + NAME + "\\) to " + VALUE + " \\(" + NAME + "\\) before it says that pawn \"" + NAME + "\" collects a salary of \\$"
                + VALUE + "$",
            (world, arguments) -> saysInOrder(world,
                movesFromPosition(arguments.text(1), arguments.number(2), arguments.text(3),
                    arguments.number(4), arguments.text(5)),
                arguments.text(6) + " collects a salary of $" + arguments.number(7))),

        given("^pawn \"" + NAME + "\" follows the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.pawnFollows(arguments.text(1), Vocabulary.strategy(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" follows the \"Greedo\" strategy, keeping a \\$"
                + VALUE + " reserve$",
            (world, arguments) -> world.pawnFollowsGreedoWithReserve(
                arguments.text(1), money(arguments.number(2)))),

        given("^pawn \"" + NAME + "\" starts in jail$",
            (world, arguments) -> world.startPawnInJail(arguments.text(1))),

        then("^pawn \"" + NAME + "\" is bankrupt$",
            (world, arguments) -> assertThat(world.isBankrupt(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\" is not bankrupt$",
            (world, arguments) -> assertThat(world.isBankrupt(arguments.text(1))).isFalse()),

        then("^pawn \"" + NAME + "\" wins the game$",
            (world, arguments) -> assertThat(world.hasWon(arguments.text(1))).isTrue()),

        then("^the game journal records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> records(world, finalBalance(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> records(world, finalAge(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE
                + " before it records that pawn \"" + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> recordsInOrder(world,
                finalBalance(arguments.text(1), arguments.number(2)),
                finalAge(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\"'s final age is " + VALUE
                + " years before it records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                finalAge(arguments.text(1), arguments.number(2)),
                finalBalance(arguments.text(3), arguments.number(4)))),

        then("^the game journal records that pawn \"" + NAME + "\" wins the game before it records that pawn \""
                + NAME + "\"'s final age is " + VALUE + " years$",
            (world, arguments) -> recordsInOrder(world,
                Claim.of(new Entry.Won(idOf(arguments.text(1)))),
                finalAge(arguments.text(2), arguments.number(3)))),

        then("^the game journal records that the game ends in a stalemate before it records that pawn \""
                + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                new Claim(entry -> entry instanceof Entry.Stalemate, "game ends in a stalemate"),
                finalBalance(arguments.text(1), arguments.number(2)))),

        then("^the game journal records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE
                + " before it records that pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                finalBalance(arguments.text(1), arguments.number(2)),
                finalBalance(arguments.text(3), arguments.number(4)))),

        given("^pawn \"" + NAME + "\" already holds a Get Out of Jail Free card$",
            (world, arguments) -> world.givePawnGetOutOfJailFreeCard(arguments.text(1))),

        given("^pawn \"" + NAME + "\" has \\$" + VALUE + " to spend$",
            (world, arguments) -> world.holdPawnBalance(arguments.text(1), money(arguments.number(2)))),

        given("^pawn \"" + NAME + "\" declines the offer for \"" + NAME + "\"$",
            (world, arguments) -> world.pawnDeclines(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" will bid \\$" + VALUE + " for \"" + NAME + "\" at auction$",
            (world, arguments) -> world.pawnWillBid(
                arguments.text(1), SpaceNames.of(arguments.text(3)), money(arguments.number(2)))),

        given("^pawn \"" + NAME + "\" will buy \"" + NAME + "\"$",
            (world, arguments) -> world.pawnWillBuy(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" owns \"" + NAME + "\"$",
            (world, arguments) -> world.givePawnOwnership(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" holds a \"Get Out of Jail Free\" card$",
            (world, arguments) -> assertThat(world.holdsGetOutOfJailFreeCard(arguments.text(1))).isTrue()),

        given("^the street \"" + NAME + "\" has " + VALUE + " house\\(s\\) built$",
            (world, arguments) -> world.arrangeHouses(SpaceNames.of(arguments.text(1)), arguments.number(2))),

        then("^the street \"" + NAME + "\" has " + VALUE + " houses built$",
            (world, arguments) -> assertThat(world.housesBuilt(SpaceNames.of(arguments.text(1)))).isEqualTo(arguments.number(2))),

        then("^the pink colour group is developed up to no more than (<total_houses>) houses$",
            (world, arguments) -> assertThat(world.totalHouses(Street.Colour.pink)).isLessThanOrEqualTo(arguments.number(1))),

        then("^no shareholder has paid more than \\$(<ceiling_share>) to the entity$",
            (world, arguments) -> assertThat(world.shareholderPaymentsWithin(arguments.number(1))).isTrue()),

        given("^the street \"" + NAME + "\" has a hotel built$",
            (world, arguments) -> world.arrangeHotel(SpaceNames.of(arguments.text(1)))),

        given("^the land \"" + NAME + "\" is mortgaged$",
            (world, arguments) -> world.arrangeMortgaged(SpaceNames.of(arguments.text(1)))),

        given("^pawn \"" + NAME + "\" declines to claim rent for \"" + NAME + "\"$",
            (world, arguments) -> world.pawnDeclinesRent(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" will claim rent for \"" + NAME + "\"$",
            (world, arguments) -> world.pawnWillClaimRent(arguments.text(1))),

        given("^pawn \"" + NAME + "\" will build a house on \"" + NAME + "\"$",
            (world, arguments) -> world.pawnWillBuildHouseOn(
                arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" lands on \"" + NAME + "\"$",
            (world, arguments) -> world.landPawnOn(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" will use the Get Out of Jail Free card to leave jail$",
            (world, arguments) -> world.pawnWillUseGetOutOfJailFreeCard(arguments.text(1))),

        step("^pawn \"" + NAME + "\" will pay the fine to leave jail$",
            (world, arguments) -> world.pawnWillPayJailFine(arguments.text(1))),

        step("^pawn \"" + NAME + "\" sells a house on \"" + NAME + "\" back to the bank$",
            (world, arguments) -> world.sellHouse(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" exchanges the hotel on \"" + NAME + "\" for houses$",
            (world, arguments) -> world.exchangeHotelForHouses(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" mortgages \"" + NAME + "\"$",
            (world, arguments) -> world.mortgage(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" lifts the mortgage on \"" + NAME + "\"$",
            (world, arguments) -> world.liftMortgage(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" keeps \"" + NAME + "\" mortgaged, paying the interest$",
            (world, arguments) -> world.keepMortgaged(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        step("^pawn \"" + NAME + "\" sells \"" + NAME + "\" to pawn \"" + NAME + "\" for \\$" + VALUE + "$",
            (world, arguments) -> world.sellLand(
                arguments.text(1), SpaceNames.of(arguments.text(2)), arguments.text(3), money(arguments.number(4)))),

        step("^pawn \"" + NAME + "\" sells the \"Get Out of Jail Free\" card to pawn \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> world.sellGetOutOfJailFreeCard(
                arguments.text(1), arguments.text(2), money(arguments.number(3)))),

        then("^pawn \"" + NAME + "\" owns \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.pawnOwns(arguments.text(1), SpaceNames.of(arguments.text(2))))
                .as("pawn \"%s\" owns \"%s\"", arguments.text(1), arguments.text(2))
                .isTrue()),

        then("^pawn \"" + NAME + "\" does not own \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.pawnOwns(arguments.text(1), SpaceNames.of(arguments.text(2))))
                .as("pawn \"%s\" owns \"%s\"", arguments.text(1), arguments.text(2))
                .isFalse()),

        then("^pawn \"" + NAME + "\" holds a \"Get Out of Jail Free\" card$",
            (world, arguments) -> assertThat(world.holdsGetOutOfJailFreeCard(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\" no longer holds a Get Out of Jail Free card$",
            (world, arguments) -> assertThat(world.holdsGetOutOfJailFreeCard(arguments.text(1))).isFalse()),

        then("^pawn \"" + NAME + "\" is in jail$",
            (world, arguments) -> assertThat(world.isInJail(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\" is just visiting$",
            (world, arguments) -> assertThat(world.isInJail(arguments.text(1))).isFalse()),

        then("^the player is no longer in jail$",
            (world, arguments) -> assertThat(world.playerIsInJail()).isFalse()),

        then("^the street \"" + NAME + "\" has " + VALUE + " house\\(s\\) built$",
            (world, arguments) -> assertThat(world.housesBuiltOn(SpaceNames.of(arguments.text(1))))
                .isEqualTo(arguments.number(2))),

        then("^the street \"" + NAME + "\" has a hotel built$",
            (world, arguments) -> assertThat(world.hasHotelOn(SpaceNames.of(arguments.text(1)))).isTrue()),

        then("^the land \"" + NAME + "\" is mortgaged$",
            (world, arguments) -> assertThat(world.isMortgaged(SpaceNames.of(arguments.text(1)))).isTrue()),

        then("^the land \"" + NAME + "\" is not mortgaged$",
            (world, arguments) -> assertThat(world.isMortgaged(SpaceNames.of(arguments.text(1)))).isFalse()),

        then("^the game journal records that pawn \"" + NAME + "\" draws the chance card \"" + NAME
                + "\" before it records that pawn \"" + NAME + "\" receives \\$" + VALUE + " from the bank$",
            (world, arguments) -> recordsInOrder(world,
                chanceCardDrawn(arguments.text(1), arguments.text(2)),
                bankReceived(arguments.text(3), arguments.number(4))))
    );
  }
}
