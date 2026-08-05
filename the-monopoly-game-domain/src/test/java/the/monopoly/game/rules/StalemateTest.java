package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Bank;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.players.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class StalemateTest {
  private static final AtomicInteger NEXT_PLAYER = new AtomicInteger();
  private final Rule.Set rules = Rule.Set.Type.official.create();

  @Test
  void thresholdIsTheOfficialBoardValueAtMaximumRental() {
    assertThat(Stalemate.threshold(rules)).isEqualTo(new Money(22790));
  }

  @Test
  void allRemainingPlayersMustClearTheThreshold() {
    Player dog = playerWith(25000);
    Player highHat = playerWith(25000);

    assertThat(Stalemate.reached(rules, List.of(dog, highHat), new Deeds())).isTrue();

    highHat.account().withdraw(new Money(23501));

    assertThat(Stalemate.reached(rules, List.of(dog, highHat), new Deeds())).isFalse();
  }

  @Test
  void bankruptPlayersDoNotBlockAStalemate() {
    Player dog = playerWith(25000);
    Player bankrupt = playerWith(0);
    Deeds deeds = new Deeds();
    deeds.bankrupt(bankrupt);

    assertThat(Stalemate.reached(rules, List.of(dog, bankrupt), deeds)).isTrue();
  }

  private Player playerWith(int balance) {
    Player.ID id = new Player.ID("player " + NEXT_PLAYER.incrementAndGet());
    Bank bank = rules.bank();
    bank.createAccountFor(id);
    Player player = new Player(id, bank.accountOf(id));
    player.account().deposit(new Money(balance));
    return player;
  }
}
