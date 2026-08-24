# mutation-stamp: sha256=dd618d6f056a2f56dfb825ebc7c57a45a3118e2ea13cbe95b0dad7b45e2cd881
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T22:07:50.107628Z","feature_name":"selling property to avoid bankruptcy","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/distressed-sale.feature","background_hash":"9a3c0c411c487ac29cd37776a8a944a1b977e6980fe8a564f53528f8edf6826a","implementation_hash":"unknown","scenarios":[{"index":0,"name":"a debtor mortgages a spare property rather than sell houses off a developed monopoly","scenario_hash":"472a20afd25628a8cde7f902ea711fc2b0f71409ae75ab2515cdd7c43cdd78ba","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":2,"name":"a debtor mortgages to the bank instead when a peer's offer does not beat the mortgage value","scenario_hash":"f109e6cec14d9008fd937e441b7183c3760d273c39f090b8928143ab1402e723","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":3,"name":"a debtor sells houses rather than sell a peer the property that would complete their monopoly","scenario_hash":"4dc3929e9881b5bfbd814eaee3dc8ca0f975c82a93d0bb72560343d550253472","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":4,"name":"a debtor sells to the monopoly-completing opponent anyway when nothing else avoids bankruptcy","scenario_hash":"383f6b4ae43e35919abb063b1b5c46ae9fb66d7493e8cf296954aa49b9deac3c","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":5,"name":"a debtor's priority table sells the lowest-priority spare property first, regardless of price","scenario_hash":"60ab143572f81f530c94042363780f9c13f4e0994dbeac9e2fc65ace84267d4b","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":6,"name":"a lone buyer's offer for a debtor's only spare property must cover the whole shortfall, not just beat its mortgage value","scenario_hash":"bab60129e3186eea7350e603b20a3742b224a9030712670b8b47db549c0954a0","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":7,"name":"a buyer only needs to beat the mortgage value when the debtor has another property to cover the rest","scenario_hash":"eebfd16499631067fda0cc92ff44530351fe763608c77f318e570c45eb6e2f72","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":8,"name":"an ascending auction's winning bid does not need to cover the whole debt alone; the rest is mortgaged separately","scenario_hash":"3b2d24e7c94ae3d0919f22c0020791209a49b5c59bd311471656a0a0eecb2a02","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":9,"name":"a buyer declines an affordable offer for a property that holds no value for them","scenario_hash":"f21381e756bb2b5e52fd55c15b0cf5e92d1fa354c92f0aa692c075a4e44903d7","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":10,"name":"a buyer who would win the game by the debtor's bankruptcy declines regardless of the property's value","scenario_hash":"e62184706cd154b47985f770b3030389bd934b53fa9003bdbb9585a51f4e9cea","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":12,"name":"a bid that completes the bidder's own monopoly is not capped at 35% and can spend the bidder's whole balance when that much is genuinely needed","scenario_hash":"1c05fbf3ba1f30b94337cd72771f118d87c01a5683d65547fddc708767fc4152","mutation_count":9,"result":{"Total":9,"Killed":9,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":13,"name":"the debtor breaks a tied offer by selling to whichever competing buyer has the lower net worth","scenario_hash":"6107a141cb332fd8a883be5b2a9d542801ab984dc86f2f9e69b4d1e146e09526","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":14,"name":"mortgaging the debtor's other spare properties does not re-attempt a property already sold to a peer","scenario_hash":"5b9b2276f7eb35644eefd0b6160ee5fdb43122d719874670e6fad4fd3d17e31b","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":15,"name":"a debtor sells a spare property to a peer despite holding an unrelated developed monopoly, when the sale does not complete the buyer's group","scenario_hash":"38b66d24242734b1080e4dba1c3cf4608636dc4f9bb390bbabe1bf581b0a8ea8","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":16,"name":"a peer's offer one dollar below the land's mortgage value is declined in favor of mortgaging to the bank","scenario_hash":"97e17e2f8b9851d30189b31d319e9ba1817ba39f15c529f1a3254518318da470","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":18,"name":"a monopoly-completing offer below the land's mortgage value is still declined in favor of mortgaging to the bank","scenario_hash":"c9538a1b76fd5ad89254302f316b1ef2a5c1bff5bfb82225bb2edc1a77c98977","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":20,"name":"settling a partial-coverage sale actually mortgages the debtor's other collateral instead of crediting its value for free","scenario_hash":"10c5bbf4abb35c27521429b5fa69465f8fba37d0180aef453121c228aaff963c","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:26.322496Z"},{"index":1,"name":"a debtor sells a spare property to a peer who offers more than the bank would lend against it","scenario_hash":"7a222cf660551d1b52c949a2f71e6d2ba0397f520224996f6eddfa266538bf2d","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:18.687662Z"},{"index":11,"name":"a bid made purely to deny an opponent's monopoly is capped at 35% of the bidder's balance","scenario_hash":"92187fe10499102181d18778ee00fd0ededd815b5c948e4ca60d6a227c6f1b33","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:18.687662Z"},{"index":17,"name":"a peer's offer that exactly reaches the land's mortgage value is accepted","scenario_hash":"fd1d68108b9915f3a4bd5e82cd67dc97e45de5eb8d2e845326e08d26179298f8","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:18.687662Z"},{"index":19,"name":"a monopoly-completing buyer with ample funds still pays only what the debtor needs, not their whole balance","scenario_hash":"1819556713af27f38753e3777761e4db3bc47c18d61a0c46e008a3aa92ff7f37","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:18.687662Z"}]}
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
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo | 0 | 200 | 0 | 0 |
      | Billionaire | 0 | 200 | 0 | 0 |

  # distressed-sale-3
  Scenario Outline: a debtor mortgages to the bank instead when a peer's offer does not beat the mortgage value
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "<strategy>" strategy, keeping a $<high_hat_reserve> reserve
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Lippenslaan Knokke"
    And the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | high_hat_reserve | expected_dog_final_balance |
      | Greedo | 10 | 95 | 85 | 0 |
      | Billionaire | 10 | 95 | 85 | 0 |

  # distressed-sale-4
  Scenario Outline: a debtor sells houses rather than sell a peer the property that would complete their monopoly
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" owns "Place Verte Verviers"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Lippenslaan Knokke"
    And the street "Rue de Diekirch Arlon" has 0 house(s) built
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 60 | 200 | 10 |
      | Billionaire | 60 | 200 | 10 |

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
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 0 | 200 | 0 |
      | Billionaire | 0 | 200 | 0 |

  # distressed-sale-6
  Scenario Outline: a debtor's priority table sells the lowest-priority spare property first, regardless of price
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the land "Boulevard Tirou Charleroi" is mortgaged
    And the land "Steenstraat Brugge" is not mortgaged
    And pawn "dog" is not bankrupt

    Examples:
      | strategy | dog_starting_balance |
      | Greedo | 40 |
      | Billionaire | 40 |

  # distressed-sale-7
  Scenario Outline: a lone buyer's offer for a debtor's only spare property must cover the whole shortfall, not just beat its mortgage value
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is bankrupt

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance |
      | Greedo | 5 | 92 |
      | Billionaire | 5 | 92 |

  # distressed-sale-8
  Scenario Outline: a buyer only needs to beat the mortgage value when the debtor has another property to cover the rest
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And the land "Boulevard Tirou Charleroi" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 0 | 92 | 40 |
      | Billionaire | 0 | 92 | 40 |

  # distressed-sale-9
  Scenario Outline: an ascending auction's winning bid does not need to cover the whole debt alone; the rest is mortgaged separately
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "iron box" follows the "<strategy>" strategy
    And pawn "iron box" has $<iron_box_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "iron box" owns "Lippenslaan Knokke"
    And the land "Boulevard Tirou Charleroi" is mortgaged
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "iron box"'s account balance is $<expected_iron_box_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance | expected_dog_final_balance | expected_iron_box_final_balance |
      | Greedo | 0 | 100 | 320 | 55 | 215 |
      | Billionaire | 0 | 100 | 320 | 55 | 215 |

  # distressed-sale-10
  Scenario Outline: a buyer declines an affordable offer for a property that holds no value for them
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog" is bankrupt

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance |
      | Greedo | 0 | 1000 |
      | Billionaire | 0 | 1000 |

  # distressed-sale-11
  Scenario Outline: a buyer who would win the game by the debtor's bankruptcy declines regardless of the property's value
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is bankrupt
    And pawn "high hat" wins the game

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance |
      | Greedo | 0 | 1000 |
      | Billionaire | 0 | 1000 |

  # distressed-sale-12
  Scenario Outline: a bid made purely to deny an opponent's monopoly is capped at 35% of the bidder's balance
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "iron box" owns "Rue Royale Tournai"
    And pawn "iron box" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo | 0 | 600 | 0 | 400 |
      | Billionaire | 0 | 600 | 0 | 400 |

  # distressed-sale-13
  Scenario Outline: a bid that completes the bidder's own monopoly is not capped at 35% and can spend the bidder's whole balance when that much is genuinely needed
    And we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo | 0 | 100 | 0 | 0 |
      | Billionaire | 0 | 100 | 0 | 0 |

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
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "iron box" follows the "<strategy>" strategy
    And pawn "iron box" has $<iron_box_starting_balance> to spend
    And pawn "iron box" owns "Diestsestraat Leuven"
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Lippenslaan Knokke"

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | iron_box_starting_balance |
      | Greedo | 0 | 600 | 600 |
      | Billionaire | 0 | 600 | 600 |

  # distressed-sale-15
  Scenario Outline: mortgaging the debtor's other spare properties does not re-attempt a property already sold to a peer
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Boulevard Tirou Charleroi"
    And pawn "high hat" owns "Veldstraat Gent"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And pawn "dog" owns "Grote Markt Hasselt"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Boulevard d'Avroy Liège"
    And the land "Grote Markt Hasselt" is mortgaged
    And pawn "dog" owns "Grote Markt Hasselt"
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 0 | 200 | 90 |
      | Billionaire | 0 | 200 | 90 |

  # distressed-sale-16
  Scenario Outline: a debtor sells a spare property to a peer despite holding an unrelated developed monopoly, when the sale does not complete the buyer's group
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" owns "Place Verte Verviers"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Steenstraat Brugge"
    And the street "Rue de Diekirch Arlon" has 1 house(s) built
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 0 | 300 | 0 |
      | Billionaire | 0 | 300 | 0 |

  # distressed-sale-17
  Scenario Outline: a peer's offer one dollar below the land's mortgage value is declined in favor of mortgaging to the bank
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Lippenslaan Knokke"
    And the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 17 | 239 | 7 |
      | Billionaire | 17 | 239 | 7 |

  # distressed-sale-18
  Scenario Outline: a peer's offer that exactly reaches the land's mortgage value is accepted
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And the land "Lippenslaan Knokke" is not mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo | 10 | 258 | 0 | 168 |
      | Billionaire | 10 | 258 | 0 | 168 |

  # distressed-sale-19
  Scenario Outline: a monopoly-completing offer below the land's mortgage value is still declined in favor of mortgaging to the bank
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue de Diekirch Arlon"
    And pawn "high hat" owns "Bruul Mechelen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Place Verte Verviers"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" owns "Place Verte Verviers"
    And the land "Place Verte Verviers" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 95 | 9 | 75 |
      | Billionaire | 95 | 9 | 75 |

  # distressed-sale-20
  Scenario Outline: a monopoly-completing buyer with ample funds still pays only what the debtor needs, not their whole balance
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "high hat" owns "Rue Royale Tournai"
    And pawn "high hat" owns "Groenplaats Antwerpen"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" owns "Lippenslaan Knokke"
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" owns "Lippenslaan Knokke"
    And pawn "dog"'s account balance is $<expected_dog_final_balance>
    And pawn "high hat"'s account balance is $<expected_high_hat_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance | expected_high_hat_final_balance |
      | Greedo | 90 | 185 | 80 | 95 |
      | Billionaire | 90 | 185 | 80 | 95 |

  # distressed-sale-21
  Scenario Outline: settling a partial-coverage sale actually mortgages the debtor's other collateral instead of crediting its value for free
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "dog" owns "Noord Station"
    And pawn "high hat" follows the "<strategy>" strategy
    And pawn "high hat" has $<high_hat_starting_balance> to spend
    And pawn "dog" has $<dog_starting_balance> to spend
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "high hat" owns "Steenstraat Brugge"
    And pawn "dog" owns "Noord Station"
    And the land "Noord Station" is mortgaged
    And pawn "dog" is not bankrupt
    And pawn "dog"'s account balance is $<expected_dog_final_balance>

    Examples:
      | strategy | dog_starting_balance | high_hat_starting_balance | expected_dog_final_balance |
      | Greedo | 0 | 286 | 0 |
      | Billionaire | 0 | 286 | 0 |
