package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.test.fixtures.repository.CurrentStreetTypeRepository;
import the.monopoly.game.test.fixtures.repository.StreetRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class StreetValidator {
  private final StreetRepository streetRepository;
  private final CurrentStreetTypeRepository currentStreetTypeRepository;

  public StreetValidator(StreetRepository streetRepository, CurrentStreetTypeRepository currentStreetTypeRepository) {
    this.streetRepository = streetRepository;
    this.currentStreetTypeRepository = currentStreetTypeRepository;
  }

  public void assertValueEquals(Money amount) {
    assertThat(streetRepository.get(currentStreetTypeRepository.get()).value()).isEqualTo(amount);
  }
}
