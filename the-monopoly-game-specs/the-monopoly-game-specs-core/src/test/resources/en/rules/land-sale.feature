# language: en

Feature: selling land between players

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # land-sale-1
  Scenario Outline: unimproved land is sold between players at an agreed price
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "dog"'s account balance is $<expected_seller_final_balance>
    And pawn "high hat"'s account balance is $<expected_buyer_final_balance>

    Examples:
      | price | expected_seller_final_balance | expected_buyer_final_balance |
      | 90    | 1590                           | 1410                          |

  # land-sale-2
  Scenario Outline: a colour group with any house built cannot be sold until the houses are sold back to the bank
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $90
    Then pawn "dog" owns "Diestsestraat Leuven"
    And pawn "high hat" does not own "Diestsestraat Leuven"
    And pawn "dog"'s account balance is $<expected_seller_final_balance>
    And pawn "high hat"'s account balance is $<expected_buyer_final_balance>

    Examples:
      | expected_seller_final_balance | expected_buyer_final_balance |
      | 1500                           | 1500                          |
