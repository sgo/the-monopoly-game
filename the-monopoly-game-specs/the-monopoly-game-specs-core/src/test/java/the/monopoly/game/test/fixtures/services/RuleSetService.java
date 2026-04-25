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

  public void selectRuleSet(Rule.Set.Type type) {
    currentRuleTypeRepository.set(type);
    ruleSetRepository.put(type, type.create());
  }

  public void withOptionalRule(Rule.Type type) {
    currentRuleSet().activate(type);
  }

  private Rule.Set currentRuleSet() {
    return ruleSetRepository.get(currentRuleTypeRepository.get());
  }
}
