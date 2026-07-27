# mutation-stamp: sha256=0c1a39d2cbf44cb3766df42e89c80e3b1e3bdcc5cf24c25b813d8a8304cc7fd7
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-07-27T09:08:52.878221Z","feature_name":"dice","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/components/dice.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[]}
# acceptance-mutation-manifest-end

# language: en

Feature: dice

  # dice-1
  Scenario: each side has an equal chance to be rolled
    Given a 6 faced dice
    When I roll the dice 600000 times
    Then each face was rolled about 100000 times within a 1% margin
