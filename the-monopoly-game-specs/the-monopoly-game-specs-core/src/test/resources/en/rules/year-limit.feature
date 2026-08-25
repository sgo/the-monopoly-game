# mutation-stamp: sha256=778538d05f23300f57d4900ed66976d669eaca2fcb8a3a2ed267d6826aafdf21
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T09:38:32.787402Z","feature_name":"year limit","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/year-limit.feature","background_hash":"e15f13aafcac0600c3aaaaf97d370d153eb29c5c34b3d00e93ab47602feefe9c","implementation_hash":"unknown","scenarios":[{"index":0,"name":"the game stops itself once a remaining player's age reaches the configured year limit","scenario_hash":"2c54391fe0491027a8169e8cf40b54d5592f7849e2606f5a6fdc8315e26c9c76","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:25:42.893912Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: year limit

  Background:
    Given the official rule set
    And we select 2 players

  # year-limit-1
  Scenario Outline: the game stops itself once a remaining player's age reaches the configured year limit
    Given the game is limited to <year limit> years
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    When we play the game
    Then the game ends because the year limit was reached
    And the game journal records that pawn "dog"'s final age is <year limit> years
    And pawn "dog" is not bankrupt
    And pawn "high hat" is not bankrupt

    Examples:
      | year limit |
      | 1          |

  # year-limit-2
  Scenario Outline: the game does not stop while every remaining player is still below the year limit
    Given the game is limited to <year limit> years
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    And every other player can complete their turn
    When we play the game
    Then the game does not end because the year limit was reached

    Examples:
      | year limit |
      | 2          |

  # year-limit-3
  Scenario: no year limit is the default, and the game is never stopped by one
    Given pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And pawn "dog" starts at position 35
    And pawn "dog" will roll 5 for their turn
    And every other player can complete their turn
    When we play the game
    Then the game does not end because the year limit was reached
