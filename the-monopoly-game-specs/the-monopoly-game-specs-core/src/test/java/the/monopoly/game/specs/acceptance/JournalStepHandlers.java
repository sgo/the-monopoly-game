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
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.MONEY;
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
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.governmentBalance;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.money;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rentRelief;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.warProfitsTax;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.warProfitsTaxPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.moved;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.movesFromPosition;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rolled;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rollsATotalOf;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rollsForInitiative;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.salaryCollected;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.megacorpTaxPaid;
import static the.monopoly.game.specs.acceptance.MonopolyStepHelpers.rentReliefPaid;
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

        given("^the " + NAME + " split's shareholders can collectively fund the next improvement after their base reserves$",
            (world, arguments) -> world.marketDeadlockCanFund(arguments.text(1))),

        given("^the " + NAME + " split's shareholders cannot collectively fund the next improvement after their base reserves$",
            (world, arguments) -> world.marketDeadlockCannotFund(arguments.text(1))),

        given("^the " + NAME + " split is an eligible three-owner split$",
            (world, arguments) -> world.marketDeadlockEligible(arguments.text(1))),

        given("^the " + NAME + " split is an eligible three-ownesplit$",
            (world, arguments) -> world.marketDeadlockEligible(arguments.text(1))),

        step("^the round completes with (no|a) ownership-consolidating action$",
            (world, arguments) -> world.completeMarketDeadlockRound(arguments.text(1))),

        step("^the round completes with (<action>) ownership-consolidating action$",
            (world, arguments) -> world.completeMarketDeadlockRound(arguments.text(1))),

        given("^legal-entity trading is enabled for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableLegalEntityTrading(arguments.text(1))),

        given("^asset-rich opening is enabled for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableAssetRichOpening(arguments.text(1))),

        then("^the game journal records that the \"Billionaire\" strategy observes asset-rich opening as " + NAME + "$",
            (world, arguments) -> {
              boolean expected = expectedTradingState(arguments.text(1));
              world.awaitGameLog(world.simulatorPlayerCount(), Entry.StrategyNamed.class::isInstance,
                  "strategy observations");
              assertThat(world.gameLog().stream().filter(Entry.StrategyNamed.class::isInstance)
                  .map(Entry.StrategyNamed.class::cast)
                  .filter(entry -> entry.name().equals("Billionaire"))
                  .allMatch(entry -> entry.assetRichOpening() == expected)).isTrue();
            }),

        given("^<enabled_flag> trading is enabled for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableStalemateTrading(arguments.text(1))),

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
              world.queuePawnRoll(arguments.text(1), World.rollTotalling(arguments.number(2)));
            }),

        given("^" + NAME + " is formed$",
            (world, arguments) -> world.formNamedEntity(arguments.text(1))),

        given("^pawn \"" + NAME + "\" and pawn \"" + NAME + "\" have both gone bankrupt$",
            (world, arguments) -> world.bankruptPawns(arguments.text(1), arguments.text(2))),

        given("^pawn \"" + NAME + "\" is bankrupt$",
            (world, arguments) -> world.bankruptPawns(arguments.text(1))),

        given("^pawn \"" + NAME + "\" returns every street except \"" + NAME
                + "\" to the bank$",
            (world, arguments) -> world.returnEveryStreetExcept(arguments.text(1), arguments.text(2))),

        given("^Pink Realty's loan has been fully repaid$",
            (world, arguments) -> world.entityLoanFullyRepaid("Pink Realty")),

        given("^" + NAME + "'s bank account holds \\$" + VALUE + "$",
            (world, arguments) -> world.entityBankHolds(arguments.text(1), money(arguments.number(2)))),

        given("^the Pink Realty bank balance is \\$" + VALUE + "$",
            (world, arguments) -> world.entityBankHolds("Pink Realty", money(arguments.number(1)))),

        given("^Pink Realty has already operated$",
            (world, arguments) -> world.entityHasAlreadyOperated("Pink Realty")),

        given("^the last-capitalised shareholder of Pink Realty has not aged since funding a build$",
            (world, arguments) -> world.entityLastCapitalizedShareholderHasNotAged("Pink Realty")),

        given("^the last-capitalised shareholder of Pink Realty is pawn \"" + NAME + "\"$",
            (world, arguments) -> world.entityLastCapitalizedShareholder("Pink Realty", arguments.text(1))),

        given("^the last-capitalised shareholder of Pink Realty grows a year older$",
            (world, arguments) -> world.entityLastCapitalizedShareholderGrewOlder("Pink Realty")),

        then("^" + NAME + "'s bank account holds \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.entityBankBalance(arguments.text(1)))
                .isEqualTo(money(arguments.number(2)))),

        step("^Pink Realty raises a loan of \\$" + VALUE + "$",
            (world, arguments) -> world.entityRaisesLoan("Pink Realty", money(arguments.number(1)))),

        given("^Pink Realty owns no outstanding loan$",
            (world, arguments) -> assertThat(world.entityLoan("Pink Realty")).isEqualTo(money(0))),

        given("^" + NAME + "'s bank account is empty$",
            (world, arguments) -> world.entityBankHolds(arguments.text(1), money(0))),

        given("^pawn \"" + NAME + "\" owns no mortgaged property$",
            (world, arguments) -> assertThat(world.pawnOwnsNoMortgagedProperty(arguments.text(1))).isTrue()),

        then("^" + NAME + " is dissolved$",
            (world, arguments) -> assertThat(world.entityIsDissolved(arguments.text(1))).isTrue()),

        then("^" + NAME + " is not dissolved$",
            (world, arguments) -> assertThat(world.entityIsNotDissolved(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\" owns every street previously held by " + NAME + "$",
            (world, arguments) -> assertThat(world.pawnOwnsEveryFormerEntityStreet(
                arguments.text(1), arguments.text(2))).isTrue()),

        then("^pawn \"" + NAME + "\" received the " + NAME + " bank balance$",
            (world, arguments) -> assertThat(world.pawnReceivedEntityBankBalance(
                arguments.text(1), arguments.text(2))).isTrue()),

        then("^pawn \"" + NAME + "\" sold (<streets_to_sell>) of the transferred " + NAME
                + " streets to settle the remaining debt$",
            (world, arguments) -> assertThat(world.transferredEntityStreetsSold(
                arguments.text(1), arguments.text(3))).isEqualTo(arguments.number(2))),

        then("^pawn \"" + NAME + "\"'s debt is settled$",
            (world, arguments) -> assertThat(world.pawnDebtIsSettled(arguments.text(1))).isTrue()),

        then("^the pink colour group is developed up to at least (<houses_at_least>) houses$",
            (world, arguments) -> assertThat(world.totalHouses(Street.Colour.pink))
                .isGreaterThanOrEqualTo(arguments.number(1))),

        then("^the pink colour group is developed up to (<total_houses>) houses$",
            (world, arguments) -> assertThat(world.totalHouses(Street.Colour.pink))
                .isEqualTo(arguments.number(1))),

        then("^the pink colour group is developed up to (<developed_total>) houses$",
            (world, arguments) -> assertThat(world.totalHouses(Street.Colour.pink))
                .isEqualTo(arguments.number(1))),

        then("^Pink Realty raises no more than \\$" + VALUE + " in loans$",
            (world, arguments) -> assertThat(world.gameLog().stream()
                .filter(Entry.LegalEntityLoanRaised.class::isInstance)
                .map(Entry.LegalEntityLoanRaised.class::cast)
                .filter(entry -> entry.name().equals("Pink Realty"))
                .mapToInt(entry -> entry.amount().amount()).sum())
                .isLessThanOrEqualTo(arguments.number(1))),

        then("^the street \"" + NAME + "\" has at least " + VALUE + " houses built$",
            (world, arguments) -> assertThat(world.housesBuilt(SpaceNames.of(arguments.text(1))))
                .isGreaterThanOrEqualTo(arguments.number(2))),

        then("^the entity repays the loan with interest before any dividend$",
            (world, arguments) -> {
              List<Entry> entries = world.gameLog();
              int repayment = entries.indexOf(entries.stream()
                  .filter(Entry.LegalEntityLoanRepaid.class::isInstance).findFirst()
                  .orElseThrow(() -> new AssertionError("No loan repayment was recorded.")));
              int dividend = entries.indexOf(entries.stream()
                  .filter(Entry.LegalEntityDividendPaid.class::isInstance).findFirst().orElse(null));
              assertThat(dividend < 0 || repayment < dividend).isTrue();
            }),

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

        given("^pawn \"" + NAME + "\" has a balance that allows only \\$(<ceiling_share>|<share>|-?[0-9,]+(?:\\.[0-9]{1,2})?) toward the entity$",
            (world, arguments) -> world.arrangePawnBalance(arguments.text(1), money(arguments.text(2)))),

        given("^each shareholder commits \\$(<share>|<commitment>) toward Pink Realty's build$",
            (world, arguments) -> world.shareholdersCommitToBuild("Pink Realty", money(arguments.number(1)))),

        given("^pawn \"" + NAME + "\" has a balance of \\$" + VALUE + "$",
            (world, arguments) -> world.holdPawnBalance(arguments.text(1), money(arguments.number(2)))),

        given("^pawn \"" + NAME + "\" is in debt by \\$" + VALUE + "$",
            (world, arguments) -> world.holdPawnBalance(arguments.text(1), money(-arguments.number(2)))),

        step("^pawn \"" + NAME + "\" considers forming a legal entity over the " + NAME + " colour group$",
            (world, arguments) -> world.considerFormingLegalEntity(arguments.text(1), arguments.text(2))),

        then("^the " + NAME + " colour group is owned by " + NAME + "$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity(arguments.text(1))).isTrue()),

        then("^the " + NAME + " colour group is not owned by a legal entity$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity(arguments.text(1))).isFalse()),

        then("^the " + NAME + " colour group is <formed_outcome> by a legal entity$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity(arguments.text(1))).isTrue()),

        then("^the " + NAME + " colour group is auto-formed into " + NAME + "$",
            (world, arguments) -> assertThat(world.colourGroupOwnedByEntity(arguments.text(1))).isTrue()),

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
            (world, arguments) -> assertThat(world.journal()).noneMatch(entry ->
                entry instanceof Entry.LegalEntityDividendPaid it && it.name().equals("Pink Realty"))),

        then("^pawn \"" + NAME + "\" collects a salary and grows a year older$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.SalaryCollected it
                && it.player().equals(idOf(arguments.text(1))), "salary and age increase"))),

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

        then("^the game journal records that pawn \"" + NAME + "\" pays \\$" + VALUE
                + " rent to " + NAME + " for \"" + NAME + "\"$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityRentPaid it
                && it.tenant().equals(idOf(arguments.text(1))) && it.rent().amount() == arguments.number(2)
                && it.name().equals(arguments.text(3)) && it.land().equals(SpaceNames.of(arguments.text(4))),
                "entity rent paid"))),

        then("^the game journal records that " + NAME + " builds a house on \"" + NAME
                + "\" for \\$" + VALUE + "$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.LegalEntityHouseBuilt it
                && it.name().equals(arguments.text(1)) && it.land().equals(SpaceNames.of(arguments.text(2)))
                && it.price().amount() == arguments.number(3), "entity house built"))),

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

        then("^the game ends because the year limit was reached$",
            (world, arguments) -> assertThat(world.endedInYearLimit()).isTrue()),

        then("^the game does not end because the year limit was reached$",
            (world, arguments) -> assertThat(world.endedInYearLimit()).isFalse()),

        given("^the game is limited to " + VALUE + " years$",
            (world, arguments) -> world.setMaxYears(arguments.number(1))),

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

        then("^the game journal records that every player uses the \"Greedo\" strategy$",
            (world, arguments) -> {
              world.awaitGameLog(world.simulatorPlayerCount(), Entry.StrategyNamed.class::isInstance,
                  "strategy observations");
              assertThat(world.gameLog().stream()
                  .filter(Entry.StrategyNamed.class::isInstance)
                  .map(Entry.StrategyNamed.class::cast)
                  .filter(entry -> entry.name().equals("Greedo"))
                  .count()).isEqualTo(world.simulatorPlayerCount());
            }),

        then("^the game journal records that pawn \"" + NAME + "\" uses the \"Billionaire\" strategy$",
            (world, arguments) -> world.awaitGameLog(1,
                entry -> entry instanceof Entry.StrategyNamed named
                    && named.player().value().equals(arguments.text(1))
                    && named.name().equals("Billionaire"), "Billionaire strategy")),

        then("^the game journal records that pawn \"" + NAME + "\" uses the \"Greedo\" strategy$",
            (world, arguments) -> world.awaitGameLog(1,
                entry -> entry instanceof Entry.StrategyNamed named
                    && named.player().value().equals(arguments.text(1))
                    && named.name().equals("Greedo"), "Greedo strategy")),

        then("^the game journal records that the \"Greedo\" strategy observes legal-entity trading as "
                + NAME + "$",
            (world, arguments) -> {
              boolean expected = expectedTradingState(arguments.text(1));
              world.awaitGameLog(world.simulatorPlayerCount(), Entry.StrategyNamed.class::isInstance,
                  "strategy observations");
              assertThat(world.gameLog().stream()
                  .filter(Entry.StrategyNamed.class::isInstance)
                  .map(Entry.StrategyNamed.class::cast)
                  .allMatch(entry -> entry.legalEntityEnabled() == expected)).isTrue();
            }),

        then("^the game journal records that the \"Greedo\" strategy observes stalemate trading as "
                + NAME + "$",
            (world, arguments) -> {
              boolean expected = expectedTradingState(arguments.text(1));
              world.awaitGameLog(world.simulatorPlayerCount(), Entry.StrategyNamed.class::isInstance,
                  "strategy observations");
              assertThat(world.gameLog().stream()
                  .filter(Entry.StrategyNamed.class::isInstance)
                  .map(Entry.StrategyNamed.class::cast)
                  .allMatch(entry -> entry.stalemateEnabled() == expected)).isTrue();
            }),

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


        then("^the game journal records that MegaCorp pays the government an individual income tax of \\$" + MONEY + "$",
            (world, arguments) -> records(world, megacorpTaxPaid("dog", Integer.parseInt(arguments.text(1))))),

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

        given("^pawn \"" + NAME + "\" follows the \"" + NAME + "\" strategy, keeping a \\$"
                + VALUE + " reserve$",
            (world, arguments) -> world.pawnFollows(arguments.text(1),
                Vocabulary.strategyWithReserve(arguments.text(2), money(arguments.number(3)),
                    world.isStalemateTrading(), world.isLegalEntityTrading()))),

        given("^pawn \"" + NAME + "\" starts in jail$",
            (world, arguments) -> world.startPawnInJail(arguments.text(1))),

        then("^pawn \"" + NAME + "\" is bankrupt$",
            (world, arguments) -> assertThat(world.isBankrupt(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\" is not bankrupt$",
            (world, arguments) -> assertThat(world.isBankrupt(arguments.text(1))).isFalse()),

        then("^pawn \"" + NAME + "\"'s bankrupt state is (<bankrupt_state>)$",
            (world, arguments) -> assertThat(world.isBankrupt(arguments.text(1)))
                .isEqualTo(expectedBankruptState(arguments.text(2)))),

        then("^pawn \"" + NAME + "\" no longer holds a share of " + NAME + "$",
            (world, arguments) -> assertThat(world.pawnHoldsShare(arguments.text(1), arguments.text(2))).isFalse()),
        then("^pawn \"" + NAME + "\" holds that " + NAME + " share$",
            (world, arguments) -> assertThat(world.pawnHoldsShare(arguments.text(1), arguments.text(2))).isTrue()),
        then("^pawn \"" + NAME + "\" and pawn \"" + NAME + "\" do not hold shares of " + NAME + "$",
            (world, arguments) -> {
              assertThat(world.pawnHoldsShare(arguments.text(1), arguments.text(3))).isFalse();
              assertThat(world.pawnHoldsShare(arguments.text(2), arguments.text(3))).isFalse();
            }),
        then("^pawn \"" + NAME + "\" holds the " + NAME + " share sold by pawn \"" + NAME + "\"$",
            (world, arguments) -> {
              assertThat(world.pawnHoldsShare(arguments.text(1), arguments.text(2))).isTrue();
            }),
        then("^pawn \"" + NAME + "\" still holds a share of " + NAME + "$",
            (world, arguments) -> assertThat(world.pawnHoldsShare(arguments.text(1), arguments.text(2))).isTrue()),
        then("^pawn \"" + NAME + "\" holds no share of " + NAME + "$",
            (world, arguments) -> assertThat(world.pawnHoldsShare(arguments.text(1), arguments.text(2))).isFalse()),
        then("^pawn \"" + NAME + "\" holds no shares of any legal entity$",
            (world, arguments) -> assertThat(world.pawnHoldsNoEntityShares(arguments.text(1))).isTrue()),
        then("^pawn \"" + NAME + "\" wins the " + NAME + " share at \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.gameLog().stream()
                .filter(Entry.LegalEntityShareSold.class::isInstance)
                .map(Entry.LegalEntityShareSold.class::cast)
                .anyMatch(entry -> entry.name().equals(arguments.text(2))
                    && entry.buyer().value().equals(arguments.text(1))
                    && entry.price().amount() == arguments.number(3))).isTrue()),
        then("^no one wins the " + NAME + " share$",
            (world, arguments) -> assertThat(world.gameLog().stream()
                .filter(Entry.LegalEntityShareSold.class::isInstance)
                .map(Entry.LegalEntityShareSold.class::cast)
                .noneMatch(entry -> entry.name().equals(arguments.text(1)))).isTrue()),
        then("^pawn \"" + NAME + "\" paid the lowest possible price within a third of bank balance$",
            (world, arguments) -> assertThat(world.gameLog().stream()
                .filter(Entry.LegalEntityShareSold.class::isInstance)
                .map(Entry.LegalEntityShareSold.class::cast)
                .anyMatch(entry -> entry.buyer().value().equals(arguments.text(1)))).isTrue()),

        then("^pawn \"" + NAME + "\" wins the game$",
            (world, arguments) -> assertThat(world.hasWon(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> assertThat(world.pawnFinalBalanceIs(arguments.text(1), money(arguments.number(2)))).isTrue()),

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

        given("^pawn \"" + NAME + "\" owes the bank \\$" + MONEY
                + " on a development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> world.oweDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)), 0)),

        given("^pawn \"" + NAME + "\" owes the bank \\$" + MONEY
                + " on a development loan secured by \"" + NAME + "\", ([0-9]+) year into its 20-year term$",
            (world, arguments) -> world.oweDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)), arguments.number(4))),

        given("^development loans are enabled for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableDevelopmentLoans(arguments.text(1))),

        given("^development loans draw the full amount for the \"" + NAME + "\" strategy$",
            (world, arguments) -> world.enableFullDrawDevelopmentLoans(arguments.text(1))),

        given("^pawn \"" + NAME + "\" holds the development loan bond secured by \"" + NAME + "\"$",
            (world, arguments) -> world.holdDevelopmentBond(arguments.text(1), SpaceNames.of(arguments.text(2)))),

        given("^" + NAME + " owes the bank \\$" + MONEY + " on a development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> world.oweEntityDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)), 0)),

        given("^" + NAME + " owes the bank \\$" + MONEY
                + " on a development loan secured by \"" + NAME + "\", ([0-9]+) year into its 20-year term$",
            (world, arguments) -> world.oweEntityDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)), arguments.number(4))),

        step("^pawn \"" + NAME + "\" grows a year older$",
            (world, arguments) -> world.growPawnOlder(arguments.text(1))),

        given("^the war profits tax is enabled$",
            (world, arguments) -> world.enableWarProfitsTax()),

        given("^rent relief is enabled$",
            (world, arguments) -> world.enableRentRelief()),

        given("^the government's account already holds \\$" + MONEY + "$",
            (world, arguments) -> world.setGovernmentAccountBalance(money(arguments.text(1)))),

        step("^pawn \"" + NAME + "\" collects a salary of \\$" + VALUE + "$",
            (world, arguments) -> world.collectSalary(arguments.text(1), money(arguments.number(2)))),

        then("^MegaCorp pays the government an individual income tax of \\$" + MONEY + "$",
            (world, arguments) -> assertThat(world.paysMegacorpTax(money(arguments.text(1)))).isTrue()),

        step("^pawn \"" + NAME + "\" pays pawn \"" + NAME + "\" \\$" + VALUE + " rent$",
            (world, arguments) -> world.payRent(arguments.text(1), arguments.text(2), money(arguments.number(3)))),

        given("^pawn \"" + NAME + "\"'s land is currently worth \\$" + MONEY + " in rent$",
            (world, arguments) -> world.setLandWorthRent(arguments.text(1), money(arguments.text(2)))),

        given("^pawn \"" + NAME + "\" has collected \\$" + MONEY
                + " in rent since their last war profits tax assessment$",
            (world, arguments) -> world.setCollectedRentSinceAssessment(arguments.text(1), money(arguments.text(2)))),

        then("^pawn \"" + NAME + "\" pays the government a war profits tax of \\$" + MONEY + "$",
            (world, arguments) -> assertThat(world.paysWarProfitsTax(arguments.text(1), money(arguments.text(2))))
                .isTrue()),

        then("^pawn \"" + NAME + "\" pays no war profits tax$",
            (world, arguments) -> assertThat(world.paysNoWarProfitsTax(arguments.text(1))).isTrue()),

        then("^the game journal records that pawn \"" + NAME + "\" pays the government a war profits tax of \\$" + MONEY + "$",
            (world, arguments) -> records(world, warProfitsTaxPaid(arguments.text(1), Integer.parseInt(arguments.text(2))))),

        then("^the game journal records that the government's final account balance is \\$" + MONEY + "$",
            (world, arguments) -> records(world, governmentBalance(Integer.parseInt(arguments.text(1))))),

        then("^the game journal records that the government pays pawn \"" + NAME + "\" \\$" + MONEY
                + " in rent relief$",
            (world, arguments) -> records(world,
                rentReliefPaid(arguments.text(1), Integer.parseInt(arguments.text(2))))),

        then("^the game journal records that the war profits tax is " + NAME + "$",
            (world, arguments) -> records(world, warProfitsTax(arguments.text(1)))),

        then("^the game journal records that rent relief is " + NAME + "$",
            (world, arguments) -> records(world, rentRelief(arguments.text(1)))),

        then("^the government's account holds \\$" + MONEY + "$",
            (world, arguments) -> assertThat(world.governmentAccountBalance())
                .isEqualTo(money(arguments.text(1)))),

        then("^the game journal records that pawn \"" + NAME + "\" raises a development loan of \\$" + MONEY
                + " from the bank, secured by \"" + NAME + "\", funded by pawn \"" + NAME + "\"'s bond purchase$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanRaised it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.amount().equals(money(arguments.text(2)))
                && it.collateral() == SpaceNames.of(arguments.text(3))
                && it.bondholder().value().equals(arguments.text(4)), "player development loan raised"))),

        then("^the game journal records that pawn \"" + NAME + "\" pays the bank \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanPayment it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.interest().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3)))
                && it.collateral() == SpaceNames.of(arguments.text(4)), "player development loan payment"))),

        then("^the game journal records that pawn \"" + NAME + "\"'s development loan on \"" + NAME
                + "\" has been fully repaid$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.DevelopmentLoanRepaid it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.collateral() == SpaceNames.of(arguments.text(2)), "player development loan repaid"))),

        then("^the game journal records that pawn \"" + NAME + "\" receives \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan bond secured by \"" + NAME + "\"$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.DevelopmentBondPayment it
                && it.bondholder().equals(idOf(arguments.text(1)))
                && it.yield().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3)))
                && it.collateral() == SpaceNames.of(arguments.text(4)), "player development bond payment"))),

        then("^the game journal records that full-draw development loans are " + NAME + "$",
            (world, arguments) -> records(world, Claim.of(new Entry.DevelopmentLoans(
                true, arguments.text(1).equals("enabled"))))),

        then("^the game journal records that development loans are " + NAME + "$",
            (world, arguments) -> records(world, Claim.of(new Entry.DevelopmentLoans(
                arguments.text(1).equals("enabled"), false)))), 

        then("^the game journal records that " + NAME + " raises a development loan of \\$" + MONEY
                + " from the bank, secured by \"" + NAME + "\", funded by pawn \"" + NAME + "\"'s bond purchase$",
            (world, arguments) -> assertThat(world.entityRaisesDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)))).isTrue()),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + " raises a development loan of \\$" + MONEY + " secured by \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.entityRaisesDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)))).isTrue()),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + " pays the bank \\$" + MONEY + " in interest on the development loan$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.EntityDevelopmentLoanPayment it
                && it.name().equals(arguments.text(1)) && it.interest().equals(money(arguments.text(2))))),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + " pays the bank \\$" + MONEY + " in principal on the development loan$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.EntityDevelopmentLoanPayment it
                && it.name().equals(arguments.text(1)) && it.principal().equals(money(arguments.text(2))))),

        then("^the game journal records that " + NAME + " pays the bank \\$" + MONEY
                + " interest and \\$" + MONEY + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.EntityDevelopmentLoanPayment it
                && it.name().equals(arguments.text(1)) && it.interest().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3)))
                && it.collateral() == SpaceNames.of(arguments.text(4)), "entity development loan payment"))),

        then("^the game journal records that " + NAME + "'s development loan on \"" + NAME
                + "\" has been fully repaid$",
            (world, arguments) -> records(world, new Claim(entry -> entry instanceof Entry.EntityDevelopmentLoanRepaid it
                && it.name().equals(arguments.text(1)) && it.collateral() == SpaceNames.of(arguments.text(2)),
                "entity development loan repaid"))),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + " owns no development loan$",
            (world, arguments) -> assertThat(world.entityOwnsNoDevelopmentLoan(arguments.text(1))).isTrue()),

        then("^the game journal records that " + NAME + " defaults on the development loan secured by \"" + NAME
                + "\"; the bank forecloses$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry ->
                (entry instanceof Entry.EntityDevelopmentLoanDefaulted it
                    && it.name().equals(arguments.text(1)) && it.collateral() == SpaceNames.of(arguments.text(2)))
                    || (entry instanceof Entry.DevelopmentLoanDefaulted playerDefaulted
                    && playerDefaulted.borrower().equals(idOf(arguments.text(1)))
                    && playerDefaulted.collateral() == SpaceNames.of(arguments.text(2))))),

        then("^the game journal records that the bank recovers \\$" + MONEY + " from the foreclosure of \"" + NAME
                + "\", added to its own account$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.DevelopmentLoanRecovered it
                && it.collateral() == SpaceNames.of(arguments.text(2))
                && it.amount().equals(money(arguments.text(1))))),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + "'s development loan on \"" + NAME + "\" has been fully repaid$",
            (world, arguments) -> assertThat(world.entityDevelopmentLoanFullyRepaid(arguments.text(1),
                SpaceNames.of(arguments.text(2)))).isTrue()),

        then("^" + NAME + " receives \\$" + MONEY + " interest and \\$" + MONEY
                + " principal on the development loan bond secured by \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.bondholderReceived(arguments.text(1),
                SpaceNames.of(arguments.text(4)), money(arguments.text(2)), money(arguments.text(3)))).isTrue()),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + " owes the bank \\$" + MONEY + " on the development loan$",
            (world, arguments) -> assertThat(world.entityDevelopmentLoanBalance(arguments.text(1), Street.Type.RueDeDiekirchArlon))
                .isEqualTo(money(arguments.text(2)))),

        then("^(?!the game (?:journal|log) records that |the game report says that )" + NAME
                + " pays the bank \\$" + MONEY + " interest and \\$" + MONEY
                + " principal on the development loan secured by \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.EntityDevelopmentLoanPayment it
                && it.name().equals(arguments.text(1)) && it.interest().equals(money(arguments.text(2)))
                && it.principal().equals(money(arguments.text(3)))
                && it.collateral() == SpaceNames.of(arguments.text(4)))),

        then("^" + NAME + " owns \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.entityOwns(arguments.text(1), SpaceNames.of(arguments.text(2)))).isTrue()),

        then("^" + NAME + " does not own \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.entityOwns(arguments.text(1), SpaceNames.of(arguments.text(2)))).isFalse()),

        then("^the game journal records that pawn \"" + NAME + "\" defaults on the development loan secured by \"" + NAME
                + "\"; the bank forecloses$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.DevelopmentLoanDefaulted it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.collateral() == SpaceNames.of(arguments.text(2)))),

        then("^pawn \"" + NAME + "\" pays the bank \\$" + MONEY + " in interest on the development loan$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.DevelopmentLoanPayment it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.interest().equals(money(arguments.text(2))))),

        then("^pawn \"" + NAME + "\" pays the bank \\$" + MONEY + " in principal on the development loan$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.DevelopmentLoanPayment it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.principal().equals(money(arguments.text(2))))),

        then("^pawn \"" + NAME + "\" defaults on the development loan secured by \"" + NAME
                + "\"; the bank forecloses$",
            (world, arguments) -> assertThat(world.journal()).anyMatch(entry -> entry instanceof Entry.DevelopmentLoanDefaulted it
                && it.borrower().equals(idOf(arguments.text(1)))
                && it.collateral() == SpaceNames.of(arguments.text(2)))),

        then("^pawn \"" + NAME + "\" owes the bank \\$" + MONEY + " on the development loan$",
            (world, arguments) -> assertThat(world.developmentLoanBalance(arguments.text(1), Street.Type.RueGrandeDinant))
                .isEqualTo(money(arguments.text(2)))),

        then("^pawn \"" + NAME + "\"'s development loan on \"" + NAME + "\" has been fully repaid$",
            (world, arguments) -> assertThat(world.developmentLoanFullyRepaid(arguments.text(1),
                SpaceNames.of(arguments.text(2)))).isTrue()),

        then("^pawn \"" + NAME + "\" owns no development loan$",
            (world, arguments) -> assertThat(world.ownsNoDevelopmentLoan(arguments.text(1))).isTrue()),

        then("^pawn \"" + NAME + "\" receives \\$" + MONEY + " interest and \\$" + MONEY
                + " principal on the development loan bond secured by \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.bondholderReceived(arguments.text(1),
                SpaceNames.of(arguments.text(4)), money(arguments.text(2)), money(arguments.text(3)))).isTrue()),

        given("^the bank's account holds \\$" + MONEY + "$",
            (world, arguments) -> world.setDevelopmentLoanBankBalance(money(arguments.text(1)))),

        then("^the bank's account holds \\$" + MONEY + "$",
            (world, arguments) -> assertThat(world.developmentLoanBankBalance())
                .isEqualTo(money(arguments.text(1)))),

        given("^the bank holds \\$" + MONEY
                + " in recycled development-loan capital, no longer securing any loan$",
            (world, arguments) -> world.setRecycledDevelopmentLoanCapital(money(arguments.text(1)))),

        then("^the bank holds \\$" + MONEY + " in recycled development-loan capital$",
            (world, arguments) -> assertThat(world.recycledDevelopmentLoanCapital())
                .isEqualTo(money(arguments.text(1)))),

        then("^pawn \"" + NAME + "\" raises a development loan of \\$" + MONEY
                + " secured by \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.pawnRaisesDevelopmentLoan(arguments.text(1),
                SpaceNames.of(arguments.text(3)), money(arguments.text(2)))).isTrue()),

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

        step("^the street \"" + NAME + "\" has " + VALUE + " house\\(s\\) built$",
            (world, arguments) -> world.arrangeOrAssertHouses(
                SpaceNames.of(arguments.text(1)), arguments.number(2))),

        step("^the street \"" + NAME + "\" has " + VALUE + " houses built$",
            (world, arguments) -> world.arrangeOrAssertHouses(
                SpaceNames.of(arguments.text(1)), arguments.number(2))),

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

        step("^pawn \"" + NAME + "\" no longer owns \"" + NAME + "\"$",
            (world, arguments) -> assertThat(world.pawnNoLongerOwns(arguments.text(1), SpaceNames.of(arguments.text(2))))
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
                bankReceived(arguments.text(3), arguments.number(4)))),
        then("^the game journal records that the game ends because the year limit was reached before it records that pawn \"" + NAME
                + "\"'s final balance is \\$" + VALUE + "$",
            (world, arguments) -> recordsInOrder(world,
                new Claim(entry -> entry instanceof Entry.YearLimitReached, "game ends because the year limit was reached"),
                finalBalance(arguments.text(1), arguments.number(2))))
    );
  }

  private static boolean expectedBankruptState(String state) {
    return switch (state) {
      case "bankrupt" -> true;
      case "not bankrupt" -> false;
      default -> throw new IllegalArgumentException("Unknown bankrupt state: " + state);
    };
  }

  private static boolean expectedTradingState(String state) {
    return switch (state) {
      case "enabled" -> true;
      case "disabled" -> false;
      default -> throw new IllegalArgumentException("Unknown trading state: " + state);
    };
  }
}

/* mutate4java-manifest
version=1
moduleHash=84b213c7c922263d8479112c70ee4cb7ba6b07efa7ab8c9a8147d43694da560a
scope.0.id=Y2xhc3M6Sm91cm5hbFN0ZXBIYW5kbGVycyNKb3VybmFsU3RlcEhhbmRsZXJzOjQ4
scope.0.kind=class
scope.0.startLine=48
scope.0.endLine=821
scope.0.semanticHash=cfed1939d01f4334642a90e3651a3e0a4538124ee057ea575246adb7f33fb344
scope.1.id=bWV0aG9kOkpvdXJuYWxTdGVwSGFuZGxlcnMjY3RvcigwKTo0OQ
scope.1.kind=method
scope.1.startLine=49
scope.1.endLine=50
scope.1.semanticHash=a7330247d1ba0ccc6eb267a7aaafe651c1edf65f9251ec84a0dadaa9c074a1ae
scope.2.id=bWV0aG9kOkpvdXJuYWxTdGVwSGFuZGxlcnMjZXhwZWN0ZWRCYW5rcnVwdFN0YXRlKDEpOjgwNg
scope.2.kind=method
scope.2.startLine=806
scope.2.endLine=812
scope.2.semanticHash=b20d3dbc4be360fe08c388e538d417ec4ea9ef295c9575b87bbcc843eb365b0a
scope.3.id=bWV0aG9kOkpvdXJuYWxTdGVwSGFuZGxlcnMjZXhwZWN0ZWRUcmFkaW5nU3RhdGUoMSk6ODE0
scope.3.kind=method
scope.3.startLine=814
scope.3.endLine=820
scope.3.semanticHash=1223c61a7217a07d1deb1477ee989f187a0f1ab4ab032f3b629c689b48cca8c9
scope.4.id=bWV0aG9kOkpvdXJuYWxTdGVwSGFuZGxlcnMjaGFuZGxlcnMoMCk6NTI
scope.4.kind=method
scope.4.startLine=52
scope.4.endLine=804
scope.4.semanticHash=5866fb93bc3be3ccdd853bfe1db81b9c5049dd3b6430102845084aaa5b97a6e7
*/
