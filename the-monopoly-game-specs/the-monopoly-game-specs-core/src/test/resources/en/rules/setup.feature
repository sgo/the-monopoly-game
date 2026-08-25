# mutation-stamp: sha256=a7d88caadff761a94cd881f78c291f4e2da13b7f730f8c990d6c22be5af690bc
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T08:22:02.538739Z","feature_name":"standard game setup","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/setup.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the standard game setup gives every player the official starting state","scenario_hash":"54f7d3d12f7dd599160d455db10d6ee249feff593d47966f29637d20d9868299","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-08-21T09:19:23.125282Z"},{"index":1,"name":"the standard game setup leaves the bank and card decks complete","scenario_hash":"a44fc637b62439be9593de7d7a6d448143fde4051135393c49f0f152b6ccd6af","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-21T09:19:23.125282Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: standard game setup

  # setup-1
  Scenario Outline: the standard game setup gives every player the official starting state
    Given the standard game setup
    When we select <players> players
    Then exactly <expected players> players are selected
    And every selected player starts at position <starting position>
    And every selected player has $<starting balance>
    And no selected player owns any street
    And no selected player has any house or hotel

    Examples:
      | players | expected players | starting position | starting balance |
      | 2       | 2                  | 0                 | 1500             |

  # setup-2
  Scenario Outline: the standard game setup leaves the bank and card decks complete
    Given the standard game setup
    When we select <players> players
    Then exactly <expected players> players are selected
    And the bank owns every ownable space
    And the bank has all houses
    And the bank has all hotels
    And all Chance cards are available in the Chance deck
    And all Community Chest cards are available in the Community Chest deck
    And no selected player holds a Get Out of Jail Free card

    Examples:
      | players | expected players |
      | 2       | 2                  |
