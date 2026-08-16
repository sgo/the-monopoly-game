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

- Additional strategies with genuinely different decision logic beyond
  "Greedo" (e.g. trading, strategic building) — the strategy abstraction
  should allow adding these later without changing the CLI, journal, or
  engine. Selective buying is no longer entirely future work: "Greedo"
  itself now carries a cash reserve and utility-monopoly awareness (see Key
  Concepts) rather than becoming a separate strategy; further selectivity
  (e.g. colour-group awareness for streets) remains unspecified.
  "Billionaire" (see Key Concepts) is a second strategy, but it reuses
  Greedo's decisions verbatim and only changes the opening balance, so it
  doesn't count against this — a strategy with genuinely different decision
  logic is still future work.
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

#### Billionaire

Makes exactly the same decisions as "Greedo" — the same buying, rent,
building, jail-fine, inherited-mortgage, and distressed-sale-bidding logic —
but the game opens its account with \$57,700,000 instead of the usual
\$1,500. Opening capital is a property of the strategy, applied once before
the game starts, and replaces the standard opening balance rather than
adding to it. There is no other behavioral difference from Greedo; it
exists to let the CLI simulate a cash-dominant player without inventing new
decision logic.

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

### Optional: Legal entity for 3+-way colour-group splits

The key blind spot of the mechanisms above is a colour group split across
*three or more* owners. Stalemate trading's buyout only handles a group split
between exactly two players, so at 3+ players a split colour group can never
be built out or consolidated — rent stays capped at vacant rates and the game
can stall forever (observed at 8 players, where this is the norm). An opt-in
CLI flag, `--optional-greedo-legal-entity`, adds a "legal entity" mechanism
that lifts exactly that impasse by letting three co-owners of a split colour
group form a company that owns, develops, and rents out the whole group
together.

#### Formation

When the flag is enabled, once the **whole board is owned** and the market is
dead (no ownership-consolidating action — trade, buyout, bankruptcy transfer,
or individual development — happened in the just-completed round), the game
automatically forms a legal entity over any eligible colour group, **before**
`Stalemate.reached` can fire. Formation requires, all at once:

- **An eligible three-owner split.** The colour group must be split across
  exactly **three distinct owners/players**. A two-owner (or non-player-owned)
  split is never consolidated this way, and the entity is never formed over a
  *highest-priority* colour group.
- **A real next improvement.** At least one of the group's streets must not yet
  have a hotel. A split whose streets are already fully developed is **not**
  selected — it has no build plan and would only pay dividends, so formation
  refuses it.
- **Collective fundability.** The three shareholders must be able to
  **collectively** fund the next improvement on the split group **after** their
  base reserves — each keeps its own cash reserve, and their combined surplus
  must cover the next build.

The entity is named after its colour group ("Pink Realty", "Yellow Realty",
"Green Realty", …) and is held in **equal thirds** by the three co-owners. It
is an opt-in behaviour, so it never forms unless `--optional-greedo-legal-
entity` is enabled (with only stalemate trading on, the split stays untouched).

`Stalemate.reached` (the final cash-threshold terminal check) stays fully
separate from this trigger: the entity may form pre-stalemate while the cash
threshold has not yet been reached.

#### Operation

A legal entity has its own **bank account** (rent and loans land there, not in
any shareholder's pocket), and acts once per round in a fixed priority:

1. **Build** — develop its streets as far as it can afford from its treasury.
   If its own funds are short, it can **solicit a build loan** from its
   shareholders: each Greedo shareholder commits a share of the shortfall, and
   the loan is only raised if **every** shareholder can afford its share and
   keep its base reserve intact (**ALL-OR-NOTHING**): one decliner blocks the
   whole loan. A shareholder's commitment is also capped at a personal
   affordability ceiling. With no loan needed, it builds as many houses as its
   treasury allows at the end of the turn.
2. **Repay debt** — pay back any outstanding shareholder loan (principal + 5%
   interest) before distributing anything.
3. **Pay a dividend** — an equal dividend to all shareholders, **only** once the
   entire loan (plus interest) has been repaid **and** every entity street has a
   hotel ("fully developed"). Two extra gates must also hold: the entity
   treasury must be at or above the dividend threshold, and the last
   shareholder to have injected build capital must have aged a year. A
   fully-developed entity therefore settles as financially inactive at the
   round boundary when those gates fail — it does not build or pay out.

Rent from tenants is collected into the entity's bank account, and a
shareholder who lands on their *own* entity's street **still pays rent** into
the entity (they are not exempt).

#### Distressed shares and liquidation

When a shareholder is about to go bankrupt, before their own houses are sold
or their own land mortgaged, they may **sell a share of their legal entity** to
a fellow shareholder to raise the shortfall — the share's value is based on the
entity's maximum developed rent. A fellow shareholder bids up to a third of
their bank balance; if no fellow shareholder will bid, the share does not
change hands. When only **one shareholder remains**, that final shareholder may
**liquidate** the entity to settle their debt, keeping the entity's remaining
treasury.

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

### Known characteristic: legal entity narrows, but doesn't guarantee, 3+-player resolution

The legal-entity mechanism above removes the specific 3+-way colour-group-
split impasse, but was never validated to force a bankruptcy or a terminal
stalemate — that empirical outcome has no deterministic acceptance
criterion to hold code to. Five real 8-player Greedo games
(`--optional-greedo-stalemate-trading --optional-greedo-legal-entity`)
found the impasse itself gone, but 4 of 5 runs still never resolved,
grinding on to 240–514 player-years; the one run that did resolve did so by
ordinary bankruptcy, not because the entity mechanism broke anything.
Treated the same way as the Greedo-vs-Greedo stalemate above: a real,
understood dynamic of the strategy, not a rules-engine defect — no fix
planned.

### Known characteristic: a Billionaire-mix endgame is slow-converging, not perpetual

Once one player follows "Billionaire" instead of "Greedo", a game that
collapses to two survivors (the billionaire plus one Greedo, or one Greedo
legal entity) doesn't share the Greedo-vs-Greedo stable-oscillation
characteristic above — a cash-rich-but-effectively-landless billionaire and
a land-rich Greedo/entity instead show a real, near-linear convergence
trend. One 5-minute-capped 8-player run measured the billionaire's balance
draining at a remarkably consistent ~1,610–1,640/year across four separate
multi-thousand-year windows, extrapolating to bankruptcy within another
4–5 minutes of wall-clock the cap narrowly missed. A 50-game batch (1
Billionaire + 7 Greedo, both optional flags on, `--max-years=2500`)
confirms this in aggregate: every single game resolved well inside the
cap — 15/50 ordinary wins, 35/50 stalemates at 2–4 survivors, none reaching
even 700 simulated years — and the billionaire was a winner or stalemate
survivor in all 50, never bankrupt. `--max-years` (see [CLI](#cli) below)
exists precisely to cap the rare game that doesn't converge quickly,
without having to tell "slow" from "perpetual" ahead of time.

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

The same measurement across 20 real 8-player Greedo games (real dice, up
to a 3000-round safety cap — well over 20,000 rolls at 8 players):

- **18 of 20 games stalemated**; the other 2 were still running at the
  safety cap. **No game ended in an ordinary win.** Average final age
  among the stalemated games: ~295 years (median 290, range 206-408).
- Unlike the 2- and 3-player cases, there is no "typical quick game"
  bucket to contrast this against — stalemate (or still running) *is*
  the normal outcome at 8 players, consistent with the buyout mechanism's
  blindness to 3+-way colour-group splits documented above.

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

Two optional flags extend the Greedo strategy's behaviour (both opt-in; neither
is on by default):
- `--optional-greedo-stalemate-trading` — enables the peer-trade/buyout bridge
  described under [Optional: Greedo stalemate trading](#optional-greedo-stalemate-trading).
- `--optional-greedo-legal-entity` — enables the legal-entity mechanism for
  3+-way colour-group splits described under
  [Optional: Legal entity for 3+-way colour-group splits](#optional-legal-entity-for-3-way-colour-group-splits).
- `--max-years=N` — caps the simulation at N simulated years (passes through
  Go/jail). The game stops when any remaining player's age reaches N. The
  default (omitted) lets the game play to its natural end.
