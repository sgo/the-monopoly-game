# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-02T21:17:02.900188Z","feature_name":"buying unowned land","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/buying-land.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an agreeable player buys affordable unowned land at its listed price","scenario_hash":"bcde8db3de026ff5d1eae696a62877bfb01f481b44ee6582b4e42534a923cb9f","mutation_count":9,"result":{"Total":9,"Killed":9,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:08.645808Z"},{"index":1,"name":"an agreeable player declines unowned land they cannot afford","scenario_hash":"a4b54dd1baf6ff60342edec3831281febf6adc984b85f05e4b4ea4a6ffe22cf3","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:08.645808Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: buying unowned land

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # buying-land-1
  Scenario Outline: an agreeable player buys affordable unowned land at its listed price
    Given pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "<property>"
    Then pawn "dog" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | property                    | dog_starting_balance | expected_dog_final_balance |
      | Diestsestraat Leuven         | 1500                 | 1440                       |
      | Noord Station                | 1500                 | 1300                       |
      | Elektriciteitscentrale       | 1500                 | 1350                       |

  # buying-land-2
  Scenario Outline: an agreeable player declines unowned land they cannot afford
    Given pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then pawn "dog" does not own "<property>"
    And pawn "high hat" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | property            | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Diestsestraat Leuven | 59                    | 60           | 59                         | 1440                            |

  # buying-land-3
  Scenario Outline: an agreeable player keeping a reserve declines land that would dip below it
    Given pawn "dog" follows the "Agree if affordable" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then pawn "dog" does not own "<property>"
    And pawn "high hat" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | property         | dog_starting_balance | reserve | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Rue Grande Dinant | 150                  | 100     | 60           | 150                        | 1440                             |

  # buying-land-4
  Scenario Outline: an agreeable player keeping a reserve buys land that would leave at least the reserve behind
    Given pawn "dog" follows the "Agree if affordable" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "<property>"
    Then pawn "dog" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | property         | dog_starting_balance | reserve | expected_dog_final_balance |
      | Rue Grande Dinant | 200                  | 100     | 140                         |
      | Rue Grande Dinant | 160                  | 100     | 100                         |

  # buying-land-5
  Scenario Outline: an agreeable player keeping a reserve still respects it for a utility nobody owns yet
    Given pawn "dog" follows the "Agree if affordable" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Watermaatschappij" at auction
    When pawn "dog" lands on "Watermaatschappij"
    Then pawn "dog" does not own "Watermaatschappij"
    And pawn "high hat" owns "Watermaatschappij"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | reserve | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | 100     | 200                  | 150          | 200                        | 1350                             |

  # buying-land-6
  Scenario Outline: an agreeable player keeping a reserve buys a utility anyway to deny another player a monopoly on them
    Given pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "dog" follows the "Agree if affordable" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Watermaatschappij"
    Then pawn "dog" owns "Watermaatschappij"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | reserve | dog_starting_balance | expected_dog_final_balance |
      | 100     | 200                  | 50                          |

  # buying-land-7
  Scenario Outline: an agreeable player keeping a reserve buys a utility anyway to complete their own monopoly on them
    Given pawn "dog" owns "Elektriciteitscentrale"
    And pawn "dog" follows the "Agree if affordable" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Watermaatschappij"
    Then pawn "dog" owns "Watermaatschappij"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | reserve | dog_starting_balance | expected_dog_final_balance |
      | 100     | 200                  | 50                          |
