# mutation-stamp: sha256=88b566a2b10e848ec94253ef346a7b33d2a50626414792c0b2771f490795cc99
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-08-25T11:50:14.550472Z","feature_name":"CLI SLF4J and Logback Configuration","feature_path":"/Users/sgo/sgo/the-monopoly-game/.worktrees/architect/the-monopoly-game-specs/the-monopoly-game-specs-cli/src/test/resources/en/cli-slf4j-config.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"CLI module pom.xml declares required logging dependencies","scenario_hash":"7fad9cdc45d1ce695b0f6a01ae9579913bade9ac1ff4c127bf1c195906e9d7e1","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-08-01T00:03:56.943284Z"}]}
# acceptance-mutation-manifest-end

# cli-1: CLI module has SLF4J logging framework available

Feature: CLI SLF4J and Logback Configuration
  The CLI module must declare SLF4J API and Logback Classic
  as dependencies so that the Journal's logging backend
  is available when the simulator runs.

  # cli-1
  Scenario Outline: CLI module pom.xml declares required logging dependencies
    Given the CLI module's pom.xml in "the-monopoly-game-cli"
    When I inspect the declared dependencies
    Then the project includes dependency "<groupId>:<artifactId>"
    And the dependency version is at least "<minimum>"

    Examples:
      | groupId        | artifactId         | minimum |
      | org.slf4j      | slf4j-api          | 2.0.0   |
      | ch.qos.logback | logback-classic    | 1.5.0   |
