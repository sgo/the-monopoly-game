# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-03T19:38:03.401236Z","feature_name":"Monopoly command line interface","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-cli/src/test/resources/en/cli.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":2,"name":"the CLI rejects a player count outside the official range","scenario_hash":"8220cf97c61e66a72d6f7c66614fea3b6320a646e80c5ab1d72317d4a02edc75","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:45:46.154201Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: Monopoly command line interface

  # cli-1
  Scenario Outline: the CLI runs a real game with the default strategy until it is stopped
    Given the simulator is configured for <players> players without strategy choices
    When I start the simulator
    Then the game log records that the game starts
    And the game log records at least <minimum rolls> rolls
    When I stop the simulator before the game ends
    Then the simulator process ends
    And the game log records no winner

    Examples:
      | players | minimum rolls |
      | 2       | 10             |

  # cli-2
  Scenario Outline: the CLI runs a real game with selected player strategies until it is stopped
    Given the simulator is configured for <players> players
    And every player selects the "Greedo" strategy
    When I start the simulator
    Then the game log records that the game starts
    And the game log records at least <minimum rolls> rolls
    When I stop the simulator before the game ends
    Then the simulator process ends
    And the game log records no winner

    Examples:
      | players | minimum rolls |
      | 8       | 50             |

  # cli-3
  Scenario Outline: the CLI rejects a player count outside the official range
    Given the simulator is configured for <players> players
    When I run the simulator
    Then the simulator exits unsuccessfully
    And the output explains that the number of players must be between 2 and 8 and received <reported players> players

    Examples:
      | players | reported players |
      | 1       | 1                |
      | 9       | 9                |

  # cli-4 held back: the simulator never reaches the standard game setup
  # assertions (no handlers were ever implemented for them), and in the
  # bounded-time design the simulator is stopped before the game ends, so the
  # pre-play state it asserts (position 0, $1500, no streets, no houses,
  # complete decks, no Get Out of Jail Free card) cannot be inspected on a
  # killed process. Those invariants are covered by the domain features in
  # specs-core. Re-enable when the journal records the setup state.
  #
  # # cli-4
  # Scenario Outline: the CLI applies the standard game setup before play
  #   Given the simulator is configured for <players> players without strategy choices
  #   When I run the simulator
  #   Then the simulator uses the standard game setup
  #   And every simulated player starts at position <starting position>
  #   And every simulated player starts with $<starting balance>
  #   And no simulated player owns any street
  #   And no simulated player has any house or hotel
  #   And all Chance cards are available in the Chance deck
  #   And all Community Chest cards are available in the Community Chest deck
  #   And no simulated player holds a Get Out of Jail Free card
  #
  #   Examples:
  #     | players | starting position | starting balance |
  #     | 2       | 0                 | 1500             |

  # cli-5
  Scenario Outline: the CLI plays with real dice without a turn limit or synthetic winner
    Given the simulator is configured for <players> players without strategy choices
    When I start the simulator
    Then the game log records at least <minimum rolls> rolls of a total between 2 and 12
    And the game log records at least two different roll totals
    And the game log records no winner
    And the simulator is still playing when the game log has recorded <minimum rolls> rolls
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | players | minimum rolls |
      | 2       | 10             |
