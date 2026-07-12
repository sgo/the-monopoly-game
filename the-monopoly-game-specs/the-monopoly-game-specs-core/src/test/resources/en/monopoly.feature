# language: en

Feature: Monopoly

  Scenario: the game always ends in a monopoly
    Given the official rule set
    And we select 8 players
    When we play 1000 times
    Then the game ends every time with a monopoly
