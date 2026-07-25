package the.monopoly.game.components.board;

import org.junit.jupiter.api.Test;
import the.monopoly.game.components.streets.Street;
import the.monopoly.game.rules.Rule;

import java.util.List;

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

  @Test
  void onlyStreetsBelongToAColourGroup() {
    assertThat(layout())
        .filteredOn(it -> it.kind() != street)
        .extracting(Street::colourGroup)
        .containsOnlyNulls();
  }

  @Test
  void eachColourGroupHasTheOfficialNumberOfStreets() {
    assertThat(layout())
        .filteredOn(it -> it.kind() == street)
        .filteredOn(it -> it.colourGroup() == brown)
        .hasSize(2);
    assertThat(layout())
        .filteredOn(it -> it.kind() == street)
        .filteredOn(it -> it.colourGroup() == dark_blue)
        .hasSize(2);
    assertThat(layout())
        .filteredOn(it -> it.kind() == street)
        .filteredOn(it -> it.colourGroup() == light_blue)
        .hasSize(3);
  }

  private Street.Colour colourOf(Street.Type type) {
    return ruleSet.create(type).colourGroup();
  }

  private List<Street> layout() {
    return ruleSet.gameboard().streets().toList();
  }
}
