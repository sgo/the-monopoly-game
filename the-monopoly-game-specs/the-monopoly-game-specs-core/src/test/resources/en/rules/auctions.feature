# language: en

Feature: auctions for declined land

  # An auction ascends from the land's mortgage value in $5 steps until only one
  # bidder is still willing to raise; "will bid $X at auction" fixes a pawn's
  # ceiling for that ascent rather than a flat winning amount, so the price paid
  # is often below any bidder's own ceiling.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # auctions-1
  Scenario Outline: the highest auction ceiling buys land after the landing player declines, at the price it took to outlast the loser
    Given pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "<winner>" owns "Diestsestraat Leuven"
    And pawn "<winner>"'s account balance is $<expected_winner_final_balance>

    Examples:
      | dog_bid | high_hat_bid | winner   | expected_winner_final_balance |
      | 90      | 120          | high hat | 1405                          |

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
      | 140     | 120          | dog    | 1375                          |

  # auctions-3
  Scenario Outline: neither player has any strategic interest in a middling-priority property, so it remains with the bank
    Given pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $40 to spend
    And pawn "high hat" follows the "<strategy>" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" does not own "Diestsestraat Leuven"
    And pawn "high hat" does not own "Diestsestraat Leuven"

    Examples:
      | strategy    |
      | Greedo      |
      | Billionaire |

  # auctions-4
  Scenario Outline: a sole bidder denying a highest-priority monopoly wins at the land's mortgage value, never at its own bidding cap
    Given pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $40 to spend
    And pawn "high hat" follows the "<strategy>" strategy
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "high hat" owns "Steenstraat Brugge"
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy    | expected_high_hat_final_balance |
      | Greedo      | 1450                             |
      | Billionaire | 57699950                         |
