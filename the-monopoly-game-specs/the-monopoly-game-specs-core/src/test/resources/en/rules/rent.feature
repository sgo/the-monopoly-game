# mutation-stamp: sha256=31e4ad24c2bc970b1e16b464ca0a33da77faa39cac89fdd122cf6385c28d6af6
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T22:26:23.311997Z","feature_name":"street rent","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/rent.feature","background_hash":"3eba7406624ca69ea442618544f7523c7122b7ad66c8b2f7f4495508c24d7aca","implementation_hash":"unknown","scenarios":[{"index":3,"name":"rent scales with the number of houses built on the street","scenario_hash":"b653d4ea76cd4a6c5da2b4924638e6338de66b4e5652f08c3639695dba82ed8d","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:57.682117Z"},{"index":4,"name":"a hotel charges the hotel rent printed on the title deed","scenario_hash":"6b294b35f3b3ea1dc63a0b83a83eff38b1eefced1cdbfbb9ec2b62d65d6c3cb1","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:57.682117Z"},{"index":5,"name":"an unimproved street in a partially built-up monopoly still charges double rent","scenario_hash":"38506c6acf14eab390de952a0f2564353ad6ea718cd508a919ad361e6b839911","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:57.682117Z"},{"index":0,"name":"an owner claims rent when a tenant lands on their unimproved, non-monopoly street","scenario_hash":"ab02d69d5c98c6156c9852e62631f2f9c0c4a9f944dc27ccab0f68bfc5212909","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-28T11:33:18.620842Z"},{"index":1,"name":"an owner of every street in a colour group charges double rent while unimproved","scenario_hash":"2c6892a9ec409213e7def3845d5940b9247a823918a547e0a9a80877234d1f4c","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T11:33:18.620842Z"},{"index":2,"name":"rent is waived if the owner does not claim it before the tenant lands","scenario_hash":"d88bc648361ed4f20cf2428614c70160a4902084fcdd4abf7469edace9f3c0ff","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T11:33:18.620842Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: street rent

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # rent-1
  Scenario Outline: an owner claims rent when a tenant lands on their unimproved, non-monopoly street
    Given pawn "high hat" owns "<property>"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "<property>"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | property             | expected_tenant_final_balance | expected_owner_final_balance |
      | Diestsestraat Leuven | 1496                          | 1504                         |
      | Rue Grande Dinant    | 1498                          | 1502                         |

  # rent-2
  Scenario Outline: an owner of every street in a colour group charges double rent while unimproved
    Given pawn "high hat" owns "Rue Grande Dinant"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1492                          | 1508                         |

  # rent-3
  Scenario Outline: rent is waived if the owner does not claim it before the tenant lands
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" declines to claim rent for "Diestsestraat Leuven"
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1500                          | 1500                         |

  # rent-4
  Scenario Outline: rent scales with the number of houses built on the street
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 2 house(s) built
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1440                          | 1560                         |

  # rent-5
  Scenario Outline: a hotel charges the hotel rent printed on the title deed
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1050                          | 1950                         |

  # rent-6
  Scenario Outline: an unimproved street in a partially built-up monopoly still charges double rent
    Given pawn "high hat" owns "Rue Grande Dinant"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 2 house(s) built
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1492                          | 1508                         |
