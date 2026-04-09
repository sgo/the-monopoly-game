# language: en

Feature: official rules

  Scenario: the game is played with 2 dice with 6 faces
    Given the official rule set
    Then we play with the following dice
      | type    |
      | 6 faced |
      | 6 faced |

  Scenario: players receive a salary when passing by start
    Given the official rule set
    And a player
    * with $1500 in his account
    When the player passes the street "Start"
    Then the player's account balance is $1700