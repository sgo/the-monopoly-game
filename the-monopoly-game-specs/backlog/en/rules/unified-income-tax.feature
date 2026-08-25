# language: en

# Not yet batched. Interaction with the two existing income taxes when
# enabled at the same time (proving independent accumulators, not a
# shared or double-reset one) lives here, since it's genuinely about
# this feature's own file. Observability and CLI wiring live in their
# own owning files' backlogs instead, matching every other optional
# flag's precedent: ../journal.feature, ../report.feature,
# ../logging.feature, and ../../specs-cli/en/cli.feature.
# cli-packaged-jar.feature's cli-jar-5 (README usage-text listing every
# optional flag) is an existing tracked scenario, not a new one to
# backlog; it needs one more assertion line for
# --optional-unified-income-tax once this feature is implemented, the
# same way development-loans' own entry notes cli-jar-5 stays red for a
# missing flag until the README is synced.

Feature: unified income tax

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # unified-income-tax-5
  # War profits tax's own accumulator ($5000, at a land value chosen to
  # land in the 100% band so its tax is easy to verify) and unified
  # income tax's accumulator ($649.12, chosen as in unified-income-tax-1
  # so the combined base is an exact $1000 and its tax an exact $430) are
  # set to deliberately different figures, so a shared or conflated
  # accumulator would produce a visibly wrong total on at least one side.
  Scenario Outline: enabling this alongside war profits tax taxes the same rent twice, from two independent accumulators
    Given the war profits tax is enabled
    And the unified income tax is enabled
    And pawn "dog"'s land is currently worth $6000 in rent
    And pawn "dog" has collected $5000 in rent since their last war profits tax assessment
    And pawn "dog" has collected $649.12 in rent since their last unified income tax assessment
    When pawn "dog" grows a year older
    And pawn "dog" collects a salary of $200
    Then pawn "dog" pays the government a war profits tax of $<war_profits_tax>
    And the government's account holds $<government_account>

    Examples:
      | war_profits_tax | government_account |
      | 5000             | 5430.00              |

  # unified-income-tax-6
  # MegaCorp's own tax ($150.88, from megacorp-salary-tax-1) and this
  # tax's own salary-only figure (also $150.88, from unified-income-tax-2)
  # both fire from the same salary collection, independently, landing on
  # the same government account: $301.76 total, not $150.88.
  Scenario Outline: enabling this alongside MegaCorp's salary tax taxes the same salary twice, independently
    Given rent relief is enabled
    And the unified income tax is enabled
    When pawn "dog" collects a salary of $<salary>
    Then MegaCorp pays the government an individual income tax of $<megacorp_tax>
    And the government's account holds $<government_account>

    Examples:
      | salary | megacorp_tax | government_account |
      | 200    | 150.88        | 301.76               |
