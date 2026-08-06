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
