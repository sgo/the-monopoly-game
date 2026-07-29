# language: en

Feature: jail

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend

  # jail-1
  Scenario Outline: landing on Go To Jail sends the pawn directly to jail and pays no salary
    When pawn "dog" lands on "<space>"
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_balance>
    And pawn "dog" is in jail

    Examples:
      | space                                 | position | expected_balance |
      | Naar de Gevangenis / Allez en Prison   | 10       | 1500              |

  # jail-2
  Scenario Outline: landing on the jail space without being sent there is just visiting
    When pawn "dog" lands on "Op Bezoek / Simple Visite"
    Then pawn "dog" is at position <position>
    And pawn "dog"'s account balance is $<expected_balance>
    And pawn "dog" is just visiting

    Examples:
      | position | expected_balance |
      | 10       | 1500              |

  # jail-3
  Scenario Outline: a strategy that can afford the fine pays it immediately and moves the same turn
    Given pawn "dog" starts in jail
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then pawn "dog" is at position <final position>
    And pawn "dog"'s account balance is $<final balance>

    Examples:
      | final position | final balance |
      | 20              | 1450          |

  # jail-4
  Scenario Outline: a strategy that cannot afford the fine attempts to roll doubles instead
    Given pawn "dog" starts in jail
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $<starting balance> to spend
    And pawn "dog" will roll <die> and <die> for their turn
    When we play the game
    Then pawn "dog" is at position <final position>
    And pawn "dog"'s account balance is $<final balance>

    Examples:
      | starting balance | die | final position | final balance |
      | 40                | 3   | 16              | 40            |

  # jail-5
  Scenario Outline: a player already holding a Get Out of Jail Free card uses it to leave jail without paying
    Given pawn "dog" starts in jail
    And pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" will use the Get Out of Jail Free card to leave jail
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then pawn "dog" is at position <final position>
    And pawn "dog"'s account balance is $<final balance>
    And pawn "dog" no longer holds a Get Out of Jail Free card

    Examples:
      | final position | final balance |
      | 20              | 1500          |

  # jail-6
  Scenario Outline: a jailed player still collects rent from a street they own
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" starts in jail
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<tenant balance>
    And pawn "high hat"'s account balance is $<owner balance>

    Examples:
      | tenant balance | owner balance |
      | 1496           | 1504          |
