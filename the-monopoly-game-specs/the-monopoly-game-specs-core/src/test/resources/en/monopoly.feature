# language: en

Feature: Monopoly

  # monopoly-1
  Scenario Outline: the game always ends in a monopoly
    Given the official rule set
    And we select <players> players
    When we play 1000 times
    Then the game ends every time with a monopoly

    Examples:
      | players |
      | 8       |
