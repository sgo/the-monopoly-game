# language: en

Feature: turn-loop

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative

  # turn-loop-1
  Scenario Outline: the game plays a turn for every player, each moved by their own rolls
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "high hat" will roll <high_hat_die_1> and <high_hat_die_2> for their turn
    When we play the game
    Then pawn "dog" is at position <dog_final_position>
    And pawn "high hat" is at position <high_hat_final_position>

    Examples:
      | dog_die_1 | dog_die_2 | dog_final_position | high_hat_die_1 | high_hat_die_2 | high_hat_final_position |
      | 2         | 3         | 5                  | 6              | 5              | 11                      |

  # turn-loop-2
  Scenario Outline: landing on a space produces no economic effect yet
    And with $<starting_balance> in pawn "dog"'s account
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And every other player can complete their turn
    When we play the game
    Then pawn "dog" is at position <dog_final_position>
    And pawn "dog"'s account balance is $<final_balance>

    Examples:
      | starting_balance | dog_die_1 | dog_die_2 | dog_final_position | final_balance |
      | 1500             | 1         | 3         | 4                  | 1500          |
