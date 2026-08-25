package the.monopoly.game.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatorFlagsTest {
  @Test
  void detectsEachOptionalRuleFlag() {
    assertThat(SimulatorFlags.stalemateTrading("--optional-greedo-stalemate-trading")).isTrue();
    assertThat(SimulatorFlags.legalEntityTrading("--optional-greedo-legal-entity")).isTrue();
    assertThat(SimulatorFlags.assetRichOpening("--optional-asset-rich-billionaire")).isTrue();
    assertThat(SimulatorFlags.developmentLoans("--optional-development-loans")).isTrue();
    assertThat(SimulatorFlags.fullDrawDevelopmentLoans("--optional-development-loans-full-draw")).isTrue();
    assertThat(SimulatorFlags.warProfitsTax("--optional-war-profits-tax")).isTrue();
    assertThat(SimulatorFlags.rentRelief("--optional-rent-relief")).isTrue();
    assertThat(SimulatorFlags.unifiedIncomeTax("--optional-unified-income-tax")).isTrue();
  }

  @Test
  void leavesOptionalRuleFlagsDisabledWhenAbsent() {
    assertThat(SimulatorFlags.stalemateTrading()).isFalse();
    assertThat(SimulatorFlags.legalEntityTrading()).isFalse();
    assertThat(SimulatorFlags.assetRichOpening()).isFalse();
    assertThat(SimulatorFlags.developmentLoans()).isFalse();
    assertThat(SimulatorFlags.fullDrawDevelopmentLoans()).isFalse();
    assertThat(SimulatorFlags.warProfitsTax()).isFalse();
    assertThat(SimulatorFlags.rentRelief()).isFalse();
    assertThat(SimulatorFlags.unifiedIncomeTax()).isFalse();
  }

  @Test
  void recognizesAllOptionalAndValueFlagsButNotStrategies() {
    assertThat(SimulatorFlags.recognized("--optional-greedo-stalemate-trading")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-greedo-legal-entity")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-asset-rich-billionaire")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-development-loans")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-development-loans-full-draw")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-war-profits-tax")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-rent-relief")).isTrue();
    assertThat(SimulatorFlags.recognized("--optional-unified-income-tax")).isTrue();
    assertThat(SimulatorFlags.recognized("--max-years=12")).isTrue();
    assertThat(SimulatorFlags.recognized("--seed=42")).isTrue();
    assertThat(SimulatorFlags.recognized("greedo")).isFalse();
  }

  @Test
  void parsesMaxYearsAndSeedValues() {
    assertThat(SimulatorFlags.maxYears("--max-years=12")).isEqualTo(12);
    assertThat(SimulatorFlags.seed("--seed=42")).isEqualTo(42L);
  }

  @Test
  void suppliesDefaultsWhenValueFlagsAreAbsent() {
    assertThat(SimulatorFlags.maxYears()).isEqualTo(-1);
    assertThat(SimulatorFlags.seed()).isNull();
  }
}
