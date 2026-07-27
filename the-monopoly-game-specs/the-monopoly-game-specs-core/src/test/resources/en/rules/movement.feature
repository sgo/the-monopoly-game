# language: en

Feature: movement

  Background:
    Given the official rule set
    And a player

  # movement-1
  Scenario Outline: moving without passing start
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <die 1> and <die 2>
    When the player takes a turn
    Then the player is at position <final position>
    And the player's account balance is $<final balance>

    Examples:
      | starting balance | starting position | die 1 | die 2 | final position | final balance |
      | 1500              | 5                  | 2     | 3     | 10             | 1500          |

  # movement-2
  Scenario Outline: the double salary rule applies only when a turn lands exactly on start
    And with optional double salary when landing on Start rule
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <die 1> and <die 2>
    When the player takes a turn
    Then the player is at position <final position>
    And the player's account balance is $<final balance>

    Examples:
      | starting balance | starting position | die 1 | die 2 | final position | final balance |
      | 1500              | 37                 | 1     | 2     | 0              | 1900          |
      | 1500              | 37                 | 2     | 3     | 2              | 1700          |

  # movement-3
  Scenario Outline: rolling doubles grants another roll in the same turn
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <double roll> and <double roll>
    And the next roll will be <second roll die 1> and <second roll die 2>
    When the player takes a turn
    Then the player is at position <final position>

    Examples:
      | starting balance | starting position | double roll | second roll die 1 | second roll die 2 | final position |
      | 1500              | 0                  | 3           | 2                  | 4                  | 12             |

  # movement-4
  Scenario Outline: rolling doubles three times in a row sends the player directly to jail
    And with $<starting balance> in his account
    And the player is at position <starting position>
    And the next roll will be <first double> and <first double>
    And the next roll will be <second double> and <second double>
    And the next roll will be <third double> and <third double>
    When the player takes a turn
    Then the player is at position 10
    And the player's account balance is $<starting balance>

    Examples:
      | starting balance | starting position | first double | second double | third double |
      | 1500              | 0                  | 2             | 5              | 1            |
