# language: en

Feature: selling legal-entity shares to avoid bankruptcy

  Background:
    Given the official rule set

  # share-sale-1
  Scenario Outline: a shareholder in distress offers their legal-entity share to a fellow shareholder instead of going bankrupt
    Given legal-entity trading is enabled for the "Greedo" strategy
    And we select 4 players
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
    And we select 4 players
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
    And we select 4 players
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
    And we select 4 players
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
    Then pawn "high hat" wins the Pink Realty share at no more than $<winning_bid>
    And pawn "high hat" holds the highest bid within a third of bank balance

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance | winning_bid |
      | 40          | 1200             | 900              | 420         |
      | 40          | 3000             | 2600             | 1050        |
      | 40          | 1600             | 1500             | 560         |