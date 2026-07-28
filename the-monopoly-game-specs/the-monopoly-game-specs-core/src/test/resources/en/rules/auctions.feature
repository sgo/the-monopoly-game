# mutation-stamp: sha256=505833ca5981508ace236eb753f52887c4a8433de287865fd05cac0f6d2c5e86
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T11:45:03.624821Z","feature_name":"auctions for declined land","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/auctions.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the highest auction bid buys land after the landing player declines","scenario_hash":"bcd64daedfa6cbed8bf3609d29e3a883c6bd0681bdc2e8e131644e1292b22a6c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:10.507967Z"},{"index":1,"name":"the player who declined the offer may win the auction","scenario_hash":"bcc547a4b0b3ddcbf2c7b9e5be76fe79708cb4256571efd7cb3788e779368ec3","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:10.507967Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: auctions for declined land

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # auctions-1
  Scenario Outline: the highest auction bid buys land after the landing player declines
    Given pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "<winner>" owns "Diestsestraat Leuven"
    And pawn "<winner>"'s account balance is $<expected_winner_final_balance>

    Examples:
      | dog_bid | high_hat_bid | winner   | expected_winner_final_balance |
      | 90      | 120          | high hat | 1380                          |

  # auctions-2
  Scenario Outline: the player who declined the offer may win the auction
    Given pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "<winner>" owns "Diestsestraat Leuven"
    And pawn "<winner>"'s account balance is $<expected_winner_final_balance>

    Examples:
      | dog_bid | high_hat_bid | winner | expected_winner_final_balance |
      | 140     | 120          | dog    | 1360                          |
