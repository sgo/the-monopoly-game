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
