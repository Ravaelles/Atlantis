# STATUS — Production redesign (Track 01, `_AI/redesign/01_PRODUCTION.md`)

> Living status file for the production-v2 refactoring. Update at the end of
> every work cycle. Plan first, then status; no narrative.

## Plan (milestones, in order)

1. **M1 — Pure domain + timeline** (no BWAPI in the computational core):
   - `atlantis.production.v2` package: `ResourceCost`, `Producible`,
     `ProductionGoal`, `TargetPlacement`, `ProductionItem`,
     `ProducerFacility`, `ResourceTimeline`, `ProductionPlan`,
     `ScheduledProductionItem`.
   - `ResourceTimeline`: frame-indexed minerals/gas/supply over a horizon;
     `addMiningIncome`, `findEarliestAffordableFrame`, `allocate`;
     solvency invariant (never negative).
   - Unit tests (pure JUnit, no game): income projection, earliest-affordable
     frame, allocation subtraction, solvency, shift-forward behaviour.
2. **M2 — Scheduler**:
   - `ProductionScheduler.schedule(goals, timeline)` → `ProductionPlan`
     (priority order, prerequisites inserted, duplicates resolved,
     time-shift instead of drop).
   - Unit tests: "pylon before gateway", "cybernetics before dragoon",
     "competing goals shift, not drop", priority ordering.
3. **M3 — Placement seam**: `PlacementPlanner` interface +
   `SimplePlacementPlanner` (delegates to the existing `APositionFinder`);
   tests with a fake.
4. **M4 — Dispatcher + feature flag**: `ProductionDispatcher` issues train /
   build commands when `startFrame <= latencyFrames`; behind an ENV flag
   (`PRODUCTION_V2=false` default) running in dry-run (log-only) mode in
   parallel with the legacy queue; comparison logs.
5. **M5 — Goal sources**: `BuildOrderGoals` (text build orders → goals),
   `DynamicGoals` (workers/supply/expansion/tech/army), priority bands.
6. **M6 — Cutover + cleanup**: flag on, legacy `Queue/**`,
   `ProductionOrder`, `PreventDuplicateOrders`, `Construction/**` healing
   commanders deleted; ArchUnit store shrinks.

## Status

- **M1 DONE** (commit `c36ce79f`): pure domain (`ResourceCost`, `Producible`,
  `ProductionGoal`, `TargetPlacement`, `ProductionItem`, `ProductionPlan`),
  `ResourceTimeline` with linear income + solvency check; `UnitProducible`
  adapter over `AUnitType`. Test: `ResourceTimelineTest` (12 tests). Suite
  149/0/4, ArchUnit 7/7, store unchanged.
- **M2 DONE** (commit `401b29ea`): `ProductionScheduler` (stateless plan per
  pass; prerequisites recursive; earliest-affordable-frame; shift-forward;
  per-pass `plannedFacilityAvailableFrom` so a planned Robotics gates the
  Reaver to its completion frame). Seam classes: `ProducerFacility`,
  `ProducerFacilityRegistry`, `PlacementPlanner`, `PlacementReservation`.
  Test: `ProductionSchedulerTest` (5 tests, fully fake-backed). Suite 154/0/4,
  ArchUnit 7/7.
- **M3 IN PROGRESS**: real `PlacementPlanner` over the existing
  `APositionFinder`; tests with a fake map/port.
- M4-M6: pending (dispatcher + ENV flag dry-run, goal sources, cutover).

## Verification protocol

- Every milestone: `bash scripts/run-tests.sh` (fast scope, < 40 s budget,
  CONVENTIONS §12) + ArchUnit 7/7, store unchanged.
- M4+: one real game run (Wine backend) with the flag in dry-run, comparison
  log recorded in this file.
- M6 closes with a game run on the new engine and the legacy tree deleted.
