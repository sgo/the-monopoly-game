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

## Observations

### A Game of Greedos

A game of 3 Greedos runs forever 50% of the time.

The reason being that as a result of randomness the streets in the more expensive colour groups will be divided over the
different players. As a result, none of them can develop the streets and charge expensive rent. Thus, the cost of moving
around the board never exceeds the $400 salary gained each turn, and everyone just becomes rich.

I ended up programming this as a stalemate condition on the simulation when every player on the board accumulated wealth
equal or larger than the full wealth of the board. So, the cost of all fully developed spaces and their potential rent
income. This total wealth amounts to around $22000.

It's interesting to observe that in a world where everyone is greedy to a fault and has equal opportunity, they live
happily ever after one out of two games.

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

|                   | Without trading (n=1000) | With trading (n=1000) |
|-------------------|--------------------------|-----------------------|
| Stalemate         | **48.2%** (482)          | **17.6%** (176)       |
| Bankruptcy winner | 51.8% (518)              | 82.4% (824)           |

Finally, in all games where a stalemate is forming and trading happens, one of the less wealthy Greedos wins **~63%** of
the time.

Now the results above were observed in a game of 3 Greedos. As soon as you have more and especially with 8 Greedos you
would still get a stalemate almost 100% of the time. This is because as soon as colour groups becomes split between
three instead of two players, making a mutually beneficial trade becomes almost impossible. The three players would need
to hold three streets from three different colour groups, and all three of them would need to hold at least one street
in the same three colour groups. Which is near impossible to happen.

So, how to break the stalemate with 8 Greedos?

##### Legal Entities

When three Greedos hold one street from the same colour group, they can transfer their property into a legal entity. In
the game these are identified by names such as "Pink Realty" after the colour group of the streets.

This legal entity now meets the requirement where a full colour group has a single owner and development can begin. This
breaks the stalemate as costs of traveling around the board now exceeds salary income.

Development of the properties in the legal entity is financed by taking out loans from the shareholders or rental income
as it comes in. After development rental income will also be used to repay the loans with a 5% interest. Finally, when
no further development is possible and no more loans need to be repaid the legal entity will pay out dividends to the
shareholders.

As a result of this change the stalemate is broken ~85% of the time even with eight Greedos.

|                   | With stalemate trading (n=20) | With legal entities (n=20) |
|-------------------|-------------------------------|----------------------------|
| Stalemate         | **80%** (16)                  | **15%** (3)                |
| Bankruptcy winner | 20% (4)                       | 85% (17)                   |

And when the game does end in a stalemate it is because most of the time everyone but the shareholders are destroyed.
While the shareholders themselves live in a sort of equilibrium because of the shared income from the legal entity. Then
the imbalance among them does push the game into a duration of thousands of years.

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
| **1 Billionaire + 7 Greedo** (n=50) | 15 (30%)                | 35 (70%)  |
| **8 Greedo, no Billionaire** (n=50) | 44 (88%)                | 6 (12%)   |

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
