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
without losing reproducibility. Five seeds per config is a reasonable
starting point.

## Execution

- Runs are driven through the real CLI entry point (`Simulator`), the same
  way a person running `java -jar ... ` would — not by calling `Game`
  internals directly — so this suite exercises the same code path a user
  actually experiences.
- Games run in parallel (multiple processes or threads — implementation's
  choice) to keep wall-clock time reasonable; 8 configs × 5 seeds is 40
  full games.
- Each game's complete log is written to a dedicated directory under the
  owning module's `target/`, grouped by config, e.g.
  `target/characterization-logs/<config-name>/seed-<N>.log` — so a
  deviation can be investigated by reading exactly the run that produced
  it. This output is build-generated (not committed), matching how
  anything else under `target/` already works.

## Game setups

Nine configs, each additionally run with `--max-years=2500` as a safety
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
- Age at game end: min, max, mean, median across the config's runs.
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

This schema is expected to grow (new extras for new flags, e.g. the
backlogged war-profits-tax feature once it ships) without invalidating
existing baseline fixtures for configs that don't use the new flag — adding
a field must not require touching every other config's fixture. Adding a
*generic core* field is different: since it applies to every config, all 8
baselines need regenerating together when one is added or changed — as they
will the first time these new core fields ship.

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
  acquisitions, mortgage count, income composition, and — for the two
  configs that have them — loan origination/servicing, entity, and
  peer-trade figures.

Only the factual data points are checked, not the hand-written analytical
asides (e.g. the `eight_greedo` income-scale comment, the "350 total =
exactly 7 losers × 50 games" arithmetic notes) — those reflect human
judgment about what's worth pointing out, not something derived
mechanically from a fixture, so there's nothing to verify them against.
Some of the checked figures are themselves derived from the fixture rather
than copied verbatim (the outcome percentages, the auction-vs-purchase
ratio) — the test needs to reproduce that derivation, not just search for
a raw JSON value as a substring.

A failure names the config, the specific figure, and both the value found
in the README and the value the fixture actually holds — same spirit as
the baseline comparison's own failure messages.
