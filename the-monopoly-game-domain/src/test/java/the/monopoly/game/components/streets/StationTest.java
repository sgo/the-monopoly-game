package the.monopoly.game.components.streets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static the.monopoly.game.components.streets.Street.Type.*;

class StationTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @ParameterizedTest
  @EnumSource(names = {"NoordStation", "CentraalStation", "Buurtspoorwegen", "ZuidStation"})
  void everyStationCostsTwoHundredAndMortgagesForOneHundred(Street.Type type) {
    Street station = ruleSet.create(type);

    assertThat(station.kind()).isEqualTo(Street.Kind.station);
    assertThat(station.toll()).isEqualTo(new Money(200));
    assertThat(station.landMortgageValue()).isEqualTo(new Money(100));
  }

  @ParameterizedTest
  @EnumSource(names = {"NoordStation", "CentraalStation", "Buurtspoorwegen", "ZuidStation"})
  void stationRentDoublesWithEachAdditionalStationOwned(Street.Type type) {
    Street station = ruleSet.create(type);

    assertThat(station.rentForOwning(1)).isEqualTo(new Money(25));
    assertThat(station.rentForOwning(2)).isEqualTo(new Money(50));
    assertThat(station.rentForOwning(3)).isEqualTo(new Money(100));
    assertThat(station.rentForOwning(4)).isEqualTo(new Money(200));
  }

  @Test
  void owningNoStationsEarnsNoRent() {
    assertThat(ruleSet.create(NoordStation).rentForOwning(0)).isEqualTo(Money.ZERO);
  }

  @Test
  void thereIsNoRentForMoreStationsThanExistOnTheBoard() {
    assertThatThrownBy(() -> ruleSet.create(NoordStation).rentForOwning(5))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aStationCannotBeBuiltOn() {
    Street station = ruleSet.create(NoordStation);

    assertThatThrownBy(station::rentForOneHouse).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(station::houseConstructionCost).isInstanceOf(UnsupportedOperationException.class);
  }
}
