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

*Note: the more players, the less likely monopolies can be formed between just two of them. More of the less wealthy
Greedos would have to come together to break the stalemate. I haven't considered what conditions they could agree on
given them all a reasonable chance to win the game and thus agree to the trade.*

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

**Conclusion: stalemates exceed the normal life expectancy of a player. It would be interesting to pass their wealth, or absence thereof, on to their inheritor. Which would introduce inequality as these inheritors would be players who start the game with a starting capital well above the standard $1500.**

*Note: I even observed games where players reached thousands of years*