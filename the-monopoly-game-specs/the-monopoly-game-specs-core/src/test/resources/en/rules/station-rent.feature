# mutation-stamp: sha256=a81c366604ba752758881e5e279bc905630b4ea82a669c2dcfc0165f46c8b942
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T16:49:39.267784Z","feature_name":"station rent","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/station-rent.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an owner of one station collects rent from a tenant who lands there","scenario_hash":"bb3eb11497aa877d878d3cc916c3deef2cccd88f815aa90f1e4a9295e97876f1","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:40:08.508753Z"},{"index":1,"name":"an owner of every station collects rent scaled to the count they hold","scenario_hash":"2b40355c9ca43160634f769e8a90513bc45b928cd576fab76eb80e6875c0b381","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:40:08.508753Z"}]}
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
    And pawn "high hat" will claim rent for "Noord Station"
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
    And pawn "high hat" will claim rent for "Zuid Station"
    When pawn "dog" lands on "Zuid Station"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1300                          | 1700                         |
