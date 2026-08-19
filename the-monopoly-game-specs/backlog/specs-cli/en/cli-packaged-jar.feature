# language: en

# Backlog scenarios for the war-profits-tax feature (see
# ../../en/rules/war-profits-tax.feature, also backlogged). When this batch
# is promoted:
#   1. Move cli-jar-10 below into the tracked cli-packaged-jar.feature
#      unchanged, renumbering to continue that file's own sequence (next
#      free index at the time this was written was cli-jar-10).
#   2. Also add this line to the tracked file's existing cli-jar-5
#      ("README embeds the `-h` usage report and names every optional
#      flag"), alongside its other "And the README usage report includes
#      the optional flag ..." lines:
#        And the README usage report includes the optional flag "--optional-war-profits-tax"

Feature: CLI packaged jar

  # cli-jar-10
  Scenario Outline: the packaged jar accepts the war-profits-tax flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that the war profits tax is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                                     | state   |
      | 3 greedo greedo greedo --optional-war-profits-tax | enabled |
