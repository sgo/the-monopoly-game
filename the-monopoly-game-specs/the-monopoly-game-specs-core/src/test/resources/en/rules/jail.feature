# mutation-stamp: sha256=032a9688a4102f3b30d8586df8474199a74e4cc1741b8926a4e33321ae65d4a2
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-29T09:24:22.618539Z","feature_name":"jail","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/jail.feature","background_hash":"c6d5cad8f59dab38e8f82651db4579147851aa1f158315a8774f5bc1f6cea7c6","implementation_hash":"unknown","scenarios":[{"index":0,"name":"landing on Go To Jail sends the pawn directly to jail and pays no salary","scenario_hash":"30bf2f4534a9cffb2d1f58d2a378519b3b6e661d17c55b46997313e98ffc1be5","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-29T09:21:46.967830Z"},{"index":1,"name":"landing on the jail space without being sent there is just visiting","scenario_hash":"763b4fcb8bfa1f12046fd4f5404a11345ab5aaa5e0f38824b6ff15596c91090f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T09:21:46.967830Z"},{"index":2,"name":"a strategy that can afford the fine pays it immediately and moves the same turn","scenario_hash":"60249365318b25722e914fde48e60e98cd495fcb1588ded3d3bca3e859d49c65","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T09:21:46.967830Z"},{"index":4,"name":"a player already holding a Get Out of Jail Free card uses it to leave jail without paying","scenario_hash":"62fa307afbe0558d5f078a683329495b611036c75f46e9dc9b5a302ab4a22f7e","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T09:21:46.967830Z"},{"index":5,"name":"a jailed player still collects rent from a street they own","scenario_hash":"35bb8427609c78181db30c986c4a18fb94d80243c25e149f39541aacd1d2c5f7","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T09:21:46.967830Z"}]}
# acceptance-mutation-manifest-end

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
  Scenario: a strategy that cannot afford the fine attempts to roll doubles instead
    Given pawn "dog" starts in jail
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $40 to spend
    And pawn "dog" will roll 3 and 3 for their turn
    When we play the game
    Then pawn "dog" is at position 16
    And pawn "dog"'s account balance is $0

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
