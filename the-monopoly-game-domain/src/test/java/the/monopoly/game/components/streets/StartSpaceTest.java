package the.monopoly.game.components.streets;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.rules.Rule.Type.double_salary_when_landing_on_start;

class StartSpaceTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @Test
  void startPaysASalaryOfTwoHundred() {
    assertThat(start().salary()).isEqualTo(new Money(200));
  }

  @Test
  void landingOnStartPaysTheSameSalaryAsPassingIt() {
    assertThat(start().salaryForLanding()).isEqualTo(start().salary());
  }

  @Test
  void theOptionalRuleDoublesTheSalaryForLandingOnStart() {
    ruleSet.activate(double_salary_when_landing_on_start);

    assertThat(start().salaryForLanding()).isEqualTo(new Money(400));
  }

  @Test
  void theOptionalRuleLeavesTheSalaryForPassingStartAlone() {
    ruleSet.activate(double_salary_when_landing_on_start);

    assertThat(start().salary()).isEqualTo(new Money(200));
  }

  private StartSpace start() {
    return (StartSpace) ruleSet.create(Street.Type.start);
  }
}
