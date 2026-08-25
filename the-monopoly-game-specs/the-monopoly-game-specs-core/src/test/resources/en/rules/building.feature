# mutation-stamp: sha256=f6daa8a670efec9690331f25ac28f70a3916bc89b5b311cba1fa5c931e924efe
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T07:38:04.646112Z","feature_name":"houses and hotels","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/building.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an agreeable player with a full colour group builds evenly across it when affordable","scenario_hash":"d6fc116d7d638ca3660bdb0e99979ec87a2ae3a22fd184f2bc99a7838fd8d520","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:22:07.894616Z"},{"index":1,"name":"an agreeable player exchanges four houses for a hotel on every street it can afford","scenario_hash":"ec06078d49341ee223641cfdc288c915a60caae063e882b3ebbe6ba7ac29656c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:22:07.894616Z"},{"index":2,"name":"a player sells a house back to the bank at half its price","scenario_hash":"afecee560f1118889839ac15f8db420173ecdcce331b9cb0a01b39c4a9f5022a","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:52.883374Z"},{"index":3,"name":"a player exchanges a hotel back for four houses and half its price in cash","scenario_hash":"821651871e3a196d37a0cd7507b24932134f2bdcf0ef8c24cbdde3bde3d97274","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:52.883374Z"}]}
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
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | strategy | houses | expected_final_balance |
      | Greedo   | 1      | 0                       |
      | Billionaire | 1   | 0                       |

  # building-2
  Scenario Outline: an agreeable player exchanges four houses for a hotel on every street it can afford
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 4 house(s) built
    And the street "Diestsestraat Leuven" has 4 house(s) built
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $700 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has a hotel built
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | strategy | expected_final_balance |
      | Greedo   | 0                       |
      | Billionaire | 0                    |

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
