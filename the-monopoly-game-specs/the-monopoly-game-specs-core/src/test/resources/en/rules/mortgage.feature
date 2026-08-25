# mutation-stamp: sha256=5818adc203e52f480ef434d3f4f68c716b23447df66770914f9695a69c6548ad
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T11:50:05.140536Z","feature_name":"mortgaging","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/mortgage.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":2,"name":"no rent may be collected for mortgaged land","scenario_hash":"c2f5cdc8229bc3d61edd34dcddfeb60f9fcf611f37e50c845e02fd334576d13f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:39:57.017868Z"},{"index":3,"name":"a monopoly may not charge double rent while any street in the group is mortgaged","scenario_hash":"eccd05429ee99d7f4fa102eba151a8e4558e8b48aed454f4762634f45a3cc360","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:39:57.017868Z"},{"index":4,"name":"a player who will build is refused while any street in the colour group is mortgaged","scenario_hash":"edf1eb31cee2574f5d7dd3ffae9666c214dbf293c4e04a1cd9dc70eeb2148d94","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:39:57.017868Z"},{"index":0,"name":"a player mortgages land to the bank for its mortgage value","scenario_hash":"1ecb2b809aba5ceb1099d37c91a75c0177861ebec65abac5a19e6cef373296e5","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-28T19:39:26.317961Z"},{"index":1,"name":"lifting a mortgage costs the mortgage value plus 10% interest","scenario_hash":"e6c3fd2a313b72ad331a3e777b1cb32407249d50b5347a9e45930c243866e793","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T19:39:26.317961Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: mortgaging

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # mortgage-1
  Scenario Outline: a player mortgages land to the bank for its mortgage value
    Given pawn "dog" owns "<property>"
    And pawn "dog" has $0 to spend
    When pawn "dog" mortgages "<property>"
    Then pawn "dog"'s account balance is $<mortgage_value>
    And the land "<property>" is mortgaged

    Examples:
      | property               | mortgage_value |
      | Diestsestraat Leuven   | 30              |
      | Noord Station          | 100             |
      | Elektriciteitscentrale | 75              |

  # mortgage-2
  Scenario Outline: lifting a mortgage costs the mortgage value plus 10% interest
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_final_balance>
    And the land "Diestsestraat Leuven" is not mortgaged

    Examples:
      | starting_balance | expected_final_balance |
      | 100               | 67                      |

  # mortgage-3
  Scenario Outline: no rent may be collected for mortgaged land
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1500                          | 1500                         |

  # mortgage-4
  Scenario Outline: a monopoly may not charge double rent while any street in the group is mortgaged
    Given pawn "high hat" owns "Rue Grande Dinant"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1496                          | 1504                         |

  # mortgage-5
  Scenario Outline: a player who will build is refused while any street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | houses | expected_final_balance |
      | 0      | 100                     |
