# The Monopoly Game

An implementation for the Belgian edition of Monopoly, with a CLI interface to simulate playthroughs
using different characteristics.

Every game event is narrated through a journal so a completed game can be
read back afterward as a plain-English report.

## Running the simulator

```sh
mvn -pl the-monopoly-game-cli -am package -DskipTests
java -jar the-monopoly-game-cli/target/the-monopoly-game-cli-0.5.0-SNAPSHOT.jar [number of players] [strategy for each player] [optional flags]
```

With no arguments, it runs a 2-player game with every player using the
"Greedo" strategy and prints the full game report to stdout. `-h`/`--h`
prints this usage:

```text
Usage: simulator [number of players] [strategy for each player]
Available strategies: greedo, billionaire
Optional flags:
  --optional-greedo-stalemate-trading
  --optional-greedo-legal-entity
  --optional-asset-rich-billionaire
  --max-years=N
Report file: $TMPDIR/the-monopoly-game.report
```

The final report is written to `the-monopoly-game.report` in the system
temporary directory.

## Building and testing

```sh
mvn test                        # unit tests, all modules
mvn test -P property-tests      # property-based tests
./acceptance/run-acceptance.sh  # regenerates and runs the full Gherkin acceptance suite
```

The acceptance suite requires an [APS](https://github.com/unclebob/Acceptance-Pipeline-Specification)
checkout; set `APS_HOME` or place one at `./tmp/aps`.

## Documentation

- [`RULES.md`](RULES.md) — the canonical rule set this project models, with
  **(project scope)** markers showing what's actually implemented versus the
  full official rules.
- [`SIMULATOR.md`](SIMULATOR.md) — the CLI simulator's design: the pluggable
  strategy abstraction, the "Greedo" strategy's full decision logic, the
  distressed-sale mechanic, and known characteristics/limitations.

## Purpose of this project

I started this project with 2 distinct objectives in mind.

1. [Gain experience with using AI for software development.](about-ai.md)
2. [Apply real-world economic policies to a simulation and observe its effects.](economics.md)
