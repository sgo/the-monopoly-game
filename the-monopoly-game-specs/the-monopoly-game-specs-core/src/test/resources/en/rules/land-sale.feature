# mutation-stamp: sha256=c925a48f0ede86bd0af421699e07ce1912961cddf05f911c92bf8182e6cb6929
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T09:38:27.288952Z","feature_name":"selling land between players","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/land-sale.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":0,"name":"unimproved land is sold between players at an agreed price","scenario_hash":"018ef698108742b44176ef664638d4a255c15e4d54b3da073e9a4213c17b44e6","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-28T20:27:45.198732Z"},{"index":1,"name":"a colour group with any house built cannot be sold until the houses are sold back to the bank","scenario_hash":"d045f24e8891f1b76854d56c6d8baeaff5026346481734266af5aa508a5b7447","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T20:27:45.198732Z"}]}
# acceptance-mutation-manifest-end

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
