# mutation-stamp: sha256=394952dd249b6c93bac1c2b69cc465b81f227c358ba806faa1bff676cc86f328
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T14:09:58.745377Z","feature_name":"utility rent","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/utility-rent.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an owner of one utility charges rent as four times the dice roll that landed there","scenario_hash":"f9b2c5db4d6eefddfb114cae90d65be451f33104106d152d7c8313b78590bfae","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T12:36:55.508104Z"},{"index":1,"name":"an owner of every utility charges rent as ten times the dice roll that landed there","scenario_hash":"d93887799bd270de90a4ad17f60d3d337cf085b905e181ebbeec673037367903","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T12:36:55.508104Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: utility rent

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # utility-rent-1
  Scenario Outline: an owner of one utility charges rent as four times the dice roll that landed there
    Given pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1480                          | 1520                         |

  # utility-rent-2
  Scenario Outline: an owner of every utility charges rent as ten times the dice roll that landed there
    Given pawn "dog" starts at position 5
    And pawn "dog" will roll 2 and 5 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" owns "Watermaatschappij"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1430                          | 1570                         |
