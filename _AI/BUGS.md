# Known bugs and behaviour debt

Not a TODO list — this file records things that are **wrong or misleading
today**, as opposed to work that is merely not started (that lives in
`_AI/NEXT.md`). Every entry states what was measured, how it was measured, and
what the consequence is.

An entry leaves this file when the behaviour is either fixed or deliberately
documented in code with a comment. The closure goes into the commit message.
**Fixed defects do not stay here as history** - the git log is the archive, and
a long post-mortem belongs in an ADR or a commit message.

## B-1 — `AUnit.eval()` has no documented scale, and 228 call sites assume one

- **Where:** `AUnit.eval()` and the ~228 production comparisons against
  thresholds in `[0.6, 3]` (`ShouldStopRunning`, `TooFarFromFocusPoint`,
  `ProtossLowEval`, ...).
- **What it is:** `enemyScore / (ourScore + 0.001)` over cost-like negative
  scores - higher means our side is stronger. It is **unbounded above**, and the
  "no enemy in reach" reading is **9873.7**, which every `eval() >= x` guard
  reads as "safe".
- **Still open:** whether the thresholds are right for an unbounded ratio. Not a
  test change and not a threshold tweak - it needs a decision (ADR 0006) and a
  scenario sweep over the real evaluator. Guard audit tracked as NEXT #41; the
  evidence tables are frozen in `DOCS/adr/0006-combat-eval-scale.md`.
- **Documented in code:** the javadoc now states what the number is, what it is
  not, and the three distortions by name. The misleading half of this entry is
  closed; only the threshold question remains.

## B-19 — workers flee a packed rush they should help kill

- **Where:** `WorkerDefenceManager` ordering + the 300-frame run/fight lockout.
- **Measured** (`FourPoolDefenseTest`, Protoss base vs 6 lings, 900 frames): the
  cannon fights and dies ~150, the zealot ~250, both trading two lings; the four
  probes never engage and the nexus falls ~880.
- **Fixed in the stub world (2026-10-03):** `WorkerDefenceHelpCannon.applies()`
  AND-ed an "ignore" predicate positively (so it never fired during the melee
  rush it exists for); `BaseUnderAttack` now suppresses Run and lifts the
  lockouts and the `id % 5` / `id % 3` skips while the base is hit. Re-measured
  from a clean run, the 4pool **holds**; the 9pool still loses the base but
  trades three lings instead of two.
- **What is left:** a **game run**. Stub-world physics is documented harness
  rules, not the game, so "the scenario flips" is evidence that the arbitration
  changed, not that the bot survives a real 4pool. Tracked as NEXT #34.

## B-25 — the bot jar ships four serialisation libraries for unreachable code

- **Measured 2026-10-04:** `atlantis.debug.object` (9 files, 510 lines,
  kryo-based) has exactly one production call site,
  `OnEveryFrameHelper.serializeMapDataLikeRegionsToAFile()`, whose first line is
  `if (true) return;`, and one test, which is `@Disabled` (all four skipped tests
  in the suite come from it).
- **Consequence:** `scripts/build-bot-jar.sh` ships `kryo`, `minlog`,
  `reflectasm` and `objenesis` in every bot jar for code nothing reachable can
  call. Measured jar: 6.1 MB fat, 3610 entries.
- **Owner's answer (2026-10-04):** note it as a candidate for removal; if nothing
  plans to use it, it is probably a dead idea. Checked against the plans and
  found nowhere. **Decision, not a chore** - tracked as NEXT #38.

## B-39 — the Protoss cannon producers are commented out

- **Where:** the buildings commander has `// || ProduceCannon.produce()` and
  `// || ProduceCannonAtNatural.produce()`, disabled 2024-10-02 and 2025-01-09
  with no recorded reason; `ProduceCannon` no longer exists in the tree.
- **Measured:** four games, zero cannons (`_AI/e2e/`).
- **Two live cannon paths remain:** `ProtossSecureBasesCommander` (needs
  `Have.forge()` + two bases) and
  `ProtossResponseEnemyHiddenUnits -> ProduceCannonAtNaturalOrMain`.
- **Decision, not a chore:** re-enable (and pick which mechanism owns "the base
  is being attacked"), delete the dead references, or leave them and own the
  consequence in a comment at the call site. Tracked as NEXT #39.

## B-40 — `ConstructionThatLooksBugged` has an unreachable branch

- **Where:** `ConstructionThatLooksBugged.handleConstructionThatLooksBugged()` -
  it returns unless `status() == NOT_STARTED`, then the inner guard is
  `if (constr.status() != NOT_STARTED) constr.assignOptimalBuilder();`, so the
  assignment never runs.
- **Consequence:** a planned construction with no builder is **cancelled**
  ("Weird case, ... has no builder. Cancel.") rather than given one. Builders are
  assigned at creation, which is why it is invisible.
- **Needs a decision** on which behaviour is intended before anyone touches it;
  the exact case is NEXT #40.

## How to add an entry

`## B-<n> - <one-line symptom>`, then: **Where**, **Measured** (with the
command/scenario), **Consequence**, and **How to settle it**. Numbers are stable
and never reused.
