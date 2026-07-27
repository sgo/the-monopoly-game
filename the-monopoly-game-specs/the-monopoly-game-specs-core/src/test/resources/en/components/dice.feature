# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-27T10:12:26.473591Z","feature_name":"dice","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/components/dice.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[]}
# acceptance-mutation-manifest-end

# language: en

Feature: dice

  # dice-1
  Scenario Outline: each side has an equal chance to be rolled
    Given a 6 faced dice
    When I roll the dice 600000 times
    Then each face was rolled about <expected count> times within a <margin>% margin

    Examples:
      | expected count | margin |
      | 100000          | 1      |
