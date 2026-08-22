# language: en

# Backlogged additions to the tracked
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/
# report.feature, continuing its scenario numbering. Mirrors
# ../en/rules/journal.feature's backlog scenarios, same numbers, "the game
# report says that" in place of "the game journal records that".
# report-92/93 needed no new wiring and have been promoted to the tracked
# file, same as journal.feature's backlog.

Feature: game report

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # report-90
  Scenario Outline: the report narrates MegaCorp's payment alongside a player's salary
    Given rent relief is enabled
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" collects a salary of $<dog_salary>
    And the game report says that MegaCorp pays the government an individual income tax of $<megacorp_payment>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | megacorp_payment |
      | 37                  | 1          | 2          | 200         | 86                 |

  # report-91
  Scenario Outline: the report narrates rent relief capping what a tenant pays and the government covering the rest
    Given rent relief is enabled
    And pawn "dog" owns "Rue de Diekirch Arlon"
    And the street "Rue de Diekirch Arlon" has a hotel built
    And the government's account already holds $<government_start>
    And pawn "high hat" has $1500 to spend
    When pawn "high hat" lands on "Rue de Diekirch Arlon"
    Then the game report says that pawn "high hat" pays pawn "dog" $200 rent for "Rue de Diekirch Arlon"
    And the game report says that the government pays pawn "dog" $<relief> in rent relief

    Examples:
      | government_start | relief |
      | 550                | 550     |
