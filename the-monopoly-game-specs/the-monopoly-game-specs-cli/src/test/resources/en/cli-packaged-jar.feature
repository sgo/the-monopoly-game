# acceptance-mutation-manifest-begin
# acceptance-mutation-manifest-end

# language: en

Feature: CLI packaged jar
  The CLI module must produce a standalone executable jar (per SIMULATOR.md's
  "standalone executable process" requirement) that includes every runtime
  dependency and can be run directly with `java -jar`, without assembling a
  classpath by hand.

  # cli-jar-1
  Scenario Outline: CLI module pom.xml packages a dependency-inclusive executable jar
    Given the CLI module's pom.xml in "the-monopoly-game-cli"
    When I inspect the declared build plugins
    Then the project packages an executable jar with main class "<main class>"

    Examples:
      | main class                      |
      | the.monopoly.game.cli.Simulator |

  # cli-jar-2
  Scenario Outline: the packaged jar runs standalone and prints usage
    Given the CLI module has been packaged
    When I run the packaged simulator jar with "<flag>"
    Then the packaged jar exits successfully
    And the packaged jar's output explains how to use the simulator

    Examples:
      | flag |
      | -h   |

  # cli-jar-3
  Scenario Outline: the packaged jar accepts the Greedo stalemate-trading flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that stalemate trading is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                                               | state   |
      | 3 greedo greedo greedo --optional-greedo-stalemate-trading | enabled |

  # cli-jar-4
  Scenario Outline: the packaged jar accepts the Greedo legal-entity flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that legal entity is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                                         | state   |
      | 3 greedo greedo greedo --optional-greedo-legal-entity | enabled |

  # cli-jar-5
  Scenario Outline: README embeds the `-h` usage report and names every optional flag
    Given the CLI module has been packaged
    When I run the packaged simulator jar with "-h"
    Then the packaged jar's output explains how to use the simulator
    And the README usage report includes the optional flag "--optional-greedo-stalemate-trading"
    And the README usage report includes the optional flag "--optional-greedo-legal-entity"
