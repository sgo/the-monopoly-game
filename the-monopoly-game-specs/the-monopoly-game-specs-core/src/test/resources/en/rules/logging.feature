# language: en

Feature: game logging

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

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
    Then the game log records that pawn "dog" starts a turn
    And the game log records that pawn "dog" rolls a total of <dog_roll_total>
    And the game log records that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>
    And the game log records that pawn "dog" starts a turn before it records that pawn "dog" rolls a total of <expected_dog_roll_total>
    And the game log records that pawn "dog" rolls a total of <expected_dog_roll_total> before it records that pawn "dog" moves from position <expected_dog_start_position> to <expected_dog_final_position>
    And the game log records that pawn "dog" starts its turn before pawn "high hat"

    Examples:
      | dog_die_1 | dog_die_2 | dog_roll_total | expected_dog_roll_total | expected_dog_start_position | dog_final_position | expected_dog_final_position |
      | 2         | 3         | 5               | 5                       | 0                           | 5                  | 5                           |

  # logging-3
  Scenario Outline: the log records a salary collected while passing start
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game log records that pawn "dog" moves from position <expected_dog_start_position> to <dog_final_position>
    And the game log records that pawn "dog" collects a salary of $<dog_salary>
    And the game log records that pawn "dog" moves from position <expected_dog_start_position> to <expected_dog_final_position> before it records that pawn "dog" collects a salary of $<expected_dog_salary>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | expected_dog_start_position | dog_final_position | expected_dog_final_position | dog_salary | expected_dog_salary |
      | 37                 | 1         | 2         | 37                          | 0                  | 0                           | 200        | 200                 |

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
    Then the game log records that pawn "dog" moves from position <start position> to <position>

    Examples:
      | start position | position |
      | 17              | 20       |

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
