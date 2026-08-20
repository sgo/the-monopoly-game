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

  # cli-jar-7
  Scenario Outline: the packaged jar accepts the Billionaire asset-rich flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that asset-rich opening is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                                            | state   |
      | 2 greedo billionaire --optional-asset-rich-billionaire   | enabled |

  # cli-jar-8
  Scenario Outline: the packaged jar accepts the development-loans flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that development loans is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                                        | state   |
      | 3 greedo greedo greedo --optional-development-loans | enabled |

  # cli-jar-9
  Scenario Outline: the packaged jar accepts the development-loans full-draw flag alongside explicit strategies
    Given the CLI module has been packaged
    When I start the packaged simulator jar with the arguments "<raw arguments>"
    Then the packaged jar's output confirms that full-draw development loans is <state>
    When I stop the packaged jar
    Then the packaged jar process ends

    Examples:
      | raw arguments                                                                                | state   |
      | 3 greedo greedo greedo --optional-development-loans --optional-development-loans-full-draw  | enabled |
