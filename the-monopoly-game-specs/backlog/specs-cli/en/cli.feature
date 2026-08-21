# language: en

# Backlogged additions to the tracked
# ../../../the-monopoly-game-specs-cli/src/test/resources/en/cli.feature,
# continuing its scenario numbering. Mirrors cli-14 and cli-15, the
# war-profits-tax flag's own CLI-wiring scenarios.

Feature: Monopoly command line interface

  # cli-16
  Scenario Outline: the CLI wires the rent-relief flag, game-wide rather than to any one strategy
    Given the simulator is configured with the raw arguments "<raw arguments>"
    When I start the simulator
    Then the game journal records that rent relief is <state>
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | raw arguments                        | state   |
      | 2 greedo greedo --optional-rent-relief | enabled |

  # cli-17
  Scenario Outline: the rent-relief flag applies the same way regardless of which strategies are playing
    Given the simulator is configured with the raw arguments "<raw arguments>"
    When I start the simulator
    Then the game journal records that pawn "<billionaire pawn>" uses the "Billionaire" strategy
    And the game journal records that pawn "<greedo pawn>" uses the "Greedo" strategy
    And the game journal records that rent relief is <state>
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | raw arguments                              | billionaire pawn | greedo pawn | state   |
      | 2 greedo billionaire --optional-rent-relief | high hat         | dog         | enabled |
