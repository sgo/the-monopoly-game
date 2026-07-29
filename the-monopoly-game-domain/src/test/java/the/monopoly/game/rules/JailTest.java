package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.dice.Roll;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Bank.Account.Balance;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.strategies.AgreeIfAffordable;
import the.monopoly.game.strategies.Strategy;

import static org.assertj.core.api.Assertions.assertThat;

class JailTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Player dog = playerWith("dog", 1500);
  private final Jail jail = new Jail(rules);

  @Test
  void goingToJailImprisonsThePawnAtTheJailSpace() {
    jail.resolve(dog, rules.create(Street.Type.NaarDeGevangenis), new Roll(1, 2));

    assertThat(dog.position().index()).isEqualTo(rules.gameboard().positionOf(Street.Type.OpBezoek));
    assertThat(jail.holds(dog)).isTrue();
  }

  @Test
  void anObserverRecordsWhyThePawnWasSentToJail() {
    Reported reported = new Reported();
    jail.observe(reported);

    jail.resolve(dog, rules.create(Street.Type.NaarDeGevangenis), new Roll(1, 2));

    assertThat(reported.cause).isEqualTo(Street.Type.NaarDeGevangenis);
  }

  @Test
  void anAffordableFineFreesThePawn() {
    jail.imprison(dog);

    assertThat(jail.mayTakeTurn(dog, new AgreeIfAffordable(), new Deeds())).isTrue();
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1450));
    assertThat(jail.holds(dog)).isFalse();
  }

  @Test
  void anUnaffordableFineLeavesThePawnToTryDoubles() {
    dog.account().withdraw(new Money(1460));
    jail.imprison(dog);

    assertThat(jail.mayTakeTurn(dog, new AgreeIfAffordable(), new Deeds())).isFalse();
    assertThat(jail.leavesOn(new Roll(3, 3), dog)).isTrue();
    assertThat(jail.holds(dog)).isFalse();
  }

  @Test
  void threeFailedDoubleAttemptsChargeTheFineAndReleaseThePawn() {
    dog.account().withdraw(new Money(1460));
    jail.imprison(dog);

    assertThat(jail.leavesOn(new Roll(1, 2), dog)).isFalse();
    assertThat(jail.leavesOn(new Roll(2, 3), dog)).isFalse();
    assertThat(jail.leavesOn(new Roll(3, 4), dog)).isTrue();

    assertThat(dog.account().balance()).isEqualTo(Balance.of(-10));
    assertThat(jail.holds(dog)).isFalse();
  }

  @Test
  void anExplicitlyUsedRetainedCardFreesThePawnWithoutAFine() {
    Deeds deeds = new Deeds();
    deeds.hold(Deeds.RetainedCard.CHANCE_GET_OUT_OF_JAIL_FREE, dog);
    jail.imprison(dog);
    jail.useCard(dog);

    assertThat(jail.mayTakeTurn(dog, Strategy.UNDECIDED, deeds)).isTrue();
    assertThat(dog.account().balance()).isEqualTo(Balance.of(1500));
    assertThat(deeds.holdsGetOutOfJailFreeCard(dog)).isFalse();
  }

  private Player playerWith(String name, int balance) {
    Bank bank = rules.bank();
    Player.ID id = new Player.ID(name);
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }

  private static final class Reported implements Jail.Events {
    private Street.Type cause;

    @Override
    public void sentToJail(Player player, Street.Type cause) {
      this.cause = cause;
    }
  }
}
