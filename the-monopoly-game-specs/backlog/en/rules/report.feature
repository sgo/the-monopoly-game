# language: en

# Not yet batched. Mirrors report-90/92/93's exact shape for the new
# unified-income-tax feature, once it exists - identical to
# journal.feature's own backlog entry except for the "report says"
# phrasing, per this project's established one-scenario-per-mechanic-
# per-file convention.

Feature: report

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # report-96
  Scenario Outline: the report narrates that unified income tax is enabled, near the start of the game
    Given the unified income tax is enabled
    When we play the game
    Then the game report says that unified income tax is <state>

    Examples:
      | state   |
      | enabled |

  # report-97
  Scenario Outline: the report narrates that unified income tax is disabled by default, near the start of the game
    When we play the game
    Then the game report says that unified income tax is <state>

    Examples:
      | state    |
      | disabled |

  # report-98
  Scenario Outline: the report narrates the unified income tax payment alongside a player's salary
    Given the unified income tax is enabled
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game report says that pawn "dog" collects a salary of $<dog_salary>
    And the game report says that pawn "dog" pays the government a unified income tax of $<unified_tax>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | unified_tax |
      | 37                  | 1          | 2          | 200         | 150.88       |
