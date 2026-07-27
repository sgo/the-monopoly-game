# language: en

Feature: game report

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # report-1
  Scenario Outline: the report narrates game start and initiative in chronological order
    When we play the game
    Then the game report says that the game starts with pawn "dog" before pawn "high hat"
    And the game report says that the game starts before it says that pawn "dog" rolls for initiative
    And the game report says that pawn "dog" rolls <expected_dog_initiative_roll> for initiative before it says that pawn "high hat" rolls <expected_high_hat_initiative_roll> for initiative
    And the game report says that pawn "high hat" rolls <expected_high_hat_initiative_roll> for initiative before it says that pawn "<initiative_winner>" wins initiative
    And the game report says that pawn "<initiative_winner>" wins initiative before it says that pawn "<initiative_winner>" starts a turn

    Examples:
      | expected_dog_initiative_roll | expected_high_hat_initiative_roll | initiative_winner |
      | 10                            | 4                                  | dog               |

  # report-2
  Scenario Outline: the report narrates a turn in chronological order
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" starts a turn before it says that pawn "dog" rolls a total of <dog_roll_total>
    And the game report says that pawn "dog" rolls a total of <expected_dog_roll_total> before it says that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | dog_final_position |
      | 2         | 3         | 5               | 5                       | 0                           | 5                  |

  # report-3
  Scenario Outline: the report narrates a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position> before it says that pawn "dog" collects a salary of $<dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | dog_final_position | dog_salary |
      | 37                 | 1         | 2         | 37                          | 0                  | 200        |
