# language: en

Feature: asset-rich billionaire
  An opt-in flag, `--optional-asset-rich-billionaire`, changes what the
  "Billionaire" strategy is granted at the start of the game. Without the
  flag, Billionaire keeps its existing cash-rich opening: the usual
  $57,700,000 balance and no land. With the flag enabled, Billionaire is
  asset-rich instead: it opens with the ordinary $1,500 balance, but the
  game starts it out already owning the whole Orange and Red colour groups
  outright — unmortgaged, but with no houses built yet. Billionaire's
  decisions stay identical to Greedo's either way; only what it's granted
  at the start changes, and only when this flag is enabled.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative

  # asset-rich-billionaire-1
  Scenario Outline: an asset-rich billionaire opens the game with the standard balance instead of the usual cash-rich one
    Given pawn "dog" follows the "Billionaire" strategy
    And asset-rich opening is enabled for the "Billionaire" strategy
    And every other player can complete their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<opening_balance> and a $<reserve> reserve

    Examples:
      | opening_balance | reserve |
      | 1500             | 0       |

  # asset-rich-billionaire-2
  Scenario Outline: an asset-rich billionaire starts the game already owning <street>, unimproved
    Given pawn "dog" follows the "Billionaire" strategy
    And asset-rich opening is enabled for the "Billionaire" strategy
    When we play up to 0 rounds
    Then pawn "dog" owns "<street>"
    And the street "<street>" has 0 house(s) built

    Examples:
      | street                     |
      | Lippenslaan Knokke         |
      | Rue Royale Tournai         |
      | Groenplaats Antwerpen      |
      | Rue St-Léonard Liège       |
      | Lange Steenstraat Kortrijk |
      | Grand Place Mons           |

  # asset-rich-billionaire-3
  Scenario Outline: an asset-rich billionaire builds evenly across its granted Orange monopoly once affordable
    Given pawn "dog" follows the "Billionaire" strategy
    And asset-rich opening is enabled for the "Billionaire" strategy
    And pawn "dog" has $300 to spend
    And every other player can complete their turn
    When we play the game
    Then the street "Lippenslaan Knokke" has <houses> house(s) built
    And the street "Rue Royale Tournai" has <houses> house(s) built
    And the street "Groenplaats Antwerpen" has <houses> house(s) built

    Examples:
      | houses |
      | 1      |

  # asset-rich-billionaire-4
  Scenario Outline: an asset-rich billionaire builds evenly across its granted Red monopoly once affordable
    Given pawn "dog" follows the "Billionaire" strategy
    And asset-rich opening is enabled for the "Billionaire" strategy
    And pawn "dog" has $450 to spend
    And every other player can complete their turn
    When we play the game
    Then the street "Rue St-Léonard Liège" has <houses> house(s) built
    And the street "Lange Steenstraat Kortrijk" has <houses> house(s) built
    And the street "Grand Place Mons" has <houses> house(s) built

    Examples:
      | houses |
      | 1      |

  # asset-rich-billionaire-5
  Scenario Outline: a billionaire keeps its ordinary cash-rich opening when the asset-rich flag is not enabled
    Given pawn "dog" follows the "Billionaire" strategy
    And every other player can complete their turn
    When we play the game
    Then the game journal records that pawn "dog" starts a turn with $<opening_balance> and a $<reserve> reserve
    And pawn "dog" does not own "<street>"

    Examples:
      | opening_balance | reserve | street             |
      | 57700000         | 0       | Lippenslaan Knokke |
