# mutation-stamp: sha256=89224a8a44bcd770cb6963fb358c201bdb32ee8375d5d025c2816a113f379b92
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T08:46:30.994256Z","feature_name":"rent relief","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/rent-relief.feature","background_hash":"8b52bb2a0a6f53558853cb174011d865171e1a4b41c107899c959054c299f158","implementation_hash":"unknown","scenarios":[{"index":2,"name":"rent relief applies the same way when the landlord is a legal entity","scenario_hash":"1bea00a251e4d62c047a58c0529dcc0401a62050096adba2f9ba2fb4e224f802","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-24T20:30:39.227141Z"},{"index":4,"name":"war profits tax still accumulates the full nominal rent a landlord receives, whether or not relief covered part of it","scenario_hash":"ecc748d4c14b143549c9ead3e870768e43e3faee48985159f4130a34b22dc14e","mutation_count":7,"result":{"Total":7,"Killed":7,"Survived":0,"Errors":0},"tested_at":"2026-08-24T20:30:39.227141Z"},{"index":3,"name":"a live played game's rent payment honors an already-funded government account, not just the isolated computation","scenario_hash":"eccc1c6a60ffd2f082cdba988c2dfe81968f016b717eec204b24fabcdb9774fe","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-24T20:24:12.502920Z"},{"index":1,"name":"the tenant's rent is capped at $200 when the government can cover the rest in full, and pays the full rent otherwise","scenario_hash":"14318af1dcc019d9983eda05aef7243b2e67c0bc3781a57834f78107805d3188","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-21T08:12:21.207736Z"}]}
# acceptance-mutation-manifest-end

# language: en

# This is the spending half of the pair with
# ../../en/rules/megacorp-salary-tax.feature, which funds the government
# account this spends from. Both scenarios here are isolated computations
# over a directly-stated rent amount, the same way war-profits-tax's band
# table is, rather than a rent charged by landing on a specific street.
# Observability (journal/logging/report), CLI wiring, legal-entity
# landlords, and proof that a live played game actually applies this cap
# during a real rent payment are backlogged at
# ../../backlog/en/rules/rent-relief.feature,
# ../../backlog/en/rules/journal.feature, logging.feature, report.feature,
# and ../../backlog/specs-cli/en/cli.feature, cli-packaged-jar.feature.

Feature: rent relief
  An opt-in flag, `--optional-rent-relief`, caps what a tenant pays in rent
  at $200 - the same amount as the ordinary salary for passing Start - the
  moment the government's account can cover the rest of the bill in full.
  The government pays the landlord that difference, so the landlord always
  receives the full nominal rent either way. If the government's account
  cannot cover the full difference, no relief is given: the tenant pays the
  full rent, exactly as without this flag.

  $200 was chosen from the real distribution of rent payments across this
  project's characterization baselines: only 3.5% of payments exceed it,
  but that tail carries roughly 30% of all rent dollars, so it is the
  threshold that catches the hotel-tier spikes capable of ending a game for
  a player still building up cash.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And rent relief is enabled

  # rent-relief-1
  Scenario Outline: rent at or under $200 is paid by the tenant in full, regardless of what the government's account holds
    Given pawn "high hat" has $1500 to spend
    And the government's account already holds $<government_start>
    When pawn "high hat" pays pawn "dog" $200 rent
    Then pawn "high hat"'s account balance is $1300
    And the government's account holds $<government_start>

    Examples:
      | government_start |
      | 0                  |
      | 5000               |

  # rent-relief-2
  # The overage above the $200 cap on a $750 rent is exactly $550. The two
  # rows pin that boundary: $550 already in the government's account covers
  # it in full and relief applies; $549 falls a dollar short and relief
  # does not, so the tenant pays the full $750 instead. Either way the
  # landlord receives the same $750 - relief only ever changes who pays the
  # tenant's share, never what the landlord is owed.
  Scenario Outline: the tenant's rent is capped at $200 when the government can cover the rest in full, and pays the full rent otherwise
    Given pawn "high hat" has $1500 to spend
    And pawn "dog" has $1500 to spend
    And the government's account already holds $<government_start>
    When pawn "high hat" pays pawn "dog" $750 rent
    Then pawn "high hat"'s account balance is $<tenant_final>
    And pawn "dog"'s account balance is $2250
    And the government's account holds $<government_final>

    Examples:
      | government_start | tenant_final | government_final |
      | 550                | 1300          | 0                  |
      | 549                | 750           | 549                |

  # rent-relief-3
  # Corrected after actually running this (architect finding,
  # 2026-08-24T22:10:00Z entry, and further tracing):
  #   - Building a hotel on only "Rue de Diekirch Arlon" left its Pink
  #     Realty group-mates undeveloped; added hotels on all three, matching
  #     every other greedo-legal-entity.feature scenario that develops a
  #     Pink Realty street. Added a third player so all three streets get
  #     real individual owners before Pink Realty forms, matching every
  #     entity-forming scenario in greedo-legal-entity.feature (all select
  #     3+ players); high hat ends up a shareholder as well as the tenant,
  #     which the tracked "a shareholder pays rent when landing on their
  #     own legal entity's street" scenario already establishes is valid.
  #   - The actual cause of the persistent wrong balance, found by
  #     temporarily asserting the journal's entity-rent entry directly
  #     instead of the final balance: high hat never landed on Rue de
  #     Diekirch Arlon at all. `World.formNamedEntity` pre-queues a roll
  #     for every player "if empty" so its own auto-formation flow has
  #     something to consume (`World.java:1045-1049`), and it runs before
  #     `takes a targeted landing on` queues its own roll for the same
  #     pawn - since queued rolls are FIFO
  #     (`World.nextQueuedPawnRoll`/`removeFirst`), the pre-queued roll is
  #     consumed first, moving high hat to the wrong space. Queuing high
  #     hat's real turn roll before "Pink Realty is formed" pre-empts the
  #     auto-queue (which only fires when a player's queue is still empty),
  #     so `takes a targeted landing`'s own roll - appended after, and
  #     equal to it - is what actually gets consumed. This is a step-timing
  #     collision, not a rent-relief or entity-rent defect: rent-relief-4
  #     (individual landlord, no entity formation involved) never hit it.
  Scenario Outline: rent relief applies the same way when the landlord is a legal entity
    Given we select 3 players
    And pawn "iron box" will roll 6 for initiative
    And pawn "high hat" will roll 1 and 2 for their turn
    And Pink Realty is formed
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the street "Bruul Mechelen" has a hotel built
    And the street "Place Verte Verviers" has a hotel built
    And pawn "high hat" has $1500 to spend
    And the government's account already holds $<government_start>
    When pawn "high hat" takes a targeted landing on "Rue de Diekirch Arlon"
    Then pawn "high hat"'s account balance is $<tenant_final>

    Examples:
      | government_start | tenant_final |
      | 550                | 1300          |
      | 549                | 750           |

  # rent-relief-4
  Scenario Outline: a live played game's rent payment honors an already-funded government account, not just the isolated computation
    Given pawn "dog" owns "Rue de Diekirch Arlon"
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the government's account already holds $<government_start>
    And pawn "high hat" has $1500 to spend
    When pawn "high hat" takes a targeted landing on "Rue de Diekirch Arlon"
    Then the game journal records that pawn "high hat" pays pawn "dog" $200 rent for "Rue de Diekirch Arlon"

    Examples:
      | government_start |
      | 550                |

  # rent-relief-5
  # Corrected twice from the backlog's illustrative values after actually
  # running this against real code:
  #   - high_hat_start_position was 37, but 37 is Meir Antwerpen's own
  #     board position, so starting there plus a roll of 3 landed high hat
  #     back on Start, not on Meir Antwerpen. Fixed to 34 (Boulevard
  #     d'Avroy Liège), so 34 + 3 = 37 = Meir Antwerpen as intended.
  #   - dog originally had a single queued roll landing exactly on Start in
  #     round 1, before high hat's round-1 turn (high hat goes second,
  #     since dog wins initiative and turn order follows it). dog's
  #     war-profits-tax assessment fires synchronously inside the same
  #     collectedSalary call that crosses Start, reading whatever rent
  #     WarProfitsTaxBook has accumulated *so far* - which was nothing yet,
  #     so the assessed tax came out $0, not $1500. Split into two queued
  #     rolls: round 1 lands dog on Veldstraat Gent (25 + 7 = 32), one of
  #     dog's own owned streets, so visiting it is a no-op, without
  #     crossing Start; round 2 then crosses Start (32 + 8 = 40 -> 0) only
  #     after high hat's round-1 rent payment has already landed, so the
  #     assessment reflects it.
  Scenario Outline: war profits tax still accumulates the full nominal rent a landlord receives, whether or not relief covered part of it
    Given the war profits tax is enabled
    And pawn "dog" owns "Meir Antwerpen"
    And pawn "dog" owns "Nieuwstraat Brussel"
    And pawn "dog" owns "Boulevard Tirou Charleroi"
    And pawn "dog" owns "Veldstraat Gent"
    And pawn "dog" owns "Boulevard d'Avroy Liège"
    And the street "Meir Antwerpen" has a hotel built
    And the street "Nieuwstraat Brussel" has a hotel built
    And the street "Boulevard Tirou Charleroi" has a hotel built
    And the street "Veldstraat Gent" has a hotel built
    And the street "Boulevard d'Avroy Liège" has a hotel built
    And the government's account already holds $1300
    And pawn "high hat" has $1500 to spend
    And pawn "high hat" starts at position <high_hat_start_position>
    And pawn "high hat" will roll <high_hat_die_1> and <high_hat_die_2> for their turn
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_round1_die_1> and <dog_round1_die_2> for their turn
    And pawn "dog" will roll <dog_round2_die_1> and <dog_round2_die_2> for their turn
    When we play up to <rounds> rounds
    Then the game journal records that pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | high_hat_start_position | high_hat_die_1 | high_hat_die_2 | dog_start_position | dog_round1_die_1 | dog_round1_die_2 | dog_round2_die_1 | dog_round2_die_2 | rounds | tax  |
      | 34                       | 1               | 2               | 25                  | 3                  | 4                  | 2                  | 6                  | 2       | 1500 |
