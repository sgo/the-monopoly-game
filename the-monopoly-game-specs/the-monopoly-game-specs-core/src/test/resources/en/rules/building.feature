# mutation-stamp: sha256=488a2871781599023c1f173368377022c360534a51d26591b914de4c9f2a8c41
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-29T13:39:54.468578Z","feature_name":"houses and hotels","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/building.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an agreeable player with a full colour group builds evenly across it when affordable","scenario_hash":"c9c2223376e3ee9dc4dd436e72fae1dd1d0dd844d6233b0f8cb9568204cd8b9d","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:52.883374Z"},{"index":1,"name":"an agreeable player exchanges four houses for a hotel on every street it can afford","scenario_hash":"1645140854a929275bbd2e00a10bc5233ba6509ec66daad7236aec9d7396a629","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:52.883374Z"},{"index":2,"name":"a player sells a house back to the bank at half its price","scenario_hash":"afecee560f1118889839ac15f8db420173ecdcce331b9cb0a01b39c4a9f5022a","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:52.883374Z"},{"index":3,"name":"a player exchanges a hotel back for four houses and half its price in cash","scenario_hash":"821651871e3a196d37a0cd7507b24932134f2bdcf0ef8c24cbdde3bde3d97274","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:52.883374Z"}]}
# acceptance-mutation-manifest-end

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
