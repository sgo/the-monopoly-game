# language: en

Feature: Monopoly command line interface

  # cli-1
  Scenario Outline: the CLI runs a complete game with the default strategy
    Given the simulator is configured for <players> players without strategy choices
    When I run the simulator
    Then the simulator exits successfully
    And the output contains a human-readable game report
    And the report contains the game's winner

    Examples:
      | players |
      | 2       |

  # cli-2
  Scenario Outline: the CLI runs a complete game with selected player strategies
    Given the simulator is configured for <players> players
    And every player selects the "Agree if affordable" strategy
    When I run the simulator
    Then the simulator exits successfully
    And the output contains a human-readable game report
    And the report contains the game's winner

    Examples:
      | players |
      | 8       |

  # cli-3
  Scenario Outline: the CLI rejects a player count outside the official range
    Given the simulator is configured for <players> players
    When I run the simulator
    Then the simulator exits unsuccessfully
    And the output explains that the number of players must be between 2 and 8

    Examples:
      | players |
      | 1       |
      | 9       |
