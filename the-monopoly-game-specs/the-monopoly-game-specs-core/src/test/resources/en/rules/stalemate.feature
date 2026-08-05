# acceptance-mutation-manifest-begin
# acceptance-mutation-manifest-end

# language: en

Feature: stalemate

  Background:
    Given the official rule set
    And we select 2 players

  # stalemate-1
  Scenario: the stalemate detection threshold is the board's full rental value at maximum development
    Then the stalemate detection threshold is $22790

  # stalemate-2
  Scenario Outline: the game ends in a stalemate once every remaining player's balance clears the threshold
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game ends in a stalemate
    And pawn "dog" is not bankrupt
    And pawn "high hat" is not bankrupt

    Examples:
      | dog_balance | high_hat_balance |
      | 22791       | 22791             |

  # stalemate-3
  Scenario Outline: the game does not end in a stalemate while any remaining player is still at or below the threshold
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game does not end in a stalemate

    Examples:
      | dog_balance | high_hat_balance |
      | 22791       | 22790             |

  # stalemate-4
  Scenario Outline: a lone remaining player still at or below the threshold blocks a stalemate call with more than two players
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    And pawn "iron box"'s account holds $<iron_box_balance>
    When we play the game
    Then the game does not end in a stalemate

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance |
      | 22791       | 22791             | 22790             |

  # stalemate-5
  Scenario Outline: a stalemate is called once every one of more than two remaining players clears the threshold
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    And pawn "iron box"'s account holds $<iron_box_balance>
    When we play the game
    Then the game ends in a stalemate

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance |
      | 22791       | 22791             | 22791             |

  # stalemate-6
  Scenario Outline: a stalemate stops the game outright, not just the round it was first detected in
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play up to 3 rounds
    Then the game journal records that the game ends in a stalemate only once

    Examples:
      | dog_balance | high_hat_balance |
      | 22791       | 22791             |
