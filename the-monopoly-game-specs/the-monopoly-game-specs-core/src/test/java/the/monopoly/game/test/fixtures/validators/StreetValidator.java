package the.monopoly.game.test.fixtures.validators;

import org.springframework.stereotype.Service;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.components.streets.Street;
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
    assertThat(currentStreet().toll()).isEqualTo(amount);
  }

  private Street currentStreet() {
    return streetRepository.get(currentStreetTypeRepository.get());
  }

  public void assertSalaryEquals(Money amount) {
    assertValueEquals(amount.multipliedBy(new Money(-1)));
  }

  public void assertVacantRentEquals(Money expectation) {
    assertThat(currentStreet().vacantRent()).isEqualTo(expectation);
  }

  public void assertRentForOneHouseEquals(Money expectation) {
    assertThat(currentStreet().rentForOneHouse()).isEqualTo(expectation);
  }

  public void assertRentForTwoHousesEquals(Money expectation) {
    assertThat(currentStreet().rentForTwoHouses()).isEqualTo(expectation);
  }

  public void assertRentForThreeHousesEquals(Money expectation) {
    assertThat(currentStreet().rentForThreeHouses()).isEqualTo(expectation);
  }

  public void assertRentForFourHousesEquals(Money expectation) {
    assertThat(currentStreet().rentForFourHouses()).isEqualTo(expectation);
  }

  public void assertRentForOneHotelEquals(Money expectation) {
    assertThat(currentStreet().rentForOneHotel()).isEqualTo(expectation);
  }

  public void assertHouseConstructionCostEquals(Money expectation) {
    assertThat(currentStreet().houseConstructionCost()).isEqualTo(expectation);
  }

  public void assertHotelConstructionCostEquals(Money expectation) {
    assertThat(currentStreet().hotelConstructionCost()).isEqualTo(expectation);
  }

  public void hotelConstructionRequiresExistingHouses(int expectation) {
    assertThat(currentStreet().hotelConstructionRequiresNumberOfHouses()).isEqualTo(expectation);
  }

  public void assertLandMortgageValueEquals(Money expectation) {
    assertThat(currentStreet().landMortgageValue()).isEqualTo(expectation);
  }
}
