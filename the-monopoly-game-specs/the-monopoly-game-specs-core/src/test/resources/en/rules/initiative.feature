# mutation-stamp: sha256=134b45c0355f8545bacf034e77dea4aaf2065cd211a22fa44d602f3f0f344aa0
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-02T13:21:54.638953Z","feature_name":"initiative","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/initiative.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the highest initiative roll goes first, then play continues clockwise","scenario_hash":"68ca59d9dc5622ad8112211f979cf057e3eb720db0cb0cf8cbbca3f6dfb68f17","mutation_count":9,"result":{"Total":9,"Killed":9,"Survived":0,"Errors":0},"tested_at":"2026-07-27T13:10:58.878685Z"},{"index":1,"name":"tied players roll again until one wins initiative","scenario_hash":"4ff40b59da557193165c144294899e476548fe4213568580ced1d3dc655d8c8b","mutation_count":9,"result":{"Total":9,"Killed":9,"Survived":0,"Errors":0},"tested_at":"2026-07-27T13:10:58.878685Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: initiative

  # initiative-1
  Scenario Outline: the highest initiative roll goes first, then play continues clockwise
    Given the official rule set
    And we select 3 players
    And pawn "<pawn 1>" will roll <roll 1> for initiative
    And pawn "<pawn 2>" will roll <roll 2> for initiative
    And pawn "<pawn 3>" will roll <roll 3> for initiative
    When we roll for initiative
    Then pawn "<first turn>" goes first
    And pawn "<first turn>" plays before pawn "<second turn>"
    And pawn "<second turn>" plays before pawn "<third turn>"

    Examples:
      | pawn 1 | roll 1 | pawn 2   | roll 2 | pawn 3   | roll 3 | first turn | second turn | third turn |
      | dog    | 4      | high hat | 10     | iron box | 6      | high hat   | iron box    | dog        |

  # initiative-2
  Scenario Outline: tied players roll again until one wins initiative
    Given the official rule set
    And we select 3 players
    And pawn "<pawn 1>" will roll <roll 1> for initiative
    And pawn "<pawn 2>" will roll <roll 2> for initiative
    And pawn "<pawn 3>" will roll <roll 3> for initiative
    And pawn "<pawn 1>" will roll <retie 1> for initiative
    And pawn "<pawn 2>" will roll <retie 2> for initiative
    When we roll for initiative
    Then pawn "<winner>" goes first

    Examples:
      | pawn 1 | roll 1 | pawn 2   | roll 2 | pawn 3   | roll 3 | retie 1 | retie 2 | winner   |
      | dog    | 8      | high hat | 8      | iron box | 5      | 6       | 9       | high hat |
