# Known bugs and behaviour debt

Not a TODO list — this file records things that are **wrong or misleading
today**, as opposed to work that is merely not started (that lives in
`_AI/NEXT.md`). Every entry states what was measured, how it was measured, and
what the consequence is.

An entry leaves this file when the behaviour is either fixed or deliberately
documented in code with a comment. The closure goes into the commit message.
**Fixed defects do not stay here as history** - the git log is the archive, and
a long post-mortem belongs in an ADR or a commit message.

## B-1 - FIXED 2026-10-26 - deleted; unbounded scale stands (owner ruling: no enemy means locally very strong, not infinite - code expecting a range is wrong, fix per site).

## B-19 - FIXED 2026-10-26 - deleted; rule `StandAndTradeAtHome`, pinned `ProbesVsPackSimTest`, live-confirmed 133232 (4 probe deaths vs 14, 1 residual RunHellFar untraced).

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
- **Status 2026-10-21: PARKED with all OpenBW work until 2026-11-09** (owner: Wine only
  for now; then reopen with the OpenBW track or keep parked). Nothing downstream is affected: `--game` fails only on
  explicit 0/0/0, TIMES_HIT is event-sourced and sane on both
  platforms, the tuner objective uses win/hits/seconds. Reopen with the
  OpenBW track, fix at the server event queue (reset/drain per frame),
  not in the counter.

## How to add an entry

`## B-<n> - <one-line symptom>`, then: **Where**, **Measured** (with the
command/scenario), **Consequence**, and **How to settle it**. Numbers are stable
and never reused.
