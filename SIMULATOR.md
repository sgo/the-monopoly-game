# Monopoly Simulator

Run a complete simulated game of Monopoly from the command line, with each
player driven by a pluggable behavior strategy, and produce a human-readable report of
everything that happened.

## Scope for this phase

- **Pluggable player strategy.** Computer players decide their actions through
  a strategy abstraction, so the decision logic for any player can be swapped
  independently of the game engine and of other players' strategies.
- **One strategy for now: "Agree if affordable".** When a player lands on an
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
  - the strategy used for each player, defaulting to "Agree if affordable" if
    not specified.

  The CLI runs the game and prints the game report.

## Out of scope for now (future work)

- Additional standalone strategies beyond "Agree if affordable" (e.g. trading,
  strategic building) — the strategy abstraction should allow adding these
  later without changing the CLI, journal, or engine. Selective buying is no
  longer entirely future work: "Agree if affordable" itself now carries a cash
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

#### Agree if affordable

This strategy will pay to buy, win an auction, build, take out a mortgage if 
funds drop too low, trade, jail exit choice, ...

Provided it has the financial means to do so.

Buying keeps a configured cash reserve: it declines a purchase that would
leave it below that reserve, same as if it couldn't afford the price at all.
Utilities carve out an exception — the strategy buys an available utility
regardless of the reserve whenever doing so completes its own utility
monopoly or denies another player theirs. Otherwise (nobody yet owns a
utility) buying one still respects the reserve like any other purchase.

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
  (defaulting to "Agree if affordable");
- runs one game
- prints the rendered game report to console
- starts from the normal standard game configuration
- uses real random dice and lets the game continue until bankruptcies leave
  one player; it must not use pre-arranged state, a turn limit, or a synthetic
  winner
- writes the final game report to a file, which defaults to
  `the-monopoly-game.report` in the system temporary directory
