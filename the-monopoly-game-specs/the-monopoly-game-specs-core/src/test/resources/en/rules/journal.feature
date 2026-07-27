# mutation-stamp: sha256=86a0c232f94bfef0f1567ed976d84756704deeddfd7d1210f807aa6f43d229e9
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-27T22:41:58.808126Z","feature_name":"game journal","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/journal.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the journal records game start and initiative","scenario_hash":"2e7a8628431ead85b28e68eb7a6c1c79223c5d8a7b37315aa3a204b4d770f730","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:41:58.808126Z"},{"index":1,"name":"the journal records a pawn's turn, roll, and movement","scenario_hash":"ad3d80456f9a3b97c1c7360e238a458631268970ed3bea120c604ea02a9e7d4c","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:41:58.808126Z"},{"index":2,"name":"the journal records a salary collected while passing start","scenario_hash":"bcbcd3a0276fd3869af9dd620b9b0338936d719291a5f3b44434ef49ab84986d","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:41:58.808126Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game journal

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # journal-1
  Scenario Outline: the journal records game start and initiative
    When we play the game
    Then the game journal records that the game starts with pawn "dog" before pawn "high hat"
    And the game journal records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game journal records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game journal records that pawn "<initiative_winner>" wins initiative
    And the game journal records game start before it records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game journal records that pawn "dog" rolls <dog_initiative_roll> for initiative before it records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game journal records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative before it records that pawn "<initiative_winner>" wins initiative
    And the game journal records that pawn "<initiative_winner>" wins initiative before starting a turn

    Examples:
      | dog_initiative_roll | high_hat_initiative_roll | initiative_winner |
      | 10                  | 4                        | dog               |

  # journal-2
  Scenario Outline: the journal records a pawn's turn, roll, and movement
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn
    And the game journal records that pawn "dog" rolls a total of <dog_roll_total>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>
    And the game journal records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game journal records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> to <expected_dog_final_position>
    And the game journal records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | dog_final_position | expected_dog_final_position |
      | 2         | 3         | 5               | 5                       | 0                           | 5                  | 5                           |

  # journal-3
  Scenario Outline: the journal records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>
    And the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> to <expected_dog_final_position> before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | dog_final_position | expected_dog_final_position | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | 0                  | 0                           | 200        | 200                 |
