package the.monopoly.game;

import org.junit.jupiter.api.Test;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Pawn;
import the.monopoly.game.components.streets.Street;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTest {
  @Test
  void aReportNamesEveryoneAtTheTableInTheOrderTheySit() {
    assertThat(report(new Entry.Start(List.of(Pawn.dog.id(), Pawn.high_hat.id()))))
        .isEqualTo("The game starts with dog, high hat");
  }

  @Test
  void aReportTellsWhatEachPlayerRolledForInitiativeAndWhoWon() {
    assertThat(report(
        new Entry.InitiativeRoll(Pawn.dog.id(), 10),
        new Entry.InitiativeWon(Pawn.dog.id())
    )).isEqualTo("""
        dog rolls 10 for initiative
        dog wins initiative""");
  }

  @Test
  void aReportTellsATurnAsItWasPlayed() {
    assertThat(report(
        new Entry.TurnStarted(Pawn.dog.id(), new Money(1500)),
        new Entry.Rolled(Pawn.dog.id(), 5),
        new Entry.Moved(Pawn.dog.id(), 0, 5)
    )).isEqualTo("""
        dog starts a turn aged 0 years with $1500 and a $0 reserve
        dog rolls a total of 5
        dog moves from position 0 (Start) to 5 (Noord Station / Gare du Nord)""");
  }

  @Test
  void aReportTellsATurnStartWithThePawnsBalanceAtThatPoint() {
    assertThat(report(new Entry.TurnStarted(Pawn.dog.id(), new Money(1300))))
        .isEqualTo("dog starts a turn aged 0 years with $1300 and a $0 reserve");
  }

  @Test
  void aReportTellsWhatAPawnWasPaidForReachingStart() {
    assertThat(report(new Entry.SalaryCollected(Pawn.dog.id(), new Money(200))))
        .isEqualTo("dog collects a salary of $200");
  }

  @Test
  void aReportTellsWarProfitsTaxStatePaymentAndGovernmentBalance() {
    assertThat(report(
        new Entry.WarProfitsTaxEnabled(true),
        new Entry.WarProfitsTaxPaid(Pawn.dog.id(), new Money(1000)),
        new Entry.GovernmentBalance(new Money(1000))))
        .isEqualTo("""
            war profits tax is enabled
            dog pays a war profits tax of $1000
            The government's account holds $1000""");
  }

  @Test
  void aReportTellsWhatAPlayerBoughtAndWhatItCostThem() {
    assertThat(report(new Entry.Bought(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(60)
    ))).isEqualTo("dog buys Diestsestraat Leuven for $60");
  }

  @Test
  void aReportExplainsWhyAPlayerDeclinedAnUnaffordablePurchase() {
    assertThat(report(new Entry.PurchaseDeclined(
        Pawn.dog.id(), Street.Type.RueDeDiekirchArlon, new Money(140),
        the.monopoly.game.strategies.Strategy.DeclineReason.CANNOT_AFFORD, Money.ZERO
    ))).isEqualTo("dog declines to buy Rue de Diekirch Arlon because it cannot afford the $140 price");
  }

  @Test
  void aReportExplainsWhyAPlayerProtectedTheirReserve() {
    assertThat(report(new Entry.PurchaseDeclined(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(60),
        the.monopoly.game.strategies.Strategy.DeclineReason.CASH_RESERVE, new Money(96)
    ))).isEqualTo("dog declines to buy Diestsestraat Leuven because it would drop the balance below the $96 reserve");
  }

  @Test
  void aReportTellsWhoWonAnAuctionAndWhatTheyBid() {
    assertThat(report(new Entry.AuctionWon(
        Pawn.high_hat.id(), Street.Type.DiestsestraatLeuven, new Money(120)
    ))).isEqualTo("high hat wins the auction for Diestsestraat Leuven at $120");
  }

  @Test
  void aReportTellsWhenAPlayerBuildsAHouse() {
    assertThat(report(new Entry.HouseBuilt(
        Pawn.dog.id(), Street.Type.RueGrandeDinant, new Money(50)
    ))).isEqualTo("dog builds a house on Rue Grande Dinant for $50");
  }

  @Test
  void aReportTellsWhenAPlayerSellsAHouseBackToTheBank() {
    assertThat(report(new Entry.HouseSold(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(25)
    ))).isEqualTo("dog sells a house on Diestsestraat Leuven for $25");
  }

  @Test
  void aReportTellsWhenAPlayerMortgagesLand() {
    assertThat(report(new Entry.Mortgaged(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(30)
    ))).isEqualTo("dog mortgages Diestsestraat Leuven for $30");
  }

  @Test
  void aReportTellsWhenAPlayerLiftsAMortgageIncludingInterest() {
    assertThat(report(new Entry.MortgageLifted(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(33), new Money(3)
    ))).isEqualTo("dog lifts the mortgage on Diestsestraat Leuven for $33 including $3 interest");
  }

  @Test
  void aReportTellsWhenLandIsSoldBetweenPlayers() {
    assertThat(report(new Entry.LandSold(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, Pawn.high_hat.id(), new Money(90)
    ))).isEqualTo("dog sells Diestsestraat Leuven to high hat for $90");
  }

  @Test
  void aReportTellsWhenLandSaleIsRefusedBecauseTheColourGroupHasHousesBuilt() {
    assertThat(report(new Entry.LandSaleRefused(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, Pawn.high_hat.id(), new Money(90)
    ))).isEqualTo("dog is refused selling Diestsestraat Leuven to high hat for $90 because the colour group has houses built");
  }

  @Test
  void aReportTellsWhenBuildingIsRefusedBecauseAStreetInTheColourGroupIsMortgaged() {
    assertThat(report(new Entry.BuildingRefused(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(50)
    ))).isEqualTo("dog is refused building a house on Diestsestraat Leuven for $50 because a street in the colour group is mortgaged");
  }

  @Test
  void aReportTellsWhenAChanceCardIsDrawn() {
    assertThat(report(new Entry.ChanceCardDrawn(
        Pawn.dog.id(), "Boete voor te snel rijden. Betaal M15."
    ))).isEqualTo("dog draws the chance card \"Boete voor te snel rijden. Betaal M15.\"");
  }

  @Test
  void aReportTellsWhenACommunityChestCardIsDrawn() {
    assertThat(report(new Entry.CommunityChestCardDrawn(
        Pawn.dog.id(), "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    ))).isEqualTo("dog draws the community chest card \"je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler.\"");
  }

  @Test
  void aReportTellsWhenAPlayerPaysTheBank() {
    assertThat(report(new Entry.BankPaid(Pawn.dog.id(), new Money(15))))
        .isEqualTo("dog pays the bank $15");
  }

  @Test
  void aReportTellsWhenAPlayerPaysAnotherPlayerOutsideRent() {
    assertThat(report(new Entry.PlayerPaid(Pawn.dog.id(), Pawn.high_hat.id(), new Money(50))))
        .isEqualTo("dog pays high hat $50");
  }

  /** A space is spelled as the board spells it, in words rather than as one. */
  @Test
  void aReportSpellsASpaceOfOneWordAsTheOneWordItIs() {
    assertThat(report(new Entry.Bought(
        Pawn.dog.id(), Street.Type.Elektriciteitscentrale, new Money(150)
    ))).isEqualTo("dog buys Elektriciteitscentrale for $150");
  }

  @Test
  void aReportOfAGameNobodyPlayedSaysNothing() {
    assertThat(report()).isEmpty();
  }

  private static String report(Entry... journal) {
    return Report.of(List.of(journal));
  }
}
