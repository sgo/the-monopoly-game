# mutation-stamp: sha256=ec074be802556356a4ae38d79adfc7154faf295215a8d1199f9fcf8bae7b0a21
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-28T11:33:16.360512Z","feature_name":"game report","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/report.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":5,"name":"the report narrates rent paid after the landing movement","scenario_hash":"2be4c913907199fd1ae2acbb338568fbc772692f9bd5f3d74248570667da4d49","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T11:33:16.360512Z"},{"index":3,"name":"the report narrates an unowned-land purchase after the landing movement","scenario_hash":"d1a400e5d400d86c1308f56b1177f9572dd4761e0d99d19a9155557b0824812a","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:14.344219Z"},{"index":4,"name":"the report narrates an auction outcome after the landing movement","scenario_hash":"9fc7a7b5bf759f7b8b819c0087f111ddf5d72ece38ec911a59d62ebaa451977d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:14.344219Z"},{"index":0,"name":"the report narrates game start and initiative in chronological order","scenario_hash":"92f4bedaea4fea6d9e3d0ef660c7c13088907cca3d2ba660701b2f2ddb93291d","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:42:01.058936Z"},{"index":1,"name":"the report narrates a turn in chronological order","scenario_hash":"1e2d8b8a330d2ce730d78a596f6975cab686e4ad9cb154dacbd2e1d380bc7bd5","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:42:01.058936Z"},{"index":2,"name":"the report narrates a salary collected while passing start","scenario_hash":"63ef999e0ebb02ab87354157e3043f3e8d88a6308ab9bd8047d79fbc7c92142e","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:42:01.058936Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game report

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # report-1
  Scenario Outline: the report narrates game start and initiative in chronological order
    When we play the game
    Then the game report says that the game starts with pawn "dog" before pawn "high hat"
    And the game report says that the game starts before it says that pawn "dog" rolls for initiative
    And the game report says that pawn "dog" rolls <expected_dog_initiative_roll> for initiative before it says that pawn "high hat" rolls <expected_high_hat_initiative_roll> for initiative
    And the game report says that pawn "high hat" rolls <expected_high_hat_initiative_roll> for initiative before it says that pawn "<initiative_winner>" wins initiative
    And the game report says that pawn "<initiative_winner>" wins initiative before it says that pawn "<initiative_winner>" starts a turn

    Examples:
      | expected_dog_initiative_roll | expected_high_hat_initiative_roll | initiative_winner |
      | 10                            | 4                                  | dog               |

  # report-2
  Scenario Outline: the report narrates a turn in chronological order
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" starts a turn before it says that pawn "dog" rolls a total of <dog_roll_total>
    And the game report says that pawn "dog" rolls a total of <expected_dog_roll_total> before it says that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | dog_final_position |
      | 2         | 3         | 5               | 5                       | 0                           | 5                  |

  # report-3
  Scenario Outline: the report narrates a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position> before it says that pawn "dog" collects a salary of $<dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | dog_final_position | dog_salary |
      | 37                 | 1         | 2         | 37                          | 0                  | 200        |

  # report-4
  Scenario Outline: the report narrates an unowned-land purchase after the landing movement
    And pawn "dog" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "<property>"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" buys "<property>" for $<purchase_price>

    Examples:
      | property            | purchase_price |
      | Diestsestraat Leuven | 60             |

  # report-5
  Scenario Outline: the report narrates an auction outcome after the landing movement
    And pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" moves before it says that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<auction_price>

    Examples:
      | dog_bid | high_hat_bid | auction_winner | auction_price |
      | 90      | 120          | high hat       | 120           |

  # report-6
  Scenario Outline: the report narrates rent paid after the landing movement
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" pays pawn "high hat" $<rent> rent for "Diestsestraat Leuven"

    Examples:
      | rent |
      | 4    |
