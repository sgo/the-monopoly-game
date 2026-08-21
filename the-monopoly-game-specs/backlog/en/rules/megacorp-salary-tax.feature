# language: en

# Backlogged from ../../../the-monopoly-game-specs-core/src/test/resources/
# en/rules/megacorp-salary-tax.feature's round 1, which proves the 43%
# arithmetic in isolation. This scenario instead proves the real
# double-salary-on-landing rule (movement.feature-2) actually produces a
# $400 salary during a live played game, and that MegaCorp's payment scales
# with it for real - not just as a given parameter the way round 1's
# $<salary> column stands in for it.
#
# This needs new wiring first: today a live played game's actual government
# account is never read back out anywhere - "the government's final account
# balance" that journal-86/89 already assert is populated from the same
# isolated fixture field war-profits-tax's "grows a year older" shortcut
# writes to, not from anything a real play does. Exposing the real balance
# through Game.Result the way deeds and the journal already are is a
# prerequisite this scenario shares with war-profits-tax, not something new
# this feature introduces.

Feature: megacorp salary tax

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And rent relief is enabled
    And with optional double salary when landing on Start rule

  # megacorp-salary-tax-3
  Scenario Outline: MegaCorp's 43% payment scales with the real double-salary rule when a pawn lands exactly on Start
    Given pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that the government's final account balance is $<government_account>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | government_account |
      | 35                   | 2          | 3          | 400         | 172                  |
