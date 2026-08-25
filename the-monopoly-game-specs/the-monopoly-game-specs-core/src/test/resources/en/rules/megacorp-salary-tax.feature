# mutation-stamp: sha256=9ec59726ea6154a21efb631f6fa7c00ac8f61b1d62b48dfae05c2cb9d350c932
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T07:38:13.005738Z","feature_name":"megacorp salary tax","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/megacorp-salary-tax.feature","background_hash":"8b52bb2a0a6f53558853cb174011d865171e1a4b41c107899c959054c299f158","implementation_hash":"unknown","scenarios":[{"index":0,"name":"collecting a salary also pays MegaCorp's 43%-of-gross share into the government account, without changing what the player collects","scenario_hash":"1514c7c351b33a41f0522ceb2c46e2c536ca55952f127926322eecfc47f67b70","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-24T22:04:31.140686Z"},{"index":1,"name":"MegaCorp's payments for multiple players accumulate together in the same government account","scenario_hash":"3e6df01b071eb05e0b3fee2a5e344fba12b1a6b6c5dc3f7773842c184b0e82d2","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-24T22:04:31.140686Z"},{"index":2,"name":"MegaCorp's 43%-of-gross payment scales with the real double-salary rule when a pawn lands exactly on Start","scenario_hash":"bd132f1e541188f4f8d21085d2ec00f546dcaf4ec0c27860ec18c1baf079fd68","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-08-24T22:04:31.140686Z"}]}
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
  by a notional employer, MegaCorp, rather than the bank. The salary a
  player actually collects (the ordinary $200, or the $400 the optional
  double-salary-on-landing rule pays) is the net amount after a 43%
  individual income tax on the gross: net is 57% of gross, so MegaCorp
  pays the government tax = net / 0.57 - net, keeping the player's own
  take unchanged at the net figure. This grows the same government
  account that the war profits tax pays into and that rent relief spends
  from.

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
  # 43% of gross, where the collected salary is the 57% net: tax = salary
  # / 0.57 - salary. $200 -> $150.88, $400 -> $301.75 (both to the cent,
  # rounded half-to-even same as Money.percentage()). The player's own
  # balance is unaffected by this change - MegaCorp still pays the tax on
  # top, not out of what the player collects.
  Scenario Outline: collecting a salary also pays MegaCorp's 43%-of-gross share into the government account, without changing what the player collects
    Given pawn "dog" has $1500 to spend
    When pawn "dog" collects a salary of $<salary>
    Then pawn "dog"'s account balance is $<final_balance>
    And MegaCorp pays the government an individual income tax of $<government_account>

    Examples:
      | salary | final_balance | government_account |
      | 200     | 1700           | 150.88               |
      | 400     | 1900           | 301.75               |

  # megacorp-salary-tax-2
  # Two independent $200 events, each rounded to the cent on its own
  # (150.88 + 150.88 = 301.76), not the $400-combined figure from
  # megacorp-salary-tax-1's second row (301.75) - rounding happens
  # per salary event, not on a pooled total.
  Scenario Outline: MegaCorp's payments for multiple players accumulate together in the same government account
    When pawn "dog" collects a salary of $200
    And pawn "high hat" collects a salary of $200
    Then the government's account holds $<government_account>

    Examples:
      | government_account |
      | 301.76               |

  # megacorp-salary-tax-3
  Scenario Outline: MegaCorp's 43%-of-gross payment scales with the real double-salary rule when a pawn lands exactly on Start
    Given with optional double salary when landing on Start rule
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that the government's final account balance is $<government_account>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | government_account |
      | 35                   | 2          | 3          | 400         | 301.75                |
