# language: en

# This round covers the core tax computation (combining gross salary and
# gross rent collected since this player's own last assessment into one
# 43% payment, and the accumulator resetting afterward), the same
# isolated-computation scope megacorp-salary-tax.feature and
# war-profits-tax.feature each started from; interaction with the two
# existing income taxes when enabled at the same time
# (unified-income-tax-5/6), proving each keeps its own independent
# accumulator rather than sharing or conflating one; and one played-game
# scenario (unified-income-tax-7, mirroring megacorp-salary-tax-3)
# proving the real double-salary-on-landing rule scales this tax
# correctly too, using only pre-existing generic journal entries so it
# needs no new observability wiring despite not being an isolated
# computation. This tax's own payment narration plus CLI wiring are
# backlogged at ../../backlog/en/rules/journal.feature, report.feature,
# logging.feature, and ../../backlog/specs-cli/en/cli.feature.

Feature: unified income tax
  An opt-in flag, `--optional-unified-income-tax`, is a third, independent
  income tax, separate from both MegaCorp's salary tax and the war profits
  tax on rent — it can be enabled on its own, or alongside either or both
  of those, in which case the same income is taxed again under this
  mechanism too; that overlap is intentional, not guarded against.

  Whenever a player collects their salary (the same passing-Start trigger
  MegaCorp's own tax already uses), this tax combines two things into one
  taxable base: their gross salary for that collection (recovered from the
  net amount they actually take home the same way MegaCorp's own tax
  does: net is 57% of gross), and the gross rent they've collected as a
  landlord since their own last assessment under *this* tax specifically —
  a separate running total from the one the war profits tax keeps, reset
  to zero every time this assessment runs, whether or not any rent had
  been collected. 43% of that combined total is paid into the same
  government account MegaCorp's tax and the war profits tax already pay
  into, and that rent relief spends from.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And the unified income tax is enabled

  # unified-income-tax-1
  # Gross salary from a $200 net collection is $350.88 (200 / 0.57,
  # already established by megacorp-salary-tax-1's own $150.88 tax figure:
  # 200 + 150.88 = 350.88). $649.12 in accumulated rent is chosen so the
  # combined base lands on an exact $1000.00, so the 43% tax ($430.00)
  # needs no rounding to verify.
  Scenario Outline: salary and accumulated rent combine into one 43%-of-gross payment
    Given pawn "dog" has collected $<rent> in rent since their last unified income tax assessment
    When pawn "dog" collects a salary of $<salary>
    Then the government's account holds $<government_account>

    Examples:
      | salary | rent    | government_account |
      | 200    | 649.12  | 430.00              |

  # unified-income-tax-2
  # With no rent accumulated, this reduces to exactly MegaCorp's own
  # 43%-of-gross salary tax - $150.88 on a $200 net collection, $301.75 on
  # the $400 the optional double-salary-on-landing rule pays (mirroring
  # both rows of megacorp-salary-tax-1) - the two mechanisms agree on the
  # salary-only figure for either amount, even though each computes it
  # independently.
  Scenario Outline: with no rent collected, the tax reduces to the same figure MegaCorp's own tax would charge on salary alone
    When pawn "dog" collects a salary of $<salary>
    Then the government's account holds $<government_account>

    Examples:
      | salary | government_account |
      | 200    | 150.88              |
      | 400    | 301.75              |

  # unified-income-tax-3
  # Two independent $200-salary, no-rent events: 150.88 + 150.88 = 301.76,
  # the same total megacorp-salary-tax-2 already establishes for the
  # equivalent MegaCorp-only scenario.
  Scenario Outline: payments for multiple players accumulate together in the same government account
    When pawn "dog" collects a salary of $200
    And pawn "high hat" collects a salary of $200
    Then the government's account holds $<government_account>

    Examples:
      | government_account |
      | 301.76              |

  # unified-income-tax-4
  # First collection taxes $1000 combined (as in scenario 1) for $430.00;
  # the second, with no new rent given since, taxes only that
  # collection's $350.88 gross salary for $150.88 more - proving the rent
  # accumulator reset to zero after the first assessment rather than
  # still counting the original $649.12 a second time.
  Scenario Outline: the rent-collected counter resets to zero after each assessment, so a later quiet collection is not taxed on the old total again
    Given pawn "dog" has collected $<rent> in rent since their last unified income tax assessment
    When pawn "dog" collects a salary of $<salary>
    And pawn "dog" collects a salary of $<salary>
    Then the government's account holds $<government_account>

    Examples:
      | salary | rent    | government_account |
      | 200    | 649.12  | 580.88              |

  # unified-income-tax-5
  # War profits tax's own accumulator ($5000, at a land value chosen to
  # land in the 100% band so its tax is easy to verify) and unified
  # income tax's accumulator ($649.12, chosen as in unified-income-tax-1
  # so the combined base is an exact $1000 and its tax an exact $430) are
  # set to deliberately different figures, so a shared or conflated
  # accumulator would produce a visibly wrong total on at least one side.
  Scenario Outline: enabling this alongside war profits tax taxes the same rent twice, from two independent accumulators
    Given the war profits tax is enabled
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
    When pawn "dog" collects a salary of $<salary>
    Then MegaCorp pays the government an individual income tax of $<megacorp_tax>
    And the government's account holds $<government_account>

    Examples:
      | salary | megacorp_tax | government_account |
      | 200    | 150.88        | 301.76               |

  # unified-income-tax-7
  # Numbered 7 rather than 5 to leave room for the two interaction
  # scenarios already reserved in this feature's backlog file
  # (unified-income-tax-5/6). Mirrors megacorp-salary-tax-3 exactly - same
  # starting position, dice, and $400 landing-on-Start salary, reducing
  # to the same $301.75 figure unified-income-tax-2 already establishes
  # for a $400 salary with no rent involved - but reached through an
  # actual played mini-game exercising the real double-salary rule,
  # rather than an isolated "collects a salary of $400" input. Needs no
  # narration specific to this tax at all: both assertions read journal
  # entries that already exist generically (`SalaryCollected`,
  # `GovernmentBalance`), so unlike the payment-narration scenarios
  # backlogged for journal/report/logging, this one only needs the core
  # tax computation to exist, not any new observability wiring.
  Scenario Outline: the tax scales with the real double-salary rule when a pawn lands exactly on Start
    Given with optional double salary when landing on Start rule
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that the government's final account balance is $<government_account>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | government_account |
      | 35                   | 2          | 3          | 400         | 301.75                |
