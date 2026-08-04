# language: en

Feature: selling property to avoid bankruptcy

  Background:
    Given the official rule set

  # distressed-sale-1
  Scenario Outline: a debtor mortgages a spare property rather than sell houses off a developed monopoly
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the street "Rue Grande Dinant" has 1 house(s) built
    And the street "Diestsestraat Leuven" has 1 house(s) built
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the street "Rue Grande Dinant" has 1 house(s) built
    And the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | expected_dog_final_balance |
      | 20                    | 10                          |

  # distressed-sale-2
  Scenario Outline: a debtor sells a spare property to a peer who offers more than the bank would lend against it
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | 0                     | 200                        | 100                         | 0                                 |

  # distressed-sale-3
  Scenario Outline: a debtor mortgages to the bank instead when a peer's offer does not beat the mortgage value
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "Greedo" strategy, keeping a $<high_hat_reserve> reserve
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Lippenslaan Knokke"
    And the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | high_hat_reserve | expected_dog_final_balance |
      | 10                    | 95                         | 85                | 0                           |

  # distressed-sale-4
  Scenario Outline: a debtor sells houses rather than sell a peer the property that would complete their monopoly
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" owns "Place Verte Verviers"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Lippenslaan Knokke"
    And the street "Rue de Diekirch Arlon" has 0 house(s) built
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | 60                    | 200                        | 10                          |

  # distressed-sale-5
  Scenario Outline: a debtor sells to the monopoly-completing opponent anyway when nothing else avoids bankruptcy
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | 0                     | 200                        | 100                         |

  # distressed-sale-6
  Scenario Outline: a debtor's priority table sells the lowest-priority spare property first, regardless of price
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the land "Boulevard Tirou Charleroi" is mortgaged
    And the land "Steenstraat Brugge" is not mortgaged
    And pawn "dog" is not bankrupt

    Examples:
      | dog_starting_balance |
      | 40                    |

  # distressed-sale-7
  Scenario Outline: a lone buyer's offer for a debtor's only spare property must cover the whole shortfall, not just beat its mortgage value
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is bankrupt

    Examples:
      | dog_starting_balance | high_hat_starting_balance |
      | 5                     | 92                         |

  # distressed-sale-8
  Scenario Outline: a buyer only needs to beat the mortgage value when the debtor has another property to cover the rest
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And the land "Boulevard Tirou Charleroi" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | 0                     | 92                         | 42                          |

  # distressed-sale-9
  Scenario Outline: a second buyer's offer to cover the whole debt pre-empts the debtor needing to sell anything else
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "Greedo" strategy
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
    Then pawn "iron box" owns "Lippenslaan Knokke"
    And the land "Boulevard Tirou Charleroi" is not mortgaged
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "iron box"'s account balance is $<expected_iron_box_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance | expected_dog_final_balance | expected_iron_box_final_balance |
      | 0                     | 100                        | 320                        | 55                          | 215                               |

  # distressed-sale-10
  Scenario Outline: a buyer declines an affordable offer for a property that holds no value for them
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog" is bankrupt

    Examples:
      | dog_starting_balance | high_hat_starting_balance |
      | 0                     | 1000                       |

  # distressed-sale-11
  Scenario Outline: a buyer who would win the game by the debtor's bankruptcy declines regardless of the property's value
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is bankrupt
    And pawn "high hat" wins the game

    Examples:
      | dog_starting_balance | high_hat_starting_balance |
      | 0                     | 1000                       |

  # distressed-sale-12
  Scenario Outline: a bid made purely to deny an opponent's monopoly is capped at 35% of the bidder's balance
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "iron box" owns "Rue Royale Tournai"
    And pawn "iron box" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | 0                     | 600                        | 10                          | 390                               |

  # distressed-sale-13
  Scenario Outline: a bid that completes the bidder's own monopoly is not capped at 35% and can spend the bidder's whole balance
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | 50                    | 300                        | 250                         | 0                                 |

  # distressed-sale-14
  Scenario Outline: the debtor breaks a tied offer by selling to whichever competing buyer has the lower net worth
    And we select 4 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 3 for initiative
    And pawn "racecar" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "racecar" owns "Rue Royale Tournai"
    And pawn "racecar" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "iron box" follows the "Greedo" strategy
    And pawn "iron box" has $<iron_box_starting_balance> to spend
    And pawn "iron box" owns "Diestsestraat Leuven"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Lippenslaan Knokke"

    Examples:
      | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance |
      | 0                     | 600                        | 600                        |
