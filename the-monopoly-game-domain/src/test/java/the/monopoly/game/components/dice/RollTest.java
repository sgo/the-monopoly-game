package the.monopoly.game.components.dice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RollTest {
  @Test
  void aRollIsWorthBothDiceTogether() {
    assertThat(new Roll(2, 3).total()).isEqualTo(5);
  }

  @Test
  void aRollOfTwoEqualDiceIsADouble() {
    assertThat(new Roll(3, 3).isDouble()).isTrue();
    assertThat(new Roll(3, 4).isDouble()).isFalse();
  }
}
