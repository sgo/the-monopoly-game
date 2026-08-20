# language: en

# Rounds 1-2 (war-profits-tax-1, 2, 3, 4, 5, 6, 7, 10: core tax
# computation and ownership-share valuation) have been promoted to the
# tracked
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/war-profits-tax.feature.
# Remaining here: round 3, payment/enforcement (war-profits-tax-8, 9).
# Indices are stable and not renumbered when a round is promoted.
#
# Sibling backlog files: ../../backlog/en/rules/journal.feature,
# logging.feature, report.feature (observability), and
# ../../backlog/specs-cli/en/cli.feature, cli-packaged-jar.feature (CLI
# wiring) — all still blocked on the full domain feature existing.
# When promoting, see each sibling file's own header for exactly where
# its scenarios move.

Feature: war profits tax
  An opt-in flag, `--optional-war-profits-tax`, taxes rental income once a
  player's land holdings grow large enough to look like wartime
  profiteering rather than ordinary success.

  Ownership share is measured by the *current* rent value of a player's
  land — vacant rent if nothing is built, the appropriate house-tier rent
  as houses go up, hotel rent once complete — as a fraction of the whole
  board's value at full development (the same board-value figure the
  stalemate condition already uses). Buying undeveloped land barely moves
  this share; only actually developing it does.

  The tax itself targets rental income, not land value directly: each
  player accumulates the rent they collect from others, and once a year —
  the same "grows a year older" trigger development loan payments already
  use — that accumulated rent is taxed at a rate set by the player's
  *current* ownership share at that moment, then the counter resets to
  zero for the next year. The rate is set by fixed bands, each band's
  lower bound inclusive (a share exactly at a boundary belongs to the
  higher band):

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

  A shortfall is handled exactly like any other unpayable debt already is
  elsewhere in the game: mortgaging a spare property, selling to a peer,
  and eventually forced liquidation if nothing else covers it. Whether a
  development loan can also be drawn against a tax shortfall (today,
  development loans only fund a house/hotel purchase a player is short on
  cash for) is an open question, deliberately left unspecified here. Land
  inherited through another player's bankruptcy counts toward ownership
  share the same as land that was bought. Legal entities are not taxed at
  all, and entity-owned land does not count toward any shareholder's own
  ownership share.

  All tax collected is paid into a new government account, reported both
  at the end of the game and after every payment into it.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And the war profits tax is enabled

  # war-profits-tax-8
  Scenario Outline: tax paid by multiple players accumulates together in the same government account
    Given pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "high hat"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $1000 in rent since their last war profits tax assessment
    And pawn "high hat" has collected $1000 in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    And pawn "high hat" grows a year older
    Then the government's account holds $<government_account>

    Examples:
      | land_value | government_account |
      | 6000       | 2000                |

  # war-profits-tax-9
  Scenario Outline: a tax bill larger than the player's cash forces a mortgage, the same as any other unpayable debt
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Nieuwstraat Brussel"
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And pawn "dog" owns "Lippenslaan Knokke"
    And the street "Meir Antwerpen" has a hotel built
    And the street "Nieuwstraat Brussel" has a hotel built
    And the street "Boulevard Tirou Charleroi" has a hotel built
    And the street "Veldstraat Gent" has a hotel built
    And the street "Boulevard d'Avroy Liège" has a hotel built
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    And pawn "dog" has $0 to spend
    When pawn "dog" grows a year older
    Then the land "Lippenslaan Knokke" is mortgaged
    And pawn "dog" is not bankrupt

    Examples:
      | collected |
      | 90        |
