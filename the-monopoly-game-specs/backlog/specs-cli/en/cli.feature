# language: en

# Not yet batched. Mirrors cli-16/17's exact shape for the new
# --optional-unified-income-tax flag, once it exists: a game-wide wiring
# check, and proof it applies the same way regardless of which strategies
# are playing (a Billionaire/Greedo mix, same as every other game-wide
# flag's own pair). No MegaCorp-style cross-check (cli-19) is needed:
# unlike MegaCorp's salary tax, this flag does not depend on
# --optional-rent-relief being enabled too - it is fully independent, by
# design, of both existing income taxes.

Feature: Monopoly command line interface

  # cli-20
  Scenario Outline: the CLI wires the unified-income-tax flag, game-wide rather than to any one strategy
    Given the simulator is configured with the raw arguments "<raw arguments>"
    When I start the simulator
    Then the game journal records that unified income tax is <state>
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | raw arguments                                  | state   |
      | 2 greedo greedo --optional-unified-income-tax | enabled |

  # cli-21
  Scenario Outline: the unified-income-tax flag applies the same way regardless of which strategies are playing
    Given the simulator is configured with the raw arguments "<raw arguments>"
    When I start the simulator
    Then the game journal records that pawn "<billionaire pawn>" uses the "Billionaire" strategy
    And the game journal records that pawn "<greedo pawn>" uses the "Greedo" strategy
    And the game journal records that unified income tax is <state>
    When I stop the simulator before the game ends
    Then the simulator process ends

    Examples:
      | raw arguments                                       | billionaire pawn | greedo pawn | state   |
      | 2 greedo billionaire --optional-unified-income-tax | high hat         | dog         | enabled |
