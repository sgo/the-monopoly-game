# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-31T19:49:36.349925Z","feature_name":"Monopoly","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/monopoly.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[]}
# acceptance-mutation-manifest-end

# language: en

Feature: Monopoly

  # monopoly-1
  Scenario Outline: the game always ends in a monopoly
    Given the official rule set
    And we select <players> players
    When we play 10 times
    Then the game ends every time with a monopoly

    Examples:
      | players |
      | 8       |
