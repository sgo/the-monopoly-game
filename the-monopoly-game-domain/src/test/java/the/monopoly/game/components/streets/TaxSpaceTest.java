package the.monopoly.game.components.streets;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.components.streets.Street.Type.ExtraBelasting;
import static the.monopoly.game.components.streets.Street.Type.InkomstenBelasting;

class TaxSpaceTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @Test
  void incomeTaxCostsTwoHundred() {
    TaxSpace space = taxSpace(InkomstenBelasting);

    assertThat(space.kind()).isEqualTo(Street.Kind.tax);
    assertThat(space.tax()).isEqualTo(new Money(200));
  }

  @Test
  void luxuryTaxCostsOneHundred() {
    TaxSpace space = taxSpace(ExtraBelasting);

    assertThat(space.kind()).isEqualTo(Street.Kind.tax);
    assertThat(space.tax()).isEqualTo(new Money(100));
  }

  private TaxSpace taxSpace(Street.Type type) {
    return (TaxSpace) ruleSet.create(type);
  }
}
