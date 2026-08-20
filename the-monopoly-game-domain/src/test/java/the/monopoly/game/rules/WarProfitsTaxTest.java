package the.monopoly.game.rules;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;

import static org.assertj.core.api.Assertions.assertThat;

class WarProfitsTaxTest {
  private final Rule.Set rules = Rule.Set.Type.official.create();
  private final Money boardValue = WarProfitsTax.boardValue(rules);

  @Test
  void boardValueMatchesTheStalemateThreshold() {
    assertThat(boardValue).isEqualTo(new Money(22790));
  }

  @Test
  void belowTwentyFivePercentOwnershipAttractsNoRate() {
    assertThat(WarProfitsTax.rate(boardValue, new Money(5000))).isZero();
    assertThat(WarProfitsTax.rate(boardValue, new Money(5697))).isZero();
  }

  @Test
  void eachBandRateAppliesAtAndAboveItsLowerBound() {
    assertThat(WarProfitsTax.rate(boardValue, new Money(5698))).isEqualTo(100);
    assertThat(WarProfitsTax.rate(boardValue, new Money(9115))).isEqualTo(100);
    assertThat(WarProfitsTax.rate(boardValue, new Money(9116))).isEqualTo(150);
    assertThat(WarProfitsTax.rate(boardValue, new Money(13673))).isEqualTo(150);
    assertThat(WarProfitsTax.rate(boardValue, new Money(13674))).isEqualTo(200);
    assertThat(WarProfitsTax.rate(boardValue, new Money(18231))).isEqualTo(200);
    assertThat(WarProfitsTax.rate(boardValue, new Money(18232))).isEqualTo(300);
    assertThat(WarProfitsTax.rate(boardValue, new Money(22789))).isEqualTo(300);
    assertThat(WarProfitsTax.rate(boardValue, new Money(22790))).isEqualTo(400);
  }

  @Test
  void taxIsCollectedRentScaledByTheBandRate() {
    assertThat(WarProfitsTax.tax(boardValue, new Money(5698), new Money(1000))).isEqualTo(new Money(1000));
    assertThat(WarProfitsTax.tax(boardValue, new Money(9116), new Money(1000))).isEqualTo(new Money(1500));
    assertThat(WarProfitsTax.tax(boardValue, new Money(13674), new Money(1000))).isEqualTo(new Money(2000));
    assertThat(WarProfitsTax.tax(boardValue, new Money(18232), new Money(1000))).isEqualTo(new Money(3000));
    assertThat(WarProfitsTax.tax(boardValue, new Money(22790), new Money(1000))).isEqualTo(new Money(4000));
  }

  @Test
  void belowThresholdPaysNoTaxRegardlessOfCollected() {
    assertThat(WarProfitsTax.tax(boardValue, new Money(5697), new Money(50000))).isEqualTo(Money.ZERO);
  }
}