# mutation-stamp: sha256=61b1f78a91e8fbf2beb4682bb44b9f5177cf6be12ec08336438a00cabce6c3ef
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T12:36:50.054698Z","feature_name":"game journal","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/journal.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":6,"name":"the journal records rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"ca1fd8201ea675765ee92abce5ef2bdd514a5257870faa7cb9968e2d37be4356","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T12:36:50.054698Z"},{"index":5,"name":"the journal records rent paid after the landing movement","scenario_hash":"c2cf4254e9e76077b2a4dff701220ac02aa2e19a7d0af4ef6ee58f0e8a0dcfe4","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T11:33:01.147036Z"},{"index":3,"name":"the journal records an unowned-land purchase after the landing movement","scenario_hash":"15d1f69e7a3dc9eb97006158d68f1c7f0f9f7d1eb8165d374b52f0fe8866d199","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:12.510519Z"},{"index":4,"name":"the journal records the winner and price of an auction after the landing movement","scenario_hash":"2270f449276e8c4a76dc3ee65d642dc84efc8720a2d57fa3db479f393387ebc5","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:12.510519Z"},{"index":0,"name":"the journal records game start and initiative","scenario_hash":"2e7a8628431ead85b28e68eb7a6c1c79223c5d8a7b37315aa3a204b4d770f730","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:41:58.808126Z"},{"index":1,"name":"the journal records a pawn's turn, roll, and movement","scenario_hash":"ad3d80456f9a3b97c1c7360e238a458631268970ed3bea120c604ea02a9e7d4c","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:41:58.808126Z"},{"index":2,"name":"the journal records a salary collected while passing start","scenario_hash":"bcbcd3a0276fd3869af9dd620b9b0338936d719291a5f3b44434ef49ab84986d","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:41:58.808126Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game journal

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # journal-1
  Scenario Outline: the journal records game start and initiative
    When we play the game
    Then the game journal records that the game starts with pawn "dog" before pawn "high hat"
    And the game journal records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game journal records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game journal records that pawn "<initiative_winner>" wins initiative
    And the game journal records game start before it records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game journal records that pawn "dog" rolls <dog_initiative_roll> for initiative before it records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game journal records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative before it records that pawn "<initiative_winner>" wins initiative
    And the game journal records that pawn "<initiative_winner>" wins initiative before starting a turn

    Examples:
      | dog_initiative_roll | high_hat_initiative_roll | initiative_winner |
      | 10                  | 4                        | dog               |

  # journal-2
  Scenario Outline: the journal records a pawn's turn, roll, and movement
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn
    And the game journal records that pawn "dog" rolls a total of <dog_roll_total>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>
    And the game journal records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game journal records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> to <expected_dog_final_position>
    And the game journal records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | dog_final_position | expected_dog_final_position |
      | 2         | 3         | 5               | 5                       | 0                           | 5                  | 5                           |

  # journal-3
  Scenario Outline: the journal records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>
    And the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> to <expected_dog_final_position> before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | dog_final_position | expected_dog_final_position | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | 0                  | 0                           | 200        | 200                 |

  # journal-4
  Scenario Outline: the journal records an unowned-land purchase after the landing movement
    And pawn "dog" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "<property>"
    Then the game journal records that pawn "dog" buys "<property>" for $<purchase_price>
    And the game journal records that pawn "dog" moves before it records that pawn "dog" buys "<property>" for $<expected_purchase_price>

    Examples:
      | property            | purchase_price | expected_purchase_price |
      | Diestsestraat Leuven | 60             | 60                      |

  # journal-5
  Scenario Outline: the journal records the winner and price of an auction after the landing movement
    And pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<auction_price>
    And the game journal records that pawn "dog" moves before it records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<expected_auction_price>

    Examples:
      | dog_bid | high_hat_bid | auction_winner | auction_price | expected_auction_price |
      | 90      | 120          | high hat       | 120           | 120                    |

  # journal-6
  Scenario Outline: the journal records rent paid after the landing movement
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" pays pawn "high hat" $<rent> rent for "Diestsestraat Leuven"
    And the game journal records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Diestsestraat Leuven"

    Examples:
      | rent | expected_rent |
      | 4    | 4             |

  # journal-7
  Scenario Outline: the journal records rent paid for a utility as a multiple of the dice roll that landed there
    And pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then the game journal records that pawn "dog" pays pawn "high hat" $<rent> rent for "Elektriciteitscentrale"
    And the game journal records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Elektriciteitscentrale"

    Examples:
      | rent | expected_rent |
      | 20   | 20            |
