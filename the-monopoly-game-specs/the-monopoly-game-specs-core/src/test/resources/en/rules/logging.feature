# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-02T14:30:15.046179Z","feature_name":"game logging","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/logging.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":2,"name":"the log records a pawn's turn, roll, and movement","scenario_hash":"7ac58d5128703ec1445b01b7a312461c1a878a5f3ca7927d817c3399cce49e01","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":3,"name":"the log records a salary collected while passing start","scenario_hash":"700e9bab2bb369e062331a4faa912979717ce887d68d7b1933266631f78f289a","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":19,"name":"the log records landing on Free Parking even though nothing happens","scenario_hash":"152d741a90091b911cac0241773663d237f9678e95d21699e85220180ca32c6e","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":23,"name":"the log records a card drawn before it pays every other player","scenario_hash":"a63499f8e082c4f0e6b488d8e8d3f05a817372c810b0be1945384ca7a9974548","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":24,"name":"the log records a card drawn before it collects from every other player","scenario_hash":"882b25b969fefaa0ce4db56412acd4119bba7ad9dc8d7306c0278c7dc2c05491","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:15.046179Z"},{"index":0,"name":"logged event text matches report rendering","scenario_hash":"f835ddb36be9c4b3df983c8fb0d309b4d4900ccb172e1b73b0ad7f91dee23892","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":1,"name":"the log records game start and initiative","scenario_hash":"a2b92a55e28235ba80668a83c039c56dcf6188c05dea393ffd5cdcf8e9e2b996","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":4,"name":"the log records an unowned-land purchase after the landing movement","scenario_hash":"57c0356f0751dd06953b499b4248d1e4a19c215866021f7c4a7404c3b6712f8f","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":6,"name":"the log records rent paid after the landing movement","scenario_hash":"9881712e1dcd55701274b295493370dbaf7b28b749d08a2525c08b9ef30e2a90","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":7,"name":"the log records rent paid for a utility as a multiple of the dice roll that landed there","scenario_hash":"de5a5a24e8fbd7935930a2dbdb8472f756d275116be40fe402e47a50eec7116e","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":8,"name":"the log records a house built during a player's turn","scenario_hash":"4692c79b47e138b5b50c4d3429a9a583c0a1cc4e8883a74528b7fe6362624f9a","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":9,"name":"the log records a house sold back to the bank","scenario_hash":"3cfc0073d188e9170feca30de7767b31a393f3ac1d8193ed39883410a53baaa5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":10,"name":"the log records land being mortgaged","scenario_hash":"e57add729a6e6abc7a6ecdfef35f6c1ef3d88c5c98b645d5b00414e22fbad84f","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":11,"name":"the log records a mortgage being lifted, including interest paid","scenario_hash":"4c8e414e048527a726da52a655359c92d93534d70fcb451a860f7f25112f3ebf","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":12,"name":"the log records land sold between players","scenario_hash":"10eb426dfd6598520acef844cbdac94b51af1184ea4ba1975c3c1084db923f22","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":13,"name":"the log records a sale refused because the colour group has houses built","scenario_hash":"acec232a2c18797a361f9dda2526ddfbbf2c0b88d275c38c9e0bed00a1c71cea","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":14,"name":"the log records a build refused because a street in the colour group is mortgaged","scenario_hash":"fd23a2f64e710b5fa93e07f9ef985b9392b488607bd8dd4f10bc6e176c5a952b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":15,"name":"the log records a card drawn before the effect it resolves","scenario_hash":"65627bc06a4c5b7a80a5e349718afd5e916a35de3c8563f87ecd6360fba51ce6","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":16,"name":"the log records a tax payment after the landing movement","scenario_hash":"285b1540daae2d937dfa0eb9f68c84e03bed6c62bd7982a0ebad33af2821c353","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":17,"name":"the log records jail entry and its cause","scenario_hash":"f4cf0a54beff41bb258129e259e3c1b0423ff2317fcebc240dfb46ab78d247c2","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"},{"index":18,"name":"the log records jail exit and its method","scenario_hash":"67bfb95cd8583adb7f41a95cef2aed5242e117e11d7847e25be50bf215fa597e","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-02T14:30:04.800526Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: game logging

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # log-1
  Scenario Outline: logged event text matches report rendering
    Given a game with an event of type "<event_type>"
    When the event is rendered for the report
    And the event is logged to the Journal
    Then the logged message text is identical to the report's rendered text

    Examples:
      | event_type           |
      | player_buys_property |
      | player_pays_rent     |
      | player_passes_go     |
      | player_draws_card    |

  # logging-1
  Scenario Outline: the log records game start and initiative
    When we play the game
    Then the game log records that the game starts with pawn "dog" before pawn "high hat"
    And the game log records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game log records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game log records that pawn "<initiative_winner>" wins initiative
    And the game log records game start before it records that pawn "dog" rolls <dog_initiative_roll> for initiative
    And the game log records that pawn "dog" rolls <dog_initiative_roll> for initiative before it records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative
    And the game log records that pawn "high hat" rolls <high_hat_initiative_roll> for initiative before it records that pawn "<initiative_winner>" wins initiative
    And the game log records that pawn "<initiative_winner>" wins initiative before starting a turn

    Examples:
      | dog_initiative_roll | high_hat_initiative_roll | initiative_winner |
      | 10                  | 4                        | dog               |

  # logging-2
  Scenario Outline: the log records a pawn's turn, roll, and movement
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game log records that pawn "dog" starts a turn with balance "<dog_starting_balance>"
    And the game log records that pawn "dog" rolls a total of <dog_roll_total>
    And the game log records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game log records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game log records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>)
    And the game log records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space               | expected_dog_final_position | expected_dog_final_space      | dog_starting_balance |
      | 2         | 3         | 5               | 5                       | 0                           | Start                     | 5                  | Noord Station / Gare du Nord  | 5                            | Noord Station / Gare du Nord  | $1500                |

  # logging-3
  Scenario Outline: the log records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game log records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <dog_final_position> (<dog_final_space>)
    And the game log records that pawn "dog" collects a salary of $<dog_salary>
    And the game log records that pawn "dog" moves from position <expected_dog_start_position> (<expected_dog_start_space>) to <expected_dog_final_position> (<expected_dog_final_space>) before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | expected_dog_start_space | dog_final_position | dog_final_space | expected_dog_final_position | expected_dog_final_space | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | Meir Antwerpen            | 0                  | Start            | 0                            | Start                     | 200        | 200                  |

  # logging-4
  Scenario Outline: the log records an unowned-land purchase after the landing movement
    And pawn "dog" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "<property>"
    Then the game log records that pawn "dog" buys "<property>" for $<purchase_price>
    And the game log records that pawn "dog" moves before it records that pawn "dog" buys "<property>" for $<expected_purchase_price>

    Examples:
      | property            | purchase_price | expected_purchase_price |
      | Diestsestraat Leuven | 60             | 60                      |

  # logging-5
  Scenario Outline: the log records the winner and price of an auction after the landing movement
    And pawn "dog" declines the offer for "Diestsestraat Leuven"
    And pawn "dog" will bid $<dog_bid> for "Diestsestraat Leuven" at auction
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<auction_price>
    And the game log records that pawn "dog" moves before it records that pawn "<auction_winner>" wins the auction for "Diestsestraat Leuven" at $<expected_auction_price>

    Examples:
      | dog_bid | high_hat_bid | auction_winner | auction_price | expected_auction_price |
      | 90      | 120          | high hat       | 120           | 120                    |

  # logging-6
  Scenario Outline: the log records rent paid after the landing movement
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" pays pawn "high hat" $<rent> rent for "Diestsestraat Leuven"
    And the game log records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Diestsestraat Leuven"

    Examples:
      | rent | expected_rent |
      | 4    | 4             |

  # logging-7
  Scenario Outline: the log records rent paid for a utility as a multiple of the dice roll that landed there
    And pawn "dog" starts at position 7
    And pawn "dog" will roll 1 and 4 for their turn
    And pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "high hat" follows the "Agree if affordable" strategy
    When we play the game
    Then the game log records that pawn "dog" pays pawn "high hat" $<rent> rent for "Elektriciteitscentrale"
    And the game log records that pawn "dog" moves before it records that pawn "dog" pays pawn "high hat" $<expected_rent> rent for "Elektriciteitscentrale"

    Examples:
      | rent | expected_rent |
      | 20   | 20            |

  # logging-8
  Scenario Outline: the log records a house built during a player's turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game log records that pawn "dog" builds a house on "Rue Grande Dinant" for $<cost>

    Examples:
      | cost |
      | 50   |

  # logging-9
  Scenario Outline: the log records a house sold back to the bank
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has 1 house(s) built
    When pawn "dog" sells a house on "Diestsestraat Leuven" back to the bank
    Then the game log records that pawn "dog" sells a house on "Diestsestraat Leuven" for $<price>

    Examples:
      | price |
      | 25    |

  # logging-10
  Scenario Outline: the log records land being mortgaged
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" mortgages "Diestsestraat Leuven"
    Then the game log records that pawn "dog" mortgages "Diestsestraat Leuven" for $<value>

    Examples:
      | value |
      | 30    |

  # logging-11
  Scenario Outline: the log records a mortgage being lifted, including interest paid
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $100 to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" lifts the mortgage on "Diestsestraat Leuven" for $<total> including $<interest> interest

    Examples:
      | total | interest |
      | 33    | 3        |

  # logging-12
  Scenario Outline: the log records land sold between players
    Given pawn "dog" owns "Diestsestraat Leuven"
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game log records that pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<expected_price>

    Examples:
      | price | expected_price |
      | 90    | 90             |

  # logging-13
  Scenario Outline: the log records a sale refused because the colour group has houses built
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    When pawn "dog" sells "Diestsestraat Leuven" to pawn "high hat" for $<price>
    Then the game log records that pawn "dog" is refused selling "Diestsestraat Leuven" to pawn "high hat" for $<expected_price> because the colour group has houses built

    Examples:
      | price | expected_price |
      | 90    | 90              |

  # logging-14
  Scenario Outline: the log records a build refused because a street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the game log records that pawn "dog" is refused building a house on "Diestsestraat Leuven" for $<cost> because a street in the colour group is mortgaged

    Examples:
      | cost |
      | 50   |

  # logging-15
  Scenario Outline: the log records a card drawn before the effect it resolves
    Given the next chance card will be "Boete voor te snel rijden. Betaal M15."
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" draws the chance card "Boete voor te snel rijden. Betaal M15." before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 15     |

  # logging-16
  Scenario Outline: the log records a tax payment after the landing movement
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "dog" moves before it records that pawn "dog" pays the bank $<amount>

    Examples:
      | amount |
      | 200    |

  # logging-17
  Scenario Outline: the log records jail entry and its cause
    When pawn "dog" lands on "<space>"
    Then the game log records that pawn "dog" moves before it records that pawn "dog" is sent to jail from landing on "<space>"

    Examples:
      | space                                 |
      | Naar de Gevangenis / Allez en Prison   |

  # logging-18
  Scenario Outline: the log records jail exit and its method
    Given pawn "dog" starts in jail
    And pawn "dog" will pay the fine to leave jail
    And pawn "dog" will roll 4 and 6 for their turn
    When we play the game
    Then the game log records that pawn "dog" leaves jail by paying the $<fine> fine

    Examples:
      | fine |
      | 50   |

  # logging-19
  Scenario Outline: the log records landing on Free Parking even though nothing happens
    When pawn "dog" lands on "Gratis Parkeren / Parc Gratuit"
    Then the game log records that pawn "dog" moves from position <start position> (<start space>) to <position> (<space>)

    Examples:
      | start position | start space                            | position | space                           |
      | 17              | Algemeen Fonds / Caisse de Communauté | 20       | Gratis Parkeren / Parc Gratuit  |

  # logging-20
  Scenario Outline: the log records a bankruptcy to the bank
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "dog" goes bankrupt to the bank

    Examples:
      | starting balance |
      | 5                 |

  # logging-21
  Scenario Outline: the log records a bankruptcy to another player
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the street "Diestsestraat Leuven" has a hotel built
    And pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then the game log records that pawn "dog" goes bankrupt to pawn "high hat"

    Examples:
      | starting balance |
      | 5                 |

  # logging-22
  Scenario Outline: the log records the game's winner
    Given pawn "dog" has $<starting balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then the game log records that pawn "high hat" wins the game

    Examples:
      | starting balance |
      | 5                 |

  # logging-23
  Scenario Outline: the log records a card drawn before it pays every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next chance card will be "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50."
    When pawn "dog" lands on "Kans / Chance"
    Then the game log records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "high hat" $<amount>
    And the game log records that pawn "dog" draws the chance card "Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50." before it records that pawn "dog" pays pawn "iron box" $<amount>

    Examples:
      | amount |
      | 50     |

  # logging-24
  Scenario Outline: the log records a card drawn before it collects from every other player
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "iron box" has $1500 to spend
    And the next community chest card will be "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler."
    When pawn "dog" lands on "Algemeen Fonds / Caisse de Communauté"
    Then the game log records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "high hat" pays pawn "dog" $<amount>
    And the game log records that pawn "dog" draws the community chest card "je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen. Je ontvangt M10 van elke speler." before it records that pawn "iron box" pays pawn "dog" $<amount>

    Examples:
      | amount |
      | 10     |
