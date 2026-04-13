# language: en

Feature: official rules

  Scenario: the game is played with 2 dice with 6 faces
    Given the official rule set
    Then we play with the following dice
      | type    |
      | 6 faced |
      | 6 faced |

  Scenario: the game is played with a minimum of 2 to a maximum of 8 players
    Given the official rule set
    Then we play with 2 to 8 players

  Scenario: players receive a salary when passing by start
    Given the official rule set
    And a player
    * with $1500 in his account
    When the player passes the street "Start"
    Then the player's account balance is $1700

  Scenario: players receive a double salary when landing on start
    Given the official rule set
    * with optional double salary when landing on Start rule
    And a player
    * with $1500 in his account
    When the player lands on the street "Start"
    Then the player's account balance is $1900