package the.monopoly.game.components.finance;

import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("property-test")
class MoneyPropertyTest {
  private static final Generator<Integer> AMOUNTS = Generator.integers();

  @Test
  void amountRoundTripsThroughConstruction() {
    PropertyChecker.forAll(AMOUNTS, amount -> new Money(amount).amount() == amount);
  }

  @Test
  void zeroIsTheIdentityForPlus() {
    PropertyChecker.forAll(AMOUNTS, amount -> new Money(amount).plus(Money.ZERO).equals(new Money(amount)));
  }

  @Test
  void plusIsCommutative() {
    PropertyChecker.forAll(pairs(), pair ->
        new Money(pair.a()).plus(new Money(pair.b())).equals(new Money(pair.b()).plus(new Money(pair.a())))
    );
  }

  @Test
  void minusIsTheInverseOfPlus() {
    PropertyChecker.forAll(pairs(), pair -> {
      Money x = new Money(pair.a());
      Money y = new Money(pair.b());
      return x.plus(y).minus(y).equals(x);
    });
  }

  @Test
  void plusIsAssociative() {
    PropertyChecker.forAll(triples(), t -> {
      Money x = new Money(t.a());
      Money y = new Money(t.b());
      Money z = new Money(t.c());
      return x.plus(y).plus(z).equals(x.plus(y.plus(z)));
    });
  }

  private static Generator<Pair> pairs() {
    return Generator.zipWith(AMOUNTS, AMOUNTS, Pair::new);
  }

  private static Generator<Triple> triples() {
    return Generator.zipWith(pairs(), AMOUNTS, (pair, c) -> new Triple(pair.a(), pair.b(), c));
  }

  private record Pair(int a, int b) {
  }

  private record Triple(int a, int b, int c) {
  }
}
