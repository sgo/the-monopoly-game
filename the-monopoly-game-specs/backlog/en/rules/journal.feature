# language: en

# Backlogged additions to the tracked
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/
# journal.feature, continuing its scenario numbering. Mirrors the pattern
# war-profits-tax-85 through 89 already established for its own
# observability round. journal-90 reuses journal-3's exact real-turn
# trigger; journal-91 needs the same real rent-event wiring flagged in
# ../en/rules/rent-relief.feature's backlog. journal-92/93 (the
# flag-enabled-near-start-of-game pair, mirroring journal-87/88) needed no
# such wiring and have been promoted to the tracked file.

Feature: game journal

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # journal-90
  Scenario Outline: the journal records MegaCorp's payment alongside a player's salary
    Given rent relief is enabled
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that MegaCorp pays the government an individual income tax of $<megacorp_payment>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | megacorp_payment |
      | 37                  | 1          | 2          | 200         | 86                 |

  # journal-91
  Scenario Outline: the journal records rent relief capping what a tenant pays and the government covering the rest
    Given rent relief is enabled
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the government's account already holds $<government_start>
    And pawn "high hat" has $1500 to spend
    When pawn "high hat" lands on "Rue de Diekirch Arlon"
    Then the game journal records that pawn "high hat" pays pawn "dog" $200 rent for "Rue de Diekirch Arlon"
    And the game journal records that the government pays pawn "dog" $<relief> in rent relief

    Examples:
      | government_start | relief |
      | 550                | 550     |
