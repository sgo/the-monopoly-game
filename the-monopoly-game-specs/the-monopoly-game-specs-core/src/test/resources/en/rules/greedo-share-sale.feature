# mutation-stamp: sha256=bc7919b00fff0f589ad9ed21954c3b81f5ee8e013c144789f243df8d960b3f1b
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-12T13:46:06.545082Z","feature_name":"selling legal-entity shares to avoid bankruptcy","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/greedo-share-sale.feature","background_hash":"9a3c0c411c487ac29cd37776a8a944a1b977e6980fe8a564f53528f8edf6826a","implementation_hash":"unknown","scenarios":[{"index":7,"name":"a lone bidder buys a legal-entity share for a nominal $5; whether the seller avoids bankruptcy depends on whether the proceeds cover the remaining debt","scenario_hash":"c83d672b429a4fc94fc6b9d317cbd9ee13f4e64fa56b8b1581044230f71751cd","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-08-12T13:46:06.545082Z"},{"index":8,"name":"a distressed shareholder disposes of a personal asset before offering their legal-entity share, and the share sale may or may not settle the remaining debt","scenario_hash":"f4bdeed307937fa5d20410a505600824b1a31709ffa21f1d469c8d2f2c4144c1","mutation_count":10,"result":{"Total":10,"Killed":10,"Survived":0,"Errors":0},"tested_at":"2026-08-11T17:07:47.105888Z"},{"index":0,"name":"a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt","scenario_hash":"7c01e3888e0422ed973168f4b4afcf3d282328b6be4b562b4676cceea9caceeb","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"},{"index":1,"name":"a shareholder does not go bankrupt because the share's value covers the tax debt","scenario_hash":"4b3436a75f0bce3f44174f1c585fcd66e9b5ad217a554c84979997ed8ec0447e","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"},{"index":2,"name":"a shareholder sells a cheaper personal asset before offering their legal-entity share","scenario_hash":"cd947d25c54d7fffa82e8160e7b9c354f4926aceca2718cb97af91e85ae0db4a","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"},{"index":3,"name":"a legal-entity share does not change hands when no fellow shareholder will bid","scenario_hash":"e3fc41d7a633110633bb760cb8826e44de54c0414085e3343e12ea9b7c72aecc","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"},{"index":4,"name":"a fellow shareholder bids up to a third of their bank balance, and the highest bid wins","scenario_hash":"6dc43117ef23c7cc4bfa395b5c6f58f1e877051d8e40ddc4d1c06baa47b10c2a","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"},{"index":5,"name":"the final shareholder may liquidate the legal entity to settle their debt","scenario_hash":"124c6e4f30c97d9e7471fb0bfe14fcd350e6b130b29cc4977f5c470605d79d87","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"},{"index":6,"name":"the final shareholder sells newly-acquired entity assets when liquidation cash is insufficient","scenario_hash":"23c20177ffd6e5e68c717c841847362f4547c8e0f6cc2113ebd058c87d344e48","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-11T11:27:10.610946Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: selling legal-entity shares to avoid bankruptcy

  Background:
    Given the official rule set

  # share-sale-1
  Scenario Outline: a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 5 players
    And Pink Realty is formed
    And pawn "dog" owns no mortgaged property
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $<high_hat_balance> to spend
    And pawn "iron box" has $<iron_box_balance> to spend
    And pawn "ship" has $<ship_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is not bankrupt
    And pawn "dog" no longer holds a share of Pink Realty
    And pawn "high hat" holds that Pink Realty share
    And pawn "iron box" still holds a share of Pink Realty
    And pawn "ship" holds no share of Pink Realty

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance | ship_balance |
      | 40          | 1000             | 200              | 200          |

  # share-sale-2
  Scenario Outline: a shareholder does not go bankrupt because the share's value covers the tax debt
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 5 players
    And Pink Realty is formed
    And pawn "dog" owns no mortgaged property
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $<high_hat_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is not bankrupt
    And pawn "high hat" holds the Pink Realty share sold by pawn "dog"

    Examples:
      | dog_balance | high_hat_balance |
      | 40          | 1000             |

  # share-sale-3
  Scenario Outline: a shareholder sells a cheaper personal asset before offering their legal-entity share
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 5 players
    And Pink Realty is formed
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $<high_hat_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" no longer owns "Rue Grande Dinant"
    And pawn "high hat" owns "Rue Grande Dinant"
    And pawn "dog" still holds a share of Pink Realty
    And pawn "dog" is not bankrupt

    Examples:
      | dog_balance | high_hat_balance |
      | 10          | 1000             |

  # share-sale-4
  Scenario Outline: a legal-entity share does not change hands when no fellow shareholder will bid
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 5 players
    And Pink Realty is formed
    And pawn "dog" owns no mortgaged property
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $<high_hat_balance> to spend
    And pawn "iron box" has $<iron_box_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is bankrupt
    And pawn "dog" holds no shares of any legal entity

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance |
      | 40          | 0                | 0                |

  # share-sale-5
  Scenario Outline: a fellow shareholder bids up to a third of their bank balance, and the highest bid wins
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And Pink Realty is formed
    And pawn "dog" owns no mortgaged property
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $<high_hat_balance> to spend
    And pawn "iron box" has $<iron_box_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" wins the Pink Realty share at $<winning_bid>
    And pawn "high hat" paid the lowest possible price within a third of bank balance

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance | winning_bid |
      | 40          | 1200             | 900              | 320         |
      | 40          | 3000             | 2600             | 915         |
      | 40          | 1600             | 1500             | 530         |

  # share-sale-6
  Scenario Outline: the final shareholder may liquidate the legal entity to settle their debt
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And Pink Realty is formed
    And pawn "high hat" and pawn "iron box" have both gone bankrupt
    And pawn "dog" owns no mortgaged property
    And Pink Realty's bank account holds $<entity_balance>
    And pawn "dog" has $<dog_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is not bankrupt
    And pawn "dog"'s final balance is $<dog_ending>
    And Pink Realty is dissolved
    And pawn "dog" owns every street previously held by Pink Realty

    Examples:
      | dog_balance | entity_balance | dog_ending |
      | 40          | 500            | 440        |

  # share-sale-7
  Scenario Outline: the final shareholder sells newly-acquired entity assets when liquidation cash is insufficient
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And Pink Realty is formed
    And pawn "high hat" and pawn "iron box" have both gone bankrupt
    And pawn "dog" owns no mortgaged property
    And pawn "dog" has $<dog_balance> to spend
    And Pink Realty's bank account holds $<entity_balance>
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog" is not bankrupt
    And Pink Realty is dissolved
    And pawn "dog" received the Pink Realty bank balance
    And pawn "dog" sold <streets_to_sell> of the transferred Pink Realty streets to settle the remaining debt
    And pawn "dog"'s debt is settled

    Examples:
      | dog_balance | entity_balance | streets_to_sell |
      | 40          | 0              | 1               |

  # share-sale-8
  Scenario Outline: a lone bidder buys a legal-entity share for a nominal $5; whether the seller avoids bankruptcy depends on whether the proceeds cover the remaining debt
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And pawn "iron box" is bankrupt
    And pawn "dog" owns no mortgaged property
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $<high_hat_balance> to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "high hat" wins the Pink Realty share at $<winning_bid>
    And pawn "dog"'s bankrupt state is <bankrupt_state>
    And pawn "dog"'s final balance is $<dog_ending>
    And pawn "high hat"'s final balance is $<high_hat_ending>

    Examples:
      | dog_balance | high_hat_balance | winning_bid | bankrupt_state | dog_ending | high_hat_ending |
      | 94          | 1000             | 5           | bankrupt       | -1         | 995             |
      | 95          | 1000             | 5           | not bankrupt   | 0          | 995             |

  # share-sale-9
  Scenario Outline: a distressed shareholder disposes of a personal asset before offering their legal-entity share, and the share sale may or may not settle the remaining debt
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 3 players
    And Pink Realty is formed
    And pawn "dog" returns every street except "Steenstraat Brugge" to the bank
    And pawn "iron box" is bankrupt
    And pawn "dog" owns "Steenstraat Brugge"
    And pawn "iron box" owns "Rue Grande Dinant"
    And pawn "iron box" owns "Diestsestraat Leuven"
    And pawn "iron box" owns "Place du Monument Spa"
    And pawn "dog" has $<dog_balance> to spend
    And pawn "high hat" has $50 to spend
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then the land "Steenstraat Brugge" is mortgaged
    And pawn "high hat" wins the Pink Realty share at $<winning_bid>
    And Pink Realty is not dissolved
    And pawn "dog"'s bankrupt state is <bankrupt_state>
    And pawn "dog"'s final balance is $<dog_ending>
    And pawn "high hat"'s final balance is $<high_hat_ending>

    Examples:
      | dog_balance | winning_bid | bankrupt_state | dog_ending | high_hat_ending |
      | 45          | 5           | not bankrupt   | 0          | 45              |
      | 40          | 5           | bankrupt       | -5         | 45              |
