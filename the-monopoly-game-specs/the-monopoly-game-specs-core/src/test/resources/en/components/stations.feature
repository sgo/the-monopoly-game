# language: en

Feature: stations

  # stations-1
  Scenario Outline: station
    Given the station "<name>"
    Then the station value is $200
    And rent for owning 1 station is $25
    And rent for owning 2 stations is $50
    And rent for owning 3 stations is $100
    And rent for owning 4 stations is $200
    And mortgage value of the land is $100

    Examples:
      | name                                  |
      | Noord Station / Gare du Nord          |
      | Centraal Station / Gare Centrale      |
      | Buurtspoorwegen / Tramways Vicinaux   |
      | Zuid Station / Gare du Midi           |
