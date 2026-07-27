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
  landing player's `Strategy`. "Agree if affordable" buys whenever it can
  afford the price, and (when bidding) bids up to the most it can afford.
- **Journal:** record purchases and auction outcomes (winner, price) so the
  report can narrate them.
- Spec: buy on landing, decline → auction → sale to highest bidder, decliner
  may still win the auction, plus "Agree if affordable" buys when it can
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
  "Agree if affordable" builds evenly across a monopoly whenever it can afford
  to, and only sells houses/hotels back when it needs the cash to cover a debt.
- **Journal:** record house/hotel purchases and sales.
- Spec: even-build enforcement (both directions), hotel construction/cost,
  monopoly-with-partial-build still allows double rent on unimproved streets in
  that group (ties back into Phase 5), bank shortage/auction behaviour, plus
  "Agree if affordable" building opportunistically when funds allow.

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
  (once affordable) are delegated to `Strategy`. "Agree if affordable"
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
  "Agree if affordable" accepts any trade it can afford; no strategy proposes
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
  (Get-Out-of-Jail-Free cards return to the bottom of their deck).
- Debt to another player: houses/hotels sold to the bank at half price first,
  then the creditor receives remaining money, deeds, and cards (mortgaged
  property transferred per Phase 8's mortgage-transfer rules, with immediate
  10% interest due).
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
- Accepts the number of players (2–8, per [Setup](RULES.md#setup)) and, per
  player, a strategy selection — defaulting to "Agree if affordable" when not
  specified.
- Runs one game via the domain module's `Game`, then prints the rendered game
  report (Phase 3's renderer) to the console.
- Since "Agree if affordable" is the only strategy for now, strategy selection
  is effectively a pass-through, but the plumbing (a strategy registry/lookup
  by name) should already accommodate more strategies being added later
  without changing the CLI's shape (per [`SIMULATOR.md`](SIMULATOR.md)'s
  out-of-scope list).
- Verification: as a console entry point rather than a domain rule, this is
  better suited to a thin manual/integration check than a Gherkin feature —
  run the CLI end-to-end for a couple of player counts and confirm a complete,
  readable report is produced.

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
