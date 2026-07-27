package the.monopoly.game.components.dice;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("property-test")
class RollPropertyTest {
  private static final Generator<Integer> FACES = Generator.integers();

  @Test
  void totalIsTheSumOfBothDice() {
    PropertyChecker.forAll(pairs(), pair -> new Roll(pair.a(), pair.b()).total() == pair.a() + pair.b());
  }

  @Test
  void isDoubleIffBothDiceMatch() {
    PropertyChecker.forAll(pairs(), pair ->
        new Roll(pair.a(), pair.b()).isDouble() == (pair.a() == pair.b())
    );
  }

  private static Generator<Pair> pairs() {
    return Generator.zipWith(FACES, FACES, Pair::new);
  }

  private record Pair(int a, int b) {
  }
}
