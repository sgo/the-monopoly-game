# mutation-stamp: sha256=991cd4f0b175c90a27400b258a3dc777943c7d04814a487f228296c2b67c4503
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T22:07:55.981498Z","feature_name":"the billionaire strategy","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/billionaire.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"a player following the billionaire strategy opens the game with its stated capital","scenario_hash":"aea321cf63b81796b987ddbe9b7b7be3747f9d87f60a88296526e022cd87c297","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:22:03.193198Z"},{"index":1,"name":"a player following the default Greedo strategy still opens the game with the standard balance","scenario_hash":"9791dc25d0c71c9d1c9a549b2f883ad80e539d5422b8fd1e51ffd7dd15d74071","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:22:03.193198Z"},{"index":2,"name":"a billionaire player still decides like Greedo and buys affordable unowned land","scenario_hash":"a63653d06eb7a108a73819ed3eeedf1af193dfa8d1cba9a03ceb09dfe4bab90d","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:22:03.193198Z"}]}
# acceptance-mutation-manifest-end

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