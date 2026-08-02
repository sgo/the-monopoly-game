# mutation-stamp: sha256=c71265e371d682bbddbcaf977425d2371d56063d3f640e486ba31f4ac1e42f30
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-02T13:21:55.728577Z","feature_name":"free parking","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/free-parking.feature","background_hash":"c6d5cad8f59dab38e8f82651db4579147851aa1f158315a8774f5bc1f6cea7c6","implementation_hash":"unknown","scenarios":[{"index":0,"name":"landing on Free Parking has no penalty and no reward","scenario_hash":"f6a36d0ab853cf6ee45076b76df0768cb63645ee9fb515f7a21f17497d4739ba","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T11:19:19.041433Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: free parking

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend

  # free-parking-1
  Scenario Outline: landing on Free Parking has no penalty and no reward
    When pawn "dog" lands on "Gratis Parkeren / Parc Gratuit"
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | position | expected_balance |
      | 20       | 1500              |
