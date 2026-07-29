# language: en

Feature: standard game setup

  # setup-1
  Scenario Outline: the standard game setup gives every player the official starting state
    Given the standard game setup
    When we select <players> players
    Then every selected player starts at position <starting position>
    And every selected player has $<starting balance>
    And no selected player owns any street
    And no selected player has any house or hotel

    Examples:
      | players | starting position | starting balance |
      | 2       | 0                 | 1500             |

  # setup-2
  Scenario Outline: the standard game setup leaves the bank and card decks complete
    Given the standard game setup
    When we select <players> players
    Then the bank owns every ownable space
    And the bank has all houses
    And the bank has all hotels
    And all Chance cards are available in the Chance deck
    And all Community Chest cards are available in the Community Chest deck
    And no selected player holds a Get Out of Jail Free card

    Examples:
      | players |
      | 2       |
