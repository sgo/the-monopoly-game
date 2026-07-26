package the.monopoly.game.components.board;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.streets.ColourStreet;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static the.monopoly.game.components.streets.Street.Colour.*;
import static the.monopoly.game.components.streets.Street.Kind.*;
import static the.monopoly.game.components.streets.Street.Type.*;

class BoardLayoutTest {
  private final Rule.Set ruleSet = Rule.Set.Type.official.create();

  @Test
  void theBoardHasFortySpaces() {
    assertThat(layout()).hasSize(40);
  }

  @Test
  void theBoardIsLaidOutInTheOfficialOrder() {
    assertThat(layout())
        .extracting(Street::type)
        .containsExactly(
            Street.Type.start,
            RueGrandeDinant,
            AlgemeenFonds,
            DiestsestraatLeuven,
            InkomstenBelasting,
            NoordStation,
            SteenstraatBrugge,
            Kans,
            PlaceDuMonumentSpa,
            KapellestraatOostende,
            OpBezoek,
            RueDeDiekirchArlon,
            Elektriciteitscentrale,
            BruulMechelen,
            PlaceVerteVerviers,
            CentraalStation,
            LippenslaanKnokke,
            AlgemeenFonds,
            RueRoyaleTournai,
            GroenplaatsAntwerpen,
            GratisParkeren,
            RueStLeonardLiege,
            Kans,
            LangeSteenstraatKortrijk,
            GrandPlaceMons,
            Buurtspoorwegen,
            GroteMarktHasselt,
            PlaceDeLAngeNamur,
            Watermaatschappij,
            HoogstraatBrussel,
            NaarDeGevangenis,
            BoulevardTirouCharleroi,
            VeldstraatGent,
            AlgemeenFonds,
            BoulevardDAvroyLiege,
            ZuidStation,
            Kans,
            MeirAntwerpen,
            ExtraBelasting,
            NieuwstraatBrussel
        );
  }

  @Test
  void everySpaceKnowsWhatKindOfSpaceItIs() {
    assertThat(layout())
        .extracting(Street::kind)
        .containsExactly(
            Street.Kind.start, street, community_chest, street, tax,
            station, street, chance, street, street,
            jail, street, utility, street, street,
            station, street, community_chest, street, street,
            free_parking, street, chance, street, street,
            station, street, street, utility, street,
            go_to_jail, street, street, community_chest, street,
            station, chance, street, tax, street
        );
  }

  @Test
  void theColourGroupsFollowTheOfficialBoard() {
    assertThat(colourOf(RueGrandeDinant)).isEqualTo(brown);
    assertThat(colourOf(SteenstraatBrugge)).isEqualTo(light_blue);
    assertThat(colourOf(RueDeDiekirchArlon)).isEqualTo(pink);
    assertThat(colourOf(LippenslaanKnokke)).isEqualTo(orange);
    assertThat(colourOf(GrandPlaceMons)).isEqualTo(red);
    assertThat(colourOf(GroteMarktHasselt)).isEqualTo(yellow);
    assertThat(colourOf(VeldstraatGent)).isEqualTo(green);
    assertThat(colourOf(NieuwstraatBrussel)).isEqualTo(dark_blue);
  }

  /**
   * Only a {@link ColourStreet} carries a colour group at all, so this holds by
   * construction. It is asserted to pin down the other half of that bargain:
   * the spaces that are colour streets are exactly the ones of kind street.
   */
  @Test
  void onlyStreetsBelongToAColourGroup() {
    assertThat(layout())
        .filteredOn(it -> it instanceof ColourStreet)
        .extracting(Street::kind)
        .containsOnly(street);
    assertThat(layout())
        .filteredOn(it -> it.kind() == street)
        .allMatch(it -> it instanceof ColourStreet);
  }

  @Test
  void eachColourGroupHasTheOfficialNumberOfStreets() {
    assertThat(colourStreetsOf(brown)).hasSize(2);
    assertThat(colourStreetsOf(dark_blue)).hasSize(2);
    assertThat(colourStreetsOf(light_blue)).hasSize(3);
  }

  private List<ColourStreet> colourStreetsOf(Street.Colour colour) {
    return colourStreets().filter(it -> it.colourGroup() == colour).toList();
  }

  private Stream<ColourStreet> colourStreets() {
    return layout().stream().filter(ColourStreet.class::isInstance).map(ColourStreet.class::cast);
  }

  private Street.Colour colourOf(Street.Type type) {
    return ((ColourStreet) ruleSet.create(type)).colourGroup();
  }

  private List<Street> layout() {
    return ruleSet.streets().toList();
  }
}
