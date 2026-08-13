# language: en

Feature: the billionaire strategy
  A player may follow the "billionaire" strategy: it makes the same decisions
  as "Greedo", but the game opens its account with a much larger starting
  balance. The opening capital is a property of the strategy, applied once
  before the game starts, and replaces the usual $1500 opening balance rather
  than being added to it.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # billionaire-1
  Scenario Outline: a player following the billionaire strategy opens the game with its stated capital
    Given pawn "dog" follows the "billionaire" strategy
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<opening_balance> and a $<reserve> reserve

    Examples:
      | opening_balance | reserve |
      | 57700000        | 0       |

  # billionaire-2
  Scenario Outline: a player following the default Greedo strategy still opens the game with the standard balance
    Given pawn "dog" follows the "Greedo" strategy
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<opening_balance> and a $<reserve> reserve

    Examples:
      | opening_balance | reserve |
      | 1500            | 0       |

  # billionaire-3
  Scenario Outline: a billionaire player still decides like Greedo and buys affordable unowned land
    Given pawn "dog" follows the "billionaire" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | expected_dog_final_balance |
      | 57699940                   |