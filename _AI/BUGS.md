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
  scenario sweep over the real evaluator. The guard audit itself closed with
  NEXT #41 (`164c377f`: quiet reading named, defend-mission chase guarded);
  the evidence tables are frozen in `DOCS/adr/0006-combat-eval-scale.md`.
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
- **Update 2026-10-14 (NEXT #87):** the 9pool twin now **holds** (pin since
  `6c0b040e`): nexus and cannon live, all 8 lings die, zealot fights. The
  drill volume moved the other way by design - hurt probes flee instead of
  feeding (flee floors from the owner's Wine death logs), measured split
  cannon 12 / zealot 2 / probes 7 strikes. The pin now demands token probe
  engagement (>= 5) rather than drill-carried kills.
- **What is left:** a **game run**. Stub-world physics is documented harness
  rules, not the game, so "the scenario flips" is evidence that the arbitration
  changed, not that the bot survives a real 4pool. Tracked as NEXT #34.
- **Update 2026-10-17 (combat cutover):** the twins are deleted with the
  manager chains they drove (`__PLAN-COMBAT.md` step 3) - rush coverage is
  dark until the cluster learns to fight. The pins above describe the
  deleted arbitration, kept in git history.

## B-25 — the bot jar ships four serialisation libraries for unreachable code

> **FIXED 2026-10-11 (`NEXT.md` #38).** `atlantis.debug.object` and three of the four
> libraries are deleted; the jar went from 10.83 MB to 10.34 MB. `objenesis` stays in the
> project because Mockito needs it for tests, but it is no longer packaged. Kept here as a
> one-line record rather than a post-mortem - the git log is the archive. The entry that
> follows is the original report.

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

## B-41 — a construction whose building was warped was never finished, holding its worker

> **FIXED 2026-10-11 (`NEXT.md` #53).** `ProtossWarping.updateNewBuildingJustWarped` marked
> the construction `IN_PROGRESS`, never `FINISHED` - and `FINISHED` is the only status that
> removes it from `ConstructionRequests` - so it survived for the rest of the game, with
> `countPendingOfType` counting a standing building and `BuilderManager.isBuilder` refusing
> its worker. A building that appeared away from its reserved tile (site moved by
> `RefreshConstructionPosition`, or a snapped footprint) matched no construction at all and
> leaked the same way, because the lookup is positional (2.5 tiles). Both fixed: `FINISHED`
> on warp, and a fallback that adopts the oldest waiting construction of the same type on a
> positional miss. Measured: the same bounded run went from `Pylons=0/5 gateways=0` to
> `Pylon=2 Gateway=1`, and `CANNOT define construction` from a 1799-frame wall to 0. Test:
> `OrphanedConstructionTest` (2). `TerranNewBuilding` had the same two
> suspected defects; NEXT #56 measured them absent in a Terran game and
> closed (`37816f95`).

## How to add an entry

`## B-<n> - <one-line symptom>`, then: **Where**, **Measured** (with the
command/scenario), **Consequence**, and **How to settle it**. Numbers are stable
and never reused.
