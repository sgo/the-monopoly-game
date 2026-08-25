# mutation-stamp: sha256=ef65b1bf64e94a8ebcf19136c0d48c2ff10b1b9ea603db3d04fb27b3153231f4
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T22:21:35.740931Z","feature_name":"stalemate","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/stalemate.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[{"index":3,"name":"a lone remaining player still below the threshold blocks a stalemate call with more than two players","scenario_hash":"c9e5ccefcbff7a8f71d579a45458ecf3a8ea355cf3abe65aff663df7f8d2591a","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-06T19:04:07.824997Z"},{"index":4,"name":"a stalemate is called once every one of more than two remaining players clears the threshold","scenario_hash":"df092e650a2be96960db9ddd76e487d24e1b90c4c19bf607b65e470a1aa8b6c9","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-05T19:53:12.411404Z"},{"index":5,"name":"a stalemate stops the game outright, not just the round it was first detected in","scenario_hash":"013bccde3a9ee68ce1997313005211b20a0ee8720199d1a060e2dee6fd7e39fc","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-05T19:53:12.411404Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: stalemate

  Background:
    Given the official rule set
    And we select 2 players

  # stalemate-1
  Scenario: the stalemate detection threshold is the board's full rental value at maximum development
    Then the stalemate detection threshold is $22790

  # stalemate-2
  Scenario Outline: the game ends in a stalemate once every remaining player's balance clears the threshold
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game ends in a stalemate
    And pawn "dog" is not bankrupt
    And pawn "high hat" is not bankrupt

    Examples:
      | dog_balance | high_hat_balance |
      | 22790       | 22790             |

  # stalemate-3
  Scenario Outline: the game does not end in a stalemate while any remaining player is still below the threshold
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $22790
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play the game
    Then the game does not end in a stalemate

    Examples:
      | high_hat_balance |
      | 22789             |

  # stalemate-4
  Scenario Outline: a lone remaining player still below the threshold blocks a stalemate call with more than two players
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $22790
    And pawn "high hat"'s account holds $22790
    And pawn "iron box"'s account holds $<iron_box_balance>
    When we play the game
    Then the game does not end in a stalemate

    Examples:
      | iron_box_balance |
      | 22789             |

  # stalemate-5
  Scenario Outline: a stalemate is called once every one of more than two remaining players clears the threshold
    Given we select 3 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "iron box" will roll 2 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    And pawn "iron box"'s account holds $<iron_box_balance>
    When we play the game
    Then the game ends in a stalemate

    Examples:
      | dog_balance | high_hat_balance | iron_box_balance |
      | 22790       | 22790             | 22790             |

  # stalemate-6
  Scenario Outline: a stalemate stops the game outright, not just the round it was first detected in
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog"'s account holds $<dog_balance>
    And pawn "high hat"'s account holds $<high_hat_balance>
    When we play up to 3 rounds
    Then the game journal records that the game ends in a stalemate only once

    Examples:
      | dog_balance | high_hat_balance |
      | 22791       | 22791             |
