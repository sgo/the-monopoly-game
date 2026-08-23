# mutation-stamp: sha256=694f43ebbea04354799b37f7f7d62615df43df9b4fc289b7e1323ae7f71c1a30
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-23T00:25:36.779606Z","feature_name":"megacorp salary tax","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/megacorp-salary-tax.feature","background_hash":"8b52bb2a0a6f53558853cb174011d865171e1a4b41c107899c959054c299f158","implementation_hash":"unknown","scenarios":[{"index":0,"name":"collecting a salary also pays MegaCorp's 43% share into the government account, without changing what the player collects","scenario_hash":"1b6a0b24d45b41a4c58c52686c7a7cd3775d398042880389cb5acb1787186790","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-21T08:12:19.232160Z"},{"index":1,"name":"MegaCorp's payments for multiple players accumulate together in the same government account","scenario_hash":"11acbc7780da7356bf51f05252cf5b6fd19342b91d99276ee846ee7217a6d564","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-21T08:12:19.232160Z"}]}
# acceptance-mutation-manifest-end

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
