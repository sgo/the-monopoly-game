# language: en

# Backlog scenarios for the war-profits-tax feature (now fully tracked at
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/war-profits-tax.feature).
# When this batch is promoted, move these scenarios into the tracked
# the-monopoly-game-specs-cli/src/test/resources/en/cli.feature unchanged,
# renumbering cli-N to continue that file's own sequence (next free index
# at the time these were written was cli-14).

Feature: Monopoly command line interface

  # cli-14
  Scenario Outline: the CLI wires the war-profits-tax flag, game-wide rather than to any one strategy
    Given the simulator is configured with the raw arguments "<raw arguments>"
    When I start the simulator
    Then the game journal records that the war profits tax is <state>
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | raw arguments                             | state   |
      | 2 greedo greedo --optional-war-profits-tax | enabled |

  # cli-15
  Scenario Outline: the war-profits-tax flag applies the same way regardless of which strategies are playing
    Given the simulator is configured with the raw arguments "<raw arguments>"
    When I start the simulator
    Then the game journal records that pawn "<billionaire pawn>" uses the "Billionaire" strategy
    And the game journal records that pawn "<greedo pawn>" uses the "Greedo" strategy
    And the game journal records that the war profits tax is <state>
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | raw arguments                                   | billionaire pawn | greedo pawn | state   |
      | 2 greedo billionaire --optional-war-profits-tax | high hat         | dog         | enabled |
