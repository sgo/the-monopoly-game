# mutation-stamp: sha256=cf13995b0b0c79ad65e0ba0da5a5785621d0acdce5ad1df3410efdfccaa84f09
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-24T18:30:47.758354Z","feature_name":"taxes","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-core/src/test/resources/en/rules/tax.feature","background_hash":"c6d5cad8f59dab38e8f82651db4579147851aa1f158315a8774f5bc1f6cea7c6","implementation_hash":"unknown","scenarios":[{"index":0,"name":"landing on Income Tax pays the bank a fixed amount","scenario_hash":"bc9b8dfb07cd3b353fc6cde6ba697dfb77956640f158162c2f41527bdf9695fa","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-29T07:34:21.387754Z"},{"index":1,"name":"landing on Luxury Tax pays the bank a fixed amount","scenario_hash":"72a32538213dced2219d5bec5a5c6dd77ad9cdcbd7fb09e6710ba31f771cf65b","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-07-29T07:34:21.387754Z"}]}
# acceptance-mutation-manifest-end

# language: en

Feature: taxes

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend

  # tax-1
  Scenario Outline: landing on Income Tax pays the bank a fixed amount
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1300              |

  # tax-2
  Scenario Outline: landing on Luxury Tax pays the bank a fixed amount
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog"'s account balance is $<expected_balance>

    Examples:
      | expected_balance |
      | 1400              |

  # tax-3
  Scenario Outline: landing on Income Tax pays the government's account instead of the bank, once rent relief is enabled
    Given rent relief is enabled
    When pawn "dog" takes a targeted landing on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog"'s account balance is $<expected_balance>
    And the government's account holds $<government_account>

    Examples:
      | expected_balance | government_account |
      | 1300               | 200                  |

  # tax-4
  Scenario Outline: landing on Luxury Tax pays the government's account instead of the bank, once rent relief is enabled
    Given rent relief is enabled
    When pawn "dog" takes a targeted landing on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog"'s account balance is $<expected_balance>
    And the government's account holds $<government_account>

    Examples:
      | expected_balance | government_account |
      | 1400               | 100                  |
