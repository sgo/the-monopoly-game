# mutation-stamp: sha256=060582943dc392e1cd3bdfbc92ac6d4920a209e74ad0566f5fedb66935cad6c5
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T10:51:55.388545Z","feature_name":"CLI packaged jar","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-cli/src/test/resources/en/cli-packaged-jar.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":3,"name":"the packaged jar confirms the configured year limit at the start of the game","scenario_hash":"15f876e09aa45bdc70f22bcd03e9dd20cb9ca891bdf25638438919f2bae3fd91","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-08-23T00:26:16.162294Z"},{"index":0,"name":"CLI module pom.xml packages a dependency-inclusive executable jar","scenario_hash":"fffaf4e834ceca9a2d0d2a51a23d6a0e50b6da215a628294adbb2949c47422bb","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:35:27.433573Z"},{"index":1,"name":"the packaged jar runs standalone and prints usage","scenario_hash":"4a20e4ac4f400f3ec07fc3350e2b3644988b0848edfaba6150a6b92bbd1d1ece","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-08-20T10:35:27.433573Z"}]}
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

  # cli-jar-5
  Scenario Outline: README embeds the `-h` usage report and names every optional flag
    Given the CLI module has been packaged
    When I run the packaged simulator jar with "-h"
    Then the packaged jar's output explains how to use the simulator
    And the README usage report includes the optional flag "--optional-greedo-stalemate-trading"
    And the README usage report includes the optional flag "--optional-greedo-legal-entity"
    And the README usage report includes the optional flag "--max-years"
    And the README usage report includes the optional flag "--optional-asset-rich-billionaire"
    And the README usage report includes the optional flag "--optional-development-loans"
    And the README usage report includes the optional flag "--optional-development-loans-full-draw"
    And the README usage report includes the optional flag "--optional-war-profits-tax"

  # cli-jar-6
  Scenario Outline: the packaged jar confirms the configured year limit at the start of the game
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that the year limit is <year limit> years
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                            | year limit |
      | 3 greedo greedo greedo --max-years=500   | 500        |

