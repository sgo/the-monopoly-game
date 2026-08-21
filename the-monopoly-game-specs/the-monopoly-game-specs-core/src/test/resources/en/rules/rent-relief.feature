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
