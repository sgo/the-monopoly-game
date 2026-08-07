# mutation-stamp: sha256=9f273d14ee6abfe6d7373e1b349244fd9dc17feedf7110183e347bb97aaadaf8
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-07T21:45:44.099197Z","feature_name":"Greedo strategy priority table","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-priority.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Greedo's monopoly-building priority for every purchaseable space on the board","scenario_hash":"95308e9d3ddba06c27b16420a8b1d8d0b401ff86ed3e394288576e0fbaa7cfeb","mutation_count":56,"result":{"Total":56,"Killed":56,"Survived":0,"Errors":0},"tested_at":"2026-08-07T21:45:44.099197Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: Greedo strategy priority table

  # greedo-priority-1
  Scenario Outline: Greedo's monopoly-building priority for every purchaseable space on the board
    Then the "Greedo" strategy's priority for "<space>" is "<priority>"

    Examples:
      | space                                        | priority |
      | Lippenslaan Knokke                            | highest  |
      | Rue Royale Tournai                             | highest  |
      | Groenplaats Antwerpen                          | highest  |
      | Rue St-Léonard Liège                           | highest  |
      | Lange Steenstraat Kortrijk                     | highest  |
      | Grand Place Mons                               | highest  |
      | Steenstraat Brugge                             | highest  |
      | Place du Monument Spa                          | highest  |
      | Kapellestraat Oostende                         | highest  |
      | Rue Grande Dinant                              | middle   |
      | Diestsestraat Leuven                           | middle   |
      | Rue de Diekirch Arlon                          | middle   |
      | Bruul Mechelen                                 | middle   |
      | Place Verte Verviers                           | middle   |
      | Grote Markt Hasselt                            | middle   |
      | Place de l'Ange Namur                          | middle   |
      | Hoogstraat (Brussel) / Rue Haute (Bruxelles)   | middle   |
      | Boulevard Tirou Charleroi                      | lowest   |
      | Veldstraat Gent                                | lowest   |
      | Boulevard d'Avroy Liège                        | lowest   |
      | Meir Antwerpen                                 | lowest   |
      | Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)  | lowest   |
      | Elektriciteitscentrale                         | lowest   |
      | Watermaatschappij                              | lowest   |
      | Noord Station                                  | lowest   |
      | Centraal Station                               | lowest   |
      | Buurtspoorwegen                                | lowest   |
      | Zuid Station                                   | lowest   |
