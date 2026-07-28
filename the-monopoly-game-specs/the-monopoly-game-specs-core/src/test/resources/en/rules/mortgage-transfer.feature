# language: en

Feature: mortgaged land sold between players

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend

  # mortgage-transfer-1
  Scenario Outline: the buyer of mortgaged land may pay off the mortgage immediately
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "high hat" has $<buyer_starting_balance> to spend
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    And pawn "high hat" lifts the mortgage on "Diestsestraat Leuven"
    Then pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is not mortgaged
    And pawn "dog"'s account balance is $<expected_seller_final_balance>
    And pawn "high hat"'s account balance is $<expected_buyer_final_balance>

    Examples:
      | buyer_starting_balance | price | expected_seller_final_balance | expected_buyer_final_balance |
      | 200                     | 50    | 1550                           | 117                           |

  # mortgage-transfer-2
  Scenario Outline: the buyer of mortgaged land may keep it mortgaged by paying only the interest
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "high hat" has $<buyer_starting_balance> to spend
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    And pawn "high hat" keeps "Diestsestraat Leuven" mortgaged, paying the interest
    Then pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog"'s account balance is $<expected_seller_final_balance>
    And pawn "high hat"'s account balance is $<expected_buyer_final_balance>

    Examples:
      | buyer_starting_balance | price | expected_seller_final_balance | expected_buyer_final_balance |
      | 200                     | 50    | 1550                           | 147                           |
