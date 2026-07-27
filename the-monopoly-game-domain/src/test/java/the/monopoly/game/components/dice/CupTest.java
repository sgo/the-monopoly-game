package the.monopoly.game.components.dice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CupTest {
  @Test
  void aRollTakesItsFirstDieFromTheFirstDiceAndItsSecondFromTheSecond() {
    Roll roll = Cup.of(alwaysRolling(2), alwaysRolling(5)).roll();

    assertThat(roll.die1()).isEqualTo(2);
    assertThat(roll.die2()).isEqualTo(5);
  }

  @Test
  void aCupNeedsExactlyTheTwoDiceTheRulesArePlayedWith() {
    assertThatThrownBy(() -> Cup.of(List.of(alwaysRolling(1))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Cup.of(List.of(alwaysRolling(1), alwaysRolling(2), alwaysRolling(3))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aScriptedCupYieldsItsRollsInOrder() {
    Cup cup = Cup.of(new Roll(1, 2), new Roll(3, 4));

    assertThat(cup.roll()).isEqualTo(new Roll(1, 2));
    assertThat(cup.roll()).isEqualTo(new Roll(3, 4));
  }

  @Test
  void aScriptedCupSaysSoWhenItRunsOut() {
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

  /**
   * A dice whose faces all read the same, so which of the two a cup reached for
   * is visible in the roll it produced.
   */
  private static Dice alwaysRolling(int face) {
    Dice.Face[] faces = new Dice.Face[6];
    java.util.Arrays.fill(faces, new Dice.Face(Integer.toString(face)));
    return new Dice(Dice.Type.six, faces);
  }
}
