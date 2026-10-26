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

## B-25 - FIXED 2026-10-11 (NEXT #38) - deleted; jar 10.83 -> 10.34 MB.

`atlantis.debug.object` and three of the four serialisation libraries are gone;
`objenesis` stays for Mockito but is no longer packaged. `scripts/build-bot-jar.sh`
is the one place that decides what enters the jar. Kept as a one-line record - the
git log is the archive.

## B-41 - FIXED 2026-10-11 (NEXT #53) - deleted; `ProtossWarping` is the finish path.

`ProtossWarping.updateNewBuildingJustWarped` marked the construction `IN_PROGRESS`,
never `FINISHED`, and a building warped away from its reserved tile matched nothing
(positional lookup, 2.5 tiles) - so constructions survived the game and held their
workers. Fix: `FINISHED` on warp + oldest-same-type adoption on a positional miss.
Test: `OrphanedConstructionTest` (2). Mechanism: `_AI/CASE-STUDIES.md` #53.

## B-42 — bot KILLED counter inflates per-frame on multi-unit UMS maps (OpenBW-only)

- **Where:** `Atlantis.KILLED++` in `OnUnitDestroyed.onEnemyUnitDestroyed`
  (fires on every BWAPI `onUnitDestroy`, no real-unit filter - the
  `isRealUnit()` check is commented out).
- **Measured 2026-10-21:** OpenBW ZealDrag_v_LingsUpg batch: KILLED
  17002-88051 over ~260 game-sec while the UNIT_DIED twin fired 0-4x;
  same counter on Wine (same bot, same map, same tuner path): 25-32 -
  exactly the real kills. So this is NOT a bot counting bug: on OpenBW
  the server never resets `eventCount` (`Server.cpp`: reset only in
  `initializeSharedMemory`, per-frame reset covers strings/commands but
  not events), and the client re-processes stale destroys every frame.
  Wine's real BWAPI does not do this.
- **Status 2026-10-21: PARKED with all OpenBW work** (owner: Wine only
  for now). Nothing downstream is affected: `--game` fails only on
  explicit 0/0/0, TIMES_HIT is event-sourced and sane on both
  platforms, the tuner objective uses win/hits/seconds. Reopen with the
  OpenBW track, fix at the server event queue (reset/drain per frame),
  not in the counter.

## How to add an entry

`## B-<n> - <one-line symptom>`, then: **Where**, **Measured** (with the
command/scenario), **Consequence**, and **How to settle it**. Numbers are stable
and never reused.
