# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-04T19:34:00.723403Z","feature_name":"selling property to avoid bankruptcy","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/distressed-sale.feature","background_hash":"9a3c0c411c487ac29cd37776a8a944a1b977e6980fe8a564f53528f8edf6826a","implementation_hash":"unknown","scenarios":[{"index":0,"name":"a debtor mortgages a spare property rather than sell houses off a developed monopoly","scenario_hash":"472a20afd25628a8cde7f902ea711fc2b0f71409ae75ab2515cdd7c43cdd78ba","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":1,"name":"a debtor sells a spare property to a peer who offers more than the bank would lend against it","scenario_hash":"a30874c7239c422dd43d7018ea1a4adc62ae288713139f8daa5a8b80be493812","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":4,"name":"a debtor sells to the monopoly-completing opponent anyway when nothing else avoids bankruptcy","scenario_hash":"02e5a1a676601cb90808322d8056109cddc7797ff114b6b89ed66457f08e9f12","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":7,"name":"a buyer only needs to beat the mortgage value when the debtor has another property to cover the rest","scenario_hash":"a91bccf61dc2c65893f771de8207ea51278318455e82c5cbf39ef6ae543b1d31","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":8,"name":"a second buyer's offer to cover the whole debt pre-empts the debtor needing to sell anything else","scenario_hash":"6ad9a9fce86514d9e4cbb1b58324627b578a7c5a84d540dfdd126653569f5ba8","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":11,"name":"a bid made purely to deny an opponent's monopoly is capped at 35% of the bidder's balance","scenario_hash":"7a0841b0ad166279ad3766bdde75dc89fe9ee589a1f951f8f372f922b0efa1a6","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":12,"name":"a bid that completes the bidder's own monopoly is not capped at 35% and can spend the bidder's whole balance","scenario_hash":"5f524fe6e08f8e266e99a7a01aba2ea3ad7d178c7b8eeccfcabb68fcb637d5ef","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":14,"name":"mortgaging the debtor's other spare properties does not re-attempt a property already sold to a peer","scenario_hash":"068d892f56f143923bcbe5a3a7c9b4d3489bd62f6e1a0205514c986b6bf59cf2","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"},{"index":15,"name":"a debtor sells a spare property to a peer despite holding an unrelated developed monopoly, when the sale does not complete the buyer's group","scenario_hash":"268828589c443c132690a29c89969f186197ffadaab16cf0e68346a1f775aa18","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-04T19:34:00.723403Z"}]}
# acceptance-mutation-manifest-end

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

  # distressed-sale-15
  Scenario Outline: mortgaging the debtor's other spare properties does not re-attempt a property already sold to a peer
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Boulevard Tirou Charleroi"
    And pawn "high hat" owns "Veldstraat Gent"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And pawn "dog" owns "Grote Markt Hasselt"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Boulevard d'Avroy Liège"
    And the land "Grote Markt Hasselt" is mortgaged
    And pawn "dog" owns "Grote Markt Hasselt"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | 0                     | 20                         | 50                          |

  # distressed-sale-16
  Scenario Outline: a debtor sells a spare property to a peer despite holding an unrelated developed monopoly, when the sale does not complete the buyer's group
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "Greedo" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" owns "Place Verte Verviers"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Steenstraat Brugge"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | 0                     | 300                        | 5                           |

  # distressed-sale-17
  Scenario Outline: a peer's nonzero offer below the land's mortgage value is declined in favor of mortgaging to the bank
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "Greedo" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Lippenslaan Knokke"
    And the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | 86                    | 40                         | 76                          |
