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
