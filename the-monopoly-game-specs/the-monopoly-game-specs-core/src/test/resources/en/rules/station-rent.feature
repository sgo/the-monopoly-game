# mutation-stamp: sha256=a087a5d84c9186163504492a7e803feffefa04777d6b99326bf2b4de7a536f15
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T22:26:23.838339Z","feature_name":"station rent","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/station-rent.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an owner of one station collects rent from a tenant who lands there","scenario_hash":"b5fff1891ad161959e46066658104675264511f437ee8711b37a715da03c028f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T12:36:53.775140Z"},{"index":1,"name":"an owner of every station collects rent scaled to the count they hold","scenario_hash":"4d9eefff4d19da55396b492e9288ff1524e8fffa0e19cf6abebb6a05fcbf097b","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T12:36:53.775140Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: station rent

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # station-rent-1
  Scenario Outline: an owner of one station collects rent from a tenant who lands there
    Given pawn "high hat" owns "Noord Station"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Noord Station"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1475                          | 1525                         |

  # station-rent-2
  Scenario Outline: an owner of every station collects rent scaled to the count they hold
    Given pawn "high hat" owns "Noord Station"
    And pawn "high hat" owns "Centraal Station"
    And pawn "high hat" owns "Buurtspoorwegen"
    And pawn "high hat" owns "Zuid Station"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Zuid Station"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1300                          | 1700                         |
