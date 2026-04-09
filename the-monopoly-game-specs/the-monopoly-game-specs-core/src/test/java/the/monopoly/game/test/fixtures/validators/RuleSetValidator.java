package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.dice.Dice;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class RuleSetValidator {
  private final RuleSetRepository ruleSetRepository;
  private final CurrentRuleTypeRepository currentRuleTypeRepository;

  public RuleSetValidator(RuleSetRepository ruleSetRepository, CurrentRuleTypeRepository currentRuleTypeRepository) {
    this.ruleSetRepository = ruleSetRepository;
    this.currentRuleTypeRepository = currentRuleTypeRepository;
  }

  public void assertPlayWithDice(List<Dice.Type> expectations) {
    assertThat(ruleSetRepository.get(currentRuleTypeRepository.get()).dice())
        .extracting(Dice::type)
        .containsExactlyElementsOf(expectations);
  }
}
