# language: en

# This is the funding half of a pair with ../../en/rules/rent-relief.feature,
# which spends from the same account. Both scenarios here are isolated
# computations, the same way war-profits-tax's band table is: a fresh
# "collects a salary" trigger applies the 43% MegaCorp payment directly,
# without running a played game. Observability (journal/logging/report), CLI
# wiring, and proof that a live played game actually invokes this on every
# real Start-passing turn are backlogged at
# ../../backlog/en/rules/megacorp-salary-tax.feature,
# ../../backlog/en/rules/journal.feature, logging.feature, report.feature,
# and ../../backlog/specs-cli/en/cli.feature, cli-packaged-jar.feature.

Feature: megacorp salary tax
  An opt-in flag, `--optional-rent-relief`, has every player's salary paid
  by a notional employer, MegaCorp, rather than the bank. MegaCorp pays the
  government an individual income tax of 43% of the salary on top of what
  the player collects, so the player's own take is unchanged. This grows
  the same government account that the war profits tax pays into and that
  rent relief spends from.

  The payment is proportional to whatever salary was actually collected, so
  it applies the same way whether a player collected the ordinary $200 for
  passing Start or the $400 the optional double-salary-on-landing rule pays
  for landing exactly on it.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And rent relief is enabled

  # megacorp-salary-tax-1
  Scenario Outline: collecting a salary also pays MegaCorp's 43% share into the government account, without changing what the player collects
    Given pawn "dog" has $1500 to spend
    When pawn "dog" collects a salary of $<salary>
    Then pawn "dog"'s account balance is $<final_balance>
    And MegaCorp pays the government an individual income tax of $<government_account>

    Examples:
      | salary | final_balance | government_account |
      | 200     | 1700           | 86                   |
      | 400     | 1900           | 172                  |

  # megacorp-salary-tax-2
  Scenario Outline: MegaCorp's payments for multiple players accumulate together in the same government account
    When pawn "dog" collects a salary of $200
    And pawn "high hat" collects a salary of $200
    Then the government's account holds $<government_account>

    Examples:
      | government_account |
      | 172                  |
