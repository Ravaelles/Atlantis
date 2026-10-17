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

## How to add an entry

`## B-<n> - <one-line symptom>`, then: **Where**, **Measured** (with the
command/scenario), **Consequence**, and **How to settle it**. Numbers are stable
and never reused.
