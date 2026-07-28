# language: en

Feature: utility rent

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # utility-rent-1
  Scenario Outline: an owner of one utility charges rent as four times the dice roll that landed there
    Given pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1480                          | 1520                         |

  # utility-rent-2
  Scenario Outline: an owner of every utility charges rent as ten times the dice roll that landed there
    Given pawn "dog" starts at position 5
    And pawn "dog" will roll 2 and 5 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" owns "Watermaatschappij"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1430                          | 1570                         |
