package the.monopoly.game.components.streets;

import the.monopoly.game.rules.Rule;

import java.util.Set;

import static java.util.Collections.emptySet;
import static the.monopoly.game.components.streets.Street.Colour.*;
import static the.monopoly.game.components.streets.Street.Kind.*;

/**
 * A space on the board.
 * <p>
 * Every space knows which space it is and what kind of space it is. What else a
 * space can do depends on its kind, so the rest lives on the subtypes: only an
 * {@link Ownable} has a price, only a {@link ColourStreet} can be built on,
 * only a {@link TaxSpace} charges tax. Asking a station for its house rent is
 * therefore a compile error rather than a runtime failure.
 */
public sealed interface Street
    permits Ownable, StartSpace, TaxSpace, UnownableSpace {
  Type type();

  Kind kind();

  enum Type {
    start(StartSpace.factory()),

    RueGrandeDinant(ColourStreet.of(brown, 60, 2, 10, 30, 90, 160, 250, 50, 30)),
    DiestsestraatLeuven(ColourStreet.of(brown, 60, 4, 20, 60, 180, 320, 450, 50, 30)),
    SteenstraatBrugge(ColourStreet.of(light_blue, 100, 6, 30, 90, 270, 400, 550, 50, 50)),
    PlaceDuMonumentSpa(ColourStreet.of(light_blue, 100, 6, 30, 90, 270, 400, 550, 50, 50)),
    KapellestraatOostende(ColourStreet.of(light_blue, 120, 8, 40, 100, 300, 450, 600, 50, 60)),
    RueDeDiekirchArlon(ColourStreet.of(pink, 140, 10, 50, 150, 450, 625, 750, 100, 70)),
    BruulMechelen(ColourStreet.of(pink, 140, 10, 50, 150, 450, 625, 750, 100, 70)),
    PlaceVerteVerviers(ColourStreet.of(pink, 160, 12, 60, 180, 500, 700, 900, 100, 80)),
    LippenslaanKnokke(ColourStreet.of(orange, 180, 14, 70, 200, 550, 750, 950, 100, 90)),
    RueRoyaleTournai(ColourStreet.of(orange, 180, 14, 70, 200, 550, 750, 950, 100, 90)),
    GroenplaatsAntwerpen(ColourStreet.of(orange, 200, 16, 80, 220, 600, 800, 1000, 100, 100)),
    RueStLeonardLiege(ColourStreet.of(red, 220, 18, 90, 250, 700, 875, 1050, 150, 110)),
    LangeSteenstraatKortrijk(ColourStreet.of(red, 220, 18, 90, 250, 700, 875, 1050, 150, 110)),
    GrandPlaceMons(ColourStreet.of(red, 240, 20, 100, 300, 750, 925, 1100, 150, 120)),
    GroteMarktHasselt(ColourStreet.of(yellow, 260, 22, 110, 330, 800, 975, 1150, 150, 130)),
    PlaceDeLAngeNamur(ColourStreet.of(yellow, 260, 22, 110, 330, 800, 975, 1150, 150, 130)),
    HoogstraatBrussel(ColourStreet.of(yellow, 280, 24, 120, 360, 850, 1025, 1200, 150, 140)),
    BoulevardTirouCharleroi(ColourStreet.of(green, 300, 26, 130, 390, 900, 1100, 1275, 200, 150)),
    VeldstraatGent(ColourStreet.of(green, 300, 26, 130, 390, 900, 1100, 1275, 200, 150)),
    BoulevardDAvroyLiege(ColourStreet.of(green, 320, 28, 150, 450, 1000, 1200, 1400, 200, 160)),
    MeirAntwerpen(ColourStreet.of(dark_blue, 350, 35, 175, 500, 1100, 1300, 1500, 200, 175)),
    NieuwstraatBrussel(ColourStreet.of(dark_blue, 400, 50, 200, 600, 1400, 1700, 2000, 200, 200)),

    NoordStation(Station.factory()),
    CentraalStation(Station.factory()),
    Buurtspoorwegen(Station.factory()),
    ZuidStation(Station.factory()),

    Elektriciteitscentrale(Utility.factory()),
    Watermaatschappij(Utility.factory()),

    InkomstenBelasting(TaxSpace.of(200)),
    ExtraBelasting(TaxSpace.of(100)),

    Kans(UnownableSpace.of(chance)),
    AlgemeenFonds(UnownableSpace.of(community_chest)),
    OpBezoek(UnownableSpace.of(jail)),
    GratisParkeren(UnownableSpace.of(free_parking)),
    NaarDeGevangenis(UnownableSpace.of(go_to_jail));

    private final Factory factory;

    Type(Factory factory) {
      this.factory = factory;
    }

    public Street create(Set<Rule> activatedRules) {
      return factory.create(this, activatedRules == null ? emptySet() : activatedRules);
    }
  }

  enum Kind {
    start, street, station, utility, tax, chance, community_chest, jail, free_parking, go_to_jail
  }

  enum Colour {
    brown, light_blue, pink, orange, red, yellow, green, dark_blue
  }

  interface Factory {
    Street create(Type type, Set<Rule> activatedRules);
  }
}
