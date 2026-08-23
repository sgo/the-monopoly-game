# mutation-stamp: sha256=b655da1cb0a8e4e4ac1860ccb2cdb79bf1b6c71838c4b6cbca087df457fcb540
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-23T16:17:00.010727Z","feature_name":"turn-loop","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/turn-loop.feature","background_hash":"d393cd2ad76babc786d9464e9f2cbd2d33b5de531a11eda2744728a563e5e9ca","implementation_hash":"unknown","scenarios":[{"index":1,"name":"landing on Income Tax charges its fixed amount","scenario_hash":"170e5cab90e1603b3deb43d6d2bd592b9dc3fe704cb1f67e92594c300ab80e01","mutation_count":5,"result":{"Total":5,"Killed":5,"Survived":0,"Errors":0},"tested_at":"2026-07-29T07:33:21.900501Z"},{"index":0,"name":"the game plays a turn for every player, each moved by their own rolls","scenario_hash":"0cdbea2858bb3e65391cd19b3616ab85588a36a90c2c3489ba082263ca7e2190","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-07-27T21:28:13.690899Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: turn-loop

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative

  # turn-loop-1
  Scenario Outline: the game plays a turn for every player, each moved by their own rolls
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And pawn "high hat" will roll <high_hat_die_1> and <high_hat_die_2> for their turn
    When we play the game
    Then pawn "dog" is at position <dog_final_position>
    And pawn "high hat" is at position <high_hat_final_position>

    Examples:
      | dog_die_1 | dog_die_2 | dog_final_position | high_hat_die_1 | high_hat_die_2 | high_hat_final_position |
      | 2         | 3         | 5                  | 6              | 5              | 11                      |

  # turn-loop-2
  Scenario Outline: landing on Income Tax charges its fixed amount
    And with $<starting_balance> in pawn "dog"'s account
    And pawn "dog" will roll <dog_die_1> and <dog_die_2> for their turn
    And every other player can complete their turn
    When we play the game
    Then pawn "dog" is at position <dog_final_position>
    And pawn "dog"'s account balance is $<final_balance>

    Examples:
      | starting_balance | dog_die_1 | dog_die_2 | dog_final_position | final_balance |
      | 1500             | 1         | 3         | 4                  | 1300          |
