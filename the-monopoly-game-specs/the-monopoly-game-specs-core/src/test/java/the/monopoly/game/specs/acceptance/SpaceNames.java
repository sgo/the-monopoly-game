package the.monopoly.game.specs.acceptance;

import the.monopoly.game.components.streets.Street;

import java.util.Map;

import static the.monopoly.game.components.streets.Street.Type.*;

/**
 * Maps the board-space names used in the feature files to their domain type.
 */
final class SpaceNames {
  private static final Map<String, Street.Type> BY_NAME = Map.ofEntries(
      Map.entry("Start", start),

      Map.entry("Rue Grande Dinant", RueGrandeDinant),
      Map.entry("Diestsestraat Leuven", DiestsestraatLeuven),
      Map.entry("Steenstraat Brugge", SteenstraatBrugge),
      Map.entry("Place du Monument Spa", PlaceDuMonumentSpa),
      Map.entry("Kapellestraat Oostende", KapellestraatOostende),
      Map.entry("Rue de Diekirch Arlon", RueDeDiekirchArlon),
      Map.entry("Bruul Mechelen", BruulMechelen),
      Map.entry("Place Verte Verviers", PlaceVerteVerviers),
      Map.entry("Lippenslaan Knokke", LippenslaanKnokke),
      Map.entry("Rue Royale Tournai", RueRoyaleTournai),
      Map.entry("Groenplaats Antwerpen", GroenplaatsAntwerpen),
      Map.entry("Rue St-Léonard Liège", RueStLeonardLiege),
      Map.entry("Lange Steenstraat Kortrijk", LangeSteenstraatKortrijk),
      Map.entry("Grand Place Mons", GrandPlaceMons),
      Map.entry("Grote Markt Hasselt", GroteMarktHasselt),
      Map.entry("Place de l'Ange Namur", PlaceDeLAngeNamur),
      Map.entry("Hoogstraat (Brussel) / Rue Haute (Bruxelles)", HoogstraatBrussel),
      Map.entry("Boulevard Tirou Charleroi", BoulevardTirouCharleroi),
      Map.entry("Veldstraat Gent", VeldstraatGent),
      Map.entry("Boulevard d'Avroy Liège", BoulevardDAvroyLiege),
      Map.entry("Meir Antwerpen", MeirAntwerpen),
      Map.entry("Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)", NieuwstraatBrussel),

      Map.entry("Noord Station / Gare du Nord", NoordStation),
      Map.entry("Centraal Station / Gare Centrale", CentraalStation),
      Map.entry("Buurtspoorwegen / Tramways Vicinaux", Buurtspoorwegen),
      Map.entry("Zuid Station / Gare du Midi", ZuidStation),

      Map.entry("Elektriciteitscentrale / Centrale Électrique", Elektriciteitscentrale),
      Map.entry("Watermaatschappij / Compagnie des Eaux", Watermaatschappij),

      Map.entry("Inkomsten Belasting / Impôts sur le revenu", InkomstenBelasting),
      Map.entry("Extra Belasting / Taxe de Luxe", ExtraBelasting)
  );

  private SpaceNames() {
  }

  static Street.Type of(String name) {
    Street.Type type = BY_NAME.get(name);
    if (type == null)
      throw new AssertionError("Unknown board space \"" + name + "\".");
    return type;
  }
}
