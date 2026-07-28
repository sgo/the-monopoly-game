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
        new Entry.TurnStarted(Pawn.dog.id()),
        new Entry.Rolled(Pawn.dog.id(), 5),
        new Entry.Moved(Pawn.dog.id(), 0, 5)
    )).isEqualTo("""
        dog starts a turn
        dog rolls a total of 5
        dog moves from position 0 to 5""");
  }

  @Test
  void aReportTellsWhatAPawnWasPaidForReachingStart() {
    assertThat(report(new Entry.SalaryCollected(Pawn.dog.id(), new Money(200))))
        .isEqualTo("dog collects a salary of $200");
  }

  @Test
  void aReportTellsWhatAPlayerBoughtAndWhatItCostThem() {
    assertThat(report(new Entry.Bought(
        Pawn.dog.id(), Street.Type.DiestsestraatLeuven, new Money(60)
    ))).isEqualTo("dog buys Diestsestraat Leuven for $60");
  }

  @Test
  void aReportTellsWhoWonAnAuctionAndWhatTheyBid() {
    assertThat(report(new Entry.AuctionWon(
        Pawn.high_hat.id(), Street.Type.DiestsestraatLeuven, new Money(120)
    ))).isEqualTo("high hat wins the auction for Diestsestraat Leuven at $120");
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
