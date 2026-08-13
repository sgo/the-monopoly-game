package the.monopoly.game.strategies;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BillionaireTest {

  @Test
  void suppliesTheBillionaireOpeningCapitalByDefault() {
    Strategy strategy = new Billionaire();

    assertThat(strategy.openingCapital()).isEqualTo(Optional.of(new Money(57_700_000)));
  }

  @Test
  void suppliesTheBillionaireOpeningCapitalWhenConstructedWithAReserve() {
    Strategy strategy = new Billionaire(Money.ZERO, false, false);

    assertThat(strategy.openingCapital()).isEqualTo(Optional.of(new Money(57_700_000)));
  }

  @Test
  void suppressesTheOpeningCapitalWhenToldNotToApplyIt() {
    Strategy strategy = new Billionaire(Money.ZERO, false, false, false);

    assertThat(strategy.openingCapital()).isEmpty();
  }
}
