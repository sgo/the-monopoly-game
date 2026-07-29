# mutation-stamp: sha256=39c1dff84960aab9950d7eb93450b3f01c0cdcaac965883b7b151e096b642b77
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-29T07:33:28.673646Z","feature_name":"game report","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/report.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":15,"name":"the report narrates a tax payment after the landing movement","scenario_hash":"0e3b666a172b3e89849ffa36dcea1bbc9867c1a5977ddb739225fc015a2229a9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-29T07:33:28.673646Z"},{"index":14,"name":"the report narrates a card drawn before the effect it resolves","scenario_hash":"c897694bf976d88d0faf503fb1e49634deb136bf87018022a102150fa649fa82","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T22:26:22.786293Z"},{"index":11,"name":"the report narrates land sold between players","scenario_hash":"3ec190057fc1f46f4d0ddd5ac65f1adbc6ce6e9770a770e8fd513511c8413cf2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T20:29:59.522012Z"},{"index":12,"name":"the report narrates a sale refused because the colour group has houses built","scenario_hash":"8e25271fb40ce7b99aba95ecc081c0659bd8adc883c69343fca9a5307ee558ee","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T20:27:50.184446Z"},{"index":13,"name":"the report narrates a build refused because a street in the colour group is mortgaged","scenario_hash":"29abaacfe0693e27001f7842d897e919e808dc320e598ed0006f1b5d6f43742e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T20:27:50.184446Z"},{"index":9,"name":"the report narrates land being mortgaged","scenario_hash":"bc1259ebb9b6cf15e999937929fffab3bec210e3c4fb5dfaeff18853b820218e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T19:39:31.742051Z"},{"index":10,"name":"the report narrates a mortgage being lifted, including interest paid","scenario_hash":"c2fa0c9fcaacccb5e2c56bab0a7f25a7d01874f8ebb711e45e225fe6aa79aae2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T19:39:31.742051Z"},{"index":7,"name":"the report narrates a house built during a player's turn","scenario_hash":"27b0544ccf2d22fb830e323776f7d1e4b5c3f4f841a47804a9fa0e9c7fd9b9a1","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:55.789971Z"},{"index":8,"name":"the report narrates a house sold back to the bank","scenario_hash":"d950c42cfc816ffe29fc30dc0e6fdd351c03b38d44200639839a82e503d85510","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T14:09:55.789971Z"},{"index":6,"name":"the report narrates rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"dd4a387f897b383c043977d1796b32dc4cf909709503344ff45980b507eb6003","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T12:36:51.483249Z"},{"index":5,"name":"the report narrates rent paid after the landing movement","scenario_hash":"2be4c913907199fd1ae2acbb338568fbc772692f9bd5f3d74248570667da4d49","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-28T11:33:16.360512Z"},{"index":3,"name":"the report narrates an unowned-land purchase after the landing movement","scenario_hash":"d1a400e5d400d86c1308f56b1177f9572dd4761e0d99d19a9155557b0824812a","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:14.344219Z"},{"index":4,"name":"the report narrates an auction outcome after the landing movement","scenario_hash":"9fc7a7b5bf759f7b8b819c0087f111ddf5d72ece38ec911a59d62ebaa451977d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:14.344219Z"},{"index":0,"name":"the report narrates game start and initiative in chronological order","scenario_hash":"92f4bedaea4fea6d9e3d0ef660c7c13088907cca3d2ba660701b2f2ddb93291d","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:42:01.058936Z"},{"index":1,"name":"the report narrates a turn in chronological order","scenario_hash":"1e2d8b8a330d2ce730d78a596f6975cab686e4ad9cb154dacbd2e1d380bc7bd5","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:42:01.058936Z"},{"index":2,"name":"the report narrates a salary collected while passing start","scenario_hash":"63ef999e0ebb02ab87354157e3043f3e8d88a6308ab9bd8047d79fbc7c92142e","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T22:42:01.058936Z"}]}
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

  # report-7
  Scenario Outline: the report narrates rent paid for a utility as a multiple of the dice roll that landed there
    And pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then the game report says that pawn "dog" moves before it says that pawn "dog" pays pawn "high hat" $<rent> rent for "Elektriciteitscentrale"

    Examples:
      | rent |
      | 20   |

  # report-8
  Scenario Outline: the report narrates a house built during a player's turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game report says that pawn "dog" builds a house on "Rue Grande Dinant" for $<cost>

    Examples:
      | cost |
      | 50   |

  # report-9
  Scenario Outline: the report narrates a house sold back to the bank
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the game report says that pawn "dog" sells a house on "Diestsestraat Leuven" for $<price>

    Examples:
      | price |
      | 25    |

  # report-10
  Scenario Outline: the report narrates land being mortgaged
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" mortgages "Diestsestraat Leuven"
    Then the game report says that pawn "dog" mortgages "Diestsestraat Leuven" for $<value>

    Examples:
      | value |
      | 30    |

  # report-11
  Scenario Outline: the report narrates a mortgage being lifted, including interest paid
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $100 to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then the game report says that pawn "dog" lifts the mortgage on "Diestsestraat Leuven" for $<total> including $<interest> interest

    Examples:
      | total | interest |
      | 33    | 3        |

  # report-12
  Scenario Outline: the report narrates land sold between players
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game report says that pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<expected_price>

    Examples:
      | price | expected_price |
      | 90    | 90             |

  # report-13
  Scenario Outline: the report narrates a sale refused because the colour group has houses built
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game report says that pawn "dog" is refused selling "Diestsestraat Leuven" to pawn "high hat" for $<expected_price> because the colour group has houses built

    Examples:
      | price | expected_price |
      | 90    | 90              |

  # report-14
  Scenario Outline: the report narrates a build refused because a street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game report says that pawn "dog" is refused building a house on "Diestsestraat Leuven" for $<cost> because a street in the colour group is mortgaged

    Examples:
      | cost |
      | 50   |

  # report-15
  Scenario Outline: the report narrates a card drawn before the effect it resolves
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then the game report says that pawn "dog" draws the chance card "Boete voor te snel rijden. Betaal M15." before it says that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 15     |

  # report-16
  Scenario Outline: the report narrates a tax payment after the landing movement
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 200    |

  # report-17
  Scenario Outline: the report narrates jail entry and its cause
    When pawn "dog" lands on "<space>"
    Then the game report says that pawn "dog" moves before it says that pawn "dog" is sent to jail from landing on "<space>"

    Examples:
      | space                                 |
      | Naar de Gevangenis / Allez en Prison   |

  # report-18
  Scenario Outline: the report narrates jail exit and its method
    Given pawn "dog" starts in jail
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then the game report says that pawn "dog" leaves jail by paying the $<fine> fine

    Examples:
      | fine |
      | 50   |
