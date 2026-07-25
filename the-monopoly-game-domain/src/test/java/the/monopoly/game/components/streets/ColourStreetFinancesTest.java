package the.monopoly.game.components.streets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import the.monopoly.game.components.finance.Money;
import the.monopoly.game.rules.Rule;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static the.monopoly.game.components.streets.Street.Type.*;

class ColourStreetFinancesTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  static Stream<Finances> officialStreetFinances() {
    return Stream.of(
        new Finances(RueGrandeDinant, 60, 2, 10, 30, 90, 160, 250, 50, 30),
        new Finances(DiestsestraatLeuven, 60, 4, 20, 60, 180, 320, 450, 50, 30),
        new Finances(SteenstraatBrugge, 100, 6, 30, 90, 270, 400, 550, 50, 50),
        new Finances(PlaceDuMonumentSpa, 100, 6, 30, 90, 270, 400, 550, 50, 50),
        new Finances(KapellestraatOostende, 120, 8, 40, 100, 300, 450, 600, 50, 60),
        new Finances(RueDeDiekirchArlon, 140, 10, 50, 150, 450, 625, 750, 100, 70),
        new Finances(BruulMechelen, 140, 10, 50, 150, 450, 625, 750, 100, 70),
        new Finances(PlaceVerteVerviers, 160, 12, 60, 180, 500, 700, 900, 100, 80),
        new Finances(LippenslaanKnokke, 180, 14, 70, 200, 550, 750, 950, 100, 90),
        new Finances(RueRoyaleTournai, 180, 14, 70, 200, 550, 750, 950, 100, 90),
        new Finances(GroenplaatsAntwerpen, 200, 16, 80, 220, 600, 800, 1000, 100, 100),
        new Finances(RueStLeonardLiege, 220, 18, 90, 250, 700, 875, 1050, 150, 110),
        new Finances(LangeSteenstraatKortrijk, 220, 18, 90, 250, 700, 875, 1050, 150, 110),
        new Finances(GrandPlaceMons, 240, 20, 100, 300, 750, 925, 1100, 150, 120),
        new Finances(GroteMarktHasselt, 260, 22, 110, 330, 800, 975, 1150, 150, 130),
        new Finances(PlaceDeLAngeNamur, 260, 22, 110, 330, 800, 975, 1150, 150, 130),
        new Finances(HoogstraatBrussel, 280, 24, 120, 360, 850, 1025, 1200, 150, 140),
        new Finances(BoulevardTirouCharleroi, 300, 26, 130, 390, 900, 1100, 1275, 200, 150),
        new Finances(VeldstraatGent, 300, 26, 130, 390, 900, 1100, 1275, 200, 150),
        new Finances(BoulevardDAvroyLiege, 320, 28, 150, 450, 1000, 1200, 1400, 200, 160),
        new Finances(MeirAntwerpen, 350, 35, 175, 500, 1100, 1300, 1500, 200, 175),
        new Finances(NieuwstraatBrussel, 400, 50, 200, 600, 1400, 1700, 2000, 200, 200)
    );
  }

  @ParameterizedTest
  @MethodSource("officialStreetFinances")
  void theStreetCarriesItsOfficialFinancialFigures(Finances expected) {
    ColourStreet street = colourStreet(expected.type());

    assertThat(street.price()).isEqualTo(new Money(expected.value()));
    assertThat(street.vacantRent()).isEqualTo(new Money(expected.vacantRent()));
    assertThat(street.rentForHouses(0)).isEqualTo(new Money(expected.vacantRent()));
    assertThat(street.rentForHouses(1)).isEqualTo(new Money(expected.oneHouse()));
    assertThat(street.rentForHouses(2)).isEqualTo(new Money(expected.twoHouses()));
    assertThat(street.rentForHouses(3)).isEqualTo(new Money(expected.threeHouses()));
    assertThat(street.rentForHouses(4)).isEqualTo(new Money(expected.fourHouses()));
    assertThat(street.rentForOneHotel()).isEqualTo(new Money(expected.oneHotel()));
    assertThat(street.houseConstructionCost()).isEqualTo(new Money(expected.constructionCost()));
    assertThat(street.hotelConstructionCost()).isEqualTo(new Money(expected.constructionCost()));
    assertThat(street.landMortgageValue()).isEqualTo(new Money(expected.mortgage()));
  }

  @Test
  void aStreetNeverHoldsMoreThanFourHouses() {
    assertThatThrownBy(() -> colourStreet(RueGrandeDinant).rentForHouses(5))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aHotelAlwaysReplacesFourHouses() {
    assertThat(colourStreet(RueGrandeDinant).hotelConstructionRequiresNumberOfHouses()).isEqualTo(4);
  }

  @Test
  void everyColourStreetOnTheBoardIsCovered() {
    assertThat(officialStreetFinances().map(Finances::type))
        .containsExactlyInAnyOrderElementsOf(
            ruleSet.streets()
                .filter(ColourStreet.class::isInstance)
                .map(Street::type)
                .toList()
        );
  }

  private ColourStreet colourStreet(Street.Type type) {
    return (ColourStreet) ruleSet.create(type);
  }

  record Finances(
      Street.Type type,
      int value,
      int vacantRent,
      int oneHouse,
      int twoHouses,
      int threeHouses,
      int fourHouses,
      int oneHotel,
      int constructionCost,
      int mortgage
  ) {
    @Override
    public String toString() {
      return type.name();
    }
  }
}
