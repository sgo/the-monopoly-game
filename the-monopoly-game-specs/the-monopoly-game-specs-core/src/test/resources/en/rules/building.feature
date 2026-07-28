# language: en

Feature: houses and hotels

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # building-1
  Scenario Outline: an agreeable player with a full colour group builds evenly across it when affordable
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | houses | expected_final_balance |
      | 1      | 0                       |

  # building-2
  Scenario Outline: an agreeable player exchanges four houses for a hotel on every street it can afford
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 4 house(s) built
    And the street "Diestsestraat Leuven" has 4 house(s) built
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $700 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has a hotel built
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | expected_final_balance |
      | 0                       |

  # building-3
  Scenario Outline: a player sells a house back to the bank at half its price
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" has $1000 to spend
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | houses | expected_final_balance |
      | 0      | 1025                    |

  # building-4
  Scenario Outline: a player exchanges a hotel back for four houses and half its price in cash
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog" has $1000 to spend
    When pawn "dog" exchanges the hotel on "Diestsestraat Leuven" for houses
    Then the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | houses | expected_final_balance |
      | 4      | 1225                    |
