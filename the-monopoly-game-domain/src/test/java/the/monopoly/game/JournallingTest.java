package the.monopoly.game;

import org.junit.jupiter.api.Test;
import the.monopoly.game.Game.Journal.Entry;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Deeds;
import the.monopoly.game.rules.DevelopmentLoanBook;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.rules.WarProfitsTax;
import the.monopoly.game.rules.WarProfitsTaxBook;
import the.monopoly.game.strategies.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JournallingTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Player dog = rules.players().select(1).findFirst().orElseThrow();

  @Test
  void disabledWarProfitsTaxDoesNotAssessOrChangeThePlayer() {
    Deeds deeds = developedLand();
    Game.Journal journal = new Game.Journal();
    WarProfitsTaxBook book = taxBook();
    book.accumulate(dog, new Money(1000));
    Money before = dog.account().balance().amount();

    journalling(journal, deeds, book, false).collectedSalary(dog, Money.ZERO);

    assertThat(dog.account().balance().amount()).isEqualTo(before);
    assertThat(book.collected(dog)).isEqualTo(new Money(1000));
    assertThat(journal.entries()).noneMatch(Entry.WarProfitsTaxPaid.class::isInstance);
    assertThat(journal.entries()).noneMatch(Entry.GovernmentBalance.class::isInstance);
  }

  @Test
  void enabledWarProfitsTaxLogsTheGovernmentBalanceAfterAnOrdinaryWin() {
    Deeds deeds = developedLand();
    Game.Journal journal = new Game.Journal();
    WarProfitsTaxBook book = taxBook();
    book.setGovernmentBalance(new Money(1000));

    journalling(journal, deeds, book, true).won(dog);

    assertThat(journal.entries()).containsSubsequence(
        new Entry.Won(dog.id()),
        new Entry.FinalAge(dog.id(), 0),
        new Entry.GovernmentBalance(new Money(1000)));
  }

  @Test
  void zeroTaxDoesNotLogPaymentOrMortgageProperty() {
    Deeds deeds = developedLand();
    Game.Journal journal = new Game.Journal();
    WarProfitsTaxBook book = taxBook();
    ColourStreet land = street(Street.Type.LippenslaanKnokke);
    deeds.sell(land, dog, Money.ZERO);
    Money before = dog.account().balance().amount();

    journalling(journal, deeds, book, true).collectedSalary(dog, Money.ZERO);

    assertThat(dog.account().balance().amount()).isEqualTo(before);
    assertThat(deeds.isMortgaged(land)).isFalse();
    assertThat(journal.entries()).noneMatch(Entry.WarProfitsTaxPaid.class::isInstance);
  }

  @Test
  void shortfallSkipsAnAlreadyMortgagedPropertyAndMortgagesTheNextOne() {
    Deeds deeds = developedLand();
    ColourStreet alreadyMortgaged = street(Street.Type.LippenslaanKnokke);
    ColourStreet nextProperty = street(Street.Type.MeirAntwerpen);
    deeds.sell(alreadyMortgaged, dog, Money.ZERO);
    deeds.sell(nextProperty, dog, Money.ZERO);
    deeds.arrangeMortgaged(alreadyMortgaged);
    dog.account().withdraw(dog.account().balance().amount());
    Game.Journal journal = new Game.Journal();
    WarProfitsTaxBook book = taxBook();
    book.accumulate(dog, new Money(90));

    journalling(journal, deeds, book, true).collectedSalary(dog, Money.ZERO);

    assertThat(deeds.isMortgaged(alreadyMortgaged)).isTrue();
    assertThat(deeds.landOwnedBy(dog)).filteredOn(type -> type != alreadyMortgaged.type())
        .anyMatch(type -> deeds.isMortgaged((the.monopoly.game.components.streets.Ownable) rules.create(type)));
    assertThat(journal.entries()).contains(new Entry.WarProfitsTaxPaid(dog.id(), new Money(90)));
  }

  private Journalling journalling(Game.Journal journal, Deeds deeds, WarProfitsTaxBook book, boolean enabled) {
    return new Journalling(journal, new HashMap<>(), deeds, new DevelopmentLoanBook(rules.bank()),
        rules, List.of(dog), player -> Strategy.UNDECIDED, book, enabled);
  }

  private WarProfitsTaxBook taxBook() {
    return new WarProfitsTaxBook(rules.bank(), WarProfitsTax.boardValue(rules));
  }

  private Deeds developedLand() {
    Deeds deeds = new Deeds();
    List<Street.Type> types = List.of(
        Street.Type.MeirAntwerpen,
        Street.Type.NieuwstraatBrussel,
        Street.Type.BoulevardTirouCharleroi,
        Street.Type.VeldstraatGent,
        Street.Type.BoulevardDAvroyLiege);
    for (Street.Type type : types) {
      ColourStreet street = street(type);
      deeds.sell(street, dog, Money.ZERO);
      deeds.arrangeHotel(street);
    }
    return deeds;
  }

  private ColourStreet street(Street.Type type) {
    return (ColourStreet) rules.create(type);
  }
}
