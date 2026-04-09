package the.monopoly.game.test.fixtures.services;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.test.fixtures.repository.CurrentRuleTypeRepository;
import the.monopoly.game.test.fixtures.repository.CurrentStreetTypeRepository;
import the.monopoly.game.test.fixtures.repository.RuleSetRepository;
import the.monopoly.game.test.fixtures.repository.StreetRepository;

@Service
public class StreetService {
  private final StreetRepository streetRepository;
  private final CurrentStreetTypeRepository currentStreetTypeRepository;

  public StreetService(
      StreetRepository streetRepository,
      CurrentStreetTypeRepository currentStreetTypeRepository
  ) {
    this.streetRepository = streetRepository;
    this.currentStreetTypeRepository = currentStreetTypeRepository;
  }

  public void select(Street.Type type) {
    currentStreetTypeRepository.set(type);
    streetRepository.put(type, type.create());
  }
}
