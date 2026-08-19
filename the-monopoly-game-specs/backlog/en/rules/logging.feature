# language: en

# Backlog scenarios for the war-profits-tax feature (see
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/war-profits-tax.feature,
# also backlogged). When this batch is promoted, move these scenarios into
# the tracked en/rules/logging.feature unchanged, renumbering logging-N to
# continue that file's own sequence (next free index at the time these were
# written was logging-85).

Feature: game logging

  # logging-85
  Scenario Outline: the log records a player's war profits tax payment
    Given the war profits tax is enabled
    And pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    When pawn "dog" grows a year older
    Then the game log records that pawn "dog" pays the government a war profits tax of $<tax>

    Examples:
      | land_value | collected | tax  |
      | 6000       | 1000      | 1000 |

  # logging-86
  Scenario Outline: the log records the government's final account balance once the game ends
    Given the war profits tax is enabled
    And pawn "dog"'s land is currently worth $<land_value> in rent
    And pawn "dog" has collected $<collected> in rent since their last war profits tax assessment
    And pawn "dog" grows a year older
    When we play up to 1 round
    Then the game log records that the government's final account balance is $<balance>

    Examples:
      | land_value | collected | balance |
      | 6000       | 1000      | 1000    |

  # logging-87
  Scenario Outline: the log records that the war profits tax is enabled, near the start of the game
    Given the war profits tax is enabled
    When we play the game
    Then the game log records that the war profits tax is <state>

    Examples:
      | state   |
      | enabled |

  # logging-88
  Scenario Outline: the log records that the war profits tax is disabled by default, near the start of the game
    When we play the game
    Then the game log records that the war profits tax is <state>

    Examples:
      | state    |
      | disabled |
