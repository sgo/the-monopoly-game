# language: en

Feature: station rent

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # station-rent-1
  Scenario Outline: an owner of one station collects rent from a tenant who lands there
    Given pawn "high hat" owns "Noord Station"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Noord Station"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1475                          | 1525                         |

  # station-rent-2
  Scenario Outline: an owner of every station collects rent scaled to the count they hold
    Given pawn "high hat" owns "Noord Station"
    And pawn "high hat" owns "Centraal Station"
    And pawn "high hat" owns "Buurtspoorwegen"
    And pawn "high hat" owns "Zuid Station"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Zuid Station"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1300                          | 1700                         |
