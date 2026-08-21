# language: en

# Backlogged additions to the tracked
# ../../../the-monopoly-game-specs-core/src/test/resources/en/rules/
# tax.feature, continuing its scenario numbering. Need the same wiring
# flagged in ../en/rules/megacorp-salary-tax.feature and rent-relief.feature's
# backlogs: exposing a live played game's real government account through
# Game.Result, since tax.feature only ever tests Income Tax and Luxury Tax
# via a real "lands on" play, and there is no isolated-shortcut equivalent
# for either the way there is for salary and rent.

Feature: taxes

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend

  # tax-3
  Scenario Outline: landing on Income Tax pays the government's account instead of the bank, once rent relief is enabled
    Given rent relief is enabled
    When pawn "dog" lands on "Inkomsten Belasting / Impôts sur le revenu"
    Then pawn "dog"'s account balance is $<expected_balance>
    And the government's account holds $<government_account>

    Examples:
      | expected_balance | government_account |
      | 1300               | 200                  |

  # tax-4
  Scenario Outline: landing on Luxury Tax pays the government's account instead of the bank, once rent relief is enabled
    Given rent relief is enabled
    When pawn "dog" lands on "Extra Belasting / Taxe de Luxe"
    Then pawn "dog"'s account balance is $<expected_balance>
    And the government's account holds $<government_account>

    Examples:
      | expected_balance | government_account |
      | 1400               | 100                  |
