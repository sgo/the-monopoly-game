# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-20T12:24:47.452053Z","feature_name":"war profits tax","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/war-profits-tax.feature","background_hash":"148ec2f4c532ce7020a4f14aa21af0f2148107d58bc35617d95875ce77771295","implementation_hash":"unknown","scenarios":[{"index":4,"name":"developing owned land raises its ownership share, and crossing 25% brings the tax with it","scenario_hash":"5e44e2da121871b9777029883d556fecbe2b965c0bd69a1aa0d68f6ea4737110","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T12:19:00.058910Z"},{"index":5,"name":"land inherited through another player's bankruptcy counts toward ownership share the same as land that was bought","scenario_hash":"0150ea760cbd2951c3537cf29283046bfc77ae3fdb0eccd7f0ca23937bfe19de","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-20T12:19:00.058910Z"}]}
# acceptance-mutation-manifest-end

# language: en

# All 3 rounds of this feature are now here: the core tax computation
# (threshold, band rate, yearly reset), ownership-share valuation (buying
# vs. developing land, bankruptcy inheritance, legal entities, selling
# back below threshold), and payment/enforcement (multi-player
# accumulation, forced mortgage on shortfall). Observability
# (journal/logging/report) and CLI wiring remain backlogged at
# ../../backlog/en/rules/journal.feature, logging.feature, report.feature,
# and ../../backlog/specs-cli/en/cli.feature, cli-packaged-jar.feature.

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
  # whenever it can afford to). For that mortgage-then-transfer step to
  # still leave high hat bankrupt, the rent owed has to exceed everything
  # mortgaging the green group alone could raise ($150 + $150 + $160 =
  # $460) — dog's pink+red hotels put the rent charged (from Rue de
  # Diekirch Arlon, $750) well above that, and dog's own pre-existing
  # land value (pink $2400 + red $3200 = $5600, 24.6% of the $22,790
  # board) sits just under the 25% floor, so it's specifically the
  # inherited green monopoly's bare vacant rent ($26+$26+$28, doubled to
  # $160 once unmortgaged) that tips dog over it.
  Scenario Outline: land inherited through another player's bankruptcy counts toward ownership share the same as land that was bought
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 6 for initiative
    And pawn "iron box" will roll 2 for initiative
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And pawn "dog" owns "Bruul Mechelen"
    And pawn "dog" owns "Place Verte Verviers"
    And pawn "dog" owns "Rue St-Léonard Liège"
    And pawn "dog" owns "Lange Steenstraat Kortrijk"
    And pawn "dog" owns "Grand Place Mons"
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And the street "Rue St-Léonard Liège" has a hotel built
    And the street "Lange Steenstraat Kortrijk" has a hotel built
    And the street "Grand Place Mons" has a hotel built
    And pawn "high hat" owns "Boulevard Tirou Charleroi"
    And pawn "high hat" owns "Veldstraat Gent"
    And pawn "high hat" owns "Boulevard d'Avroy Liège"
    And pawn "high hat" has $0 to spend
    And pawn "dog" will claim rent for "Rue de Diekirch Arlon"
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "high hat" lands on "Rue de Diekirch Arlon"
    Then pawn "high hat" is bankrupt
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    When pawn "dog" grows a year older
    Then pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | collected | tax  |
      | 1000      | 1000 |

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
