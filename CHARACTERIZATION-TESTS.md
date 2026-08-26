# Characterization tests

A JUnit-integrated test suite that runs the real CLI simulator across a
fixed set of game setups, extracts a statistical breakdown from each run's
logs, and compares it against a checked-in baseline — to catch regressions
in how the game actually plays, not just whether individual rules pass in
isolation. This complements the Gherkin acceptance suite (which proves each
rule behaves correctly in a specific, engineered scenario) by proving the
*whole system*, played the way a real game plays it, still behaves the same
way it did before a change.

Run only deliberately, not as part of the default `mvn test`:

```sh
mvn test -P characterization-tests
```

matching how `-P property-tests` already works. The natural trigger point
is whenever the architect accepts a feature-complete report — not every
commit — since it exercises full games rather than isolated rules.

## Prerequisite: deterministic play

A characterization run must be byte-for-byte reproducible, so a diff
against the baseline is unambiguously a real behavior change, not luck of
the dice. That requires every source of randomness in a game to accept an
injected seed. Today, neither does:

- `Dice.java` uses a bare `ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new)`
  — never seeded.
- `Cards.java`'s deck shuffling calls `Collections.shuffle(shuffled)` with no
  seed.

Both need to accept an injected, deterministic source of randomness before
this suite can be built. How that's threaded through (a seed parameter on
the CLI/`Game` construction path, a shared `Random` passed down, etc.) is
an implementation decision; the requirement is only that two runs with the
same seed and the same code produce an identical game, and that the CLI
simulator exposes a way to supply that seed (a new flag, e.g.
`--seed=N`, following the existing `--max-years=N` convention).

Determinism is *not* meant to make the game predictable in general — real
games still use unseeded randomness by default. It only applies when a
seed is explicitly supplied, which characterization runs always do.

Each config below runs across a handful of different fixed seeds (not just
one), so the suite still exercises more than one dice/card path per setup
without losing reproducibility. 50 seeds per config is what the suite
actually runs today.

## Execution

- Runs are driven through the real CLI entry point (`Simulator`), the same
  way a person running `java -jar ... ` would — not by calling `Game`
  internals directly — so this suite exercises the same code path a user
  actually experiences.
- Games run in parallel (multiple processes or threads — implementation's
  choice) to keep wall-clock time reasonable; 10 configs × 50 seeds is 500
  full games.
- Each game's complete log is written to a dedicated directory under the
  owning module's `target/`, grouped by config, e.g.
  `target/characterization-logs/<config-name>/seed-<N>.log` — so a
  deviation can be investigated by reading exactly the run that produced
  it. This output is build-generated (not committed), matching how
  anything else under `target/` already works.

## Game setups

Ten configs, each additionally run with `--max-years=2500` as a safety
cap against a run that never naturally terminates:

| # | Players | Strategies | Optional flags |
|---|---------|-----------|-----------------|
| 1 | 2 | Greedo | none |
| 2 | 3 | Greedo | none |
| 3 | 3 | Greedo | `--optional-greedo-stalemate-trading` |
| 4 | 8 | Greedo | none |
| 5 | 8 | Greedo | `--optional-greedo-stalemate-trading` |
| 6 | 8 | Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` |
| 7 | 8 | 1 Billionaire (cash-rich, default) + 7 Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` |
| 8 | 8 | 1 Billionaire (asset-rich) + 7 Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-asset-rich-billionaire` |
| 9 | 8 | 1 Billionaire (asset-rich) + 7 Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-asset-rich-billionaire` `--optional-development-loans` |
| 10 | 8 | 1 Billionaire (asset-rich) + 7 Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-asset-rich-billionaire` `--optional-war-profits-tax` |
| 11 | 8 | Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-development-loans` `--optional-rent-relief` |
| 12 | 8 | Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-development-loans` `--optional-rent-relief` `--optional-war-profits-tax` |
| 13 | 8 | 1 Billionaire (asset-rich) + 7 Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-development-loans` `--optional-rent-relief` `--optional-war-profits-tax` `--optional-asset-rich-billionaire` |
| 14 | 8 | Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-development-loans` |
| 15 | 8 | Greedo | `--optional-greedo-stalemate-trading` `--optional-greedo-legal-entity` `--optional-development-loans` `--optional-rent-relief` `--optional-unified-income-tax` |

Configs 7 and 8 are identical except for the one flag that matters
(`--optional-asset-rich-billionaire`), so a comparison between their two
breakdowns isolates that flag's effect specifically. Configs 8 and 9 are
likewise identical except for `--optional-development-loans`, isolating
that flag's effect specifically for the asset-rich billionaire — the two
prior configs (formerly `eight_billionaire_greedo_loans` and
`eight_billionaire_greedo_loans_asset_rich`) both had development loans on
unconditionally, which never let the flag's own effect be isolated from
the billionaire's opening mode. Renaming both to drop the now-inaccurate
`_loans` (they no longer have loans on) is expected as part of this
change; config 9 reuses the identifier the asset-rich config previously
had, since it now holds what that config used to represent.

Config 10 is likewise config 8 plus only `--optional-war-profits-tax`,
isolating that flag's effect the same way — orthogonal to config 9's
development-loans axis, both branching from config 8 rather than from
each other. It exists because an ad-hoc 50-seed run of this exact setup
found a large effect worth permanently tracking: the asset-rich
billionaire's win rate collapsed from 50/50 to 5/50, ordinary wins fell
from 100% to 74% (the rest stalemating), and mean age at end rose from
~4 to ~82 years.

Configs 11 through 13 form their own isolation chain, all-Greedo apart
from config 13, all with `--optional-rent-relief` on throughout — every
config found while adding this chain was reproducible with rent relief
active, so it stays on rather than being isolated as its own axis here.
Config 12 is config 11 plus only `--optional-war-profits-tax`, isolating
that flag's effect on an all-Greedo table the same way config 10 isolates
it for a billionaire. Config 13 is config 12 with one Greedo seat replaced
by an asset-rich Billionaire, isolating that swap specifically. The
checked-in baselines show war-profits-tax has a much larger effect here
than the billionaire does: config 11 resolves in an ordinary win 48/50
times (4% stalemate), mean age at end 28.06 years; adding war-profits-tax
in config 12 flips that balance hard toward stalemate (34%, 17/50) and
more than a five-fold increase in game length (mean age 152.28 years,
max 1730) — the same runaway dynamic Greedo-vs-Greedo games are already
known to exhibit, now showing up with a full 8-player table once
war-profits-tax and rent relief are both recirculating money. Swapping in
the asset-rich Billionaire for config 13 moves the outcome split only
slightly (40% stalemate, 30/50 wins) and actually *shortens* the average
game (mean age 132.66 years) — a far milder, and differently-shaped,
effect than the billionaire's win-rate collapse in config 10, where rent
relief is absent; with relief active, the Billionaire's usual advantage
from an early land-heavy opening appears substantially diluted by money
flowing back to every player rather than concentrating with land
ownership. These
three configs were also where an entity-dissolution/development-loan
crash (`entity-dev-loan-dissolution-desync`) and a defaulted-loan-auction
null-winner crash (`loan-foreclosure-null-winner-desync`) first surfaced
and were fixed — both bugs pre-dated this chain but had gone unexercised
by every existing config until legal-entity trading, development loans,
and a foreclosure with cash-poor remaining bidders all coincided here.

Config 14 is config 11 with only `--optional-rent-relief` removed — the
one true matched pair in the whole suite, added specifically to isolate
relief's own effect on how many players survive to the end of a game,
something none of configs 11-13 could answer alone since every one of
them has relief on. Survival rate here means the fraction of all player
seats across every seed that are never bankrupted (`1 - total
bankruptcies / (8 x 50)`), derivable from the bankruptcy count both
configs already track. Config 14 resolves in an ordinary win in all 50
seeds, with exactly 7 bankruptcies every single time (350 total,
`350 = 7 x 50`) - a 12.5% survival rate (50 of 400 seats), no exceptions.
Config 11 barely moves that: 348 bankruptcies against 400 seats, a 13.0%
survival rate - only 0.5 percentage points higher, from the 2 seeds (of
50) where a second player survives alongside the winner. Relief alone,
without war profits tax clawing back concentrated land wealth, keeps
tenants solvent turn to turn but does essentially nothing to how the game
*ends* - confirming, now precisely isolated rather than inferred, that
the dramatic stalemate/survival shift documented for config 12 is a
war-profits-tax effect (or the interaction of the two), not something
relief produces on its own.

## The breakdown

Computed once per game from its log, then aggregated across the config's
seeds into the value actually compared against the baseline. Two parts:

**Generic core** — present for every config, regardless of flags. Everything
here is a mechanic that's always potentially active, not gated behind an
optional flag, so tracking it only for some configs would leave the others
blind to a regression in it:
- Outcome distribution: how each of the config's runs ended (ordinary win,
  stalemate, year-limit reached), and, for ordinary wins, which pawn won
  how many times.
- Age at game end: min, max, mean, median across the config's runs, plus the same `Stats` block split by termination outcome (`ageAtEndByOutcome`) so bankruptcy-driven endings can be compared separately from deliberate stalemates and year-limit endings.
- Bankruptcy count, broken down by who received the assets (a specific
  pawn, or "the bank"). Today only the eventual winner is visible in the
  breakdown; a regression that changes how or when a *non*-winning player
  goes bankrupt is otherwise invisible even though the winner and age
  stats can look identical.
- Auctions: count, and direct-purchase count alongside it (so the ratio
  between the two is derivable) — a core board mechanic, not currently
  tracked at all.
- Mortgage count.
- Income composition, summed across all players: salary collected, rent
  collected, direct bank payments (Chance/Community Chest), each as a
  total $.
- Income by player: salary collected and rent collected (as landlord),
  each broken down by pawn, alongside the aggregate totals above — a
  regression that shifts income between specific players without moving
  the total would otherwise be invisible. This is also the income side
  needed to compute effective tax burden per player (see the
  `--optional-rent-relief` extra and the README sync check below);
  tracking it here in the generic core, rather than gating it behind a
  tax-related flag, means burden is always computable for any config that
  goes on to add a tax extra, without a separate income-tracking flag of
  its own.

**Extras** — present only for a config where the relevant flag is active,
absent (not zeroed) otherwise, so a baseline never carries meaningless
always-empty fields:
- `--optional-development-loans`: loans raised (count, total $), borrower
  breakdown by pawn, bondholder breakdown by pawn, defaults count — plus
  loan *servicing*: total interest paid and total principal paid by
  borrowers, and, on the bondholder side, total interest and principal
  actually received. Today only origination is tracked; a regression that
  broke the repayment schedule or interest calculation entirely wouldn't
  move any currently-tracked field.
- `--optional-greedo-legal-entity`: entities formed count, entities
  dissolved count.
- `--optional-greedo-stalemate-trading`: peer trades executed count.
- `--optional-rent-relief`: relief payments (count, total $), games with at
  least one relief payment. Relief is funded by whatever tax revenue is
  active, war profits tax and/or MegaCorp's individual income tax on
  salary (`--optional-rent-relief` binds both the relief mechanism and
  MegaCorp's salary tax to the same flag, so a config with relief on
  always has MegaCorp active too) — so this extra also tracks MegaCorp
  payments into the government account (count, total $), the other side
  of relief's funding besides war profits tax, plus that same total
  broken down by pawn (mirroring war profits tax's own payer breakdown
  below) — the tax side needed, alongside the generic core's per-player
  income, to compute effective tax burden per player.
  Also relief received, broken down by pawn — not the landlord named in
  the `RentReliefPaid` line, but the *tenant* whose payment it capped:
  `RentRelief.pay` always deposits the full nominal rent into the
  landlord's account regardless of relief (`landlord.account().deposit(rent)`,
  the whole amount, not `rent.minus(relief)`), so a landlord's income is
  identical whether relief exists or not — relief only ever reduces what
  the *tenant* pays. The tenant is recoverable from the `RentPaid` line
  `Journalling.paid` always logs immediately before a `RentReliefPaid`
  line in the same call, the same adjacent-line pattern already used to
  attribute MegaCorp tax to "whoever last collected salary." Scoped to
  player-owned landlords only, same as the existing relief-payments
  count above: the legal-entity rent-payment path caps the tenant's
  payment identically but logs no distinguishable relief line at all, so
  entity-landlord relief is invisible to log-parsing entirely, not just
  to this new field — a pre-existing gap in observability, not something
  newly introduced by attributing the player-landlord case correctly.
  Also relief starved: count and total $ shortfall, plus games with at
  least one starved event, broken down by pawn (the tenant left exposed)
  the same way relief received is. `RentRelief.reliefFor` is a hard
  cliff, not a graceful reduction: `government.balance().covers(difference)
  ? difference : Money.ZERO` — if the government's *current* balance
  can't cover a bill's entire excess over the $200 cap, relief doesn't
  partially apply, it doesn't apply at all, and the tenant pays the full
  nominal rent with no cushion. This is invisible in the existing relief
  fields (a starved payment just looks like an ordinary large rent
  payment) but detectable from the same two-line adjacency: any
  `RentPaid` line paying more than $200, in a config with relief active,
  that is *not* immediately followed by a `RentReliefPaid` line is a
  starved event — relief would have fired for that bill in a no-relief
  config too (the amount and mechanics are identical either way), so the
  distinguishing signal is genuinely just "relief is on for this config,
  and this specific over-cap bill got no relief line." The $ shortfall is
  `tenant paid - $200`, the exposure relief was supposed to prevent.
  Same player-owned-landlord scope as relief received, for the same
  reason (no distinguishable line exists for the entity path to check
  adjacency against in the first place).
  Both relief received and relief starved also carry an age-at-event
  `Stats` block (min/max/mean/median, the same shape `ageAtEnd` and the
  war-profits-tax government-balance stats already use) — needs no new
  production entry, since `Game.takeTurn` already logs a `TurnStarted`
  line with the mover's current age unconditionally, every turn, before
  that turn's landings (including rent and relief) resolve. Because rent
  is only ever paid as a result of the current mover's own landing, the
  tenant in any `RentPaid`/`RentReliefPaid` pair is always the same
  player whose `TurnStarted` line was the most recent one before it — the
  same "last line wins" attribution already used for MegaCorp tax
  ("whoever last collected salary"), just keyed on age instead of payer.
  The point of tracking both age distributions, not just one, is
  comparison: if starved events cluster at a meaningfully older mean age
  than received ones, that is direct, checkable evidence that the
  funding gap between a flat labour tax and inflating rent widens over
  the course of a game, rather than being a flat, constant risk
  throughout — the specific hypothesis this pair of fields exists to
  test, not just "when does starvation happen" in isolation.
- `--optional-war-profits-tax`: tax payments (count, total $), payer
  breakdown by pawn — plus final government-account balance as a
  min/max/mean/median `Stats` block, the same shape `ageAtEnd` already
  uses, computed across *all* the config's seeds including the ones
  where the tax was never triggered (balance $0). A flat total across
  seeds would bury how concentrated collection is in the handful of
  seeds where a player actually crosses 25% ownership; the distribution
  is the interesting part.
  Also survivors at first tax: the number of players still solvent
  (never yet the debtor in a "goes bankrupt to" line) at the moment the
  billionaire pawn's *first* war-profits tax payment is reported — the
  point where the billionaire, previously untouchable, becomes taxable
  like anyone else — as a min/max/mean/median `Stats` block. Seeds where
  the billionaire is never taxed at all have no such moment and are
  excluded from this Stats block (not zero-filled — the event didn't
  happen, unlike a balance that's meaningfully $0). This needs no new
  production entry: replay the existing "goes bankrupt to" and "pays a
  war profits tax of $" lines in order, stopping at the billionaire's
  first tax line. An ad-hoc 50-seed analysis of config 10 found the
  billionaire is taxed at least once in 48/50 seeds, with a mean of
  3.96 players (out of 8) still standing at that moment — i.e. the tax
  typically catches up with the billionaire only after roughly half the
  table is already gone, not early.
  Only meaningful for a config with a billionaire pawn; today that's
  config 10 alone (the only config combining a billionaire with
  war-profits-tax), but the field is defined generically as "the
  billionaire pawn's first tax payment" so it still makes sense if a
  future config pairs war-profits-tax with the cash-rich billionaire
  instead. A war-profits-tax config with no billionaire at all (e.g. an
  all-Greedo variant) would have no billionaire pawn to anchor this on
  and should omit the field entirely, not error.

**Effective tax burden by pawn** — not its own extra, since it has no flag
of its own and no dedicated JSON field: a README-only figure, derived at
render/sync-check time the same way the outcome percentages already are,
from the generic core's per-player income and whichever tax extras are
active. Salary must be converted from the *net* figure the generic core
tracks (what a pawn actually collects, after MegaCorp's tax is already
withheld) to *gross* (before it), since dividing tax by an already-taxed
income figure overstates the rate; net and gross differ only by the tax
itself, so no new field or division is needed to recover it:
```
gross salary(pawn) = salary collected(pawn) + MegaCorp tax paid(pawn)
burden %(pawn) = (MegaCorp tax paid(pawn) + war profits tax paid(pawn))
                  / (gross salary(pawn) + rent collected(pawn)) × 100
```
Rent needs no such adjustment — it is never taxed at the point of
collection (only later, in aggregate, by a war profits tax assessment),
so the generic core's "rent collected" is already the gross figure.
Pooled across every seed in the config (not averaged seed-by-seed) —
summing every dollar of tax and every dollar of income a pawn ever
touched across the whole config, then taking one ratio, so a long,
lopsided seed doesn't get diluted down to the same weight as a short one.
MegaCorp's term is $0 for a config without `--optional-rent-relief` (e.g.
config 10); war profits tax's term is $0 for a config without
`--optional-war-profits-tax`; a config with neither tax extra active has
nothing to show and omits the figure entirely, the same way
survivors-at-first-tax omits itself for a billionaire-less config. A pawn
who never collects salary or rent (zero income) has nothing to divide by
and is likewise omitted, not zero-filled or shown as an error value.
Meaningful for any config combining at least one tax extra with the
generic-core income breakdown — today that's configs 10 through 13.

**Net fiscal position by pawn** — also README-only and derived, not a new
JSON field: how much a pawn's relief benefit outweighs their tax burden,
in the same normalized unit so the two are directly comparable rather
than left as a $ tax figure and a $ relief figure a reader has to
reconcile by hand:
```
relief %(pawn) = relief received(pawn) / (gross salary(pawn) + rent collected(pawn)) × 100
net position %(pawn) = relief %(pawn) − burden %(pawn)
```
Positive means a pawn received more in relief than it paid across
MegaCorp and war profits tax combined, over its lifetime in the config;
negative means the reverse. Only meaningful where both burden % and
relief % are computable, i.e. wherever `--optional-rent-relief` is
active — today configs 11 through 13 (config 10 has war-profits-tax but
not relief, so has a burden % with nothing to net it against).

This schema is expected to grow (new extras for new flags) without
invalidating existing baseline fixtures for configs that don't use the new
flag — adding a field must not require touching every other config's
fixture. Adding a *generic core* field is different: since it applies to
every config, all baselines need regenerating together when one is added
or changed.

## Baseline comparison

One checked-in fixture per config (e.g. JSON, named after the config) under
`src/test/resources`, holding its expected breakdown. The test runs the
config fresh (all its seeds), computes the same breakdown, and asserts it
against the fixture. A failure names exactly which field(s) drifted and by
how much (e.g. `winner distribution: expected {billionaire: 8}, got
{billionaire: 7, high hat: 1}`), not just "test failed" — the point is
telling a developer what changed, pointing them at the matching log
directory to see why.

## README sync check

`README.md`'s "Simulated game characteristics" section (the `Summary` table
and the `Detailed Breakdown` `<details>` blocks) presents these same
baselines for a human reader. It must not be able to silently drift from
the fixtures it's describing — the same principle `cli-jar-5` already
applies to keeping the README's `-h` usage text in sync with the real CLI
output, just against the baseline JSONs instead of the packaged jar.

A test, part of the same `characterization-tests` profile, reads
`README.md` and every baseline fixture directly (no packaging or CLI
invocation needed — the ground truth is already on disk) and checks that
every data point shown for a config matches that config's fixture:

- Summary table row: the outcome percentages and counts, and the age
  min/median/mean/max.
- Detailed Breakdown block: everything the Summary row has, plus winners,
  bankruptcies, auction count and its percentage of total land
  acquisitions, mortgage count, income composition (both the aggregate
  total and the per-pawn breakdown), and — for whichever configs have
  them — loan origination/servicing, entity, peer-trade, war-profits-tax,
  and rent-relief figures (including relief received and relief starved
  by pawn, plus their age-at-event stats), plus effective tax burden by
  pawn and net fiscal position by pawn for any config where those
  figures apply.

Only the factual data points are checked, not the hand-written analytical
asides (e.g. the `eight_greedo` income-scale comment, the "350 total =
exactly 7 losers × 50 games" arithmetic notes) — those reflect human
judgment about what's worth pointing out, not something derived
mechanically from a fixture, so there's nothing to verify them against.
Some of the checked figures are themselves derived from the fixture rather
than copied verbatim (the outcome percentages, the auction-vs-purchase
ratio, effective tax burden by pawn, net fiscal position by pawn) — the
test needs to reproduce that
derivation, not just search for a raw JSON value as a substring.

A failure names the config, the specific figure, and both the value found
in the README and the value the fixture actually holds — same spirit as
the baseline comparison's own failure messages.

The check must fail if the README's block count doesn't exactly match
`CharacterizationConfig.values()` — never silently check only the shorter
of the two counts. A config added without its matching `<details>` block
(or renamed without the README catching up) has to turn this test red,
not quietly skip verifying the config that's missing coverage; anything
short of exact-count equality defeats the whole reason this check exists.
