package the.monopoly.game.components.streets;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.rules.Rule;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Street.rentForOwning and Street.rentDiceMultiplierForOwning are only ever
 * exercised at a handful of hand-picked boundary values in the example-based
 * tests. These properties sweep a much wider range of owned counts to pin
 * down the invariant those tests only sample: valid indices never throw and
 * out-of-board indices always throw, for every station and utility on the
 * board.
 */
@Tag("property-test")
class OwnedCountBoundsPropertyTest {
  private static final int STATIONS_ON_BOARD = 4;
  private static final int UTILITIES_ON_BOARD = 2;
  private static final Generator<Integer> OWNED_COUNTS = Generator.integers(-10_000, 10_000);

  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @Test
  void stationRentIsDefinedOnlyWithinBoardBounds() {
    assertOwnedCountBounds(
        stationTypes(),
        STATIONS_ON_BOARD,
        c -> () -> ((Station) ruleSet.create(c.type())).rentForOwning(c.owned())
    );
  }

  @Test
  void utilityRentMultiplierIsDefinedOnlyWithinBoardBounds() {
    assertOwnedCountBounds(
        utilityTypes(),
        UTILITIES_ON_BOARD,
        c -> () -> ((Utility) ruleSet.create(c.type())).rentDiceMultiplierForOwning(c.owned())
    );
  }

  private void assertOwnedCountBounds(
      Generator<Street.Type> types,
      int onBoard,
      Function<Case, ThrowingCallable> callFor
  ) {
    PropertyChecker.forAll(Generator.zipWith(types, OWNED_COUNTS, Case::new),
        c -> isBoundedByOwnedOnBoard(c.owned(), onBoard, callFor.apply(c)));
  }

  private boolean isBoundedByOwnedOnBoard(int owned, int onBoard, ThrowingCallable call) {
    if (owned < 0 || owned > onBoard) {
      assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class);
    } else {
      assertThatCode(call).doesNotThrowAnyException();
    }
    return true;
  }

  private static Generator<Street.Type> stationTypes() {
    return Generator.sampledFrom(
        Street.Type.NoordStation,
        Street.Type.CentraalStation,
        Street.Type.Buurtspoorwegen,
        Street.Type.ZuidStation
    );
  }

  private static Generator<Street.Type> utilityTypes() {
    return Generator.sampledFrom(Street.Type.Elektriciteitscentrale, Street.Type.Watermaatschappij);
  }

  private record Case(Street.Type type, int owned) {
  }
}
