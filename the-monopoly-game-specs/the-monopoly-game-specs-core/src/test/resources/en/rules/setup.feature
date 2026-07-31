# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-31T19:48:10.720054Z","feature_name":"standard game setup","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/setup.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[]}
# acceptance-mutation-manifest-end

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
