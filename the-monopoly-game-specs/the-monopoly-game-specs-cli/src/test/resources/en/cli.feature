# mutation-stamp: sha256=0a9e9b65bfc3127ceec8a41bca2cd65fdad7cddc2cc83dcc32d13445dee262b0
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-29T14:25:37.309297Z","feature_name":"Monopoly command line interface","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-cli/src/test/resources/en/cli.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":2,"name":"the CLI rejects a player count outside the official range","scenario_hash":"8220cf97c61e66a72d6f7c66614fea3b6320a646e80c5ab1d72317d4a02edc75","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:45:46.154201Z"},{"index":0,"name":"the CLI runs a complete game with the default strategy","scenario_hash":"faf2e3fadbfc5efb5170144de99cda0bf43a41a98ee2cb5f7b7385744d92e5dd","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:40:13.247310Z"},{"index":1,"name":"the CLI runs a complete game with selected player strategies","scenario_hash":"67c5a3b977f2703c80f4961d0dacea7c427404bba17aca6a879e13f93bed386b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-29T13:40:13.247310Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: Monopoly command line interface

  # cli-1
  Scenario Outline: the CLI runs a complete game with the default strategy
    Given the simulator is configured for <players> players without strategy choices
    When I run the simulator
    Then the simulator exits successfully
    And every player starts with $<starting balance> before the first turn
    And the output contains a human-readable game report
    And the report contains a bankruptcy before the game's winner
    And the report contains the game's winner

    Examples:
      | players | starting balance |
      | 2       | 1500             |

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

  # cli-4
  Scenario Outline: the CLI applies the standard game setup before play
    Given the simulator is configured for <players> players without strategy choices
    When I run the simulator
    Then the simulator uses the standard game setup
    And every simulated player starts at position <starting position>
    And every simulated player starts with $<starting balance>
    And no simulated player owns any street
    And no simulated player has any house or hotel
    And all Chance cards are available in the Chance deck
    And all Community Chest cards are available in the Community Chest deck
    And no simulated player holds a Get Out of Jail Free card

    Examples:
      | players | starting position | starting balance |
      | 2       | 0                 | 1500             |

  # cli-5
  Scenario Outline: the CLI plays a real game to its natural terminal state
    Given the simulator is configured for <players> players without strategy choices
    When I run the simulator
    Then the simulator uses real random dice
    And the game continues until all but one player are bankrupt
    And the simulator does not impose a turn limit
    And the report contains no synthetic winner

    Examples:
      | players |
      | 2       |
