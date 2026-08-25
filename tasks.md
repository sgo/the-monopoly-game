# Implementation Plan

This is a proposed, step-by-step plan for implementing the game described in
`RULES.md` on top of the existing `the-monopoly-game-domain` codebase, driven by
Gherkin specs in `the-monopoly-game-specs`, verified by the APS-based acceptance
pipeline under `acceptance/`. Each phase is meant to be small enough to land as
its own set of commits, with its own spec scenarios (English under `en/` only —
the earlier Dutch `nl/` mirror convention was dropped because the acceptance
pipeline cannot parse localized Gherkin keywords) proving it before moving to
the next phase. Phases are ordered so that each one only depends on mechanics
already built in a previous phase.

This plan also folds in the objectives from [`SIMULATOR.md`](SIMULATOR.md): a
pluggable player strategy, a human-readable journal/report, and a CLI to run
simulated games. Rather than treating the simulator as a separate effort bolted
on at the end, each phase below notes the strategy decision point and/or journal
entries it introduces, so the simulator grows in lockstep with the rules engine.

## Current state (starting point)

Already implemented, so **not** repeated below:

- `Dice` (6-faced, uniform distribution) — `components/dice`.
- `Money`, `Bank`/`Bank.Account` (deposit/credit, no overdraft protection yet).
- `Player`, `Player.Pool` (2–8 players, seeded with starting capital).
- `Rule.Set` / `Rule.Set.Simple` / `Official` rule set: 2 six-faced dice, 2–8
  players, €1500 starting capital, one optional rule
  (`double_salary_when_landing_on_start`).
- `Street` / `Street.Type` / `Street.Factory` pattern, with 3 spaces modelled:
  `start`, `RueGrandeDinant`, `DiestsestraatLeuven`.
- `Board` (ordered list of `Street.Type`, resolved to `Street` via the active
  rule set).
- `Game.play()` currently only rolls for initiative and logs it to a `Journal`;
  no turn loop, movement, or resolution exists yet (`Game.Result` is empty).
- Spec coverage: dice fairness, the 3 modelled streets, rule-set composition
  (dice, player count/pawns, starting balances, start-salary/double-salary
  rules), and a full-board layout scenario currently listing only 3 spaces.
- `en/monopoly.feature` already asserts an end-to-end property: "the game always
  ends in a monopoly" over 1000 simulated games — this is the acceptance test the
  later phases build toward, but the step definitions behind it are not real yet.

## Phase 1 — Complete the board data

Purely additive, low-risk: extend `Street.Type` and add one `Street.Factory`
implementation per remaining space, plus `Board`'s layout list in `Official`, so
all 40 spaces from the [board table](RULES.md#the-board) exist.

### Key Deliverables

- Add the remaining 20 streets (`Street.Colour` needs 7 more values: light blue,
  pink, orange, red, yellow, green, dark blue), each wired to the price/rent/
  house-cost/mortgage figures from
  [Rent, House Costs, and Mortgage Values](RULES.md#rent-house-costs-and-mortgage-values).
- Add a `Station` concept (4 spaces) — price M200, rent by count owned
  (M25/M50/M100/M200), mortgage M100. Likely a new `Street` subtype or a
  sibling `Board`-space kind, since rent depends on *count of same-type spaces
  owned* rather than colour-group monopoly + houses.
- Add a `Utility` concept (2 spaces) — price M150, rent = dice roll × 4 or ×10
  depending on count owned, mortgage M75.
- Add the non-purchasable spaces: `Kans`/Chance (3×), `Algemeen Fonds`/Community
  Chest (3×), `Inkomsten Belasting` (pay M200), `Extra Belasting` (pay M100),
  `Op Bezoek`/Jail-Just Visiting, `Gratis Parkeren`/Free Parking, `Naar de
  Gevangenis`/Go To Jail.
- Update `Official.create()`'s board list to the full 40-space layout in order.
- Extend `official.feature`'s "gameboard layout" scenario to assert all 40
  spaces instead of 3, and add one scenario per new street/station/utility/tax
  space asserting its financial figures (mirroring the existing Rue Grande
  Dinant / Diestsestraat Leuven scenarios).

No turn logic changes here — this phase only makes the board queryable.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Run scenarios → observe failures.
1. Implement minimal code to pass.
1. Refactor. Repeat for each new behavior.
1. Run coverage; do not proceed until ≥90% on this module.

### Success Criteria:

- All Task 1 scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 2 — Turn loop and pawn movement

Introduce the concept of a pawn's position on the board and a real turn loop in
`Game.play()`, replacing the current initiative-only stub.

### Key Deliverables

- Give `Player` a position on the `Board` (index into the 40-space layout).
- Implement "roll dice, move pawn N spaces, wrap around the board" (Passing /
  Landing on `START` in RULES.md).
- Implement the €200 (M200) salary on passing or landing on `START`, including
  the "twice in one turn" edge case already covered by the existing
  `double_salary_when_landing_on_start` optional rule.
- Implement doubles: roll again after resolving the current space; three
  doubles in a row sends the player directly to Jail instead of a third move
  (see [Rolling doubles](RULES.md#rolling-doubles) and [Jail](RULES.md#jail)).
- Land on a space → dispatch to a per-space-type resolution (stub the actual
  effects for now — buy/rent/tax/card/jail/free-parking — each gets fleshed out
  in a later phase). This dispatch point is the seam later phases hang off of,
  and is also where per-turn strategy decisions and journal entries (Phase 3)
  will eventually be invoked from.
- Spec: turn order (highest initiative roll goes first, then clockwise),
  movement wrap-around, salary on pass vs. land, and the three-doubles-to-jail
  rule.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.
1. Follow the three laws for every micro-behavior.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.
- Game state can be instantiated and inspected in tests without side effects.

## Phase 3 — Strategy interface, journal, and report scaffolding

Wires up the simulator's plumbing early, against today's minimal turn loop,
rather than waiting for the whole rules engine to be complete (per
[`SIMULATOR.md`](SIMULATOR.md)). No real decisions exist yet — this phase is
about the seams later phases will fill in.

### Key Deliverables

- Introduce a `Strategy` abstraction that the game engine will consult at each
  decision point a player faces. Shape it so adding a new decision point later
  (auction bid, build, mortgage, jail exit, ...) doesn't require reshaping the
  ones already wired in.
- Give each `Player` a `Strategy`, defaulting to a placeholder until Phase 4
  introduces the first real decision (buy-or-decline) for "Agree if
  affordable" to answer.
- Extend `Game.Journal.Entry` to cover the events Phase 2 already produces
  (turn start, dice roll, pawn movement, salary collected) instead of just
  game-start/roll-for-initiative.
- Add a report renderer that turns a completed journal into a human-readable,
  chronological account, kept decoupled from the structured `Entry` records so
  report formatting doesn't leak into the engine's internal logging.
- Log the events written to the journal through SLF4J, so the running game
  exposes its progress via the standard Java logging facade (per
  [`SIMULATOR.md`](SIMULATOR.md)).
- Spec: journal captures the turn-loop events from Phase 2, and the renderer
  produces readable text for a short simulated game.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 4 — Buying land and auctions

### Key Deliverables

- Landing on unsold streets/stations/utilities offers the current player first
  right to buy at face value.
- Declining triggers an auction (open to all players including the decliner),
  won by the highest bidder, who pays the bank.
- **Strategy hook:** buy-or-decline, and auction bids, are delegated to the
  landing player's `Strategy`. "Greedo" buys whenever it can
  afford the price, and (when bidding) bids up to the most it can afford.
- **Journal:** record purchases and auction outcomes (winner, price) so the
  report can narrate them.
- Spec: buy on landing, decline → auction → sale to highest bidder, decliner
  may still win the auction, plus "Greedo" buys when it can
  afford to and declines otherwise.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 5 — Rent (streets)

### Key Deliverables

- Landing on an owned, unmortgaged street charges rent per
  [Rent](RULES.md#rent): base rent, or house/hotel rent if built up.
- Monopoly rule: owning every street in a colour group doubles rent while
  unimproved, unless any street in the group is mortgaged.
- No rent charged on mortgaged streets.
- Rent must be actively claimed by the owner (a "waived if not claimed before
  next roll" rule can be modelled as an explicit `claimRent` action rather than
  automatic).
- **Journal:** record rent charged and paid, and who paid whom.
- Spec: base rent, monopoly double rent, no rent while mortgaged, rent with
  houses/hotel (once Phase 7 exists — sequence Phase 5 and Phase 7 so this last
  scenario can be added incrementally).

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 6 — Rent (stations and utilities)

### Key Deliverables

- Station rent by count of stations the owner holds (M25/M50/M100/M200).
- Utility rent = dice roll × 4 (one owned) or × 10 (both owned); if arriving via
  a Chance/Community Chest instruction, roll dice solely to compute rent.
- **Journal:** record rent charged and paid, as in Phase 5.
- Spec: per [Stations](RULES.md#stations) and [Utilities](RULES.md#utilities).

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 7 — Houses and hotels

### Key Deliverables

- Building requires owning every street in a colour group ("monopoly"), and
  even building across the group (no street gets a 2nd house before all have
  1, etc.), up to 4 houses, then a hotel (which returns the 4 houses to the
  bank).
- No building while any street in the group is mortgaged.
- Selling houses/hotels back to the bank at half price, also even-build in
  reverse; hotel exchange back to 4 houses + cash when houses are scarce.
- Bank shortage of houses/hotels: wait, or bank auctions among competing buyers.
- **Strategy hook:** building (and selling back) is delegated to `Strategy`.
  "Greedo" builds evenly across a monopoly whenever it can afford
  to, and only sells houses/hotels back when it needs the cash to cover a debt.
- **Journal:** record house/hotel purchases and sales.
- Spec: even-build enforcement (both directions), hotel construction/cost,
  monopoly-with-partial-build still allows double rent on unimproved streets in
  that group (ties back into Phase 5), bank shortage/auction behaviour, plus
  "Greedo" building opportunistically when funds allow.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 8 — Mortgaging

### Key Deliverables

- Mortgage a street/station/utility (after selling any houses on it) for its
  mortgage value; 10% interest to lift.
- No rent collectible while mortgaged; no building in a group with any mortgage
  outstanding.
- Selling mortgaged property to another player, who chooses to pay off
  immediately (value + 10%) or just pay the 10% and keep it mortgaged.
- **Strategy hook:** mortgaging (to cover a shortfall) and lifting a mortgage
  (once affordable) are delegated to `Strategy`. "Greedo"
  mortgages property only when it can't otherwise cover a debt, and lifts a
  mortgage as soon as it can afford the principal plus 10% interest.
- **Journal:** record mortgage and lift-mortgage events, including interest
  paid.
- Spec: mortgage/lift cycle, interest calculation, rent suppression, mortgaged
  transfer between players with both buyer choices.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 9 — Selling land between players

### Key Deliverables

- Unimproved land/stations/utilities sellable at any agreed price.
- Built-up colour groups cannot be traded until all houses in the group are
  sold back to the bank first.
- Houses/hotels themselves never trade between players, only back to the bank.
- **Strategy hook:** accepting a proposed trade is delegated to `Strategy`.
  "Greedo" accepts any trade it can afford; no strategy proposes
  trades yet, since strategies that initiate trades are future work per
  [`SIMULATOR.md`](SIMULATOR.md)'s out-of-scope list, so this phase's specs
  exercise the trade mechanic directly rather than through a strategy decision.
- Spec: peer-to-peer sale, block on trading a built-up group, block on
  trading houses/hotels directly.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 10 — Chance and Community Chest

**Status: fully shipped (as of 2026-08-02).** Card resolution (every card's
effect, drawn-card journal entries, get-out-of-jail-free retention/sale) was
spec'd first and is covered in `cards.feature`/`journal.feature`/
`logging.feature`/`report.feature`. The "shuffled at setup ... placed at the
bottom of the deck" deliverable below, however, was **not** actually built
alongside it: `Game`'s public constructors defaulted `decks` to
`Cards.Decks.EMPTY`, a permanent no-op, so a real game (including every CLI
run to date) never drew a card at all when landing on Chance or Community
Chest — only the acceptance fixtures' scripted next-card double ever
exercised card resolution. Found 2026-08-02 from a real CLI trace showing a
landing on Chance with no draw logged. Fixed via `cards-15`/`cards-16` in
`cards.feature` (landing without a scripted override must still log a real
card draw) plus an implementation-only follow-up: `Cards.Decks.official()`
now shuffles the real 16-card sets, deals without replacement, cycles when
exhausted, and withholds a drawn Get-Out-of-Jail-Free card from the deck
until `Deeds` reports it released (`Cards.WithholdingDeck`, covered by
`CardsDeckTest`, not Gherkin — see the specifier's logbook notes around
2026-08-02 for why these deck-internal invariants don't fit this project's
full-game-play Gherkin style).

### Key Deliverables

- Two decks (16 cards each, texts now captured in
  [Chance Cards](RULES.md#chance-cards) and
  [Community Chest Cards](RULES.md#community-chest-cards)), shuffled at setup,
  drawn face-down, resolved immediately, then placed at the bottom of the deck.
- Each card's effect maps onto mechanics from earlier phases (move, pay,
  receive, go to jail, get-out-of-jail-free card retained until used/sold).
- "Advance/move" cards pay START salary only if they pass `START` — direct
  "Go to Jail" cards do not pass `START` and collect nothing (already true of
  the Phase 2 movement/salary logic, this phase just needs to reuse it rather
  than re-implement it).
- **Journal:** record each card drawn and its resolved effect.
- Spec: one scenario per card is likely excessive; prefer covering each
  *mechanic* a card exercises (move-and-collect-salary, move-without-salary,
  pay bank, receive from bank, pay each player, receive from each player, go to
  jail, get-out-of-jail-free retained/used/sold, nearest-railroad/utility
  advance with correct rent multiplier) plus a smoke test that all 32 card
  texts parse/resolve without error.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 11 — Taxes

### Key Deliverables

- `Inkomsten Belasting` (M200) and `Extra Belasting` (M100) pay the bank
  outright on landing.
- **Journal:** record tax payments.
- Spec: both tax spaces charge the correct fixed amount.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 12 — Jail

### Key Deliverables

- Entering jail: landing on "Go to Jail", a Chance/Community Chest instruction,
  or three doubles in a turn (Phase 2) — turn ends immediately, no salary.
- Leaving jail: pay M50 fine, use/buy a Get-Out-of-Jail-Free card, or attempt
  doubles for up to 3 turns (success moves that many spaces; failure after 3
  turns forces the fine before moving).
- Players in jail still collect rent normally on unmortgaged property.
- Landing on the jail space without being sent there is "just visiting" —
  no effect.
- **Strategy hook:** the jail-exit choice is delegated to `Strategy`. "Agree
  if affordable" pays the M50 fine immediately if it can afford to, otherwise
  attempts doubles.
- **Journal:** record jail entry (and cause) and jail exit (and method).
- Spec: all four exits, forced-fine-after-three-turns, rent still collectible
  while jailed, just-visiting is a no-op.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 13 — Free Parking

### Key Deliverables

- Already effectively a no-op space; add an explicit scenario asserting landing
  here has no penalty and no reward (guards against a common house-rule
  regression where people expect a jackpot).
- **Journal:** record the landing (even though it's a no-op), so the report
  still narrates every turn without gaps.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 14 — Bankruptcy and winning

### Key Deliverables

- Debt to the bank: forfeit all money/property, which the bank then auctions
  (Get-Out-of-Jail-Free cards return to the bottom of their deck — confirmed
  working: `Bankruptcy.java` calls `Deeds.returnRetainedCardsToDeck`, and
  Phase 10's deck now returns the physical card once `Deeds` no longer
  reports it held).
- Debt to another player: houses/hotels sold to the bank at half price first,
  then the creditor receives remaining money, deeds, and cards (mortgaged
  property transferred per Phase 8's mortgage-transfer rules, with immediate
  10% interest due).
- **Distressed sale** (implemented as part of this phase but never named in
  this plan until now): before falling back to selling the debtor's own
  houses/hotels or mortgaging their own land, a debtor who owes another
  player first gets a chance to raise the shortfall by selling off some of
  their own land to a solvent opponent. Candidate land is offered
  cheapest-priority-first (any street whose colour group has houses or a
  hotel built anywhere is excluded, so a developed monopoly is never forced
  into this path), one property at a time, stopping as soon as the debtor is
  solvent again. Eligible opponents ascend a $5-increment auction; a lone
  eligible bidder simply pays the minimum required. See the post-plan
  `distressed-sale-needs-based-pricing` entry below for how that minimum is
  computed and other refinements made to this mechanic since.
- Last player standing wins; `Game.Result` should carry the winner and enough
  journal detail to answer "did the game end in a monopoly" for the existing
  `en/monopoly.feature` scenario (1000-game simulation).
- **Journal:** record bankruptcy events (debtor, creditor, cause) and the final
  winner — the closing narrative of the game report depends on this.
- Spec: both bankruptcy paths, winner detection, and the full-game simulation
  scenario passing reliably at scale.

This phase completes the rules engine end-to-end; from here, the CLI (Phase 15)
can run genuinely complete games rather than partial ones.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 15 — CLI

### Key Deliverables

- Build a new module (e.g. `the-monopoly-game-cli`) alongside
  `the-monopoly-game-domain` and `the-monopoly-game-specs`.
- Deliver a standalone executable process with a command-line entry point,
  rather than only a programmatic simulator API.
- Define the standard game starting configuration in the core module, including
  player cash and positions, unowned land, unused houses and hotels, and full
  Chance and Community Chest decks.
- Accepts the number of players (2–8, per [Setup](RULES.md#setup)) and, per
  player, a strategy selection — defaulting to "Greedo" when not
  specified.
- Accepts command-line arguments, runs one game via the domain module's `Game`,
  prints the rendered game report (Phase 3's renderer) to stdout, and writes
  the final game report to a file, which defaults to `the-monopoly-game.report`
  in the system temporary directory (per [`SIMULATOR.md`](SIMULATOR.md)).
- Provides a `-h`/`--h` flag explaining how to use the CLI.
- Returns a nonzero exit code for invalid input.
- Since "Greedo" is the only strategy for now, strategy selection
  is effectively a pass-through, but the plumbing (a strategy registry/lookup
  by name) should already accommodate more strategies being added later
  without changing the CLI's shape (per [`SIMULATOR.md`](SIMULATOR.md)'s
  out-of-scope list).
- Verification: invoke the standalone executable end-to-end for a couple of
  player counts, the help flag, and invalid input; confirm complete readable
  reports, usage guidance, stdout output, the report file written to its
  default location in the system temporary directory, nonzero failure status,
  and correct application of the standard game starting configuration. Confirm
  that a complete game uses real random dice and reaches its natural terminal
  state when bankruptcies leave one player, without a turn limit or synthetic
  winner.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 16 — Short Game variant

### Key Deliverables

- Alternate setup (deal 2 title deeds per player, immediate purchase at face
  value), 3-houses-before-hotel threshold, and the second-bankruptcy end
  condition with the net-worth scoring formula from
  [Short Game Variant](RULES.md#short-game-variant).
- Likely modelled as an alternative `Rule.Set.Factory` alongside `Official`,
  reusing Phases 1–14's mechanics with different parameters rather than
  duplicating logic.
- Spec: alternate setup, 3-house hotel threshold, end-of-game scoring on second
  bankruptcy.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Phase 17 — Timed Game variant

### Key Deliverables

- Same deal-two-deeds setup as the short game, but ends on a wall-clock timer
  instead of a bankruptcy count; richest player by the same net-worth formula
  wins.
- Spec: setup, and end-of-game triggered by elapsed time rather than player
  count.

### BDD/TDD Process (mandatory):

1. Create/update scenario files with natural-language Given/When/Then
1. Every scenario must be executed and fail before any glue or production code is written for it.

### Success Criteria:

- All scenarios pass.
- Linter clean.
- Coverage report in the high 90s.

## Notes on sequencing

- Phases 1–2 are foundational and should land first; everything else hangs off
  the per-space dispatch point introduced in Phase 2.
- Phase 3 (strategy/journal/report scaffolding) is deliberately placed right
  after the turn loop exists, so every later phase can add its journal entries
  and strategy hooks incrementally instead of retrofitting the simulator once
  the rules engine is "done".
- Phases 4–9 (buying, rent, houses, mortgaging, trading) are the core economic
  loop and are ordered by dependency (you can't test hotel rent before houses
  exist, can't test mortgaged-rent-suppression before mortgaging exists, etc.),
  but 5 and 6 (street rent vs. station/utility rent) could be swapped or done
  in parallel since they don't depend on each other.
- Phase 10 (cards) is deliberately late because most card effects are thin
  wrappers around mechanics from Phases 2–9; building it earlier would mean
  re-doing it as those mechanics land.
- Phases 12–14 (jail, free parking, bankruptcy) close out the rules needed for
  a complete game and for the existing 1000-game simulation spec to become
  meaningful rather than vacuous.
- Phase 15 (CLI) comes right after the rules engine is complete, since a
  simulated game only reliably ends in a winner once Phase 14 exists — earlier
  phases only produce partial games, which is fine for exercising the journal/
  report (Phase 3) but not for a CLI meant to run real games.
- Phases 16–17 (variants) are last since they're parametrisations of the
  already-complete official ruleset, not new mechanics.

## Plan status

Phases 1–17 above have all shipped (see `logbook.md` and each phase's commit
history for the specifier/coder/refactorer/architect record). Work since then
has been incremental refinement requested directly against the running game
rather than new phases of this plan; it's tracked below instead of renumbered
into the phase list above, since none of it introduces a new mechanic the plan
didn't already call for.

## Post-plan refinements

- **`movement-space-names`** (done) — the journal/log/report's "moves from
  position X to Y" lines now name the board space next to each position
  number (e.g. "7 (Kans / Chance)"), not just the bare index. Pure
  readability; not called for by any phase above.
- **`logging-output`** (done) — the "starts a turn" journal/log/report line
  now carries the player's balance at that point, not just the player name.
  An extension of Phase 3's journal-entry deliverable rather than new scope.
- **`card-payment-logging`** (done) — card effects that pay or collect from
  every other player (the two multi-player Chance/Community Chest cards) now
  produce a journal/log/report entry per payment. This was always implied by
  Phase 10's "record each card drawn and its resolved effect" deliverable;
  `Cards.java`'s `transfer()` helper simply never logged anything, so
  multi-player card effects were invisible in the journal even though the
  money moved correctly.
- **Real Chance/Community Chest decks** (done) — see Phase 10's status note
  above. `cards-15`/`cards-16` in `cards.feature` specify that landing on
  either space without a scripted override still draws a real card, completing
  Phase 10's original "shuffled ... placed at the bottom of the deck"
  deliverable, which was never actually implemented despite Phase 10 being
  closed out. `Cards.Decks.official()` now shuffles the real 16-card sets,
  deals without replacement, cycles when exhausted, and withholds a drawn
  Get-Out-of-Jail-Free card until `Deeds` reports it released — verified with
  `CardsDeckTest` rather than additional Gherkin, per the reasoning above.
- **Cash reserve and utility-monopoly awareness for "Greedo"**
  (done) — refines Phase 4's "'Greedo' buys whenever it can
  afford the price" deliverable rather than introducing a new strategy:
  buying now also declines a purchase that would leave the player below a
  configured cash reserve, and bidding at auction caps the bid the same way.
  Utilities are an exception — the strategy buys or bids up to its full
  balance regardless of the reserve whenever doing so completes its own
  utility monopoly or denies another player theirs; with neither player
  owning a utility yet, the reserve still applies normally. See
  `buying-land-3` through `buying-land-7` in `buying-land.feature`.
- **`bankruptcy-resolution-narration`** (specified, pending implementation) —
  refines Phase 14's "record bankruptcy events (debtor, creditor, cause)"
  deliverable: several individual actions `Bankruptcy.java` already performs
  correctly (verified by `bankruptcy.feature`'s `bankruptcy-1`/`2`/`3`/`5`/
  `6`/`7`) never produce a journal/log/report line of their own today, only
  the top-level "goes bankrupt to X"/"wins the game" lines. Covers: a forced
  house sale or mortgage while trying to stay solvent (already specified as
  `journal-26`/`27` and their `logging`/`report` equivalents, six known
  pre-existing failures carried since `nearest-station-rent-and-jail-
  narration`); the bank auctioning a bankrupt player's land off to another
  player; a creditor inheriting a bankrupt player's remaining land outright;
  and a creditor's automatic choice to immediately pay off an inherited
  mortgage versus keep it in place and pay only the mandatory 10% interest.
  See `journal-36` through `journal-39` (and `logging`/`report` equivalents)
  in `journal.feature`.
- **`distressed-sale-needs-based-pricing`** (done) — refines the distressed-
  sale mechanic above with three fixes bundled as one task, found by tracing
  a real $9 sale against an $80 mortgage value in a live game log:
  - **Needs-based pricing.** The minimum bid for a distressed-sale candidate
    is now `max(that land's mortgage value, remaining shortfall − mortgage
    value of the debtor's other still-available candidates)`, replacing an
    exemption that let a monopoly-completing bid ignore the land's
    mortgage-value floor entirely (a vendor would never rationally accept
    less than what the bank would pay to mortgage the same land).
  - **No whole-balance overpay.** A monopoly-completing or denial-motivated
    buyer now pays only the needs-based minimum, not their entire available
    balance — they still try to get the property as cheaply as the auction
    allows, same as any other bidder.
  - **Auto-credit heuristic removed.** `DistressedSale.settle()` used to
    credit a debtor with another property's mortgage value without actually
    mortgaging it, whenever the winning bid covered at least half the
    shortfall; needs-based pricing makes this redundant, so it was removed —
    any remaining shortfall is now covered by a real mortgage of the
    debtor's other land, exactly as narrated.

  See `distressed-sale-2`, `-5`, `-8`, `-9`, `-12`, `-13`, `-15`, `-16`
  (retuned) and new scenarios `distressed-sale-19`, `-20`, `-21` in
  `distressed-sale.feature`.
- **`card-bank-payout-narration`** (done) — closes a sibling gap to
  `card-payment-logging` above: ten Chance/Community Chest cards pay the
  drawing player directly from the bank without going through any
  `Cards.Events` method, so — unlike every other cash-affecting mechanic —
  this movement of money never appeared in the journal, log, or report.
  Found from a real 3-player CLI run that never terminated (2.25M+ turns,
  balances into the tens of millions, zero bankruptcies): this silent,
  repeated income alone was enough to keep the real economy (rent, tax)
  from ever producing a bankruptcy. Added `Cards.Events.receivedBank`, a new
  `Journal.Entry.BankReceived`, and a `Report` case rendering `"<name>
  receives $<amount> from the bank"`, mirroring the existing
  `BankPaid`/`paidBank` shape. See `journal-45`, `logging-45`, `report-45`.
- **`turn-doubles-phantom-move`** (done) — refines Phase 2's doubles-roll-
  again rule and Phase 12/14's jail/bankruptcy handling: `Turn.take()`'s
  doubles loop granted another roll whenever the dice came up doubles,
  without checking whether the player's state changed while resolving the
  landing that roll just produced. Two symptoms of the same cause, found
  from real 2-player CLI runs: (1) a player who goes bankrupt on a doubles
  roll kept rolling, moving, and paying rent after the game had already
  declared a winner, leaving a permanently negative balance since
  `Bankruptcy.resolve()` no-ops for an already-bankrupt player; (2) a player
  who lands on "Go To Jail" on a doubles roll got an illegitimate extra roll
  and moved away from the jail cell in the same turn, instead of the turn
  ending immediately as it should. Fixed by stopping the loop once
  `deeds.isBankrupt(player)` or `jail.holds(player)` becomes true. See
  `bankruptcy-8` and `jail-9`.
- **`legal-entity-for-split-colour-groups`** (done) — resolves the 3+-way
  colour-group split impasse that `greedo-stalemate-trading` above cannot
  touch (a split across three or more owners can never be built or
  consolidated, so an 8-player game tends to stall forever). Adds an opt-in
  **legal entity** mechanism behind
  `--optional-greedo-legal-entity`; a company formed over a colour group
  owns, develops, and rents out all of its streets as one collective.
  Refines how the game ends impasses without removing the separate
  cash-threshold `Stalemate` terminal. See
  [`SIMULATOR.md`](SIMULATOR.md) and the `greedo-legal-entity.feature` /
  `greedo-share-sale.feature` specs.
  - **Formation** — once the whole board is owned and the just-completed
    round contained no ownership-consolidating action, the game
    automatically forms an entity over any eligible colour group split
    across exactly **three** owners, **before** `Stalemate.reached` fires.
    Refuses a highest-priority group, a two-owner split, a split whose
    streets are already fully developed (no real next improvement), and one
    whose shareholders cannot **collectively** fund the next improvement
    after each keeps its base reserve.
  - **Operation** — the entity holds its own bank account and acts per round
    in priority: build as far as affordable (soliciting an **ALL-OR-NOTHING**
    build loan from its shareholders, each capped at a personal
    affordability ceiling), then repay any shareholder loan
    (principal + 5% interest), then pay an equal dividend — the dividend
    only once the loan is fully repaid **and** every street has a hotel,
    the treasury is at/above threshold, and the last-capitalised shareholder
    has aged a year.
  - **Distressed shares & liquidation** — a shareholder about to go bankrupt
    may sell a legal-entity share to a fellow shareholder (bid up to a third
    of bank balance); when one shareholder remains they may liquidate the
    entity and keep its remaining treasury. See `greedo-share-sale.feature`.
- **`auto-formation-real-next-improvement`** (done) — closes a
  development-selection gap in the legal-entity auto-formation trigger:
  formation previously treated a fully-developed colour split as fundable
  (`standardBuildCost` summed a house cost for every street, including ones
  that already have a hotel), so it would form an entity that had no build
  plan and went straight to its dividend path (one run: 4 entities formed,
  0 houses, 0 loans, 566 dividends). Auto-formation now requires a **real
  next improvement** — at least one entity street not yet at a hotel — and
  `standardBuildCost(entity, deeds)` excludes hotel streets. Pinned by
  `entity-m10`.
- **`billionaire-strategy`** (done) — adds a second strategy, "Billionaire":
  reuses "Greedo"'s decision logic verbatim (buying, rent, building, jail
  fine, inherited mortgages, distressed-sale bidding — all unchanged) and
  changes only the opening capital, $57,700,000 instead of the standard
  $1,500, applied once before the game starts as a strategy-level
  replacement rather than an addition to the usual balance. Not a strategy
  with genuinely different decision logic per [`SIMULATOR.md`](SIMULATOR.md)'s
  out-of-scope list — exists to let the CLI simulate a cash-dominant player
  without inventing new decisions. See `billionaire.feature`.
- **`cli-year-limit`** (done) — adds an optional `--max-years=N` CLI flag
  that caps a simulation at N simulated years (player age, incremented on
  passing/landing Start or being sent to jail); the game stops as soon as
  any remaining player's age reaches N, recording a full survivor snapshot
  (final balance and age for everyone left) the same way a stalemate does.
  Omitted (the default) plays to the game's natural end, unchanged. Exists
  to let a user avoid waiting through a rare slow-converging or genuinely
  non-terminating game (see [`SIMULATOR.md`](SIMULATOR.md)'s Billionaire-mix
  and Greedo-vs-Greedo characteristics) without having to tell the two
  apart ahead of time. See `year-limit.feature` and the matching
  `journal-70`/`report-70`/`logging-70`/`cli-8`/`cli-9`/`cli-jar-6`
  scenarios.
- **`asset-rich-billionaire`** (done) — adds an opt-in
  `--optional-asset-rich-billionaire` flag that changes what the
  "Billionaire" strategy is granted at the start of the game: instead of
  the standard $57.7M cash-rich opening, it opens with the ordinary
  $1,500 balance but starts already owning the whole Orange and Red
  colour groups outright, unmortgaged and unimproved, building them up
  itself via ordinary Greedo logic rather than starting with free
  hotels. Off by default, leaving the existing cash-rich Billionaire
  behavior unchanged. Portfolio choice (Orange + Red specifically) was
  derived from the same per-property valuation `Stalemate.threshold()`
  already uses — these are the game's own `Strategy.HIGHEST`-priority
  colour groups, together worth ~25.9% of the board by that measure,
  echoing the real-world statistic that the top 1% of Belgians hold
  roughly a quarter of all assets. See
  `asset-rich-billionaire.feature`.
- **`turn-development`** (done) — fixes a long-standing, game-wide defect
  found while investigating why the asset-rich billionaire above wasn't
  developing its granted land during real play: `Game.playTurn` only
  ever called `Building.develop()` for the single player who won the
  initiative roll at the start of the game, for the entire game,
  regardless of strategy — every other player, no matter what they
  could afford, never developed at all. Batch evidence: a 50-game
  asset-rich-billionaire run showed it building during real play in only
  6/50 games (12%) before the fix, vs. 50/50 (100%) after. Fixed so
  every solvent player may develop on their own turn unconditionally,
  including when legal-entity trading is enabled — the market-deadlock/
  quiet-round detector's `roundHadConsolidatingAction` signal was
  reworked to drop board-wide individual development entirely (it can
  never target a still-split group in the first place, since developing
  requires owning the whole group) while keeping trade/buyout/bankruptcy
  as real per-split signals. Also fixed a systemic test-fixture bug this
  surfaced along the way: the shared `World.formEntity` test helper
  defaulted "everything else unowned" to one of the entity's own
  shareholders, which the fix's now-routine development turned into
  unintended monopoly-building and bidding side effects across a dozen
  pre-existing scenarios once that shareholder could legitimately spend
  its own cash. See `turn-development.feature` and the corrected
  fixtures across `greedo-legal-entity.feature`, `journal.feature`,
  `report.feature`, `logging.feature`, and `greedo-share-sale.feature`.
- **`development-loans`** (done) — motivated by
  wanting the asset-rich billionaire above to win over a *longer*, richer
  game rather than making it artificially harder to win: lets cash-short
  opponents borrow to develop instead of just stalling out, without letting
  the bank conjure money from nothing. Adds two opt-in flags:
  `--optional-development-loans` (a player or legal entity short on cash for
  a development may borrow from the bank instead of simply being unable to
  build) and `--optional-development-loans-full-draw` (only meaningful when
  the first is on; changes the amount borrowed, see below). Off by default;
  existing behavior is unchanged either way.
  - **Collateral and sizing.** The loan is secured only by the street and
    houses/hotel it finances — nothing else the borrower owns is at risk.
    The bank lends at most 80% of the construction cost being financed (an
    80% loan-to-value cap, no exceptions). By default it lends only the
    shortfall between the borrower's cash and the development's cost; with
    `--optional-development-loans-full-draw`, it always lends the full 80%
    cap regardless of the actual shortfall. If the shortfall itself exceeds
    the 80% cap, no loan is raised and the development still cannot happen
    — borrowing to the cap wouldn't be enough either way.
  - **Funding (no money-printing).** The bank does not create this money out
    of nothing: every development loan is funded, dollar for dollar, by
    another player or legal entity buying a matching bank bond, issued
    reactively (one bond per loan, sized to it, at the moment the loan is
    raised — no standing bank reserve). The bond pays its buyer 3% annual
    yield versus the borrower's 5%, amortizing on the same schedule as the
    loan (the bank keeps the 2-point spread). Without a buyer able to fund
    it, no loan is raised and the development doesn't happen, flag or no
    flag.
  - **Repayment.** Amortized over 20 years at 5% annual interest: equal
    annual principal instalments, plus interest on the balance still
    outstanding that year (so the payment shrinks over time as the balance
    does). If a borrower can't cover an annual payment in cash, they try to
    raise it the same way they already would for any other debt (mortgage a
    spare property, sell to a peer, etc. — the existing distressed-sale
    cascade) before the loan is treated as defaulted.
  - **Default.** If nothing can be raised, the bank forecloses — but only on
    the street and improvements that secured that specific loan. Nothing
    else the borrower owns is touched, and a foreclosure does not by itself
    bankrupt the borrower. Foreclosure liquidates the whole collateral
    package the same way any other forced sale already works in this game:
    houses/hotel sold back to the bank at half price, then the bare land
    auctioned to another player (mirroring `bankruptcy-2`'s existing
    debt-to-the-bank pattern). Once the outstanding balance is covered, any
    surplus goes back to the borrower, the same way a real-world foreclosure
    sale works.
  - **Eligibility.** Both individual players and legal entities can borrow;
    an entity's bank development loan is a distinct mechanism from its
    existing internal shareholder "build loan" (`Pink Realty raises a loan
    of $X`, 5% interest, blocks dividends until repaid) — the two use
    deliberately different vocabulary (`raises a development loan of $X
    secured by "<street>"`) to avoid confusion between an entity borrowing
    from its own shareholders versus borrowing from the bank. Three
    behaviors differ from the individual-player case, deliberately: (1) the
    existing "no dividend while a shareholder loan is outstanding" rule
    extends to cover an outstanding bank development loan too, so the
    entity can't distribute cash while still servicing 20-year bank debt;
    (2) an entity's missed-payment fallback is "mortgage another street in
    the same group that isn't this loan's collateral," not the build-loan
    shareholder-commitment mechanism, which stays scoped to funding new
    development only; (3) foreclosure on one of an entity's streets needs
    no special-case handling — the entity keeps existing and collecting
    rent on what's left, just loses build rights on that colour group until
    (if ever) it owns the complete set again, same as any other partial
    owner, with no interaction with the existing "last shareholder
    liquidates the entity" mechanic (that's keyed off shareholder count,
    not property count).
  - **Observability.** Four events had no journal/report/logging coverage
    at all: a loan being raised, its annual interest/principal payment, it
    being fully repaid, and a default/foreclosure. Each is specified for
    both an individual player and a legal entity (so a batch-run analysis
    can tell which kind of borrower is involved straight from the log,
    same as every other event in the game), following the project's
    existing pattern of one identically-worded scenario per event across
    `journal.feature` (`"the game journal records that ..."`),
    `report.feature` (`"the game report says that ..."`), and
    `logging.feature` (`"the game log records that ..."`) —
    `journal-71`/`report-71`/`logging-71` through `-78`. The missed-payment
    mortgage fallback and the mechanical steps of a foreclosure (house sold
    at half price, land auctioned) reuse the mortgage/sale/auction
    narration that already exists for other debts; only the loan-specific
    events above needed new coverage.
  - **Overfitting audit.** Every individual-player scenario had the same
    borrower (`dog`, who never wins initiative) and every scenario in both
    files used the same single collateral street — exactly the shape of
    two bugs this project has already hit once (`turn-development`,
    logged above; a similar dividend/initiative-order bug from an earlier
    session). `development-loans-12` proves the loan mechanism works for
    the initiative *winner* too (`high hat` borrows against `Meir
    Antwerpen`/`Nieuwstraat Brussel`, `dog` is the bondholder); `entity-48`
    proves it isn't tied to `Rue de Diekirch Arlon` specifically
    (`Bruul Mechelen` instead). A second issue found in the same pass is
    structural, not fixable by choosing different example numbers: because
    the interest rate (5%) equals 1/20 (the loan's own term), first-year
    interest and first-year principal payment are mathematically identical
    for *any* principal — `development-loans-6`/`entity-44` and their
    journal/report/logging twins can't actually distinguish a coder
    swapping "pays $X interest" for "pays $Y principal" from a correct
    implementation. Fixing that needs a design decision (how to express a
    loan partway through its term, since `"owes the bank $X"` only
    captures the current balance) — resolved by extending that same Given
    with an optional trailing clause, `"... secured by "<street>", N years
    into its 20-year term"`, defaulting to the start of the term when
    omitted (so every existing scenario is unaffected). `development-
    loans-13`/`entity-49` use it to arrange a loan one year in (original
    principal $400, chosen so both 5% and the fixed $20/year principal
    payment stay whole dollars for the entire 20-year schedule, not just
    year one) and assert the second payment: $19 interest vs. $20
    principal — genuinely different numbers, so a coder swapping which
    variable holds which value would now fail. No new journal/report/
    logging scenarios were needed for this: `journal-73`/`74` already
    prove the narration format is correct for whatever numbers the domain
    produces; `development-loans-13`/`entity-49` are what prove the domain
    produces the *right* numbers. Those are separate concerns and the
    existing narration coverage doesn't need duplicating just because the
    domain-level example changed.
  - **Decimal money, game-wide.** This feature is what forced the
    decision: `Money` gains 2-decimal-place (cent) precision everywhere in
    the game, not just here, since 5%/3% rarely divide evenly. Display
    hides trailing zero cents ($140.00 shows as $140), and any calculation
    landing on a fractional cent resolves via banker's rounding (round
    half to even). Because of the display rule, none of the ~60
    already-written scenarios needed to change — every existing example is
    a whole-dollar amount, so it renders identically whether `Money` is
    integer or decimal internally. Only `development-loans-13`/`entity-49`
    were revised, from principal $400 to **$400.10**: $400.10 ÷ 20 is
    exactly 2000.5 cents, a genuine half-cent tie that banker's rounding
    resolves to $20.00 (the even cent) rather than the $20.01 ordinary
    rounding would give; the resulting $380.10 remaining balance produces
    a second exact tie (1900.5 cents) for year two's interest, again
    resolving to $19.00. Same final numbers as before, but now only
    reachable by correctly implementing banker's rounding at two separate
    ties, not just clean division.
  - **Bank ledger and capital recycling on default**, superseding the
    "bondholder cashed out at par + one payment's interest" design
    immediately above. The bond is a claim on the *bank*, not on one
    specific loan — the bondholder doesn't know or need to know which loan
    their money backs, trusting the bank to manage that risk, the same way
    a real depositor doesn't track which specific mortgage their deposit
    funds. That reframing fixed a real problem with the previous design:
    paying a defaulted bond's full 20-year remaining value out immediately
    would make bonds risk-free (undermining why they yield less than the
    loan) and could easily exceed what a single street's collateral is
    worth. The new mechanics:
    - **The bank's own account** — a new tracked ledger, accumulating the
      2-point spread from every scheduled payment across every loan
      (`development-loans-14`/`entity-50`). It exists specifically as a
      loss-absorption buffer: covers the difference when a foreclosure's
      proceeds fall short of what a defaulted loan's collateral should
      have recovered (`development-loans-15`, individual only — see below).
    - **Recovery target, revised.** On default the bank now recovers the
      *full outstanding loan value* — principal plus the borrower's 5%
      interest for the missed payment, i.e. what the borrower would have
      owed to stay current — not just the bondholder's narrower 3%-based
      claim. `development-loans-11`/`entity-47` were re-derived: the
      bondholder's cash balance is asserted *unchanged* (no payout), the
      bank's account absorbs the recovered value ($21/$42), and only the
      genuine excess reaches the borrower ($34/$33, both smaller than the
      superseded $34.40/$33.80 since the recovery target grew from the
      bond's 3% to the loan's 5%). `journal-81`/`82` (and `report`/
      `logging` twins) rewritten to narrate the bank's recovery instead of
      a bondholder payout.
    - **Recycling.** Once a defaulted loan's value is fully recovered
      (collateral proceeds, topped up from the bank's account if short),
      that capital doesn't sit idle or get paid out — it funds the next
      loan that needs one, the bondholder's investment continuing
      uninterrupted rather than being cashed out. `development-loans-16`
      arranges this directly via a new Given (`"the bank holds $X in
      recycled development-loan capital, no longer securing any loan"`)
      rather than chaining two full loan lifecycles in one scenario.
    - **Bond top-up.** When recycled capital only partly covers a new
      loan's shortfall, a freshly-issued bond covers the rest, the same
      as an ordinary loan with no recycled capital behind it at all
      (`development-loans-17`).
    - **Scoped to individual players for this pass**: `development-loans-
      15`/`16`/`17` (bank-buffer shortfall coverage, recycled-capital
      funding, bond top-up) were not mirrored for legal entities, nor
      given journal/report/logging coverage — unlike everything else in
      this feature. This is a deliberate, explicit scope cut given how
      large this feature had already grown, not an oversight; entity
      mirrors and narration for these three behaviors are the natural next
      slice if this needs a second pass.
  - **The bondholder's own side.** Every scenario above proved a loan
    requires a willing bondholder to exist, but none of them proved the
    bondholder is actually paid — an easy gap to leave in place, since
    "no bondholder, no loan" already exercises the bond as a gate. Two
    more behaviors close it: the bondholder receives their annual payout
    (3% yield plus the same principal instalment the borrower just paid,
    the bank keeping only the 2-point spread on interest, never on
    principal — `development-loans-10`, `entity-46`), and on a default the
    bondholder is made whole out of the foreclosure proceeds *before* any
    surplus reaches the borrower, protected by the same 80% loan-to-value
    cushion that protects the loan itself (`development-loans-11`,
    `entity-47`, the latter introducing a 5th player, `ship`, as bondholder
    so the test doesn't conflate "the bondholder" with "the auction
    winner"). Mirrored into `journal-79` through `82` (and the matching
    `report`/`logging`) the same way as everything else.
  - **CLI wiring, game-wide rather than strategy-attached.** Unlike
    `--optional-greedo-legal-entity`/`--optional-greedo-stalemate-trading`
    (genuinely strategy-specific: they toggle a *decision an algorithm
    makes*, which a differently-coded future strategy could answer
    differently) or `--optional-asset-rich-billionaire` (specific to
    Billionaire's unique starting-capital substitute), development loans
    are a *bank rule* — any strategy facing "I want to develop but can't
    afford it" would use it identically, the same way `--max-years` is a
    game-wide ending condition tied to no one strategy. `cli-11`/`cli-12`
    (in `cli.feature`) assert this game-wide, mirroring `journal-51`'s
    existing `"the game journal records that stalemate trading is
    <state>"` pattern rather than `cli-6`/`cli-10`'s `"strategy observes
    X"` framing; `cli-13` proves it explicitly by running a mixed
    Greedo+Billionaire game and confirming the flag applies the same way
    regardless. The matching domain-level fact (`journal-83`/`84`, plus
    `report`/`logging` twins) fills the same gap `journal-51`/`52` already
    fill for stalemate trading, since `cli-11`/`12` needed it to exist as
    a real, independently-tested step rather than only appearing at the
    CLI layer. `cli-jar-8`/`cli-jar-9` (in `cli-packaged-jar.feature`) were
    already phrased game-wide from the start, so needed no change; `cli-
    jar-5` still asserts the README usage report names both flags.
    README's own usage text isn't updated yet — that's implementation-time
    doc sync, same as every other flag — so `cli-jar-5` will stay red for
    those two lines until the coder (or a later doc-sync pass) adds them.
  - Specified in `development-loans.feature` (11 individual-player
    scenarios: flag on/off baseline, shortfall-vs-full-draw sizing, the 80%
    cap actually blocking development, no-bondholder-available, first-year
    interest/principal breakdown, final-payment payoff, mortgage-first-on-
    missed-payment, default/foreclosure scoped to just the collateralized
    street, and the two bondholder-side scenarios above) plus `entity-36`
    through `entity-47` in `greedo-legal-entity.feature` — a deliberate 1:1
    mirror of every one of those behaviors for a legal-entity borrower (`entity-36`/`41`
    shortfall-under-cap, `entity-40` flag-off baseline, `entity-41` cap
    blocks, `entity-42` full-draw, `entity-43` no-bondholder, `entity-44`
    interest/principal breakdown, `entity-45` final payoff, `entity-38`
    mortgage-fallback, `entity-39` default/foreclosure), plus `entity-37`
    for the dividend-block extension that has no individual-player
    equivalent. Mirrored deliberately rather than relying on "the
    implementation will obviously share code": this project has already
    split into separate, diverging branches before when a behavior wasn't
    spec'd identically for two contexts, so the spec itself is the
    guardrail against a coder agent implementing the entity path
    differently from the individual-player path. The bondholder's own
    cash-flow (interest spread, principal pass-through) is asserted
    directly too, not just that a loan requires a willing bondholder to
    exist — see "The bondholder's own side" below.
  - **Possible gap: no strategy actually prefers buying bonds.** Not yet
    designed. Development loans only work at all if some player or entity
    chooses to buy the bond funding one — right now nothing in either
    strategy's decision logic expresses a preference for doing so.
    "Billionaire" should plausibly be motivated to prefer buying bonds
    over buying/developing property, since capital deployment (not
    day-to-day play) is its whole differentiator. "Greedo" would need a
    separate, narrower rule — buying a bond only if funds allow, i.e.
    after its normal purchase/build/reserve priorities are satisfied —
    but the exact condition (how much spare cash, where it ranks against
    buying land or building) still needs to be worked out with the user.
    Flagged 2026-08-18; not yet spec'd.
- **`war-profits-tax`** (done) — adds an opt-in `--optional-war-profits-tax`
  flag taxing rental income once a player's land holdings grow large
  enough to look like wartime profiteering rather than ordinary success.
  Ownership share is measured by the *current* rent value of a player's
  land as a fraction of the whole board's value at full development (the
  same board-value figure `Stalemate.threshold()` already uses). Each
  player accumulates the rent they collect from others, and once a year —
  the same "grows a year older" trigger development-loan payments already
  use — that accumulated rent is taxed at a rate set by the player's
  *current* ownership share at that moment, then the counter resets to
  zero: 0% below 25%, then 100/150/200/300/400% in five bands up to full
  ownership (each band's lower bound inclusive, so a share exactly at a
  boundary belongs to the higher band). A rate above 100% means the
  player owes more than they collected that year, out of pocket. All tax
  collected is paid into a new government account — the same account
  `rent-relief` below spends from and `megacorp-salary-tax` below also
  feeds. See `war-profits-tax.feature`.
- **`megacorp-salary-tax`** (done) — adds a behavior bound to the same
  `--optional-rent-relief` flag as `rent-relief` below (not a separate
  flag of its own): every player's salary is now paid by a notional
  employer, MegaCorp, rather than the bank directly. The salary a player
  actually collects (the ordinary $200, or the $400 the optional
  double-salary-on-landing rule pays) is the *net* amount after a 43%
  individual income tax on the *gross*: net is 57% of gross, so MegaCorp
  pays the government `tax = net / 0.57 - net`, keeping the player's own
  take unchanged at the net figure they always collected. This grows the
  same government account `war-profits-tax` feeds and `rent-relief`
  spends from — a steady, per-player, per-lap inflow, in contrast to
  war profits tax's lumpy, occasional-but-heavy one. See
  `megacorp-salary-tax.feature`.
- **`rent-relief`** (done) — adds an opt-in `--optional-rent-relief` flag
  (which also activates `megacorp-salary-tax` above) that caps what a
  tenant pays in rent at $200 — the same amount as the ordinary salary
  for passing Start — the moment the government's account can cover the
  rest of the bill in full; the government pays the *landlord* that
  difference directly, so the landlord always receives the full nominal
  rent either way and only the tenant's payment is ever reduced. If the
  government's account cannot cover the full difference, no relief is
  given at all — not a partial reduction — and the tenant pays the full
  rent, exactly as without this flag. $200 was chosen from the real
  distribution of rent payments across this project's characterization
  baselines: only 3.5% of payments exceed it, but that tail carries
  roughly 30% of all rent dollars, so it is the threshold that catches
  the hotel-tier spikes capable of ending a game for a player still
  building up cash. See `rent-relief.feature`.
  - Measured effects across configs 10-14 (survival rate, effective tax
    burden and net fiscal position by pawn, relief received vs. starved
    and the age at which each happens) are tracked as permanent, checked
    baselines rather than one-off prose here — see
    [`CHARACTERIZATION-TESTS.md`](CHARACTERIZATION-TESTS.md) and the
    "Simulated game characteristics" section of [`README.md`](README.md),
    kept in sync with the fixtures by `ReadmeSyncTest`. Headline finding:
    relief funded by MegaCorp's tax alone (config 11, no war-profits-tax)
    already fails more often than it succeeds by dollar volume, and barely
    moves survival rate versus having no relief at all (config 14, the
    matched pair); adding war-profits-tax (config 12) doesn't make the
    funding gap widen gradually so much as switch regimes — fragile before
    a player's first big war-tax payment refills the government, abundant
    after.
- **`entity-dev-loan-dissolution-desync`** (done) — fixes a real crash
  found while scoping new rent-relief characterization configs (not a
  regression from this session's own changes; a pre-existing defect never
  exercised until legal-entity trading, development loans, and rent
  relief all combined for the first time). `Bankruptcy.liquidateEntity`
  (reached when a distressed player is an entity's sole shareholder)
  transferred every one of the entity's streets to that player
  unconditionally via `Deeds.dissolve`, without checking
  `DevelopmentLoanBook` for an outstanding loan against any of them; the
  orphaned position later crashed (`Deeds.verifyEntityOwner`,
  "does not own") the next time it came up for servicing, against land a
  player now owned individually. Fixed by settling every outstanding
  entity loan position — from treasury, or foreclosure if that falls
  short — *before* `deeds.dissolve` runs, rather than letting the debt
  silently follow the collateral to the new owner. See
  `greedo-share-sale.feature`'s `share-sale-24`/`25`.
- **`loan-foreclosure-null-winner-desync`** (done) — fixes a second,
  unrelated crash found in the same scoping pass, in the *individual*
  loan-foreclosure path this time: `DevelopmentLoanBook.foreclose` (and
  its `forecloseEntity` mirror) auction a defaulted loan's collateral via
  `Auction.qualified(..., floorOpening=false)`, which admits any bidder
  with a positive ceiling even one below the land's mortgage value (the
  auction's opening price). If every admitted bidder's ceiling fell short
  of that opening price, `Auction.ascendMany` correctly returned a null
  winner, but both foreclosure call sites unconditionally passed that
  winner to `Deeds.sell`, crashing the moment two or more cash-poor
  bidders were the only ones left. Fixed by falling back to
  `Deeds.returnToBank` on a null winner, identical to the existing
  zero-bidders outcome. See `development-loans.feature`'s
  `development-loans-18`.
