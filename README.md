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
| 8 | Greedo | peer-trading + legal-entity + dev-loans | 100% win (50) | 5 / 10 / 11.6 / 32 |
| 8 | Greedo | peer-trading + legal-entity + dev-loans + rent relief + war-profits-tax | 66% win (33), 34% stalemate (17) | 7 / 68 / 152.28 / 1730 |
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
- Income by player: high hat salary \$161,200, rent \$104,699, dog salary \$163,400, rent \$100,293

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
- Income by player: racecar salary \$154,200, rent \$678,579, wheelbarrow salary \$108,600, rent \$401,109, iron box salary \$127,400, rent \$269,072, ship salary \$124,200, rent \$320,572, dog salary \$89,400, rent \$275,312, shoe salary \$111,800, rent \$204,665, thimble salary \$114,400, rent \$150,418, high hat salary \$123,600, rent \$554,069
- Loans: 478 raised, \$27,157 total, 12 defaults
  - Borrowers: iron box 42, Green Realty 35, Yellow Realty 49, racecar 39, Pink Realty 52, ship 60, thimble 24, shoe 43, dog 35, high hat 62, wheelbarrow 37
  - Bondholders: dog 292, high hat 73, iron box 42, racecar 36, shoe 6, thimble 5, ship 12, wheelbarrow 6
  - Servicing: borrowers paid \$6,744 interest + \$12,279 principal; bondholders received \$3,244 interest + \$12,271 principal
- Entities: 113 formed, 66 dissolved
- Peer trades: 6
- Rent relief: 1,274 payments, \$621,670 total, 50 games
- MegaCorp salary tax: 4,768 payments, \$715,200 total
- MegaCorp tax payers: wheelbarrow \$81450, dog \$67050, ship \$93150, racecar \$115650, shoe \$83850, high hat \$92700, iron box \$95550, thimble \$85800
- Relief received: thimble \$80165, wheelbarrow \$91100, racecar \$88400, ship \$80935, shoe \$92540, dog \$62105, high hat \$57500, iron box \$68925
- Relief starved: 1,297 payments, \$1,484,885 shortfall, 50 games
- Starved by pawn: dog \$130160, thimble \$191875, ship \$195475, high hat \$121450, wheelbarrow \$188075, iron box \$229425, racecar \$233850, shoe \$194575
- Effective tax burden: racecar 12.19%, wheelbarrow 13.78%, iron box 19.42%, ship 17.32%, dog 15.53%, shoe 20.95%, thimble 24.47%, high hat 12.03%
- Net fiscal position: racecar -2.87%, wheelbarrow 1.63%, iron box -5.41%, ship -2.27%, dog -1.15%, shoe 2.17%, thimble -1.61%, high hat -4.57%

</details>

<details>
<summary>8 players — Greedo — peer-trading + legal-entity + dev-loans</summary>

- Outcome: 100% win (50)
- Winners: iron box 4, racecar 8, dog 9, high hat 8, wheelbarrow 9, ship 6, shoe 5, thimble 1
- Age at end: min 5, median 10, mean 11.6, max 32
- Bankruptcies: iron box 22, ship 46, racecar 37, the bank 63, thimble 12, dog 40, shoe 40, high hat 46, wheelbarrow 44
- Auctions: 42 (2.8% of 1,485 land acquisitions)
- Mortgages: 1,196
- Income: salary \$485,600, rent \$1,271,793, bank payments \$61,545
- Income by player: racecar salary \$66,600, rent \$142,841, wheelbarrow salary \$60,800, rent \$154,386, iron box salary \$55,600, rent \$85,786, ship salary \$57,800, rent \$181,041, dog salary \$58,200, rent \$134,030, shoe salary \$64,600, rent \$119,509, thimble salary \$56,000, rent \$55,860, high hat salary \$66,000, rent \$169,834
- Loans: 520 raised, \$29,700 total, 2 defaults
  - Borrowers: iron box 47, racecar 62, Green Realty 30, Yellow Realty 40, ship 62, Pink Realty 41, thimble 22, dog 58, shoe 52, high hat 54, wheelbarrow 52
  - Bondholders: dog 270, high hat 118, ship 8, shoe 13, iron box 32, thimble 23, wheelbarrow 7, racecar 46
  - Servicing: borrowers paid \$5,359 interest + \$8,457 principal; bondholders received \$2,626 interest + \$8,435 principal
- Entities: 100 formed, 65 dissolved
- Peer trades: 4

</details>

<details>
<summary>8 players — Greedo — peer-trading + legal-entity + dev-loans + rent relief + war-profits-tax</summary>

- Outcome: 66% win (33), 34% stalemate (17)
- Winners: shoe 5, racecar 8, ship 2, high hat 4, dog 7, thimble 4, wheelbarrow 2, iron box 1
- Age at end: min 7, median 68, mean 152.28, max 1730
- Bankruptcies: racecar 31, iron box 19, the bank 115, thimble 14, dog 28, high hat 38, shoe 23, ship 31, wheelbarrow 25
- Auctions: 109 (6.3% of 1,736 land acquisitions)
- Mortgages: 741
- Income: salary \$4,266,200, rent \$11,740,451, bank payments \$518,515
- Income by player: racecar salary \$413,400, rent \$633,704, wheelbarrow salary \$338,000, rent \$479,664, iron box salary \$374,800, rent \$571,569, ship salary \$870,800, rent \$904,256, dog salary \$266,000, rent \$319,661, shoe salary \$694,400, rent \$530,189, thimble salary \$795,200, rent \$1,478,843, high hat salary \$513,600, rent \$2,176,118
- Loans: 401 raised, \$24,695 total, 73 defaults
  - Borrowers: iron box 27, Green Realty 42, Yellow Realty 46, racecar 30, Pink Realty 51, ship 42, thimble 27, shoe 27, high hat 54, dog 34, wheelbarrow 21
  - Bondholders: dog 242, high hat 62, iron box 30, racecar 19, shoe 5, thimble 5, ship 2
  - Servicing: borrowers paid \$6,483 interest + \$12,586 principal; bondholders received \$3,005 interest + \$12,070 principal
- Entities: 115 formed, 44 dissolved
- Peer trades: 10
- War-profits tax: 334 payments, \$620,779 total
- Tax payers: racecar 12, thimble 2, dog 8, high hat 15, wheelbarrow 36, ship 38, iron box 4, shoe 219
- Government balance: min 2, median 647, mean 12257.92, max 316087
- Rent relief: 4,242 payments, \$2,737,245 total, 50 games
- MegaCorp salary tax: 21,331 payments, \$3,199,650 total
- MegaCorp tax payers: wheelbarrow \$253500, dog \$199500, ship \$653100, racecar \$310050, shoe \$520800, high hat \$385200, iron box \$281100, thimble \$596400
- Relief received: thimble \$271810, wheelbarrow \$262920, racecar \$272880, ship \$830960, shoe \$633090, dog \$141815, high hat \$121955, iron box \$201815
- Relief starved: 2,142 payments, \$2,523,695 shortfall, 50 games
- Starved by pawn: dog \$217010, thimble \$313200, ship \$359175, high hat \$358825, wheelbarrow \$158250, iron box \$472635, racecar \$273750, shoe \$370850
- Effective tax burden: racecar 27.73%, wheelbarrow 31.69%, iron box 23.72%, ship 31.38%, dog 29.70%, shoe 44.89%, thimble 20.91%, high hat 14.13%
- Net fiscal position: racecar -7.63%, wheelbarrow -7.14%, iron box -7.28%, ship 2.84%, dog -11.64%, shoe -8.62%, thimble -11.44%, high hat -10.17%

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
- Income by player: racecar salary \$430,600, rent \$1,169,253, wheelbarrow salary \$419,600, rent \$184,304, dog salary \$676,400, rent \$736,045, iron box salary \$393,400, rent \$521,411, ship salary \$663,600, rent \$976,534, shoe salary \$601,200, rent \$1,284,018, thimble salary \$319,600, rent \$87,911, high hat salary \$530,800, rent \$273,188
- Loans: 310 raised, \$18,243 total, 128 defaults
  - Borrowers: dog 131, ship 21, racecar 24, Yellow Realty 19, shoe 19, iron box 9, Green Realty 23, Pink Realty 19, thimble 10, high hat 27, wheelbarrow 8
  - Bondholders: high hat 157, ship 6, wheelbarrow 1, dog 63, racecar 2, iron box 18, shoe 2
  - Servicing: borrowers paid \$3,401 interest + \$6,986 principal; bondholders received \$1,356 interest + \$5,189 principal
- Entities: 46 formed, 9 dissolved
- Peer trades: 4
- Rent relief: 4,919 payments, \$2,749,045 total, 49 games
- MegaCorp salary tax: 20,176 payments, \$3,026,400 total
- MegaCorp tax payers: wheelbarrow \$314700, dog \$507300, ship \$497700, racecar \$322950, shoe \$450900, high hat \$398100, thimble \$239700, iron box \$295050
- Relief received: iron box \$178695, ship \$572805, high hat \$436535, shoe \$399375, wheelbarrow \$160395, thimble \$139585, racecar \$182905, dog \$678750
- Relief starved: 936 payments, \$830,100 shortfall, 50 games
- Starved by pawn: iron box \$81650, wheelbarrow \$111150, high hat \$103095, thimble \$91250, ship \$129390, shoe \$85565, racecar \$93125, dog \$134875
- Effective tax burden: racecar 56.97%, wheelbarrow 34.26%, dog 42.90%, iron box 33.45%, ship 48.37%, shoe 40.78%, thimble 37.37%, high hat 33.88%
- Net fiscal position: racecar -47.46%, wheelbarrow -16.80%, dog -7.54%, iron box -18.68%, ship -21.58%, shoe -23.69%, thimble -15.80%, high hat 2.44%
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
- Income by player: iron box salary \$585,800, rent \$307,487, dog salary \$565,200, rent \$305,739, high hat salary \$585,200, rent \$319,390

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
- Income by player: iron box salary \$114,200, rent \$139,848, dog salary \$110,800, rent \$93,846, high hat salary \$116,800, rent \$116,925
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
- Income by player: racecar salary \$3,007,800, rent \$1,293,567, wheelbarrow salary \$2,846,000, rent \$2,224,150, iron box salary \$2,900,400, rent \$1,213,430, ship salary \$2,997,800, rent \$1,194,556, dog salary \$3,016,800, rent \$3,037,279, shoe salary \$2,971,000, rent \$1,791,547, thimble salary \$2,997,600, rent \$1,188,549, high hat salary \$2,860,000, rent \$1,452,568

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
- Income by player: racecar salary \$67,400, rent \$184,311, wheelbarrow salary \$64,400, rent \$155,013, iron box salary \$62,400, rent \$112,116, ship salary \$69,400, rent \$227,561, dog salary \$69,000, rent \$183,205, shoe salary \$68,800, rent \$128,519, thimble salary \$64,600, rent \$106,680, high hat salary \$65,600, rent \$221,236
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
- Income by player: racecar salary \$61,800, rent \$117,793, wheelbarrow salary \$60,200, rent \$110,695, iron box salary \$55,400, rent \$86,451, ship salary \$59,400, rent \$177,720, dog salary \$59,600, rent \$161,995, shoe salary \$62,000, rent \$118,721, thimble salary \$56,000, rent \$67,505, high hat salary \$58,000, rent \$127,815
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
- Income by player: racecar salary \$68,800, rent \$182,918, wheelbarrow salary \$58,600, rent \$123,357, iron box salary \$69,800, rent \$130,802, ship salary \$68,600, rent \$243,006, dog salary \$144,400, rent \$823,101, shoe salary \$60,600, rent \$111,221, thimble salary \$61,400, rent \$88,895, high hat salary \$74,600, rent \$153,106
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
- Income by player: racecar salary \$21,800, rent \$6,871, wheelbarrow salary \$20,400, rent \$9,062, dog salary \$36,200, rent \$703,044, iron box salary \$17,600, rent \$4,114, ship salary \$20,400, rent \$8,173, shoe salary \$21,000, rent \$6,359, thimble salary \$18,600, rent \$3,725, high hat salary \$18,600, rent \$7,808
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
- Income by player: racecar salary \$18,800, rent \$5,796, wheelbarrow salary \$19,000, rent \$4,674, dog salary \$33,400, rent \$690,300, iron box salary \$17,000, rent \$5,882, ship salary \$22,600, rent \$16,699, thimble salary \$16,600, rent \$3,237, shoe salary \$20,600, rent \$5,204, high hat salary \$18,000, rent \$6,972
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
- Income by player: racecar salary \$290,800, rent \$55,372, wheelbarrow salary \$258,800, rent \$85,752, dog salary \$512,600, rent \$550,131, iron box salary \$184,200, rent \$77,194, ship salary \$142,200, rent \$24,039, shoe salary \$169,600, rent \$54,239, thimble salary \$143,800, rent \$65,041, high hat salary \$179,600, rent \$63,916
- Entities: 23 formed, 3 dissolved
- Peer trades: 1
- War-profits tax: 52 payments, \$275,456 total
- Tax payers: dog 48, racecar 2, iron box 2
- Government balance: min 0, median 5335, mean 5509.12, max 10956
- Survivors at first tax: min 2, median 4, mean 3.9583333333333335, max 7
- Effective tax burden: racecar 0.72%, wheelbarrow 0.00%, dog 25.61%, iron box 0.32%, ship 0.00%, shoe 0.00%, thimble 0.00%, high hat 0.00%

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
