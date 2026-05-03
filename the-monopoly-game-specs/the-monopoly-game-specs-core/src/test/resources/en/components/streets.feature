# language: en

Feature: streets

  Scenario: start
    Given the street "Start"
    Then your salary is $200

  Scenario: Rue Grande Dinant
    Given the street "Rue Grande Dinant"
    Then the street value is $60
    And vacant rent is $2
    And rent for 1 house is $10
    And rent for 2 houses is $30
    And rent for 3 houses is $90
    And rent for 4 houses is $160
    And rent for 1 hotel is $250
    And house construction cost is $50
    And hotel construction cost is $50
    And hotel construction requires 4 existing houses
    And mortgage value of the land is $30

  Scenario: Diestsestraat Leuven
    Given the street "Diestsestraat Leuven"
    Then the street value is $60
    And vacant rent is $4
    And rent for 1 house is $20
    And rent for 2 houses is $60
    And rent for 3 houses is $180
    And rent for 4 houses is $320
    And rent for 1 hotel is $450
    And house construction cost is $50
    And hotel construction cost is $50
    And hotel construction requires 4 existing houses
    And mortgage value of the land is $30