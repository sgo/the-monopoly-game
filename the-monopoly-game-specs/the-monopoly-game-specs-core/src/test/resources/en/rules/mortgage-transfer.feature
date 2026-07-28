# mutation-stamp: sha256=5445b1142a8fccdc98c4520f6dd0a9d612166ecbf5750c100b7be785a8340881
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T19:39:28.299050Z","feature_name":"mortgaged land sold between players","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/mortgage-transfer.feature","background_hash":"c6d5cad8f59dab38e8f82651db4579147851aa1f158315a8774f5bc1f6cea7c6","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the buyer of mortgaged land may pay off the mortgage immediately","scenario_hash":"678661bfd967379be076a3881c111fac143f54216566a0529481a9d29fec2707","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T19:39:28.299050Z"},{"index":1,"name":"the buyer of mortgaged land may keep it mortgaged by paying only the interest","scenario_hash":"edc9d8f7f9a4d7313daecf1bc6ede52b5c7c6aaabf78c482f2cf7ae8ca1e0c33","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T19:39:28.299050Z"}]}
# acceptance-mutation-manifest-end

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
