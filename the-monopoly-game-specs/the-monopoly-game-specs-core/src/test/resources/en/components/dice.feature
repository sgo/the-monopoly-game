# language: en

Feature: dice

  Scenario: each side has an equal chance to be rolled
    Given a 6 faced dice
    When I roll the dice 600000 times
    Then each face was rolled an equal amount of times
      | symbol | times seen | error margin % |
      | 1      | 100000     | 1              |
      | 2      | 100000     | 1              |
      | 3      | 100000     | 1              |
      | 4      | 100000     | 1              |
      | 5      | 100000     | 1              |
      | 6      | 100000     | 1              |
