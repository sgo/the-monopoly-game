# language: en

Feature: year limit

  Background:
    Given the official rule set
    And we select 2 players

  # year-limit-1
  Scenario Outline: the game stops itself once a remaining player's age reaches the configured year limit
    Given the game is limited to <year limit> years
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    When we play the game
    Then the game ends because the year limit was reached
    And the game journal records that pawn "dog"'s final age is <year limit> years
    And pawn "dog" is not bankrupt
    And pawn "high hat" is not bankrupt

    Examples:
      | year limit |
      | 1          |

  # year-limit-2
  Scenario Outline: the game does not stop while every remaining player is still below the year limit
    Given the game is limited to <year limit> years
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    And every other player can complete their turn
    When we play the game
    Then the game does not end because the year limit was reached

    Examples:
      | year limit |
      | 2          |

  # year-limit-3
  Scenario: no year limit is the default, and the game is never stopped by one
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    And every other player can complete their turn
    When we play the game
    Then the game does not end because the year limit was reached
