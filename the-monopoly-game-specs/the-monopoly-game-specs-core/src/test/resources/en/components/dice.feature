# language: en

Feature: dice

  # dice-1
  Scenario: each side has an equal chance to be rolled
    Given a 6 faced dice
    When I roll the dice 600000 times
    Then each face was rolled about 100000 times within a 1% margin
