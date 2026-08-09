# Monopoly Simulator

Run a complete simulated game of Monopoly from the command line, with each
player driven by a pluggable behavior strategy, and produce a human-readable report of
everything that happened.

## Scope for this phase

- **Pluggable player strategy.** Computer players decide their actions through
  a strategy abstraction, so the decision logic for any player can be swapped
  independently of the game engine and of other players' strategies.
- **One strategy for now: "Greedo".** When a player lands on an
  unowned, purchasable space (street, station, or utility) and can afford the
  listed price, it buys — unless buying would leave it below a configured cash
  reserve, in which case it declines instead (and the space goes to auction as
  normal, per [Buying Land](RULES.md#buying-land)). Utilities are exempt from
  the reserve check when buying one would complete the player's own utility
  monopoly or deny another player theirs; the strategy buys in either of those
  cases regardless of the reserve.
- **Journal.** Every game event (dice rolls, moves, purchases, rent payments,
  card draws, jail entry/exit, bankruptcies, the eventual winner, etc.) is
  recorded to a journal.
- **Game report.** The journal can be rendered as a human-readable account of
  the game — a chronological narrative, not raw structured data — for someone
  to read after the fact.
- **CLI.** An entry point to start a simulated game, letting the user choose:
  - the number of players (2–8, per [Setup](RULES.md#setup));
  - the strategy used for each player, defaulting to "Greedo" if
    not specified.

  The CLI runs the game and prints the game report.

## Out of scope for now (future work)

- Additional standalone strategies beyond "Greedo" (e.g. trading,
  strategic building) — the strategy abstraction should allow adding these
  later without changing the CLI, journal, or engine. Selective buying is no
  longer entirely future work: "Greedo" itself now carries a cash
  reserve and utility-monopoly awareness (see Key Concepts) rather than
  becoming a separate strategy; further selectivity (e.g. colour-group
  awareness for streets) remains unspecified.
- Human/interactive players.
- Persisting, replaying, or comparing results across many simulated games
  (the existing `en/monopoly.feature` 1000-game scenario already exercises
  repeated simulation at the spec level, but that's a correctness check, not a
  reporting feature).

## Key concepts

### Player Behavior Strategy

An abstraction the game engine consults at each decision point a player faces.
The interface should be shaped so adding a decision point later doesn't require 
reshaping the ones already there.

The supported behavioral strategies are...

#### Greedo

Provided it has the financial means to do so, this strategy always tries to
get ahead, but never overpays when it doesn't have to.

- **Buying** (landing on unowned land, or bidding at an ordinary auction):
  buys/bids up to its full available balance above a configured cash
  reserve; below that it declines/stops bidding, same as if it couldn't
  afford the price at all. Utilities are exempt from the reserve whenever
  buying completes its own utility monopoly or denies another player theirs.
- **Cash reserve** is not fixed: it grows to protect the price of a street
  the player is one purchase away from turning into a monopoly (picking the
  tightest such opportunity if more than one exists), and similarly protects
  $200 when the player is one station away from owning all four.
- **Rent**: always claims rent it's owed.
- **Building**: builds a house or hotel whenever it can afford to.
- **Jail fine**: pays the M50 fine immediately whenever it can afford to
  (never chooses to attempt doubles instead if paying is an option).
- **Inherited mortgages** (after a bankruptcy transfer): lifts an inherited
  mortgage immediately if it can afford the principal plus 10% interest,
  otherwise keeps it mortgaged and pays only the interest.
- **Distressed-sale bidding** (see [Distressed sale](#distressed-sale)
  below): declines outright if winning would let the debtor go fully
  bankrupt into it for free instead (i.e. it's the debtor's last solvent
  opponent and already has more cash than the debtor's total remaining
  property and debt combined); otherwise bids its full available balance if
  the purchase completes its own colour-group monopoly; otherwise bids up to
  35% of its available balance if the land is one it considers
  highest-priority to deny an opponent; otherwise declines.

### Distressed sale

When a player owes a debt to another player (not the bank) that they can't
cover outright, before their own houses are sold or their own land
mortgaged, they get one attempt to raise the shortfall by selling land to a
solvent opponent instead. Candidate land is offered cheapest-priority-first,
one property at a time, stopping as soon as the debtor is solvent again; any
street whose colour group has houses or a hotel built anywhere is excluded,
so a developed monopoly can never be forced into this path. The price is
needs-based: never less than that land's own mortgage value, and never more
than covering the actual remaining shortfall (after crediting the mortgage
value of whatever other candidate land the debtor still has left). Eligible
opponents ascend a $5-increment auction; a lone eligible bidder simply pays
the minimum. If a sale would complete the buyer's own monopoly while the
debtor still has a house or hotel they could sell instead, that property is
skipped in favor of forcing the house sale first, so a buyer can't pick up a
monopoly-completing property on the cheap when the debtor had another way to
pay. Anything still unsold once the debtor remains insolvent falls through
to the ordinary bankruptcy sequence (sell houses, mortgage remaining land,
then forfeit).

### Known limitation: Greedo vs. Greedo can stalemate

Two players both using "Greedo" (the CLI's default) can produce a game that
runs for millions of turns without a bankruptcy. Cause: ordinary buying has
no monopoly-denial logic (only distressed-sale bidding does), so against a
symmetric copy of itself, colour groups end up split roughly 50/50 by
chance and rarely complete. Once no more monopolies can form, rent stays
capped at low/vacant rates while salary and card income keep both balances
growing; once a balance clears the largest remaining single-turn liability
on the board, that player is effectively unbankruptable. Observed directly:
one player's balance staying a stable ~1.5–2.7x the other's over many
thousands of turns, neither closing nor widening.

This isn't fixable by adding trading between two identical Greedos, even
though that was the obvious next idea: a one-sided trade never gets
accepted by a self-interested opponent, and a mutually fair swap (each
giving up a piece of a group they can never complete anyway) only helps
when the board split happens to offer one. Whether trading even away a
"dead" piece is rational depends on relative position, not just fairness —
the currently-leading player has no incentive to raise the stakes (the
stalemate already locks in their lead), while the trailing player would
benefit from escalating since the status quo guarantees they never catch
up. But a rational leader would then only ever offer a lopsided trade (a
high-value monopoly for itself, a low-value one for the opponent), which a
rational trailing player — correctly weighing the offer rather than just
wanting *a* monopoly — would decline as worse than the safety of the
stalemate. No reasonably-scoped trading strategy eliminates the deadlock in
general; at best it narrows how often it happens. Treated as a known
characteristic, not a defect — no fix planned.

### Optional: Greedo stalemate trading

An opt-in CLI flag, `--optional-greedo-stalemate-trading`, enables a second
mechanism once the whole board is owned: at the start of each "Greedo"
player's turn, it first tries an ordinary cash-free peer trade to complete
one of its own colour groups (same acceptance rules as normal — it will
never give up or accept a highest-priority street, or trade within the same
colour group), and only if no trade is available does it fall back to a
buyout of any colour group currently split between it and exactly one other
player: the co-owner already holding the majority of that group wins it
(favoring whichever trailing player already has a foothold, not whichever
is richer), settling in cash within a 35%-of-balance ceiling, deferring
rather than forcing a free transfer if neither can afford it yet, and never
touching a split highest-priority group at all.

This confirms the "at best it narrows how often it happens" prediction
above, with real numbers. Measured directly, playing each game to genuine
completion (no round limit, matching the CLI's own real dice/no-turn-limit
behavior — see [CLI](#cli) below):

- **3-player, 1000 games each:** stalemate rate drops from 48.2% (482/1000)
  without the flag to 17.6% (176/1000) with it.
- **2-player, 3000 games:** a trade happened in 616 games (~21%). Of the 571
  of those that went on to resolve by ordinary bankruptcy, the player who
  was trailing (lower cash) right before that first trade won 362 of them —
  63%, against 37% for the player who was already ahead. The trade itself is
  actually initiated by the *leading* player slightly more often (57% vs.
  43%) — the mechanism has no built-in preference for the trailing player,
  it just completes whichever monopoly it can on whoever's turn it is — but
  the trailing player still comes out ahead more often than not once it
  fires.

Still not a general fix — most of the "narrows how often it happens" caveat
above still applies, and a meaningful fraction of games still stalemate
even with the flag on — but it measurably converts a large share of
would-be stalemates into an ordinary, decisive bankruptcy ending, and does
so more often in the trailing player's favor than not.

### Player age

Each player has an age: it starts at zero and increases by one every time
they pass or land on Start, and by one every time they're sent to jail
(landing there while "just visiting" doesn't count). It's shown alongside
their balance at the start of every turn, and as a final figure for the
winner, or for every remaining player if the game ends in a stalemate.

Measured directly across 20 real 2-player Greedo games (real dice, played
to a natural conclusion or a 3000-round safety cap):

- **17 of 20 games ended in an ordinary win.** Average age reached: ~19
  years (median 16), ranging from 6-9 in the fastest game (79 rolls) to
  65-69 in the longest ordinary win (694 rolls).
- **2 of 20 stalemated, and 1 was still running at the safety cap** — the
  same long tail documented above. Those three games alone averaged
  381-690 years, dragging the all-games average up to ~89; they're the
  exception, not the typical outcome.

So for a typical (non-stalemate) 2-player game, expect players to reach
somewhere around their late teens to low twenties in age by the time
someone goes bankrupt.

The same measurement across 20 real 3-player Greedo games (real dice,
played to a natural conclusion or a 3000-round safety cap):

- **11 of 20 games ended in an ordinary win.** Average age reached: ~14
  years (median 13, range 7-29) — noticeably lower than the 2-player
  figure, since three players splitting rent income tends to bring on a
  bankruptcy sooner relative to how many laps of the board have passed.
- **9 of 20 stalemated** — matching the ~48% no-trading stalemate rate
  already measured above — averaging ~266 years, the same long-tail
  pattern as the 2-player case.

### Journal and report

As the game is played, game events should be written to a journal and included
in the game result report shown at the end of the game.

The game should log the events written to the journal using SLF4J.

The game result report and journal should have a human-readable format.

### CLI

- The CLI is a standalone executable process. It must accept command-line
  arguments, print the report to stdout, include a `-h`/`--h` flag explaining
  how to use it, and return a nonzero exit code for invalid input.
- prompts for or accepts the number of players and a strategy per player
  (defaulting to "Greedo");
- runs one game
- prints the rendered game report to console
- starts from the normal standard game configuration
- uses real random dice and lets the game continue until bankruptcies leave
  one player; it must not use pre-arranged state, a turn limit, or a synthetic
  winner
- writes the final game report to a file, which defaults to
  `the-monopoly-game.report` in the system temporary directory
