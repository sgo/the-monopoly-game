### Rent Relief and Unified Income Tax

Previously we managed to re-introduce equal opportunity in a world with an asset-rich billionaire using an aggressive
tax on rental income called the [war profits tax]({% link economics.md %}#war-profits-tax). However, equal opportunity in a
world of [Greedos]({% link economics.md %}#doesnt-want-to-live-happily-ever-after) is still cut-throat.

How to introduce stability into our economy?

#### Rent Relief and Taxes on Labour

My first attempt was to introduce a government policy of rent relief, capping rent at \$200. Available to all provided
the government has the funds to do so.

But the [war profits tax]({% link economics.md %}#war-profits-tax) doesn't tax anyone before they already own 25% of the board.
Which is a terminal condition for the economy. It doesn't make sense to wait on that. We need funds from somewhere else.

In the real world the government is primarily funded by taxes on labour and consumption. So let's introduce taxes on
labour as our players already collect a salary.

To do this, two concepts are introduced to the game:

- **The government**, collecting taxes into a bank account and giving out rent relief.
- **MegaCorp**, a multinational employing every player in the game and paying out salaries every time they pass by
  Start.

Players were already collecting $200 as they passed by Start.
The game now no longer prints money but instead has MegaCorp pay out the salary and wire taxes on labour to the
government's bank account.

Here's how it's all broken down:

| Income Type             | Gross    | Tax                | Net   |
|-------------------------|----------|--------------------|-------|
| **Labour**              | \$350.88 | \$150.88 (**43%**) | \$200 |
| **Rent (illustrative)** | \$200    | \$0 (**0%**)       | \$200 |

For now, I elected to skip progressive taxation on labour. Instead, I used the [average tax burden of **43%
**](https://wid.world/document/rethinking-capital-and-wealth-taxation-world-inequality-lab-working-paper-2022-18/) to
act as a simpler stand-in. Rent collectors are in luck and pay 0%.

As for the rent relief, it follows these rules:

| Rentor | Rentee | Rent  | Government | Rentor? | Rentee? | Government? |
|--------|--------|-------|------------|---------|---------|-------------|
| \$0    | \$1000 | \$500 | \$300      | \$500   | \$800   | \$0         |          
| \$0    | \$1000 | \$500 | \$200      | \$500   | \$500   | \$200       |

The Rentor always receives the full rent for the property. So this is not a cap on rent. The Government will pay the
Rento the difference. Unless its funds are too low. Then the full weight of the rent falls on the Rentee. If the Rentee
is unable to pay the rent, then the usual [distressed sale](https://github.com/sgo/the-monopoly-game/blob/main/SIMULATOR.md#distressed-sale) mechanism kicks in to
avoid bankruptcy.

Did this help stabilise the economy?

| Condition (n=50 + rent relief) | WIN (bankruptcy-driven) | STALEMATE |
|--------------------------------|-------------------------|-----------|
| **8 Greedo - rent relief**     | 50 (100%)               | 0 (0%)    |
| **8 Greedo + tax on labour**   | 48 (96%)                | 2 (4%)    |
| **8 Greedo + war profits tax** | 33 (66%)                | 17 (34%)  |

No, it did not. In the economy of Monopoly, income from labour is low. Buying property is cheap and paying rent is
brutal. Taxes on labour alone have a negligible effect. The existing [war profits tax]({% link economics.md %}#war-profits-tax)
does more, but it only kicks in when about half the population has perished. So we need something better.

#### Unified Income Tax

The real source of wealth in the game is rent collection. So what happens if we tax all sources of income equally at
43%?

| Condition (n=50 + rent relief)    | WIN (bankruptcy-driven) | STALEMATE | AGE AT END                               |
|-----------------------------------|-------------------------|-----------|------------------------------------------|
| **8 Greedo + war profits tax**    | 33 (66%)                | 17 (34%)  | min 7, median 68, mean 152.28, max 1730  |
| **8 Greedo + unified income tax** | 10 (20%)                | 40 (80%)  | min 11, median 56, mean 68.04, max 259   |
| **8 Greedo - all regulations**    | 8 (16%)                 | 42 (84%)  | min 6, median 281, mean 348.02, max 1419 |

With a unified income tax, the [stalemate condition]({% link economics.md %}#a-game-of-greedos) becomes the most probable outcome. In fact, the only time this happened before was when we let eight [Greedos]({% link economics.md %}#greedo) play each other without any regulations to help them break the stalemate.

Does that mean we should simply deregulate instead of make the effort of collecting all these taxes? It's certainly an idea put forward by politicians on the right side of the spectrum.

To answer that question, I included the **AGE AT END** column in the table above. It shows when the stalemate condition was reached. And as you can see, the unified income tax reaches the [stalemate condition]({% link economics.md %}#a-game-of-greedos) much faster.

**Conclusion:** If you want everyone to become wealthy quickly, then go for a unified income tax. If you want to struggle amongst the happy few for 1730 years, then deregulate.