# WO-150b — builder-dead warp twin + progress-based demote signal (NEXT.md #150, part: the twin gap)

## Goal

A warping construction whose builder dies leaves zero further twin
rows today (live: warping Observatory [118,30] STARTED 12:35, builder
#126 died 12:28 - no cancel, no death, no warp; the orphan-drop twin
covers builderless NOT-STARTED warps only). Name the builder-dead
warp in the twins and give `demoteStalledInProgress` a progress
signal, so the next one is decidable from logs instead of forensics.

## Non-goals (do not touch)

- Demote thresholds, the retry path, or any gate ordering. The
  blind one was already fixed (`dropBuilderlessNotStarted` honors
  `UnitsArchive`, pinned); this order only ADDS signal, never
  re-tunes.
- No twin until the HP-static signal exists: a blocked-demote twin
  without a progress signal would mislabel healthy builderless warps
  (NEXT #150 rule, do not relax it here).
- No batch migration of status readers (that is NEXT #157's track).

## Prerequisites

- Read NEXT #150 (remainder section: the twin gap, the mechanism,
  the WO-sized design), `demoteStalledInProgress` (presence check
  order: buildingNearSite FIRST - the standing warp self-matches and
  shields everything below, correct for progressing Protoss warps,
  fatal for frozen ones), the orphan-drop twin (not-started scope),
  `UnitsArchive` (already consulted by the demote path AND the retry
  path - verify both call sites before touching either).
- Baseline: full suite (`bash scripts/run-tests.sh`) numbers,
  written down.

## The map (what gets built)

| Today | After |
|---|---|
| Presence-only stall check (`buildingNearSite` first) | Same order, plus a progress read: warping building HP static over N frames names a frozen warp; progressing warps behave exactly as today |
| Orphan-drop twin (builder-dead NOT-STARTED only) | Extended twin (or a sibling row) covering builder-dead warps: builder death + STARTED with no further rows |
| No HP-static signal anywhere | Signal first (stub-pinned: static HP trips, progressing warp does not), twin second (no twin without the signal) |

## Proof

1. Sim pins (no game needed): frozen warp (static HP) trips the
   signal; progressing warp (rising HP) does not; healthy builderless
   warp never trips (the mislabel guard, mutation-proofed).
2. Live proof rides the next builder-dead warp game (do NOT stage
   one: wait for the shape, then read the twin). Until then the
   item stays open no matter how green the suite is.

## Stop conditions

- Stop and report (no improvising) if the archive cannot tell
  "frozen entity" from "completed but unlogged" (no death twin, no
  ledger row either way) - that ambiguity is the reason this is a
  WO and not a drive-by; a design that needs the ambiguity resolved
  first goes back to NEXT #150, not into this order.
- One logical change per commit (CONVENTIONS section 6); twin and
  signal may land together (same unit: the builder-dead warp
  lifecycle) or split - never with unrelated work.

## Negative sweep 2026-10-24 (all 287 retained Wine logs)

- FROZEN_WARP rows: 0 everywhere. DEMOTE_STALLED reasons: all
  `stoodStillFor=*` (90f standard + 127-741f long-standstill
  variants); zero `builder gone` demotes anywhere.
- Reading: the builder-dead warp freeze the WO targets has not
  recurred once in 287 games on the same vehicle as the original
  Observatory case. The signal + twin stay (harmless, already
  shipped, fail-open by construction); the demote gate stays parked
  behind a live freeze, and that live freeze is now known-rare, not
  just unseen. Do not build urgency on top of this WO.

## Demote-chain integrity 2026-10-24 (290 retained logs)
- 94 DEMOTE_STALLED rows, all `stoodStillFor=*`, zero `builder gone`
  (negative sweep extended: 287 -> 290 logs).
- 94 = 93 chain ordinals (1st..93rd, DRY_RUN 002228's two included -
  its CyberneticsC demote is the referenced first-Cyber ordinal) + 1
  explicitly ordinal-less foreign row (141623 Pylon f5014, documented
  without ordinal, never in tally). Counter exact, no gap, no double.
