# language: en

Feature: mortgaging

  Background:
    Given the official rule set
    And we select 2 players
    And pawn "dog" will roll 10 for initiative
    And pawn "high hat" will roll 4 for initiative
    And every other player can complete their turn
    And pawn "dog" has $1500 to spend
    And pawn "high hat" has $1500 to spend

  # mortgage-1
  Scenario Outline: a player mortgages land to the bank for its mortgage value
    Given pawn "dog" owns "<property>"
    And pawn "dog" has $0 to spend
    When pawn "dog" mortgages "<property>"
    Then pawn "dog"'s account balance is $<mortgage_value>
    And the land "<property>" is mortgaged

    Examples:
      | property               | mortgage_value |
      | Diestsestraat Leuven   | 30              |
      | Noord Station          | 100             |
      | Elektriciteitscentrale | 75              |

  # mortgage-2
  Scenario Outline: lifting a mortgage costs the mortgage value plus 10% interest
    Given pawn "dog" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "dog" has $<starting_balance> to spend
    When pawn "dog" lifts the mortgage on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_final_balance>
    And the land "Diestsestraat Leuven" is not mortgaged

    Examples:
      | starting_balance | expected_final_balance |
      | 100               | 67                      |

  # mortgage-3
  Scenario Outline: no rent may be collected for mortgaged land
    Given pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Diestsestraat Leuven" is mortgaged
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1500                          | 1500                         |

  # mortgage-4
  Scenario Outline: a monopoly may not charge double rent while any street in the group is mortgaged
    Given pawn "high hat" owns "Rue Grande Dinant"
    And pawn "high hat" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "high hat" follows the "Agree if affordable" strategy
    When pawn "dog" lands on "Diestsestraat Leuven"
    Then pawn "dog"'s account balance is $<expected_tenant_final_balance>
    And pawn "high hat"'s account balance is $<expected_owner_final_balance>

    Examples:
      | expected_tenant_final_balance | expected_owner_final_balance |
      | 1496                          | 1504                         |

  # mortgage-5
  Scenario Outline: a player who will build is refused while any street in the colour group is mortgaged
    Given pawn "dog" owns "Rue Grande Dinant"
    And pawn "dog" owns "Diestsestraat Leuven"
    And the land "Rue Grande Dinant" is mortgaged
    And pawn "dog" follows the "Agree if affordable" strategy
    And pawn "dog" will build a house on "Diestsestraat Leuven"
    And pawn "dog" has $100 to spend
    When we play the game
    Then the street "Rue Grande Dinant" has <houses> house(s) built
    And the street "Diestsestraat Leuven" has <houses> house(s) built
    And pawn "dog"'s account balance is $<expected_final_balance>

    Examples:
      | houses | expected_final_balance |
      | 0      | 100                     |
