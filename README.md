# The Monopoly Game

An implementation for the Belgian edition of Monopoly, with a CLI interface to simulate playthroughs
using different characteristics.

Every game event is narrated through a journal so a completed game can be
read back afterward as a plain-English report.

## Purpose of this project

I started this project with 2 distinct objectives in mind.

1. [Gain experience with using AI for software development.](about-ai.md)
2. [Apply real-world economic policies to a simulation and observe its effects.](economics.md)

## Simulated game characteristics

Built from the [`CHARACTERIZATION-TESTS.md`](CHARACTERIZATION-TESTS.md)
baselines: a `Summary` table for a quick read, and a `Detailed Breakdown`
per config below it for the full picture. Kept in sync with the checked-in
fixtures by the same test suite (see `README sync check` in
`CHARACTERIZATION-TESTS.md`).

### Summary

| Players | Strategies | Optional flags | Outcome | Age at end (min / median / mean / max) |
|---|---|---|---|---|
| 2 | Greedo | — | 98% win (49), 2% stalemate (1) | 6 / 15 / 18.4 / 190 |
| 3 | Greedo | — | 78% win (39), 22% stalemate (11) | 5 / 14 / 67.88 / 392 |
| 3 | Greedo | peer-trading | 100% win (50) | 5 / 13 / 14.7 / 28 |
| 8 | Greedo | — | 16% win (8), 84% stalemate (42) | 6 / 281 / 348.02 / 1419 |
| 8 | Greedo | peer-trading | 100% win (50) | 4 / 10 / 11.8 / 28 |
| 8 | Greedo | peer-trading + legal-entity | 100% win (50) | 5 / 11 / 11.2 / 23 |
| 8 | 1 Billionaire (cash-rich) + 7 Greedo | peer-trading + legal-entity | 86% win (43), 14% stalemate (7) | 5 / 12 / 16.54 / 57 |
| 8 | 1 Billionaire (asset-rich) + 7 Greedo | peer-trading + legal-entity + asset-rich | 100% win (50) | 1 / 4 / 4.12 / 10 |
| 8 | 1 Billionaire (asset-rich) + 7 Greedo | peer-trading + legal-entity + asset-rich + dev-loans | 100% win (50) | 1 / 4 / 4.0 / 12 |
| 8 | 1 Billionaire (asset-rich) + 7 Greedo | peer-trading + legal-entity + asset-rich + war-profits-tax | 74% win (37), 26% stalemate (13) | 0 / 17 / 81.62 / 454 |
| 8 | Greedo | peer-trading + legal-entity + dev-loans + rent relief | 96% win (48), 4% stalemate (2) | 7 / 14 / 28.06 / 170 |
| 8 | Greedo | peer-trading + legal-entity + dev-loans + rent relief + war-profits-tax | 64% win (32), 36% stalemate (18) | 7 / 69 / 153.56 / 1730 |
| 8 | 1 Billionaire (asset-rich) + 7 Greedo | peer-trading + legal-entity + asset-rich + dev-loans + rent relief + war-profits-tax | 60% win (30), 40% stalemate (20) | 0 / 57 / 132.66 / 843 |

Full breakdown for any config lives in
[`the-monopoly-game-cli/src/test/resources/characterization/`](the-monopoly-game-cli/src/test/resources/characterization/).

### Detailed Breakdown

<details>
<summary>2 players — Greedo — no optional flags</summary>

- Outcome: 98% win (49), 2% stalemate (1)
- Winners: dog 24, high hat 25
- Age at end: min 6, median 15, mean 18.4, max 190
- Bankruptcies: dog 22, high hat 23, the bank 4
- Auctions: 138 (11.7% of 1,182 land acquisitions)
- Mortgages: 661
- Income: salary \$324,600, rent \$204,992, bank payments \$39,550

</details>

<details>
<summary>8 players — Greedo — peer-trading + legal-entity + dev-loans + rent relief</summary>

- Outcome: 96% win (48), 4% stalemate (2)
- Winners: shoe 6, racecar 10, thimble 3, dog 7, high hat 9, ship 7, iron box 2, wheelbarrow 4
- Age at end: min 7, median 14, mean 28.06, max 170
- Bankruptcies: racecar 41, iron box 21, the bank 94, thimble 14, dog 32, high hat 52, shoe 26, ship 42, wheelbarrow 26
- Auctions: 49 (3.2% of 1,551 land acquisitions)
- Mortgages: 1,006
- Income: salary \$953,600, rent \$4,501,517, bank payments \$118,855
- Loans: 478 raised, \$27,157 total, 12 defaults
  - Borrowers: iron box 42, Green Realty 35, Yellow Realty 49, racecar 39, Pink Realty 52, ship 60, thimble 24, shoe 43, dog 35, high hat 62, wheelbarrow 37
  - Bondholders: dog 292, high hat 73, iron box 42, racecar 36, shoe 6, thimble 5, ship 12, wheelbarrow 6
  - Servicing: borrowers paid \$6,744 interest + \$12,279 principal; bondholders received \$3,244 interest + \$12,271 principal
- Entities: 113 formed, 66 dissolved
- Peer trades: 6

</details>

<details>
<summary>8 players — Greedo — peer-trading + legal-entity + dev-loans + rent relief + war-profits-tax</summary>

- Outcome: 64% win (32), 36% stalemate (18)
- Winners: shoe 5, racecar 8, ship 2, high hat 4, dog 6, thimble 4, wheelbarrow 2, iron box 1
- Age at end: min 7, median 69, mean 153.56, max 1730
- Bankruptcies: racecar 31, iron box 19, the bank 113, thimble 14, dog 29, high hat 38, shoe 23, ship 31, wheelbarrow 25
- Auctions: 103 (6.0% of 1,730 land acquisitions)
- Mortgages: 737
- Income: salary \$4,279,200, rent \$11,616,524, bank payments \$521,200
- Loans: 401 raised, \$24,695 total, 73 defaults
  - Borrowers: iron box 27, Green Realty 42, Yellow Realty 46, racecar 30, Pink Realty 51, ship 42, thimble 27, shoe 27, high hat 54, dog 34, wheelbarrow 21
  - Bondholders: dog 242, high hat 62, iron box 30, racecar 19, shoe 5, thimble 5, ship 2
  - Servicing: borrowers paid \$6,483 interest + \$12,588 principal; bondholders received \$3,005 interest + \$12,070 principal
- Entities: 115 formed, 44 dissolved
- Peer trades: 10
- War-profits tax: 334 payments, \$620,779 total
- Tax payers: racecar 12, thimble 2, dog 8, high hat 15, wheelbarrow 36, ship 38, iron box 4, shoe 219
- Government balance: min 2, median 630, mean 12244.54, max 316087

</details>

<details>
<summary>8 players — 1 Billionaire (asset-rich) + 7 Greedo — peer-trading + legal-entity + asset-rich + dev-loans + rent relief + war-profits-tax</summary>

- Outcome: 60% win (30), 40% stalemate (20)
- Winners: thimble 2, high hat 5, racecar 8, ship 5, dog 5, iron box 1, shoe 4
- Age at end: min 0, median 57, mean 132.66, max 843
- Bankruptcies: dog 141, the bank 90, ship 15, wheelbarrow 6, racecar 18, high hat 18, shoe 13, thimble 2, iron box 6
- Auctions: 137 (9.8% of 1,400 land acquisitions)
- Mortgages: 610
- Income: salary \$4,035,200, rent \$7,644,728, bank payments \$486,435
- Loans: 310 raised, \$18,243 total, 128 defaults
  - Borrowers: dog 131, ship 21, racecar 24, Yellow Realty 19, shoe 19, iron box 9, Green Realty 23, Pink Realty 19, thimble 10, high hat 27, wheelbarrow 8
  - Bondholders: high hat 157, ship 6, wheelbarrow 1, dog 63, racecar 2, iron box 18, shoe 2
  - Servicing: borrowers paid \$3,401 interest + \$6,986 principal; bondholders received \$1,356 interest + \$5,189 principal
- Entities: 46 formed, 9 dissolved
- Peer trades: 4
- War-profits tax: 1068 payments, \$2,247,817 total
- Tax payers: dog 45, shoe 416, iron box 64, thimble 1, ship 236, racecar 302, high hat 4
- Government balance: min 61, median 1834, mean 26783.32, max 271144
- Survivors at first tax: min 2, median 5, mean 5.177777777777778, max 8

</details>

<details>
<summary>3 players — Greedo — no optional flags</summary>

- Outcome: 78% win (39), 22% stalemate (11)
- Winners: dog 5, high hat 17, iron box 17
- Age at end: min 5, median 14, mean 67.88, max 392
- Bankruptcies: dog 9, high hat 32, iron box 32, the bank 5
- Auctions: 103 (7.6% of 1,353 land acquisitions)
- Mortgages: 733
- Income: salary \$1,736,200, rent \$932,616, bank payments \$208,770

</details>

<details>
<summary>3 players — Greedo — peer-trading</summary>

- Outcome: 100% win (50)
- Winners: dog 13, high hat 16, iron box 21
- Age at end: min 5, median 13, mean 14.7, max 28
- Bankruptcies: dog 22, high hat 33, iron box 40, the bank 5
- Auctions: 100 (7.4% of 1,350 land acquisitions)
- Mortgages: 1,004
- Income: salary \$341,800, rent \$350,619, bank payments \$41,070
- Peer trades: 11

</details>

<details>
<summary>8 players — Greedo — no optional flags</summary>

- Outcome: 16% win (8), 84% stalemate (42)
- Winners: dog 1, iron box 1, wheelbarrow 2, ship 2, high hat 2
- Age at end: min 6, median 281, mean 348.02, max 1419
- Bankruptcies: dog 9, high hat 13, ship 13, wheelbarrow 11, iron box 5, thimble 2, shoe 1, the bank 5
- Auctions: 7 (0.5% of 1,410 land acquisitions)
- Mortgages: 147
- Income: salary \$23,597,400, rent \$13,395,646, bank payments \$2,840,320 — the long-running stalemates (up to 1,419 simulated years) dwarf every other config's income totals

</details>

<details>
<summary>8 players — Greedo — peer-trading</summary>

- Outcome: 100% win (50)
- Winners: iron box 5, racecar 6, dog 7, thimble 4, shoe 5, high hat 9, ship 9, wheelbarrow 5
- Age at end: min 4, median 10, mean 11.8, max 28
- Bankruptcies: ship 61, high hat 59, dog 45, racecar 39, wheelbarrow 36, shoe 33, thimble 27, iron box 26, the bank 24 (350 total = exactly 7 losers × 50 games, every game resolves by ordinary bankruptcy)
- Auctions: 16 (1.1% of 1,445 land acquisitions)
- Mortgages: 1,227
- Income: salary \$531,600, rent \$1,318,641, bank payments \$66,735
- Peer trades: 9

</details>

<details>
<summary>8 players — Greedo — peer-trading + legal-entity</summary>

- Outcome: 100% win (50)
- Winners: iron box 7, racecar 6, ship 7, shoe 8, thimble 4, dog 10, wheelbarrow 2, high hat 6
- Age at end: min 5, median 11, mean 11.2, max 23
- Bankruptcies: the bank 109, dog 46, ship 40, shoe 40, high hat 35, wheelbarrow 24, racecar 21, iron box 20, thimble 15 (350 total, same 7×50 pattern as above — but nearly a third go to the bank here, versus 24/350 without legal entities, since a dissolving entity's collateral often auctions rather than transferring to a peer)
- Auctions: 96 (6.1% of 1,573 land acquisitions)
- Mortgages: 1,285
- Income: salary \$472,400, rent \$1,380,439, bank payments \$59,975
- Entities: 99 formed, 51 dissolved
- Peer trades: 4

</details>

<details>
<summary>8 players — 1 Billionaire (cash-rich) + 7 Greedo — peer-trading + legal-entity</summary>

- Outcome: 86% win (43), 14% stalemate (7)
- Winners: dog 43
- Age at end: min 5, median 12, mean 16.54, max 57
- Bankruptcies: dog 147, the bank 102, high hat 15, iron box 15, ship 14, racecar 13, wheelbarrow 13, shoe 12, thimble 11
- Auctions: 21 (1.4% of 1,537 land acquisitions)
- Mortgages: 732
- Income: salary \$606,800, rent \$2,807,207, bank payments \$71,030
- Entities: 106 formed, 54 dissolved
- Peer trades: 6

</details>

<details>
<summary>8 players — 1 Billionaire (asset-rich) + 7 Greedo — peer-trading + legal-entity + asset-rich</summary>

- Outcome: 100% win (50)
- Winners: dog 50
- Age at end: min 1, median 4, mean 4.12, max 10
- Bankruptcies: dog 309, the bank 13, ship 5, racecar 5, high hat 5, wheelbarrow 5, iron box 4, shoe 3, thimble 1
- Auctions: 32 (3.1% of 1,026 land acquisitions)
- Mortgages: 824
- Income: salary \$174,600, rent \$754,039, bank payments \$23,605
- Entities: 3 formed, 3 dissolved
- Peer trades: 0

</details>

<details>
<summary>8 players — 1 Billionaire (asset-rich) + 7 Greedo — peer-trading + legal-entity + asset-rich + dev-loans</summary>

- Outcome: 100% win (50)
- Winners: dog 49, ship 1
- Age at end: min 1, median 4, mean 4.0, max 12
- Bankruptcies: dog 310, the bank 16, ship 6, high hat 5, iron box 5, shoe 3, racecar 2, thimble 2, wheelbarrow 1 (350 total = 7×50 again, despite the much shorter games)
- Auctions: 24 (2.4% of 995 land acquisitions)
- Mortgages: 850
- Income: salary \$166,000, rent \$743,323, bank payments \$22,680
- Loans: 265 raised, \$13,860 total, 0 defaults
  - Borrowers: dog 216, ship 12, high hat 9, wheelbarrow 8, iron box 8, shoe 5, thimble 4, racecar 1, Pink Realty 1, Yellow Realty 1
  - Bondholders: high hat 159, iron box 16, shoe 14, racecar 12, ship 11, dog 47, wheelbarrow 4, thimble 2
  - Servicing: borrowers paid \$1,193 interest + \$1,495 principal; bondholders received \$629 interest + \$1,495 principal
- Entities: 3 formed, 3 dissolved
- Peer trades: 1

</details>

<details>
<summary>8 players — 1 Billionaire (asset-rich) + 7 Greedo — peer-trading + legal-entity + asset-rich + war-profits-tax</summary>

- Outcome: 74% win (37), 26% stalemate (13)
- Winners: ship 4, dog 5, iron box 9, thimble 5, high hat 3, shoe 5, racecar 4, wheelbarrow 2
- Age at end: min 0, median 17, mean 81.62, max 454
- Bankruptcies: dog 183, the bank 64, racecar 17, iron box 16, thimble 11, high hat 11, ship 9, shoe 9, wheelbarrow 11
- Auctions: 148 (10.9% of 1,360 land acquisitions)
- Mortgages: 755
- Income: salary \$1,881,600, rent \$1,910,428, bank payments \$224,990
- Entities: 23 formed, 3 dissolved
- Peer trades: 1
- War-profits tax: 52 payments, \$275,456 total
- Tax payers: dog 48, racecar 2, iron box 2
- Government balance: min 0, median 5335, mean 5509.12, max 10956
- Survivors at first tax: min 2, median 4, mean 3.9583333333333335, max 7

</details>

## Running the simulator

```sh
mvn -pl the-monopoly-game-cli -am package -DskipTests
java -jar the-monopoly-game-cli/target/the-monopoly-game-cli-0.7.0-SNAPSHOT.jar [number of players] [strategy for each player] [optional flags]
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
  --optional-development-loans
  --optional-development-loans-full-draw
  --optional-war-profits-tax
  --max-years=N
Report file: $TMPDIR/the-monopoly-game.report
```

The final report is written to `the-monopoly-game.report` in the system
temporary directory.

## Building and testing

```sh
mvn test                              # unit tests, all modules
mvn test -P property-tests            # property-based tests
mvn test -P characterization-tests    # full-game regression suite, see CHARACTERIZATION-TESTS.md
./acceptance/run-acceptance.sh        # regenerates and runs the full Gherkin acceptance suite
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
- [`CHARACTERIZATION-TESTS.md`](CHARACTERIZATION-TESTS.md) — a JUnit suite
  that plays full games across a fixed set of setups and compares the
  outcome against a checked-in baseline, to catch whole-system regressions
  that isolated rule tests can miss.
