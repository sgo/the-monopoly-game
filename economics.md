# Economics

I wanted to build a simulator based on the game of Monopoly as it is often used by people who talk about inequality. The
saying would go something like "You're entering the game when everything is already sold."

## Personas

To make the simulation interesting, I need personas that compete against each other using different strategies. I'll
summarize them here.

### Greedo

- The archetype of the super rich.
- They pursue wealth above all else no matter the consequences.
- They do not trade as a rule as their greed does not allow win-win relationships.
    - Inversely, they won't accept a trade in which they are the losing side

For more details see [simulator.md](SIMULATOR.md#Greedo)

### The Billionaire

- Behaves like Greedo
- Has two modes of extreme wealth
  - Starts the game with \$57.7 million.
  - Starts the game owning 25% of the board.

## Observations

### A Game of Greedos

A game of 3 Greedos runs forever 22% of the time.

The reason being that as a result of randomness the streets in the more expensive colour groups will be divided over the
different players. As a result, none of them can develop the streets and charge expensive rent. Thus, the cost of moving
around the board never exceeds the \$200 salary gained each turn, and everyone just becomes rich.

I ended up programming this as a stalemate condition on the simulation when every player on the board accumulated wealth
equal or larger than the full wealth of the board. So, the cost of all fully developed spaces and their potential rent
income. This total wealth amounts to around $22000.

It's interesting to observe that in a world where everyone is greedy to a fault and has equal opportunity, they live
happily ever after one out of five games.

#### Doesn't Want to Live Happily Ever After

I realised that it fits the nature of the Greedo persona to see the value in cooperation when confronted with a
stalemate.

Specifically, the less wealthy Greedos would feel envy towards the wealthiest. And thus they are willing to take a
chance. Because doing nothing means being less wealthy forever.

So, the Greedos with lower wealth start trading amongst each other. the rules are a bit complex but in short they will
start handing each other monopolies. Not of the high-value streets (orange, yellow and red) because that would mean
handing the win over to your opponent. But of the lower value streets. That is enough to change the cost of moving
around the board above the $200 salary. And as the less wealthy Greedos are the ones receiving the rent they are now
best positioned to win the game.

The table below shows the impact this sudden sense of cooperation had on the outcome of the game.

|                   | Without trading (n=50) | With trading (n=50) |
|-------------------|------------------------|---------------------|
| Stalemate         | **22%** (11)           | **0%** (0)          |
| Bankruptcy winner | 78% (39)               | 100% (50)           |

Now the results above were observed both in games of three and eight Greedos. However, the first time the peer-trading feature was added there was still an undiscovered bug preventing eight player games from breaking the stalemate. As a result I assumed this was because as soon as colour groups become split between
three instead of two players, making a mutually beneficial trade becomes almost impossible.

This was false. Instead, a bug prevented most players from developing their properties even if they handed each other monopolies. This issue has since been resolved and peer-trading breaks a stalemate 100% of the time even with eight players.

##### Legal Entities

When three Greedos hold one street from the same colour group, they can transfer their property into a legal entity. In
the game these are identified by names such as "Pink Realty" after the colour group of the streets.

This legal entity now meets the requirement where a full colour group has a single owner and development can begin. This
breaks the stalemate as costs of traveling around the board now exceeds salary income.

Development of the properties in the legal entity is financed by taking out loans from the shareholders or rental income
as it comes in. After development rental income will also be used to repay the loans with a 5% interest. Finally, when
no further development is possible and no more loans need to be repaid the legal entity will pay out dividends to the
shareholders.

As this functionality was introduced following a bug preventing peer-trading from breaking the stalemate with eight players this functionality has no appreciable effect in eight player games.

|                   | With peer trading (n=50) | With legal entities (n=50) |
|-------------------|--------------------------|----------------------------|
| Stalemate         | **0%** (0)               | **0%** (0)                 |
| Bankruptcy winner | 100% (50)                | 100% (50)                  |

### How Long Does a Game Last?

Especially for games ending in a stalemate, one can wonder how long it took to reach that state.
The stalemate condition is clear, everyone owns a minimum of ~$22000. But how long it takes to meet that condition is
less clear.

So I updated the game to count each player's age.
Which means we need to decide how much playtime equates to 1 year.

I decided to consider the salary received as you pass by Start to be a yearly salary. So, age is incremented by passing
Start or being sent to Jail. As being sent to Jail, explicitly states you do not pass by Start to do so.

Then the following information was observed running the game several times.

| #players | avg age (ended in bankruptcy) | avg age (ended in stalemate) |
|----------|-------------------------------|------------------------------|
| 2        | ~19.4                         | ~381                         |
| 3        | ~13.9                         | ~265.7                       |
| 8        | -                             | ~295.4                       |

**Conclusion: stalemates exceed the normal life expectancy of a player. It would be interesting to pass their wealth, or
absence thereof, on to their inheritor. Which would introduce inequality as these inheritors would be players who start
the game with a starting capital well above the standard $1500.**

*Note: I even observed games where players reached thousands of years*

### The Billionaire

To determine what a billionaire would be in the game of Monopoly I looked up the median income of a Belgian. Which
equates to €3728. Or €44736 per year. At this rate a Belgian needs **28866 years** to earn the full wealth of the richest
Belgian. (€12 913 683 000)

Given the yearly salary in Monopoly is $400 a billionaire in Monopoly would have \$57.7 million.

#### It's all about money...

The first version of our Billionaire strategy is one that starts the game with \$57.7 million instead of the standard \$1500. Otherwise, the Billionaire is identical to Greedo.

What does this mean for the game?
I ran 50 games with peer trading and legal entity formation enabled.

| Condition                           | WIN (bankruptcy-driven) | STALEMATE |
|-------------------------------------|-------------------------|-----------|
| **1 Billionaire + 7 Greedo** (n=50) | 44 (88%)                | 6 (12%)   |
| **8 Greedo, no Billionaire** (n=50) | 50 (100%)               | 0 (0%)    |

Visibly, the Billionaire made the stalemate outcome more likely.
Not because he prevents other players from acquiring all the properties but because they simply can't bankrupt him before they reach the stalemate condition. (\$22000)

So are Billionaires a stabilising force on the economy in spite of the obvious inequality?

Not quite. You see these stalemates weren't of the happily ever after type. Most of the time everyone got destroyed except for one lucky and one unlucky one. And we should classify these three as follows...

* The Billionaire
* The Multi Millionaire
* The Millionaire

So where most of the other players simply bankrupted these guys picked up their wealth potential. The Millionaire is actually the unlucky one as around the 15.000 year mark he is destroyed by the other two.

Then it goes one of two ways.

1. The Billionaire destroys the Multi Millionaire because of his financial advantage.
2. The Multi Millionaire slowly destroys the Billionaire because he happened to be asset poor.

Btw, it takes about 30.000 years to destroy the Billionaire. Which is close to the 28.800 years a Belgian has to work to earn the amount of wealth the richest Belgian holds.

**Conclusion: we need a Billionaire that is asset rich... just like in real life.**

#### It's all about assets...

You know. In the real world, billionaires are asset rich. They don't have a lot of money in their bank account. They're actually quite poor that way. Which is why they apply for, and receive, benefits.

Why don't we test that?

In Belgium the 1% own 25% of the GDP.
Now, Monopoly has only eight players so our one billionaire represents more than just the 1% of the game world.

So I've decided to introduce an optional feature which converts our billionaire into an asset rich one owning 25% of the board and having the same starting capital as everyone else.

Here are the results:

| Condition                           | WIN (bankruptcy-driven) | STALEMATE |
|-------------------------------------|-------------------------|-----------|
| **1 Billionaire + 7 Greedo** (n=50) | 50 (100%)               | 0 (0%)    |

An asset rich billionaire always wins!
So, not having a lot of money in the bank account appears not to matter much.

As for the others. Some managed to almost make $2000 but everyone was destroyed within 4 years. That's the complete absence of social safety nets.

#### Collateralized Loans & Corporate Bonds

In an attempt to prolong the game with an asset-rich billionaire I decided to introduce the ability to take out loans from the bank.

Now the game already has a system like it, but it requires that you sell all the houses on it and then mortgage the undeveloped land instead. This would dry up your income stream and would be very punishing.  So, instead you can now take out a loan where the asset you're going to build with it is the collateral. Not just the land. But also the houses and hotels.

The bank can't just hand over money to anyone who asks. As that would equate to quantitative easing and cause inflation. At this point I did not want to program in the effects of inflation so no loans backed by money printing.

Instead, the bank needs to sell corporate bonds to finance their loans. Corporate bonds ar essentially loans from other players to the bank. In practice if someone asks for a \$100 loan with an interest rate of 5% then the bank will need a corporate bond for \$100 with an interest rate of 3%. The bank will then pocket the remaining 2% should the player default, the collateral does not cover the difference and the corporate bond can not be reallocated to another loan in time.

So, did this help expanding the duration of the game beyond the ~4 years the asset-rich bilionaire needed to win the game?

No, it didn't. Loans were taken out. They weren't even primarily backed by the billionaire. The billionaire was the primary one taking out loans in fact. And he was doing this to buy up th entire board and win the game. If anything, it made things easier as money no longer was an obstacle. And it already wasn't. So, the billionaire would win the game 100% of the time within ~4 years even with loans in play.

I suppose we can't blame our players from trying to win the game. And I don't think we should make them self-aware enough that winning the game means starvation even for them. Because when we look at billionaires in the real world they don't show any signs of self awareness. They'll push for war and more tax exemptions and more subsidies paid with taxes from regular people.

So, I just don't see any mechanic other than government imposed regulation or taxes that could stop the billionaire from buying up the whole board.