# language: en

Feature: taxes

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend

  # tax-1
  Scenario Outline: landing on Income Tax pays the bank a fixed amount
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1300              |

  # tax-2
  Scenario Outline: landing on Luxury Tax pays the bank a fixed amount
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1400              |
