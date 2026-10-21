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
- **Status 2026-10-18:** the arbitration this entry pinned is GONE - the
  manager chains were deleted with the combat cutover (`__PLAN-COMBAT.md`
  step 3) and the 4pool/9pool twins with them, so rush coverage is dark
  until the cluster learns to fight (NEXT #94). The full measurement
  history (4pool holds since the fix, 9pool holds since `6c0b040e`,
  drill-volume split cannon 12 / zealot 2 / probes 7) is kept in git
  history and `_AI/CASE-STUDIES.md`. What is left is a **game run**:
  stub-world physics is harness rules, not the game (NEXT #34).

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

> **FIXED 2026-10-11 (`NEXT.md` #53).** `ProtossWarping.updateNewBuildingJustWarped`
> marked the construction `IN_PROGRESS`, never `FINISHED`, and a building warped
> away from its reserved tile matched nothing (positional lookup, 2.5 tiles) -
> so constructions survived the game and held their workers. Fixed: `FINISHED`
> on warp + oldest-same-type adoption on positional miss. Measured: bounded run
> `Pylons=0/5 gateways=0` → `Pylon=2 Gateway=1`, `CANNOT define construction`
> 1799-frame wall → 0. Test: `OrphanedConstructionTest` (2). Full mechanism in
> `_AI/CASE-STUDIES.md` (#53); the original report lives in git history.

## B-42 — bot KILLED counter inflates per-frame on multi-unit UMS maps

- **Where:** `Atlantis.KILLED++` in `OnUnitDestroyed.onEnemyUnitDestroyed`
  (fires on every BWAPI `onUnitDestroy`, no real-unit filter - the
  `isRealUnit()` check is commented out).
- **Measured 2026-10-21** (`bash scripts/build-openbw.sh --game ...`):
  8Drag_v_8Drag KILLED 7078 / 307 game-s (~23/s), Drag_v_LingsUpg2 KILLED
  31004 / 195 game-s (~159/s), while the UNIT_DIED twin fired 0-1 times
  and 1Drag_v_1Zeal in the same session reported a clean 0/0. Shape fits
  destroy events re-firing per visible enemy per frame, mechanism open.
- **Consequence:** `--game` verdict unaffected (fails only on explicit
  0/0/0; TIMES_HIT is event-sourced and sane), but a tuner reading
  KILLED would conclude massacres. Tuner objective (NEXT #160) uses
  win/hits/seconds, so nothing downstream consumes it today.
- **How to settle it:** name the destroy source (log unit IDs in
  `onEnemyUnitDestroyed` for one 8Drag run: repeat IDs = re-fire,
  revealer IDs = wrong population), then filter or fix the counter.

## How to add an entry

`## B-<n> - <one-line symptom>`, then: **Where**, **Measured** (with the
command/scenario), **Consequence**, and **How to settle it**. Numbers are stable
and never reused.
