package the.monopoly.game.components.dice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DiceTest {
  @Test
  void aDiceIsTheFacesItWasMadeWith() {
    assertThat(new Dice(face("H"), face("T")).faces()).containsExactly(face("H"), face("T"));
  }

  /**
   * Rolled with two faces rather than six, so a roll that reached past them —
   * or stopped short of them — shows up as a face the dice does not have.
   */
  @Test
  void aRollAlwaysShowsOneOfTheDicesOwnFaces() {
    Dice dice = new Dice(face("H"), face("T"));

    for (int i = 0; i < 100; i++)
      assertThat(dice.roll()).isIn(face("H"), face("T"));
  }

  @Test
  void bothFacesOfATwoFacedDiceComeUp() {
    Dice dice = new Dice(face("H"), face("T"));

    assertThat(rolledOver(dice, 100)).containsExactlyInAnyOrder(face("H"), face("T"));
  }

  @Test
  void theSixFacedDiceTheRulesCallForReadsOneThroughSix() {
    assertThat(Dice.Type.six.create().faces())
        .containsExactly(face("1"), face("2"), face("3"), face("4"), face("5"), face("6"));
  }

  private static Set<Dice.Face> rolledOver(Dice dice, int rolls) {
    Set<Dice.Face> seen = new HashSet<>();
    for (int i = 0; i < rolls; i++) seen.add(dice.roll());
    return seen;
  }

  private static Dice.Face face(String symbol) {
    return new Dice.Face(symbol);
  }
}
