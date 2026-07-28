# mutation-stamp: sha256=be6243e0e5973a0cf77cb5ad1b28ae2252965c706369dbb11fb9f2d7962884cb
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T11:33:10.688058Z","feature_name":"utilities","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/components/utilities.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"utility","scenario_hash":"14994c5677a17c45585c1283aa6f62057bc7035d5b3d901af1bb10b75a47a4e3","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:30:43.576533Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: utilities

  # utilities-1
  Scenario Outline: utility
    Given the utility "<name>"
    Then the utility value is $150
    And rent for owning 1 utility is 4 times the dice roll
    And rent for owning 2 utilities is 10 times the dice roll
    And mortgage value of the land is $75

    Examples:
      | name                                          |
      | Elektriciteitscentrale / Centrale Électrique  |
      | Watermaatschappij / Compagnie des Eaux        |
