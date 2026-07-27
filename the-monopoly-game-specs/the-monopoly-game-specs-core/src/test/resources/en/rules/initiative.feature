# language: en

Feature: initiative

  # initiative-1
  Scenario Outline: the highest initiative roll goes first, then play continues clockwise
    Given the official rule set
    And we select 3 players
    And pawn "<pawn 1>" will roll <roll 1> for initiative
    And pawn "<pawn 2>" will roll <roll 2> for initiative
    And pawn "<pawn 3>" will roll <roll 3> for initiative
    When we roll for initiative
    Then pawn "<first turn>" goes first
    And pawn "<first turn>" plays before pawn "<second turn>"
    And pawn "<second turn>" plays before pawn "<third turn>"

    Examples:
      | pawn 1 | roll 1 | pawn 2   | roll 2 | pawn 3   | roll 3 | first turn | second turn | third turn |
      | dog    | 4      | high hat | 10     | iron box | 6      | high hat   | iron box    | dog        |

  # initiative-2
  Scenario Outline: tied players roll again until one wins initiative
    Given the official rule set
    And we select 3 players
    And pawn "<pawn 1>" will roll <roll 1> for initiative
    And pawn "<pawn 2>" will roll <roll 2> for initiative
    And pawn "<pawn 3>" will roll <roll 3> for initiative
    And pawn "<pawn 1>" will roll <retie 1> for initiative
    And pawn "<pawn 2>" will roll <retie 2> for initiative
    When we roll for initiative
    Then pawn "<winner>" goes first

    Examples:
      | pawn 1 | roll 1 | pawn 2   | roll 2 | pawn 3   | roll 3 | retie 1 | retie 2 | winner   |
      | dog    | 8      | high hat | 8      | iron box | 5      | 6       | 9       | high hat |
