# language: en

# Backlogged addition to the tracked
# ../../../the-monopoly-game-specs-cli/src/test/resources/en/
# cli-packaged-jar.feature, continuing its scenario numbering. Mirrors
# cli-jar-10, the war-profits-tax flag's own packaged-jar scenario.

Feature: Monopoly command line interface, packaged jar

  # cli-jar-11
  Scenario Outline: the packaged jar accepts the rent-relief flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that rent relief is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                          | state   |
      | 3 greedo greedo greedo --optional-rent-relief | enabled |
