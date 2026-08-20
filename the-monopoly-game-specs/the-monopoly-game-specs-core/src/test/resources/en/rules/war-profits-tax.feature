# language: en

# Round 1 of 3 for this feature: the core tax computation (threshold, band
# rate, yearly reset). Ownership-share valuation nuances (buying vs.
# developing land, bankruptcy inheritance, legal entities, selling back
# below threshold) and payment/enforcement (multi-player accumulation,
# forced mortgage on shortfall) remain backlogged at
# ../../../../../backlog/en/rules/war-profits-tax.feature, to follow once
# this round lands.

Feature: war profits tax
  An opt-in flag, `--optional-war-profits-tax`, taxes rental income once a
  player's land holdings grow large enough to look like wartime
  profiteering rather than ordinary success.

  Ownership share is measured by the *current* rent value of a player's
  land as a fraction of the whole board's value at full development (the
  same board-value figure the stalemate condition already uses).

  Each player accumulates the rent they collect from others, and once a
  year — the same "grows a year older" trigger development loan payments
  already use — that accumulated rent is taxed at a rate set by the
  player's *current* ownership share at that moment, then the counter
  resets to zero for the next year. The rate is set by fixed bands, each
  band's lower bound inclusive (a share exactly at a boundary belongs to
  the higher band):

  | Ownership share | Rate |
  |------------------|------|
  | below 25%        | 0%   |
  | 25% – 40%         | 100% |
  | 40% – 60%         | 150% |
  | 60% – 80%         | 200% |
  | 80% – 100%        | 300% |
  | 100%              | 400% |

  A rate above 100% means the player owes more than they collected that
  year, out of pocket.

  All tax collected is paid into a new government account.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And the war profits tax is enabled

  # war-profits-tax-1
  Scenario Outline: below 25% ownership, no war profits tax is owed no matter how much rent was collected
    Given pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then pawn "dog" pays no war profits tax
    And the government's account holds $0

    Examples:
      | land_value | collected |
      | 5000       | 1000      |
      | 5697       | 50000     |

  # war-profits-tax-2
  # Board value is $22,790 (colour $21,850 + stations $800 + utilities $140,
  # the same figure Stalemate.threshold uses). Each pair below straddles a
  # band boundary — the top of one band ($1 short of the next threshold)
  # and the bottom of the next (exactly at it) — so the boundary itself,
  # not just a comfortably-interior point, is pinned: 5698/9115 pin band
  # 25%-40%, 9116/13673 pin 40%-60%, 13674/18231 pin 60%-80%, 18232/22789
  # pin 80%-100%, and 22790 is the single top point at exactly 100%. The
  # $5697 counterpart just below the 25% threshold is covered by
  # war-profits-tax-1, which asserts no tax rather than a $0 tax.
  Scenario Outline: the tax rate climbs in bands as ownership share crosses 25%, applied to the rent collected that year
    Given pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $1000 in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | land_value | tax  |
      | 5698       | 1000 |
      | 9115       | 1000 |
      | 9116       | 1500 |
      | 13673      | 1500 |
      | 13674      | 2000 |
      | 18231      | 2000 |
      | 18232      | 3000 |
      | 22789      | 3000 |
      | 22790      | 4000 |

  # war-profits-tax-7
  Scenario Outline: the rent-collected counter resets to zero after each assessment, so a quiet year owes nothing even at a high ownership share
    Given pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $1000 in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    And pawn "dog" grows a year older
    Then pawn "dog" pays no war profits tax

    Examples:
      | land_value |
      | 10000      |
