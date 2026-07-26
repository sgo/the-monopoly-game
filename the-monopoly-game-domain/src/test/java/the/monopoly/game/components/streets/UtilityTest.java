package the.monopoly.game.components.streets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static the.monopoly.game.components.streets.Street.Type.Elektriciteitscentrale;

class UtilityTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @ParameterizedTest
  @EnumSource(names = {"Elektriciteitscentrale", "Watermaatschappij"})
  void everyUtilityCostsOneHundredFiftyAndMortgagesForSeventyFive(Street.Type type) {
    Utility utility = utility(type);

    assertThat(utility.kind()).isEqualTo(Street.Kind.utility);
    assertThat(utility.price()).isEqualTo(new Money(150));
    assertThat(utility.landMortgageValue()).isEqualTo(new Money(75));
  }

  @ParameterizedTest
  @EnumSource(names = {"Elektriciteitscentrale", "Watermaatschappij"})
  void utilityRentIsAMultipleOfTheDiceRoll(Street.Type type) {
    Utility utility = utility(type);

    assertThat(utility.rentDiceMultiplierForOwning(1)).isEqualTo(4);
    assertThat(utility.rentDiceMultiplierForOwning(2)).isEqualTo(10);
  }

  @Test
  void owningNoUtilitiesEarnsNoRent() {
    assertThat(utility(Elektriciteitscentrale).rentDiceMultiplierForOwning(0)).isZero();
  }

  @Test
  void thereIsNoMultiplierForMoreUtilitiesThanExistOnTheBoard() {
    assertThatThrownBy(() -> utility(Elektriciteitscentrale).rentDiceMultiplierForOwning(3))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private Utility utility(Street.Type type) {
    return (Utility) ruleSet.create(type);
  }
}
