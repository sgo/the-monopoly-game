# mutation-stamp: sha256=d9038457a45efbd281ff7e3f35f75967696736634425af75255db838f093204d
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-27T13:10:51.635803Z","feature_name":"stations","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/components/stations.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"station","scenario_hash":"f0308789c04f4673b7b98b0e9e7b7fc4285bbb4fd8a1dfa01735a8a5e138b78e","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-27T10:30:42.246640Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: stations

  # stations-1
  Scenario Outline: station
    Given the station "<name>"
    Then the station value is $200
    And rent for owning 1 station is $25
    And rent for owning 2 stations is $50
    And rent for owning 3 stations is $100
    And rent for owning 4 stations is $200
    And mortgage value of the land is $100

    Examples:
      | name                                  |
      | Noord Station / Gare du Nord          |
      | Centraal Station / Gare Centrale      |
      | Buurtspoorwegen / Tramways Vicinaux   |
      | Zuid Station / Gare du Midi           |
