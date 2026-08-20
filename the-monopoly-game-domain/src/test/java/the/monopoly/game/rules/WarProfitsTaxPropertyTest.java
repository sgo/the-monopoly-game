package the.monopoly.game.rules;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;

import java.util.Set;

/**
 * The example-based {@link WarProfitsTaxTest} pins a handful of band boundaries
 * by hand. These properties sweep continuous ranges of land worth and collected
 * rent to pin the invariants those samples only spot-check: rates drawn from the
 * defined band set, rate/tax monotonicity, the zero-below-threshold rule, and
 * tax being the rate-scaled (and correctly rounded) collected rent.
 */
@Tag("property-test")
class WarProfitsTaxPropertyTest {
  private static final Money BOARD =
      WarProfitsTax.boardValue(Rule.Set.Type.official.create());
  private static final Set<Integer> BAND_RATES = Set.of(0, 100, 150, 200, 300, 400);
  private static final Generator<Integer> LAND_CENTS = Generator.integers(0, 60_000);
  private static final Generator<Integer> COLLECTED_CENTS = Generator.integers(0, 100_000_000);

  @Test
  void rateIsAlwaysOneOfTheDefinedBands() {
    PropertyChecker.forAll(LAND_CENTS, land ->
        BAND_RATES.contains(WarProfitsTax.rate(BOARD, Money.fromCents(land))));
  }

  @Test
  void rateNonDecreasingAsLandGrows() {
    PropertyChecker.forAll(Generator.zipWith(LAND_CENTS, LAND_CENTS, Pair::new), pair -> {
      int rateLo = WarProfitsTax.rate(BOARD, Money.fromCents(Math.min(pair.a(), pair.b())));
      int rateHi = WarProfitsTax.rate(BOARD, Money.fromCents(Math.max(pair.a(), pair.b())));
      return rateLo <= rateHi;
    });
  }

  @Test
  void belowTwentyFivePercentOwnershipPaysNoTaxRegardlessOfCollected() {
    Money below = Money.fromCents(BOARD.cents() * 25 / 100 - 1);
    assertNoTax(below);
  }

  @Test
  void taxNonDecreasingAsCollectedGrows() {
    PropertyChecker.forAll(Generator.zipWith(COLLECTED_CENTS, COLLECTED_CENTS, Pair::new), pair -> {
      int rate = WarProfitsTax.rate(BOARD, Money.fromCents(10_000));
      Money smaller = WarProfitsTax.tax(BOARD, Money.fromCents(10_000), Money.fromCents(Math.min(pair.a(), pair.b())));
      Money larger = WarProfitsTax.tax(BOARD, Money.fromCents(10_000), Money.fromCents(Math.max(pair.a(), pair.b())));
      return smaller.cents() <= larger.cents();
    });
  }

  @Test
  void taxEqualsCollectedScaledByRateWithinHalfCentRounding() {
    PropertyChecker.forAll(Generator.zipWith(LAND_CENTS, COLLECTED_CENTS, Pair::new), pair -> {
      Money land = Money.fromCents(pair.a());
      Money collected = Money.fromCents(pair.b());
      int rate = WarProfitsTax.rate(BOARD, land);
      Money tax = WarProfitsTax.tax(BOARD, land, collected);
      double exact = collected.cents() * (double) rate / 100.0;
      return Math.abs(tax.cents() - exact) <= 0.5;
    });
  }

  private void assertNoTax(Money land) {
    PropertyChecker.forAll(COLLECTED_CENTS, collected ->
        WarProfitsTax.tax(BOARD, land, Money.fromCents(collected)).equals(Money.ZERO));
  }

  private record Pair(int a, int b) {
  }
}