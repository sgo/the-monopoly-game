# language: en

Feature: street rent

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # rent-1
  Scenario Outline: an owner claims rent when a tenant lands on their unimproved, non-monopoly street
    Given pawn "high hat" owns "<property>"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "<property>"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | property             | expected_tenant_final_balance | expected_owner_final_balance |
      | Diestsestraat Leuven | 1496                          | 1504                         |
      | Rue Grande Dinant    | 1498                          | 1502                         |

  # rent-2
  Scenario Outline: an owner of every street in a colour group charges double rent while unimproved
    Given pawn "high hat" owns "Rue Grande Dinant"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1492                          | 1508                         |

  # rent-3
  Scenario Outline: rent is waived if the owner does not claim it before the tenant lands
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" declines to claim rent for "Diestsestraat Leuven"
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1500                          | 1500                         |
