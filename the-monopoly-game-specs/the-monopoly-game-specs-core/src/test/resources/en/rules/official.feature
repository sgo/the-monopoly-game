# language: en

Feature: official rules

  Scenario: the game is played with 2 dice with 6 faces
    Given the official rule set
    Then we play with the following dice
      | type    |
      | 6 faced |
      | 6 faced |
