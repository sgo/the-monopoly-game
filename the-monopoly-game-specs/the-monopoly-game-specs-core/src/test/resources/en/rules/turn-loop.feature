# language: en

Feature: turn-loop

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "iron box" will roll 4 for initiative

  # turn-loop-1
  Scenario Outline: the game plays a turn for every player, each moved by their own rolls
    And pawn "dog" will roll <dog die 1> and <dog die 2> for their turn
    And pawn "iron box" will roll <iron box die 1> and <iron box die 2> for their turn
    When we play the game
    Then pawn "dog" is at position <dog final position>
    And pawn "iron box" is at position <iron box final position>

    Examples:
      | dog die 1 | dog die 2 | dog final position | iron box die 1 | iron box die 2 | iron box final position |
      | 2         | 3         | 5                   | 6              | 5              | 11                       |

  # turn-loop-2
  Scenario Outline: landing on a space produces no economic effect yet
    And with $<starting balance> in pawn "dog"'s account
    And pawn "dog" will roll <die 1> and <die 2> for their turn
    And pawn "iron box" will roll 3 and 5 for their turn
    When we play the game
    Then pawn "dog" is at position <final position>
    And pawn "dog"'s account balance is $<final balance>

    Examples:
      | starting balance | die 1 | die 2 | final position | final balance |
      | 1500              | 1     | 3     | 4              | 1500          |
