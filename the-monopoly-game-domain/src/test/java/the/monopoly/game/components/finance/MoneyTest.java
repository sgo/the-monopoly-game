package the.monopoly.game.components.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Worked examples for the arithmetic. {@link MoneyPropertyTest} states the
 * same behaviour as laws over generated amounts, but property tests are kept
 * out of normal verification, so the arithmetic needs cover here too.
 */
class MoneyTest {
  @Test
  void amountsAdd() {
    assertThat(new Money(60).plus(new Money(140))).isEqualTo(new Money(200));
  }

  @Test
  void amountsSubtract() {
    assertThat(new Money(200).minus(new Money(140))).isEqualTo(new Money(60));
  }

  @Test
  void anAmountCanFallBelowNothing() {
    assertThat(new Money(50).minus(new Money(75))).isEqualTo(new Money(-25));
  }

  @Test
  void addingNothingChangesNothing() {
    assertThat(new Money(1500).plus(Money.ZERO)).isEqualTo(new Money(1500));
  }

  @Test
  void nothingIsWorthNothing() {
    assertThat(Money.ZERO.amount()).isZero();
  }

  @Test
  void anAmountCoversASmallerPrice() {
    assertThat(new Money(1500).covers(new Money(60))).isTrue();
  }

  @Test
  void anAmountCoversAPriceItMatchesExactly() {
    assertThat(new Money(60).covers(new Money(60))).isTrue();
  }

  @Test
  void anAmountDoesNotCoverAPriceBeyondIt() {
    assertThat(new Money(59).covers(new Money(60))).isFalse();
  }

  @Test
  void aLargerAmountExceedsASmallerOne() {
    assertThat(new Money(120).exceeds(new Money(90))).isTrue();
  }

  @Test
  void anEqualAmountDoesNotExceedTheOneItMatches() {
    assertThat(new Money(120).exceeds(new Money(120))).isFalse();
  }

  @Test
  void preservesExactCents() {
    assertThat(Money.fromDollars("400.10").cents()).isEqualTo(40010);
    assertThat(Money.fromDollars("400.10")).isEqualTo(Money.fromCents(40010));
  }

  @Test
  void roundsPercentageWithBankersRounding() {
    assertThat(Money.fromDollars("400.10").percentage(5)).isEqualTo(Money.fromDollars("20.00"));
    assertThat(Money.fromDollars("400.10").percentage(3)).isEqualTo(Money.fromDollars("12.00"));
  }
}
