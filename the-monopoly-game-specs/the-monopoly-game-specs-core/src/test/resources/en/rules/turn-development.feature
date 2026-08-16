# language: en

Feature: development is not limited to whoever won initiative
  Building a house or hotel is a decision every player makes on their own
  turn, the same as buying land or claiming rent — it must not depend on
  who happened to win the initiative roll at the start of the game.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "high hat" will roll 10 for initiative
    And pawn "dog" will roll 4 for initiative
    And every other player can complete their turn

  # turn-development-1
  Scenario Outline: a player who did not win initiative still builds evenly across a full colour group when affordable
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | strategy | houses | expected_final_balance |
      | Greedo   | 1      | 0                       |
