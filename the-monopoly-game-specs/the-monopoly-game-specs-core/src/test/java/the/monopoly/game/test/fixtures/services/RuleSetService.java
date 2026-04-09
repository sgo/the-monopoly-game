package the.monopoly.game.test.fixtures.services;

import org.springframework.stereotype.Service;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;

@Service
public class RuleSetService {
  private final RuleSetRepository ruleSetRepository;
  private final CurrentRuleTypeRepository currentRuleTypeRepository;

  public RuleSetService(RuleSetRepository ruleSetRepository, CurrentRuleTypeRepository currentRuleTypeRepository) {
    this.ruleSetRepository = ruleSetRepository;
    this.currentRuleTypeRepository = currentRuleTypeRepository;
  }

  public void selectRuleSet(Rule.Type type) {
    currentRuleTypeRepository.set(type);
    ruleSetRepository.put(type, type.create());
  }
}
