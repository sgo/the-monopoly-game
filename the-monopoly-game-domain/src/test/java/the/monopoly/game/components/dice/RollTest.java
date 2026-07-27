package the.monopoly.game.components.dice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @Test
  void aCupYieldsTheRollsItWasGivenInOrder() {
    Cup cup = Cup.of(new Roll(1, 2), new Roll(3, 4));

    assertThat(cup.roll()).isEqualTo(new Roll(1, 2));
    assertThat(cup.roll()).isEqualTo(new Roll(3, 4));
  }

  @Test
  void aCupThatHasRunOutSaysSoRatherThanInventingARoll() {
    Cup cup = Cup.of(new Roll(1, 2));
    cup.roll();

    assertThatThrownBy(cup::roll)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no more rolls");
  }

  @Test
  void aCupOfDiceRollsEveryDieWithinItsFaces() {
    Cup cup = Cup.of(Dice.Type.six.create(), Dice.Type.six.create());

    for (int i = 0; i < 100; i++) {
      Roll roll = cup.roll();
      assertThat(roll.die1()).isBetween(1, 6);
      assertThat(roll.die2()).isBetween(1, 6);
    }
  }
}
