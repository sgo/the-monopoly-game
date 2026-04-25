package the.monopoly.game.test.fixtures.services;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.CurrentStreetTypeRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;
import the.monopoly.game.test.fixtures.repository.StreetRepository;

@Service
public class StreetService {
  private final StreetRepository streetRepository;
  private final CurrentStreetTypeRepository currentStreetTypeRepository;
  private final RuleSetRepository ruleSetRepository;
  private final CurrentRuleTypeRepository currentRuleTypeRepository;

  public StreetService(
      StreetRepository streetRepository,
      CurrentStreetTypeRepository currentStreetTypeRepository,
      RuleSetRepository ruleSetRepository,
      CurrentRuleTypeRepository currentRuleTypeRepository
  ) {
    this.streetRepository = streetRepository;
    this.currentStreetTypeRepository = currentStreetTypeRepository;
    this.ruleSetRepository = ruleSetRepository;
    this.currentRuleTypeRepository = currentRuleTypeRepository;
  }

  public void select(Street.Type type) {
    currentStreetTypeRepository.set(type);
    streetRepository.put(type, type.create(null));
  }

  public Street create(Street.Type type) {
    return currentRuleSet().create(type);
  }

  private Rule.Set currentRuleSet() {
    return ruleSetRepository.get(currentRuleTypeRepository.get());
  }
}
