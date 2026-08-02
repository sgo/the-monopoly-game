# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-02T14:29:41.597972Z","feature_name":"game journal","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/journal.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the journal records game start and initiative","scenario_hash":"2e7a8628431ead85b28e68eb7a6c1c79223c5d8a7b37315aa3a204b4d770f730","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":1,"name":"the journal records a pawn's turn, roll, and movement","scenario_hash":"2f94b9a5c5f2f6549c407bc028a81adc5c302a11da4f9c949bffb4bcdea0064f","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":2,"name":"the journal records a salary collected while passing start","scenario_hash":"fade8b7f9787cac8169e2d7a0ec3b625c6b615955c549b0f58f36ff2769586ae","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":3,"name":"the journal records an unowned-land purchase after the landing movement","scenario_hash":"15d1f69e7a3dc9eb97006158d68f1c7f0f9f7d1eb8165d374b52f0fe8866d199","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":5,"name":"the journal records rent paid after the landing movement","scenario_hash":"c2cf4254e9e76077b2a4dff701220ac02aa2e19a7d0af4ef6ee58f0e8a0dcfe4","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":6,"name":"the journal records rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"ca1fd8201ea675765ee92abce5ef2bdd514a5257870faa7cb9968e2d37be4356","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":7,"name":"the journal records a house built during a player's turn","scenario_hash":"ceb5d071e206bc72ef81059394eb2e8199d6f76679de5060b62d00ec16e68ad0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":8,"name":"the journal records a house sold back to the bank","scenario_hash":"c08e1685f14b09566c50668ecd475023568227c04af9b0fcb54d4ab6a81935d9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":9,"name":"the journal records land being mortgaged","scenario_hash":"6f9feeaf37794c9f44050361cebab29cd7e116c809f58e778839d8d8f53d17d9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":10,"name":"the journal records a mortgage being lifted, including interest paid","scenario_hash":"37fddc6c1e943a6c64d33185599391f2203cfc9f0740ed851abb2acf77b82888","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":11,"name":"the journal records land sold between players","scenario_hash":"f28df5ecbcc6ba68402201dd345b42a67e1251671940ee743a4a12fe77070d19","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":12,"name":"the journal records a sale refused because the colour group has houses built","scenario_hash":"2c400d4c73dcc7bd4a7e93a7b3f3700a03a26e7b072a5fc5dc8e50943cd0ce51","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":13,"name":"the journal records a build refused because a street in the colour group is mortgaged","scenario_hash":"827192584b65f402fc0cc74b08df980ea2d073ff665f1fdf60889f70359455f0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":14,"name":"the journal records a card drawn before the effect it resolves","scenario_hash":"bc27ca16bdeda5e6a1ad0460af415d5f7af8422423c0764fc670c94daccce8e0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":15,"name":"the journal records a tax payment after the landing movement","scenario_hash":"74eb521f23ec7091d0208fc0c851c65032779cd0f2e04ea62a16fcede9fce74e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":16,"name":"the journal records jail entry and its cause","scenario_hash":"6d7b8656f204ec9c0e8fbe67be2fe3262ca7dd522a93715d60d7d5a8e5991b40","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":17,"name":"the journal records jail exit and its method","scenario_hash":"c4394c650e5a39b8b5e45c900c5854598501e038bc52e4c5d84071593f560fa7","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":18,"name":"the journal records landing on Free Parking even though nothing happens","scenario_hash":"dbb3150a6daa7381ec82d4dcc20cfcdd83841154f99d30bf3e010a94a5cf8a55","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":22,"name":"the journal records a card drawn before it pays every other player","scenario_hash":"30f57d39a048adf84c4fdea2c2376990d5515603e4f0bfe427198bc2fc5f6371","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":23,"name":"the journal records a card drawn before it collects from every other player","scenario_hash":"dd40d3685cd2b6b4664621b62225816b50579bec1675e5e756cb10f1a7177209","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":4,"name":"the journal records the winner and price of an auction after the landing movement","scenario_hash":"2270f449276e8c4a76dc3ee65d642dc84efc8720a2d57fa3db479f393387ebc5","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:12.510519Z"}]}
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
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game journal records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game journal records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>)
    And the game journal records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space               | expected_dog_final_position | expected_dog_final_space      |
      | 2         | 3         | 5               | 5                       | 0                           | Start                     | 5                  | Noord Station / Gare du Nord  | 5                            | Noord Station / Gare du Nord  |

  # journal-3
  Scenario Outline: the journal records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>) before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space | expected_dog_final_position | expected_dog_final_space | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | Meir Antwerpen            | 0                  | Start            | 0                            | Start                     | 200        | 200                  |

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

  # journal-8
  Scenario Outline: the journal records a house built during a player's turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game journal records that pawn "dog" builds a house on "Rue Grande Dinant" for $<cost>

    Examples:
      | cost |
      | 50   |

  # journal-9
  Scenario Outline: the journal records a house sold back to the bank
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the game journal records that pawn "dog" sells a house on "Diestsestraat Leuven" for $<price>

    Examples:
      | price |
      | 25    |

  # journal-10
  Scenario Outline: the journal records land being mortgaged
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" mortgages "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" mortgages "Diestsestraat Leuven" for $<value>

    Examples:
      | value |
      | 30    |

  # journal-11
  Scenario Outline: the journal records a mortgage being lifted, including interest paid
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $100 to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" lifts the mortgage on "Diestsestraat Leuven" for $<total> including $<interest> interest

    Examples:
      | total | interest |
      | 33    | 3        |

  # journal-12
  Scenario Outline: the journal records land sold between players
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game journal records that pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<expected_price>

    Examples:
      | price | expected_price |
      | 90    | 90             |

  # journal-13
  Scenario Outline: the journal records a sale refused because the colour group has houses built
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game journal records that pawn "dog" is refused selling "Diestsestraat Leuven" to pawn "high hat" for $<expected_price> because the colour group has houses built

    Examples:
      | price | expected_price |
      | 90    | 90              |

  # journal-14
  Scenario Outline: the journal records a build refused because a street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game journal records that pawn "dog" is refused building a house on "Diestsestraat Leuven" for $<cost> because a street in the colour group is mortgaged

    Examples:
      | cost |
      | 50   |

  # journal-15
  Scenario Outline: the journal records a card drawn before the effect it resolves
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "Boete voor te snel rijden. Betaal M15." before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 15     |

  # journal-16
  Scenario Outline: the journal records a tax payment after the landing movement
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "dog" moves before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 200    |

  # journal-17
  Scenario Outline: the journal records jail entry and its cause
    When pawn "dog" lands on "<space>"
    Then the game journal records that pawn "dog" moves before it records that pawn "dog" is sent to jail from landing on "<space>"

    Examples:
      | space                                 |
      | Naar de Gevangenis / Allez en Prison   |

  # journal-18
  Scenario Outline: the journal records jail exit and its method
    Given pawn "dog" starts in jail
    And pawn "dog" will pay the fine to leave jail
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then the game journal records that pawn "dog" leaves jail by paying the $<fine> fine

    Examples:
      | fine |
      | 50   |

  # journal-19
  Scenario Outline: the journal records landing on Free Parking even though nothing happens
    When pawn "dog" lands on "Gratis Parkeren / Parc Gratuit"
    Then the game journal records that pawn "dog" moves from position <start position> (<start space>) to <position> (<space>)

    Examples:
      | start position | start space                            | position | space                           |
      | 17              | Algemeen Fonds / Caisse de Communauté | 20       | Gratis Parkeren / Parc Gratuit  |

  # journal-20
  Scenario Outline: the journal records a bankruptcy to the bank
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "dog" goes bankrupt to the bank

    Examples:
      | starting balance |
      | 5                 |

  # journal-21
  Scenario Outline: the journal records a bankruptcy to another player
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" goes bankrupt to pawn "high hat"

    Examples:
      | starting balance |
      | 5                 |

  # journal-22
  Scenario Outline: the journal records the game's winner
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" wins the game

    Examples:
      | starting balance |
      | 5                 |

  # journal-23
  Scenario Outline: the journal records a card drawn before it pays every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next chance card will be "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "high hat" $<amount>
    And the game journal records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "iron box" $<amount>

    Examples:
      | amount |
      | 50     |

  # journal-24
  Scenario Outline: the journal records a card drawn before it collects from every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next community chest card will be "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game journal records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "high hat" pays pawn "dog" $<amount>
    And the game journal records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "iron box" pays pawn "dog" $<amount>

    Examples:
      | amount |
      | 10     |

  # journal-25
  Scenario Outline: the journal records a card drawn before the move it causes
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200." before it records that pawn "dog" moves from position <chance_position> (<chance_space>) to <destination_position> (<destination_space>)

    Examples:
      | chance_position | chance_space  | destination_position | destination_space     |
      | 7                | Kans / Chance | 11                    | Rue de Diekirch Arlon |
