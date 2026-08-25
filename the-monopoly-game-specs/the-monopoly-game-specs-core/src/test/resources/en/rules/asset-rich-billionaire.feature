# mutation-stamp: sha256=d165bc0e0c7b0d220973c7722be141e95d0b9e5c5719982fb3f041ec3dd25059
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T08:22:19.701986Z","feature_name":"asset-rich billionaire","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/asset-rich-billionaire.feature","background_hash":"d393cd2ad76babc786d9464e9f2cbd2d33b5de531a11eda2744728a563e5e9ca","implementation_hash":"unknown","scenarios":[{"index":0,"name":"an asset-rich billionaire opens the game with the standard balance instead of the usual cash-rich one","scenario_hash":"fe62d9be49bd39ebb5af8fcbee5a136abf4c1c54e657480d98e1f3e89ba668e2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:45.219492Z"},{"index":1,"name":"an asset-rich billionaire starts the game already owning <street>, unimproved","scenario_hash":"c64e8e07cdf4305d4a1642b11d3bdd12fe4145ef4c1521d333d7499a6391c233","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:45.219492Z"},{"index":2,"name":"an asset-rich billionaire builds evenly across its granted Orange monopoly once affordable","scenario_hash":"9e787b7b3d36b17e2ea65881a1ba3a04e3af6a98b54cd0fa3e00ae3a125b56e5","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:45.219492Z"},{"index":3,"name":"an asset-rich billionaire builds evenly across its granted Red monopoly once affordable","scenario_hash":"659d2a3c5e189bdab287a1b0f6d37eff22347802b11ff1c18334f4d77abeec11","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:45.219492Z"},{"index":4,"name":"a billionaire keeps its ordinary cash-rich opening when the asset-rich flag is not enabled","scenario_hash":"326a50880654c3ef965532f1c73ab92a15d3bc1226a4ae32ad66c5e5853e6e1a","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:45.219492Z"}]}
# acceptance-mutation-manifest-end

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
