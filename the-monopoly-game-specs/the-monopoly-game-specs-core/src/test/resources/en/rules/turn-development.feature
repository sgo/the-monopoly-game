# mutation-stamp: sha256=31cafa480942d996697fd043e2474c81d4ccd9d99c5c2b1190a9840c7a18ebd6
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T07:37:59.661935Z","feature_name":"development is not limited to whoever won initiative","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/turn-development.feature","background_hash":"4e201472a993e81042043ca60f95171c250b1440edcd305129806024d51291e6","implementation_hash":"unknown","scenarios":[{"index":0,"name":"a player who did not win initiative still builds evenly across a full colour group when affordable","scenario_hash":"696a71b1a5c036194f35b0732666f138a7565ed419cb5484a8cb8e20cf7e0508","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:23.539504Z"},{"index":1,"name":"a player who did not win initiative still builds evenly across a full colour group when legal-entity trading is enabled","scenario_hash":"f0c6e50a2b1c985d301268dfc08f9a794f0bb34b18e14ff4ab2ac0c7f6f6f8ef","mutation_count":3,"result":{"Total":3,"Killed":3,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:11:23.539504Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: development is not limited to whoever won initiative
  Building a house or hotel is a decision every player makes on their own
  turn, the same as buying land or claiming rent — it must not depend on
  who happened to win the initiative roll at the start of the game. This
  holds whether or not legal-entity trading is enabled: a player's own
  development is itself a legitimate consolidating action the market-
  deadlock detector needs to see, not something to suppress just to force
  a false "quiet" round that auto-forms an entity nobody actually needed.

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "high hat" will roll 10 for initiative
    And pawn "dog" will roll 4 for initiative
    And every other player can complete their turn

  # turn-development-1
  Scenario Outline: a player who did not win initiative still builds evenly across a full colour group when affordable
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | strategy | houses | expected_final_balance |
      | Greedo   | 1      | 0                       |

  # turn-development-2
  Scenario Outline: a player who did not win initiative still builds evenly across a full colour group when legal-entity trading is enabled
    Given legal-entity trading is enabled for the "<strategy>" strategy
    And pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And pawn "dog" follows the "<strategy>" strategy
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | strategy | houses | expected_final_balance |
      | Greedo   | 1      | 0                       |
