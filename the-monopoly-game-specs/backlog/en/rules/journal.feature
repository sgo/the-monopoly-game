# language: en

# Not yet batched. Mirrors journal-90/92/93's exact shape for the new
# unified-income-tax feature, once it exists: the flag's enabled/
# disabled-near-start state, and the tax payment narrated alongside the
# salary collection that triggered it. Reuses journal-90's own numbers
# (dog starting at position 37, rolling 1 and 2, collecting $200 salary)
# since this scenario's purpose is proving the narration line exists, not
# re-verifying the computation unified-income-tax-2 already covers for
# that exact figure ($150.88, the no-rent-collected case).

Feature: journal

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn

  # journal-96
  Scenario Outline: the journal records that unified income tax is enabled, near the start of the game
    Given the unified income tax is enabled
    When we play the game
    Then the game journal records that unified income tax is <state>

    Examples:
      | state   |
      | enabled |

  # journal-97
  Scenario Outline: the journal records that unified income tax is disabled by default, near the start of the game
    When we play the game
    Then the game journal records that unified income tax is <state>

    Examples:
      | state    |
      | disabled |

  # journal-98
  Scenario Outline: the journal records the unified income tax payment alongside a player's salary
    Given the unified income tax is enabled
    And pawn "dog" starts at position <dog_start_position>
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    When we play the game
    Then the game journal records that pawn "dog" collects a salary of $<dog_salary>
    And the game journal records that pawn "dog" pays the government a unified income tax of $<unified_tax>

    Examples:
      | dog_start_position | dog_die_1 | dog_die_2 | dog_salary | unified_tax |
      | 37                  | 1          | 2          | 200         | 150.88       |
