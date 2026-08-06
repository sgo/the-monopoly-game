# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-05T05:37:03.391444Z","feature_name":"game journal","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/journal.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":44,"name":"the journal records a card drawn before the bank pays the player directly","scenario_hash":"b7f14f3099501a75da09ccd11251fc13efc675b60b0f71f3411d7850ef2c8e26","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-05T05:37:03.391444Z"},{"index":40,"name":"the journal records the reserve dynamically sized for a near-complete colour monopoly at the start of a turn","scenario_hash":"a6b7c8d249a790fa9abdb234f9327155b3beed62f6ddd794fbe877c757a11bd7","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-04T21:36:05.297048Z"},{"index":3,"name":"the journal records an unowned-land purchase after the landing movement","scenario_hash":"bfeb22c634f1b7747c02cfed8d070004056f5727be03b4f56841377194137bfe","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":5,"name":"the journal records rent paid after the landing movement","scenario_hash":"729b65a730a774a565eff9c40e2d092f83fa9b5b8814460d8daf19ffeefed19f","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":6,"name":"the journal records rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"55de404e33475d2b0003b1cc791d4a82c3927a9e6605ca721e767eb15d5dacf9","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":7,"name":"the journal records a house built during a player's turn","scenario_hash":"57a9eed08dfd7102becc299c391a325af767d81f7f3e3d8805365e29123241f1","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":13,"name":"the journal records a build refused because a street in the colour group is mortgaged","scenario_hash":"6c68747f294495435d828f230572d80138a9e19d000f6c1249962471c0e77a4b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-03T19:37:12.371535Z"},{"index":19,"name":"the journal records a bankruptcy to the bank","scenario_hash":"9a4690aaabd97b99707d3aa8c9fdb08f8e7e34508dbdcd89c6f79b04927eb9d5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":20,"name":"the journal records a bankruptcy to another player","scenario_hash":"8aea23c0c5a6b549c7ae9ca6e3fd0b640e801cff614f0acf27f472f075396e3d","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":21,"name":"the journal records the game's winner","scenario_hash":"97a5787a82dbd16a8352bd18e631baf96b702d384bb3f6ca3025b46fd02dd109","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":25,"name":"the journal records a mortgage forced by an unaffordable debt","scenario_hash":"deb7140946b6b70950be9d8ebb145bf6c60c0e11d809447995f3e7fdcaaaae9e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":26,"name":"the journal records a house sale forced by an unaffordable debt","scenario_hash":"53cce6780f31c6df485b07ff5b545069b3bd139a4e3eaf28f47391ba707f4688","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":27,"name":"the journal records a jailed player staying in jail after failing to roll doubles","scenario_hash":"28b43257b22a013b5316612e9a8868b0d855bc7fe3a941e5c75f8c1abd2d85d0","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":28,"name":"the journal records a jailed player leaving jail by rolling doubles","scenario_hash":"12e9796f87b2029cc27eaadab39718d22c158bff4ed2fb71e35124724fcff50d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":29,"name":"the journal records a jailed player leaving jail with a Get Out of Jail Free card","scenario_hash":"ec31fa01760e5eaeb54ee9bcb6b683ea744ad9c081f350df919ea1f425b0372d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T19:34:19.254756Z"},{"index":24,"name":"the journal records a card drawn before the move it causes","scenario_hash":"d121e12df0db250e665642f7e09430246213f12d4bf7655cdfc00953645bf9c7","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T16:45:15.436363Z"},{"index":0,"name":"the journal records game start and initiative","scenario_hash":"2e7a8628431ead85b28e68eb7a6c1c79223c5d8a7b37315aa3a204b4d770f730","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":1,"name":"the journal records a pawn's turn, roll, and movement","scenario_hash":"2f94b9a5c5f2f6549c407bc028a81adc5c302a11da4f9c949bffb4bcdea0064f","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":2,"name":"the journal records a salary collected while passing start","scenario_hash":"fade8b7f9787cac8169e2d7a0ec3b625c6b615955c549b0f58f36ff2769586ae","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":8,"name":"the journal records a house sold back to the bank","scenario_hash":"c08e1685f14b09566c50668ecd475023568227c04af9b0fcb54d4ab6a81935d9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":9,"name":"the journal records land being mortgaged","scenario_hash":"6f9feeaf37794c9f44050361cebab29cd7e116c809f58e778839d8d8f53d17d9","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":10,"name":"the journal records a mortgage being lifted, including interest paid","scenario_hash":"37fddc6c1e943a6c64d33185599391f2203cfc9f0740ed851abb2acf77b82888","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":11,"name":"the journal records land sold between players","scenario_hash":"f28df5ecbcc6ba68402201dd345b42a67e1251671940ee743a4a12fe77070d19","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":12,"name":"the journal records a sale refused because the colour group has houses built","scenario_hash":"2c400d4c73dcc7bd4a7e93a7b3f3700a03a26e7b072a5fc5dc8e50943cd0ce51","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":14,"name":"the journal records a card drawn before the effect it resolves","scenario_hash":"bc27ca16bdeda5e6a1ad0460af415d5f7af8422423c0764fc670c94daccce8e0","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":15,"name":"the journal records a tax payment after the landing movement","scenario_hash":"74eb521f23ec7091d0208fc0c851c65032779cd0f2e04ea62a16fcede9fce74e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":16,"name":"the journal records jail entry and its cause","scenario_hash":"6d7b8656f204ec9c0e8fbe67be2fe3262ca7dd522a93715d60d7d5a8e5991b40","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":17,"name":"the journal records jail exit and its method","scenario_hash":"c4394c650e5a39b8b5e45c900c5854598501e038bc52e4c5d84071593f560fa7","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":18,"name":"the journal records landing on Free Parking even though nothing happens","scenario_hash":"dbb3150a6daa7381ec82d4dcc20cfcdd83841154f99d30bf3e010a94a5cf8a55","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":22,"name":"the journal records a card drawn before it pays every other player","scenario_hash":"30f57d39a048adf84c4fdea2c2376990d5515603e4f0bfe427198bc2fc5f6371","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":23,"name":"the journal records a card drawn before it collects from every other player","scenario_hash":"dd40d3685cd2b6b4664621b62225816b50579bec1675e5e756cb10f1a7177209","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:29:10.059675Z"},{"index":4,"name":"the journal records the winner and price of an auction after the landing movement","scenario_hash":"2270f449276e8c4a76dc3ee65d642dc84efc8720a2d57fa3db479f393387ebc5","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-28T08:44:12.510519Z"}]}
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
    And pawn "dog" follows the "Greedo" strategy
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
    And pawn "high hat" follows the "Greedo" strategy
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
    And pawn "high hat" follows the "Greedo" strategy
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
    And pawn "dog" follows the "Greedo" strategy
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
    And pawn "dog" follows the "Greedo" strategy
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

  # journal-26
  Scenario Outline: the journal records a mortgage forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $70 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" mortgages "Rue Grande Dinant" for $<value>

    Examples:
      | value |
      | 30    |

  # journal-27
  Scenario Outline: the journal records a house sale forced by an unaffordable debt
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" has $80 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" sells a house on "Rue Grande Dinant" for $<price>

    Examples:
      | price |
      | 25    |

  # journal-28
  Scenario Outline: the journal records a jailed player staying in jail after failing to roll doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game journal records that pawn "dog" stays in jail

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 2         | 5          |

  # journal-29
  Scenario Outline: the journal records a jailed player leaving jail by rolling doubles
    Given pawn "dog" starts in jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game journal records that pawn "dog" leaves jail by rolling doubles

    Examples:
      | first_die | second_die |
      | 3         | 3          |
      | 5         | 5          |

  # journal-30
  Scenario Outline: the journal records a jailed player leaving jail with a Get Out of Jail Free card
    Given pawn "dog" starts in jail
    And pawn "dog" already holds a Get Out of Jail Free card
    And pawn "dog" will use the Get Out of Jail Free card to leave jail
    And pawn "dog" will roll <first_die> and <second_die> for their turn
    When we play the game
    Then the game journal records that pawn "dog" leaves jail using the Get Out of Jail Free card

    Examples:
      | first_die | second_die |
      | 4         | 6          |
      | 3         | 3          |

  # journal-31
  Scenario Outline: the journal records why a player declines to buy land they cannot afford
    Given pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game journal records that pawn "dog" declines to buy "<property>" because it cannot afford the $<price> price

    Examples:
      | property             | dog_starting_balance | high_hat_bid | price |
      | Diestsestraat Leuven | 59                    | 60           | 60    |

  # journal-32
  Scenario Outline: the journal records why a player keeping a reserve declines a purchase that would dip below it
    Given pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then the game journal records that pawn "dog" declines to buy "<property>" because it would drop the balance below the $<reserve> reserve

    Examples:
      | property         | dog_starting_balance | reserve | high_hat_bid |
      | Rue Grande Dinant | 150                  | 96      | 60           |

  # journal-33
  Scenario Outline: the journal records a player's reserve alongside their balance at the start of a turn
    Given pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 1500                 | 0       |
      | 1500                 | 100     |

  # journal-34
  Scenario Outline: the journal records why a player declines to buy a card-driven property they cannot afford
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it cannot afford the $<price> price

    Examples:
      | dog_starting_balance | price |
      | 100                  | 140   |

  # journal-35
  Scenario Outline: the journal records why a player keeping a reserve declines a card-driven purchase that would dip below it
    Given the next chance card will be "Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200."
    And pawn "dog" follows the "Greedo" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" declines to buy "Rue de Diekirch Arlon" because it would drop the balance below the $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 200                  | 65      |

  # journal-36
  Scenario Outline: the journal records a bank-forced auction win during another player's bankruptcy
    Given pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" has $5 to spend
    And pawn "high hat" will bid $<bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" wins the auction for "Diestsestraat Leuven" at $<bid>

    Examples:
      | bid |
      | 10  |

  # journal-37
  Scenario Outline: the journal records land inherited by a creditor when a debtor goes bankrupt to them
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" will claim rent for "Diestsestraat Leuven"
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "high hat" inherits "Rue Grande Dinant" from pawn "dog"

    Examples:
      | starting_balance |
      | 10                |

  # journal-38
  Scenario Outline: the journal records a creditor paying interest to keep an inherited mortgage in place
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<owner_starting_balance> to spend
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "high hat" pays $<interest> interest to keep the mortgage on "Rue Grande Dinant"

    Examples:
      | owner_starting_balance | starting_balance | interest |
      | 0                       | 2                 | 3        |

  # journal-39
  Scenario Outline: the journal records a creditor immediately lifting an inherited mortgage
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "high hat" lifts the mortgage on "Rue Grande Dinant" for $<total> including $<interest> interest

    Examples:
      | starting_balance | total | interest |
      | 10                | 33    | 3        |

  # journal-40
  Scenario Outline: the journal records a decline with no reason when the strategy has no buying policy
    Given pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game journal records that pawn "dog" declines to buy "Diestsestraat Leuven"

    Examples:
      | dog_starting_balance |
      | 100                   |

  # journal-41
  Scenario Outline: the journal records the reserve dynamically sized for a near-complete colour monopoly at the start of a turn
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 1500                  | 60      |

  # journal-42
  Scenario Outline: the journal records a debtor putting a property up for sale and the sole buyer's winning offer
    Given pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" puts "Lippenslaan Knokke" up for sale to avoid bankruptcy
    And the game journal records that pawn "high hat" offers $<expected_bid> for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" wins the distressed sale for "Lippenslaan Knokke" at $<expected_bid>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_bid |
      | 0                     | 200                        | 100           |

  # journal-43
  Scenario Outline: the journal records every $5 raise in a bidding war before the winning offer
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    Given pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "iron box" follows the "Greedo" strategy
    And pawn "iron box" has $<iron_box_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game journal records that pawn "high hat" offers $90 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" offers $90 for "Lippenslaan Knokke" before it records that pawn "iron box" offers $95 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $95 for "Lippenslaan Knokke" before it records that pawn "high hat" offers $100 for "Lippenslaan Knokke"
    And the game journal records that pawn "high hat" offers $100 for "Lippenslaan Knokke" before it records that pawn "iron box" offers $105 for "Lippenslaan Knokke"
    And the game journal records that pawn "iron box" offers $105 for "Lippenslaan Knokke" before it records that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105
    And the game journal records that pawn "iron box" wins the distressed sale for "Lippenslaan Knokke" at $105

    Examples:
      | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance |
      | 0                     | 100                        | 320                        |

  # journal-44
  Scenario Outline: the journal records a near-complete colour group's reserve only while its missing street remains affordable
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "dog" will roll 2 and 3 for their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<dog_starting_balance> and a $<reserve> reserve

    Examples:
      | dog_starting_balance | reserve |
      | 200                   | 160     |
      | 100                   | 0       |

  # journal-45
  Scenario Outline: the journal records a card drawn before the bank pays the player directly
    Given the next chance card will be "De bank betaald je een dividend van M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game journal records that pawn "dog" draws the chance card "De bank betaald je een dividend van M50." before it records that pawn "dog" receives $<amount> from the bank

    Examples:
      | amount |
      | 50     |

  # journal-46
  Scenario Outline: the journal records that the game ends in a stalemate once every remaining player clears the threshold
    Given we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game journal records that the game ends in a stalemate before it records that pawn "dog"'s final balance is $<dog_balance>
    And the game journal records that pawn "dog"'s final balance is $<dog_balance> before it records that pawn "high hat"'s final balance is $<high_hat_balance>

    Examples:
      | dog_balance | high_hat_balance |
      | 25000       | 26000             |

  # journal-47
  Scenario Outline: the journal records that no one bids before it records the resulting mortgage
    Given we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<high_hat_reserve> reserve
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the game journal records that pawn "dog" puts Lippenslaan Knokke up for sale to avoid bankruptcy before it records that pawn "dog" finds no bidder for Lippenslaan Knokke
    And the game journal records that pawn "dog" finds no bidder for Lippenslaan Knokke before it records that pawn "dog" mortgages Lippenslaan Knokke for $<mortgage_value>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | high_hat_reserve | mortgage_value |
      | 10                    | 95                         | 85                | 90               |
