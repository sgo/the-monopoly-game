package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Ownable;
import the.monopoly.game.components.streets.Street;

import static org.assertj.core.api.Assertions.assertThat;

class MonopolyBuyoutTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Deeds deeds = new Deeds();

  @Test
  void theRicherCoOwnerWinsWithCashAlone() {
    Player dog = player("dog", 1000);
    Player highHat = player("high hat", 100);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds).orElseThrow();

    assertThat(outcome.winner()).isEqualTo(dog);
    assertThat(outcome.payment()).isEqualTo(new Money(40));
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
  }

  @Test
  void aTurnStartBuyoutUsesTheSameRuleAtADifferentBalance() {
    Player dog = player("dog", 2500);
    Player highHat = player("high hat", 100);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds)
        .orElseThrow();

    assertThat(outcome.payment()).isEqualTo(new Money(40));
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
  }

  @Test
  void theRicherCoOwnerWinsRegardlessOfWhichPawnInitiates() {
    Player dog = player("dog", 100);
    Player highHat = player("high hat", 1000);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds).orElseThrow();

    assertThat(outcome.winner()).isEqualTo(highHat);
    assertThat(outcome.payment()).isEqualTo(new Money(40));
    assertThat(deeds.ownerOf(Street.Type.MeirAntwerpen)).contains(highHat.id());
  }

  @Test
  void aWinnerGivesUpASpareStreetTheyCannotAffordToBuyBackWithCash() {
    Player dog = player("dog", 1000);
    Player highHat = player("high hat", 100);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds).orElseThrow();

    assertThat(outcome.winner()).isEqualTo(dog);
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).contains(highHat.id());
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
  }

  @Test
  void aWinnerKeepsASpareStreetByPayingDoubleItsRentValueInCashInstead() {
    Player dog = player("dog", 3000);
    Player highHat = player("high hat", 100);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds).orElseThrow();

    assertThat(outcome.winner()).isEqualTo(dog);
    assertThat(outcome.payment()).isEqualTo(new Money(900));
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).contains(dog.id());
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
  }

  @Test
  void anExactTieInCashWithNoEligibleStreetsLeavesTheMonopolySplit() {
    Player dog = player("dog", 100);
    Player highHat = player("high hat", 100);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    assertThat(MonopolyBuyout.resolve(dog, highHat, rules, deeds)).isEmpty();
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(highHat.id());
    assertThat(deeds.ownerOf(Street.Type.MeirAntwerpen)).contains(dog.id());
  }

  @Test
  void aTiedCoOwnerBreaksTheTieByCombiningASpareStreetWithCash() {
    Player dog = player("dog", 1000);
    Player highHat = player("high hat", 1000);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.RueGrandeDinant), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds).orElseThrow();

    assertThat(outcome.winner()).isEqualTo(dog);
    assertThat(outcome.payment()).isEqualTo(new Money(105));
    assertThat(deeds.ownerOf(Street.Type.RueGrandeDinant)).contains(highHat.id());
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
  }

  @Test
  void aRicherWinnerWhoCannotDoubleAffordTheSparePriceGetsItForFree() {
    Player dog = player("dog", 60);
    Player highHat = player("high hat", 10);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds).orElseThrow();

    assertThat(outcome.winner()).isEqualTo(dog);
    assertThat(outcome.payment()).isEqualTo(Money.ZERO);
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(dog.id());
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).contains(highHat.id());
  }

  @Test
  void aRicherWinnerWithNoSpareAndNoAffordableCashLeavesTheMonopolySplit() {
    Player dog = player("dog", 50);
    Player highHat = player("high hat", 10);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), highHat, Money.ZERO);

    assertThat(MonopolyBuyout.resolve(dog, highHat, rules, deeds)).isEmpty();
    assertThat(deeds.ownerOf(Street.Type.NieuwstraatBrussel)).contains(highHat.id());
    assertThat(deeds.ownerOf(Street.Type.MeirAntwerpen)).contains(dog.id());
  }

  @Test
  void noSharedCompleteColourGroupResolvesNothing() {
    Player dog = player("dog", 1000);
    Player highHat = player("high hat", 1000);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);

    assertThat(MonopolyBuyout.resolve(dog, highHat, rules, deeds)).isEmpty();
  }

  @Test
  void aColourGroupOwnedEntirelyByOnePlayerIsNotASplitMonopoly() {
    Player dog = player("dog", 1000);
    Player highHat = player("high hat", 1000);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), dog, Money.ZERO);

    assertThat(MonopolyBuyout.resolve(dog, highHat, rules, deeds)).isEmpty();
  }

  @Test
  void itResolvesASplitGroupEvenWhenAnUnsplitGroupHasALargerPriceSpread() {
    Player dog = player("dog", 1000);
    Player highHat = player("high hat", 100);
    deeds.sell(ownable(Street.Type.MeirAntwerpen), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.NieuwstraatBrussel), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.RueGrandeDinant), dog, Money.ZERO);
    deeds.sell(ownable(Street.Type.DiestsestraatLeuven), highHat, Money.ZERO);

    MonopolyBuyout.Outcome outcome = MonopolyBuyout.resolve(dog, highHat, rules, deeds)
        .orElseThrow();

    assertThat(outcome.winner()).isEqualTo(dog);
    assertThat(deeds.ownerOf(Street.Type.DiestsestraatLeuven)).contains(dog.id());
  }

  private Ownable ownable(Street.Type type) {
    return (Ownable) rules.create(type);
  }

  private Player player(String name, int balance) {
    Player.ID id = new Player.ID(name);
    Bank bank = rules.bank();
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }
}
