# mutation-stamp: sha256=9f273d14ee6abfe6d7373e1b349244fd9dc17feedf7110183e347bb97aaadaf8
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-07T21:45:44.099197Z","feature_name":"Greedo strategy priority table","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-priority.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Greedo's monopoly-building priority for every purchaseable space on the board","scenario_hash":"95308e9d3ddba06c27b16420a8b1d8d0b401ff86ed3e394288576e0fbaa7cfeb","mutation_count":56,"result":{"Total":56,"Killed":56,"Survived":0,"Errors":0},"tested_at":"2026-08-07T21:45:44.099197Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: Greedo strategy priority table

  # greedo-priority-1
  Scenario Outline: "<strategy>"'s monopoly-building priority for every purchaseable space on the board
    Then the "<strategy>" strategy's priority for "<space>" is "<priority>"

    Examples:
      | strategy | space | priority |
      | Greedo | Lippenslaan Knokke | highest |
      | Billionaire | Lippenslaan Knokke | highest |
      | Greedo | Rue Royale Tournai | highest |
      | Billionaire | Rue Royale Tournai | highest |
      | Greedo | Groenplaats Antwerpen | highest |
      | Billionaire | Groenplaats Antwerpen | highest |
      | Greedo | Rue St-Léonard Liège | highest |
      | Billionaire | Rue St-Léonard Liège | highest |
      | Greedo | Lange Steenstraat Kortrijk | highest |
      | Billionaire | Lange Steenstraat Kortrijk | highest |
      | Greedo | Grand Place Mons | highest |
      | Billionaire | Grand Place Mons | highest |
      | Greedo | Steenstraat Brugge | highest |
      | Billionaire | Steenstraat Brugge | highest |
      | Greedo | Place du Monument Spa | highest |
      | Billionaire | Place du Monument Spa | highest |
      | Greedo | Kapellestraat Oostende | highest |
      | Billionaire | Kapellestraat Oostende | highest |
      | Greedo | Rue Grande Dinant | middle |
      | Billionaire | Rue Grande Dinant | middle |
      | Greedo | Diestsestraat Leuven | middle |
      | Billionaire | Diestsestraat Leuven | middle |
      | Greedo | Rue de Diekirch Arlon | middle |
      | Billionaire | Rue de Diekirch Arlon | middle |
      | Greedo | Bruul Mechelen | middle |
      | Billionaire | Bruul Mechelen | middle |
      | Greedo | Place Verte Verviers | middle |
      | Billionaire | Place Verte Verviers | middle |
      | Greedo | Grote Markt Hasselt | middle |
      | Billionaire | Grote Markt Hasselt | middle |
      | Greedo | Place de l'Ange Namur | middle |
      | Billionaire | Place de l'Ange Namur | middle |
      | Greedo | Hoogstraat (Brussel) / Rue Haute (Bruxelles) | middle |
      | Billionaire | Hoogstraat (Brussel) / Rue Haute (Bruxelles) | middle |
      | Greedo | Boulevard Tirou Charleroi | lowest |
      | Billionaire | Boulevard Tirou Charleroi | lowest |
      | Greedo | Veldstraat Gent | lowest |
      | Billionaire | Veldstraat Gent | lowest |
      | Greedo | Boulevard d'Avroy Liège | lowest |
      | Billionaire | Boulevard d'Avroy Liège | lowest |
      | Greedo | Meir Antwerpen | lowest |
      | Billionaire | Meir Antwerpen | lowest |
      | Greedo | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | lowest |
      | Billionaire | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | lowest |
      | Greedo | Elektriciteitscentrale | lowest |
      | Billionaire | Elektriciteitscentrale | lowest |
      | Greedo | Watermaatschappij | lowest |
      | Billionaire | Watermaatschappij | lowest |
      | Greedo | Noord Station | lowest |
      | Billionaire | Noord Station | lowest |
      | Greedo | Centraal Station | lowest |
      | Billionaire | Centraal Station | lowest |
      | Greedo | Buurtspoorwegen | lowest |
      | Billionaire | Buurtspoorwegen | lowest |
      | Greedo | Zuid Station | lowest |
      | Billionaire | Zuid Station | lowest |
