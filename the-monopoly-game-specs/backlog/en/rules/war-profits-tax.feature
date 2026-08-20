# language: en

# Round 1 (war-profits-tax-1, 2, 7: core tax computation) has been
# promoted to the tracked
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/war-profits-tax.feature.
# Remaining here: round 2, ownership-share valuation (war-profits-tax-3,
# 4, 5, 6, 10), and round 3, payment/enforcement (war-profits-tax-8, 9).
# Indices are stable and not renumbered when a round is promoted.
#
# Sibling backlog files: ../../backlog/en/rules/journal.feature,
# logging.feature, report.feature (observability), and
# ../../backlog/specs-cli/en/cli.feature, cli-packaged-jar.feature (CLI
# wiring) — all still blocked on round 1's tracked mechanic existing.
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
  zero for the next year. Below 25% ownership, the rate is 0%. At and
  above 25% it climbs in bands, reaching 400% at full board ownership — a
  rate above 100% means the player owes more than they collected that
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

  # war-profits-tax-3
  Scenario Outline: buying land does not by itself trigger a large tax bill, because undeveloped land is worth its vacant rent, not its hotel rent
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Nieuwstraat Brussel"
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then pawn "dog" pays no war profits tax

    Examples:
      | collected |
      | 500       |

  # war-profits-tax-4
  Scenario Outline: developing owned land raises its ownership share, and crossing 25% brings the tax with it
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Nieuwstraat Brussel"
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And the street "Meir Antwerpen" has a hotel built
    And the street "Nieuwstraat Brussel" has a hotel built
    And the street "Boulevard Tirou Charleroi" has a hotel built
    And the street "Veldstraat Gent" has a hotel built
    And the street "Boulevard d'Avroy Liège" has a hotel built
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | collected | tax  |
      | 1000      | 1000 |

  # war-profits-tax-5
  # A bankrupt debtor already has every house sold and everything
  # mortgageable mortgaged before land ever transfers to the creditor (see
  # bankruptcy.feature), so inherited land always arrives bare and
  # mortgaged, not with whatever houses it had. It only starts counting
  # once the new owner lifts that mortgage (an existing Greedo behaviour
  # whenever it can afford to).
  Scenario Outline: land inherited through another player's bankruptcy counts toward ownership share the same as land that was bought
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 6 for initiative
    And pawn "iron box" will roll 2 for initiative
    And pawn "dog"'s land is currently worth $<land_value_before> in rent
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "high hat" owns "Boulevard Tirou Charleroi"
    And pawn "high hat" owns "Veldstraat Gent"
    And pawn "high hat" owns "Boulevard d'Avroy Liège"
    And pawn "high hat" has $0 to spend
    And pawn "dog" will claim rent for "Meir Antwerpen"
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "high hat" lands on "Meir Antwerpen"
    Then pawn "high hat" is bankrupt
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    When pawn "dog" grows a year older
    Then pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | land_value_before | collected | tax  |
      | 5620               | 1000      | 1000 |

  # war-profits-tax-6
  Scenario Outline: a legal entity's ownership share never taxes the entity, and its land does not count toward any shareholder's own share
    Given Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then pawn "dog" pays no war profits tax

    Examples:
      | collected |
      | 500       |

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

  # war-profits-tax-10
  Scenario Outline: selling back below 25% ownership stops the tax on the following assessment
    Given pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Nieuwstraat Brussel"
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And the street "Meir Antwerpen" has a hotel built
    And the street "Nieuwstraat Brussel" has a hotel built
    And the street "Boulevard Tirou Charleroi" has a hotel built
    And the street "Veldstraat Gent" has a hotel built
    And the street "Boulevard d'Avroy Liège" has a hotel built
    And pawn "dog" has collected $1000 in rent since their last war profits tax assessment
    And pawn "dog" grows a year older
    And pawn "dog" no longer owns "Nieuwstraat Brussel"
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then pawn "dog" pays no war profits tax

    Examples:
      | collected |
      | 1000      |
