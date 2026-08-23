# mutation-stamp: sha256=118ba058e7ae6c541015f5f617eb47daf476c92e2315e97facfae75c9f5ec6a9
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-23T16:17:01.154476Z","feature_name":"buying unowned land","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/buying-land.feature","background_hash":"f9cad8a167d25a58781606f42a70845a9afe8a18f1f63bed35985499ab19f099","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an agreeable player buys affordable unowned land at its listed price","scenario_hash":"212b35c8a92f930618f3e8ba31ab978c379296848bc3d6126b33255aa85c5be4","mutation_count":23,"result":{"Total":23,"Killed":23,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":1,"name":"an agreeable player declines unowned land they cannot afford","scenario_hash":"8081bd7d22c6ed4c78ef573195c8858aa84563954fd7f0f55b01ade4742579a6","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":2,"name":"an agreeable player keeping a reserve declines land that would dip below it","scenario_hash":"e64f4dcfbf8bc5bbf5ba476072bc0ff3490c9636118b8af44fcaa8b01d91e031","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":3,"name":"an agreeable player keeping a reserve buys land that would leave at least the reserve behind","scenario_hash":"3419315c049a1412c95cce0e7a85c338f075b463b594164e5783634ba5e6ed5c","mutation_count":18,"result":{"Total":18,"Killed":18,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":4,"name":"an agreeable player keeping a reserve still respects it for a utility nobody owns yet","scenario_hash":"938bc4cc087c0c030cd285c80bc09f4b13ae7d7b72397bebccf243af49baf821","mutation_count":9,"result":{"Total":9,"Killed":9,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":5,"name":"an agreeable player keeping a reserve buys a utility anyway to deny another player a monopoly on them","scenario_hash":"858d4e14f20ea08940ffd83c27eb41d2e9c6e0e02f9131afb2b0bda81f740f96","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":6,"name":"an agreeable player keeping a reserve buys a utility anyway to complete their own monopoly on them","scenario_hash":"d2bdf9ccbebcddb32537f2e1da59deb4239dbca69533f5041c677dbcc4b1ab70","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":7,"name":"an agreeable player one street away from a colour monopoly reserves that street's price","scenario_hash":"711904a33102cbc5c79485488a1da2a8d288ad100183ae0b3c94d5fa888a5194","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":8,"name":"an agreeable player one street away from a colour monopoly still buys land that would leave at least that street's price behind","scenario_hash":"77df290b3e3af6f5f93a1f37154f18aa67ca76b92590235e3749fb1e063a11ca","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":9,"name":"an agreeable player one street away from completing two colour groups reserves only the pricier missing street","scenario_hash":"56d478a9a4742f7c9365cc82c5a73a16e4bb82f6551232fe1c36b106c335daae","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":10,"name":"an agreeable player one street away from completing two colour groups still declines land that would dip below the pricier missing street","scenario_hash":"509fd2d09615318a5d122eece17cb0785066f3d35277a5fa194378c237225d44","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":11,"name":"an agreeable player falls back to a cheaper reachable missing street within the same priority tier when the pricier one is unaffordable","scenario_hash":"2593d7ff00fcc8779ac1aeddc1b112653060c51ecd79dc6f0755f0afca8d985c","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":12,"name":"an agreeable player is not blocked by an unaffordable missing street once a cheaper reachable street in the same priority tier sets the reserve instead","scenario_hash":"086a37f0d974f700768f8dea07590ae1f8838806a4580b99ac46775272dde426","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":13,"name":"an agreeable player with no reachable near-complete monopoly still respects their configured reserve","scenario_hash":"b3debf5c39ed42e313656a7ae37d465f36feb72609b186b94663261163eed268","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":14,"name":"an agreeable player with no reachable near-complete monopoly is not blocked by an unaffordable missing street once the configured reserve is small enough","scenario_hash":"4dc845bbd3498f9b32be28c224031878a6a6e56c129c82ee852fab24ae3ecc19","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":15,"name":"an agreeable player one station away from completing the set reserves that station's price","scenario_hash":"598cd5909c8bbe83d480b1be27fe1963ae4c7f62b20ddc09e1e09c7199adf141","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":16,"name":"an agreeable player reserves a middle-priority colour group's missing street over a pricier missing station in the lowest tier","scenario_hash":"e4173765887f22bc145d9b923e902f5c8b5b825dbe4d3747b6b59af0768935bb","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":17,"name":"an agreeable player still buys into a colour group another player already blocks, provided the reserve is maintained","scenario_hash":"adefca1d6871007f19c969329482af2b8e211950b1a2a4807b1f8a45f4d0cbdd","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":18,"name":"an agreeable player does not buy into a colour group another player already blocks if doing so would dip below the reserve, and a lone-owning bystander has no auction interest in a second piece either","scenario_hash":"70b9640685ab72641d9ef7f03ed07fa4151cdadb633a3143f801c37556840a92","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":19,"name":"an agreeable player is not limited to a pricier missing station's reserve when a middle-priority colour group's cheaper missing street applies instead","scenario_hash":"c86f79055cb0fe92d9c302d63abb0ad0a3c2c1540c228704860fd9760e9a58e7","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":20,"name":"an agreeable player reserves a highest-priority colour group's missing street over a pricier missing street in a middle-priority group","scenario_hash":"20fd4623868a5274951861c9fd8c274c844f616ae7695c068976fb30f5f829d7","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":21,"name":"an agreeable player is not limited to a pricier middle-priority group's reserve when a highest-priority group's cheaper missing street applies instead","scenario_hash":"ec3a6e10eca7c34f751f631efaaed3aac33c98df0ec67433c7f18ed03aa59f21","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"},{"index":22,"name":"an agreeable player reserves a highest-priority colour group's missing street over a pricier missing street in the lowest tier","scenario_hash":"39cbf14804e338d048908dc50279a302632f20e403da20faa70f363eaabc5e7d","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:22:31.109725Z"}]}
# acceptance-mutation-manifest-end

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
