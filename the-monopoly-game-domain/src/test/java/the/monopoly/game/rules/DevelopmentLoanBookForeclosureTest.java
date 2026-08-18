package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.Strategy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentLoanBookForeclosureTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Bank bank = rules.bank();
  private final Player dog = player("dog");
  private final Player highHat = player("high hat");

  @Test
  void recoveredCollateralRecyclesLoanValueAndReturnsOnlyTheSurplus() {
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    DevelopmentLoanBook.Position position = book.recordPlayerLoan(
        dog, Street.Type.RueGrandeDinant, new Money(20), 0, highHat);

    DevelopmentLoanBook.Foreclosure result = book.recover(position, new Money(30));

    assertThat(result.recovered()).isEqualTo(new Money(20));
    assertThat(result.surplus()).isEqualTo(new Money(10));
    assertThat(book.recycledCapital()).isEqualTo(new Money(20));
    assertThat(dog.account().balance().amount()).isEqualTo(new Money(10));
    assertThat(position.outstanding()).isEqualTo(Money.ZERO);
  }

  @Test
  void bankReserveTopsUpARecoveryShortfallBeforeRecycling() {
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    book.setBankBalance(new Money(15));
    DevelopmentLoanBook.Position position = book.recordPlayerLoan(
        dog, Street.Type.RueGrandeDinant, new Money(20), 0, highHat);

    DevelopmentLoanBook.Foreclosure result = book.recover(position, new Money(5));

    assertThat(result.recovered()).isEqualTo(new Money(20));
    assertThat(result.surplus()).isEqualTo(Money.ZERO);
    assertThat(book.bankBalance()).isEqualTo(Money.ZERO);
    assertThat(book.recycledCapital()).isEqualTo(new Money(20));
    assertThat(dog.account().balance().amount()).isEqualTo(Money.ZERO);
  }

  @Test
  void foreclosureSellsOnlyCollateralImprovementsAndCollateralLand() {
    Deeds deeds = new Deeds();
    ColourStreet collateral = (ColourStreet) rules.create(Street.Type.RueGrandeDinant);
    deeds.grant(collateral, dog);
    deeds.arrangeHouses(collateral, 1);
    deeds.grant((ColourStreet) rules.create(Street.Type.DiestsestraatLeuven), dog);
    DevelopmentLoanBook book = new DevelopmentLoanBook(bank);
    DevelopmentLoanBook.Position position = book.recordPlayerLoan(
        dog, Street.Type.RueGrandeDinant, new Money(20), 0, highHat);
    Strategy.OfPlayers strategies = player -> player.id().equals(highHat.id())
        ? new Strategy() {
          @Override
          public Money bidFor(Offer offer) {
            return new Money(30);
          }
        }
        : Strategy.UNDECIDED;

    book.foreclose(position, deeds, rules, List.of(dog, highHat), strategies);

    assertThat(deeds.ownerOf(Street.Type.RueGrandeDinant)).contains(highHat.id());
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).contains(dog.id());
    assertThat(deeds.isMortgaged(collateral)).isTrue();
  }

  private Player player(String name) {
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    return new Player(id, bank.accountOf(id));
  }
}
