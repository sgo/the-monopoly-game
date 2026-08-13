# language: en

Feature: buying unowned land

  # A property auction (see auctions.feature) ascends from the land's mortgage
  # value; "will bid $X at auction" fixes a pawn's ceiling for that ascent, not
  # a flat winning amount, so the price paid is often below the ceiling.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # buying-land-1
  Scenario Outline: an agreeable player buys affordable unowned land at its listed price
    Given pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "<property>"
    Then pawn "dog" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | property                    | dog_starting_balance | expected_dog_final_balance |
      | Greedo   | Diestsestraat Leuven         | 1500                 | 1440                       |
      | Billionaire | Diestsestraat Leuven         | 1500                 | 1440                       |
      | Greedo   | Noord Station                | 1500                 | 1300                       |
      | Billionaire | Noord Station                | 1500                 | 1300                       |
      | Greedo   | Elektriciteitscentrale       | 1500                 | 1350                       |
      | Billionaire | Elektriciteitscentrale       | 1500                 | 1350                       |

  # buying-land-2
  Scenario Outline: an agreeable player declines unowned land they cannot afford
    Given pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then pawn "dog" does not own "<property>"
    And pawn "high hat" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | property            | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | Diestsestraat Leuven | 59                    | 60           | 59                         | 1470                            |
      | Billionaire | Diestsestraat Leuven | 59                  | 60           | 59                         | 1470                            |

  # buying-land-3
  Scenario Outline: an agreeable player keeping a reserve declines land that would dip below it
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then pawn "dog" does not own "<property>"
    And pawn "high hat" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | property         | dog_starting_balance | reserve | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | Rue Grande Dinant | 150                  | 96      | 60           | 150                        | 1470                             |
      | Billionaire | Rue Grande Dinant | 150                | 96      | 60           | 150                        | 1470                             |

  # buying-land-4
  Scenario Outline: an agreeable player keeping a reserve buys land that would leave at least the reserve behind
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "<property>" at auction
    When pawn "dog" lands on "<property>"
    Then pawn "dog" owns "<property>"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | property         | dog_starting_balance | reserve | high_hat_bid | expected_dog_final_balance |
      | Greedo   | Rue Grande Dinant | 161                  | 100     | 110          | 101                         |
      | Billionaire | Rue Grande Dinant | 161                | 100     | 110          | 101                         |
      | Greedo   | Rue Grande Dinant | 160                  | 100     | 110          | 100                         |
      | Billionaire | Rue Grande Dinant | 160                | 100     | 110          | 100                         |

  # buying-land-5
  Scenario Outline: an agreeable player keeping a reserve still respects it for a utility nobody owns yet
    Given pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Watermaatschappij" at auction
    When pawn "dog" lands on "Watermaatschappij"
    Then pawn "dog" does not own "Watermaatschappij"
    And pawn "high hat" owns "Watermaatschappij"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 56      | 200                  | 150          | 200                        | 1425                             |
      | Billionaire | 56   | 200                  | 150          | 200                        | 1425                             |

  # buying-land-6
  Scenario Outline: an agreeable player keeping a reserve buys a utility anyway to deny another player a monopoly on them
    Given pawn "high hat" owns "Elektriciteitscentrale"
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Watermaatschappij"
    Then pawn "dog" owns "Watermaatschappij"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | expected_dog_final_balance |
      | Greedo   | 100     | 200                  | 50                          |
      | Billionaire | 100  | 200                  | 50                          |

  # buying-land-7
  Scenario Outline: an agreeable player keeping a reserve buys a utility anyway to complete their own monopoly on them
    Given pawn "dog" owns "Elektriciteitscentrale"
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Watermaatschappij"
    Then pawn "dog" owns "Watermaatschappij"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | expected_dog_final_balance |
      | Greedo   | 100     | 200                  | 50                          |
      | Billionaire | 100  | 200                  | 50                          |

  # buying-land-8
  Scenario Outline: an agreeable player one street away from a colour monopoly reserves that street's price
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" does not own "Steenstraat Brugge"
    And pawn "high hat" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 150                   | 100          | 150                         | 1445                             |
      | Billionaire | 150                | 100          | 150                         | 1445                             |

  # buying-land-9
  Scenario Outline: an agreeable player one street away from a colour monopoly still buys land that would leave at least that street's price behind
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 161                   | 200          | 61                          |
      | Billionaire | 161                | 200          | 61                          |
      | Greedo   | 160                   | 200          | 60                          |
      | Billionaire | 160                | 200          | 60                          |

  # buying-land-10
  Scenario Outline: an agreeable player one street away from completing two colour groups reserves only the pricier missing street
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 280                   | 330          | 180                         |
      | Billionaire | 280                | 330          | 180                         |

  # buying-land-11
  Scenario Outline: an agreeable player one street away from completing two colour groups still declines land that would dip below the pricier missing street
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" does not own "Steenstraat Brugge"
    And pawn "high hat" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 250                   | 100          | 250                         | 1410                             |
      | Billionaire | 250                | 100          | 250                         | 1410                             |

  # buying-land-12
  Scenario Outline: an agreeable player falls back to a cheaper reachable missing street within the same priority tier when the pricier one is unaffordable
    Given pawn "dog" owns "Rue St-Léonard Liège"
    And pawn "dog" owns "Lange Steenstraat Kortrijk"
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" owns "Place du Monument Spa"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" does not own "Diestsestraat Leuven"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 150                   | 60           | 150                         | 1470                             |
      | Billionaire | 150                | 60           | 150                         | 1470                             |

  # buying-land-13
  Scenario Outline: an agreeable player is not blocked by an unaffordable missing street once a cheaper reachable street in the same priority tier sets the reserve instead
    Given pawn "dog" owns "Rue St-Léonard Liège"
    And pawn "dog" owns "Lange Steenstraat Kortrijk"
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" owns "Place du Monument Spa"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Diestsestraat Leuven" at auction
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 200                   | 250          | 140                         |
      | Billionaire | 200                | 250          | 140                         |

  # buying-land-14
  Scenario Outline: an agreeable player with no reachable near-complete monopoly still respects their configured reserve
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" does not own "Steenstraat Brugge"
    And pawn "high hat" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 30      | 120                   | 100          | 120                         | 1450                             |
      | Billionaire | 30   | 120                   | 100          | 120                         | 1450                             |

  # buying-land-15
  Scenario Outline: an agreeable player with no reachable near-complete monopoly is not blocked by an unaffordable missing street once the configured reserve is small enough
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 30      | 150                   | 200          | 50                          |
      | Billionaire | 30   | 150                   | 200          | 50                          |

  # buying-land-16
  Scenario Outline: an agreeable player one station away from completing the set reserves that station's price
    Given pawn "dog" owns "Noord Station"
    And pawn "dog" owns "Centraal Station"
    And pawn "dog" owns "Buurtspoorwegen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Steenstraat Brugge" at auction
    When pawn "dog" lands on "Steenstraat Brugge"
    Then pawn "dog" does not own "Steenstraat Brugge"
    And pawn "high hat" owns "Steenstraat Brugge"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 250                   | 100          | 250                         | 1410                             |
      | Billionaire | 250                | 100          | 250                         | 1410                             |

  # buying-land-17
  Scenario Outline: an agreeable player reserves a middle-priority colour group's missing street over a pricier missing station in the lowest tier
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Noord Station"
    And pawn "dog" owns "Centraal Station"
    And pawn "dog" owns "Buurtspoorwegen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Rue de Diekirch Arlon" at auction
    When pawn "dog" lands on "Rue de Diekirch Arlon"
    Then pawn "dog" does not own "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Rue de Diekirch Arlon"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 150                   | 100          | 150                         | 1430                             |
      | Billionaire | 150                | 100          | 150                         | 1430                             |

  # buying-land-18
  Scenario Outline: an agreeable player still buys into a colour group another player already blocks, provided the reserve is maintained
    Given pawn "high hat" owns "Grote Markt Hasselt"
    And pawn "high hat" follows the "Greedo" strategy, keeping a $1100 reserve
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Place de l'Ange Namur"
    Then pawn "dog" owns "Place de l'Ange Namur"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | expected_dog_final_balance |
      | Greedo   | 40      | 300                  | 40                          |
      | Billionaire | 40   | 300                  | 40                          |

  # buying-land-19
  Scenario Outline: an agreeable player does not buy into a colour group another player already blocks if doing so would dip below the reserve, and a lone-owning bystander has no auction interest in a second piece either
    Given pawn "high hat" owns "Grote Markt Hasselt"
    And pawn "high hat" follows the "Greedo" strategy, keeping a $1000 reserve
    And pawn "dog" follows the "<strategy>" strategy, keeping a $<reserve> reserve
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Place de l'Ange Namur"
    Then pawn "dog" does not own "Place de l'Ange Namur"
    And pawn "high hat" does not own "Place de l'Ange Namur"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | reserve | dog_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 40      | 290                   | 290                         | 1500                             |
      | Billionaire | 40   | 290                   | 290                         | 1500                             |

  # buying-land-20
  Scenario Outline: an agreeable player is not limited to a pricier missing station's reserve when a middle-priority colour group's cheaper missing street applies instead
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Noord Station"
    And pawn "dog" owns "Centraal Station"
    And pawn "dog" owns "Buurtspoorwegen"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Rue de Diekirch Arlon" at auction
    When pawn "dog" lands on "Rue de Diekirch Arlon"
    Then pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 200                   | 250          | 60                          |
      | Billionaire | 200                | 250          | 60                          |

  # buying-land-21
  Scenario Outline: an agreeable player reserves a highest-priority colour group's missing street over a pricier missing street in a middle-priority group
    Given pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" owns "Place du Monument Spa"
    And pawn "dog" owns "Grote Markt Hasselt"
    And pawn "dog" owns "Place de l'Ange Namur"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Rue de Diekirch Arlon" at auction
    When pawn "dog" lands on "Rue de Diekirch Arlon"
    Then pawn "dog" does not own "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Rue de Diekirch Arlon"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo   | 150                   | 75           | 150                         | 1430                             |
      | Billionaire | 150                | 75           | 150                         | 1430                             |

  # buying-land-22
  Scenario Outline: an agreeable player is not limited to a pricier middle-priority group's reserve when a highest-priority group's cheaper missing street applies instead
    Given pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" owns "Place du Monument Spa"
    And pawn "dog" owns "Grote Markt Hasselt"
    And pawn "dog" owns "Place de l'Ange Namur"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Rue de Diekirch Arlon" at auction
    When pawn "dog" lands on "Rue de Diekirch Arlon"
    Then pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 270                   | 320          | 130                         |
      | Billionaire | 270                | 320          | 130                         |

  # buying-land-23
  Scenario Outline: an agreeable player reserves a highest-priority colour group's missing street over a pricier missing street in the lowest tier
    Given pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" owns "Place du Monument Spa"
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $<dog_starting_balance> to spend
    And pawn "high hat" will bid $<high_hat_bid> for "Rue de Diekirch Arlon" at auction
    When pawn "dog" lands on "Rue de Diekirch Arlon"
    Then pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_bid | expected_dog_final_balance |
      | Greedo   | 270                   | 320          | 130                         |
      | Billionaire | 270                | 320          | 130                         |
