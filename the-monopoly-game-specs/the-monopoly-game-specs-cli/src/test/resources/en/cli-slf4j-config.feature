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
