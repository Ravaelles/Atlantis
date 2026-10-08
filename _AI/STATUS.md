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

### M0 — PRODUCTION BLOCKER FIXED FIRST (2026-10-07)

Before any v2 work: the owner's bot could not build a Cybernetics Core, and the
log was full of `OrderSink.train failed for #138 Gateway` plus
`ArrayIndexOutOfBoundsException`.

- Root cause: the engine does not RETURN false when a production building cannot
take an order - it THROWS, walking its training queue and stepping past the end.
`BwapiOrderSink.issue()` caught it, printed the stack once a minute and lost the
order, so a busy or unfinished Gateway produced nothing forever while the log
filled up.
- Fixes: `BwapiOrderSink.train` refuses a unit that is not alive/completed;
`ProduceZealot.produceZealot` checks `isTrainingAnyUnit()` before asking;
`GatewayClosestToEnemy.get()` fallback returns `ourOneNotTrainingUnits(Gateway)`
instead of any Gateway that exists.
- Test: `TrainOrderGuardTest` (4 tests, source-pinned: the crash is a
`StackOverflow`-style engine behaviour, not something the stub world can
reproduce). Suite **250/0/4**, ArchUnit 7/7, store unchanged.

### Milestones

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
- **M3 DONE**: `LegacyPlacementPlanner` over `APositionFinder`, plus
  `PlacementPlanner.startPass()/endPass()` so reservations are concrete per pass
  (the finder caches per builder/type, so two identical buildings in one frame
  would otherwise get the same tile). Test: `PlacementPassTest` (3).
- **M4 DONE**: `ProductionDispatcher` (latency window for units; a building is
  only issued when due NOW, never early - travel time is production time),
  `OrderDirector` seam with `GameOrderDirector` (live) and
  `DryRunOrderDirector` (log-only), `ProductionV2Mode` OFF/DRY_RUN/LIVE.
  Tests: `ProductionDispatcherTest` (7), `ProductionV2ModeTest` (4).
- **M5 DONE**: `BuildOrderGoals` (text build orders -> goals, line order is
  priority, supply gate respected) and `DynamicGoals` (workers up to base
  saturation, supply emergency, army floor, expansion).
  Tests: `GoalSourcesTest` (12).
- **M6 PARTIAL**: `PRODUCTION_V2=LIVE` makes v2 the only policy
  (`ProductionCommander` drops the legacy dynamic/supply commanders) and the
  flag is read from ENV. The legacy tree is NOT deleted yet, and the cutover has
  not been verified in a real game.
- Wiring: `GameStateSnapshot` (the only game bridge: stocks, mining rate,
  facility registry, supply in production) + `ProductionEngine` (composition
  root). `ExistingItems` port fixes the phantom second Nexus that blocked the
  opening. Test: `ProductionEngineSmokeTest`, `WorkerProductionTest` (3),
  `OpeningDoesNotHoardMineralsTest`.
- **Tech and upgrades DONE**: `TechProducible` and `UpgradeProducible` close the
  §2-smell-2 gap - v2 planned units and buildings only, so a research goal would
  have been sent to `train()` and silently done nothing. The dispatcher now
  routes them to `OrderDirector.researchOrUpgrade`, and `GameOrderDirector`
  implements it (with the same "is the facility able to take it" guard the train
  path needed). Test: `TechAndUpgradeProducibleTest` (6).
- **Pull-forward DONE**: `PullForwardGoals` implements `Producer::update()` steps
  3 and 4 (01_PRODUCTION.md): supply providers are pulled earlier while minerals
  allow it, extra refineries are wanted before gas is short, and a maxed-out
  supply queue emits an emergency Pylon at `PRIORITY_EMERGENCY`.
  Test: `PullForwardGoalsTest`.
- **Pylon scoring DONE**: `PylonPlacementScore` implements the
  `BuildPositionResolver` rule from 01_PRODUCTION.md - a Pylon tile is worth the
  number of buildable, still-unpowered tiles it unlocks, and ties keep the
  finder's own order. Pure geometry (tiles in, a number out), so it is tested
  without a game. Test: `PylonPlacementScoreTest` (8).
  NOT wired into `LegacyPlacementPlanner` on purpose: choosing among several
  candidates needs a finder that can return more than one, which the legacy
  `findStandardPosition` does not expose - inventing candidates it never
  validated would be worse than the one validated tile we place today.
- Open: a real game run with PRODUCTION_V2=DRY_RUN, then LIVE; then the legacy
  `Queue/**` + `ProductionOrder` + `PreventDuplicateOrders` + `Construction/**`
  healing commanders deleted and the ArchUnit store shrunk.

## Verification protocol

- Every milestone: `bash scripts/run-tests.sh` (fast scope, < 40 s budget,
  CONVENTIONS §12) + ArchUnit 7/7, store unchanged.
- M4+: one real game run (Wine backend) with the flag in dry-run, comparison
  log recorded in this file.
- M6 closes with a game run on the new engine and the legacy tree deleted.
