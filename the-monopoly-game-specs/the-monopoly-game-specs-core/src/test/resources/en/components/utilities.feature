# language: en

Feature: utilities

  # utilities-1
  Scenario Outline: utility
    Given the utility "<name>"
    Then the utility value is $150
    And rent for owning 1 utility is 4 times the dice roll
    And rent for owning 2 utilities is 10 times the dice roll
    And mortgage value of the land is $75

    Examples:
      | name                                          |
      | Elektriciteitscentrale / Centrale Électrique  |
      | Watermaatschappij / Compagnie des Eaux        |
