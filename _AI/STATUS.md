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

## Next steps (resume here; do not skip verification)

Detailed actionable checklist is in `_AI/__01_PRODUCTION_TODO.md`. The Gateway
train report is explicitly deferred there; proceed with the V2 correctness gaps
first, without treating the unconfirmed `: 5`/`ArrayIndexOutOfBoundsException`
as a proven root cause.

### A. Establish a trustworthy baseline
1. Check `git status --short` before editing. Preserve any owner changes.
2. Run `timeout 360 bash scripts/run-tests.sh`; record the actual totals and
   ArchUnit result below. The last observed report was 256 tests, but do not
   assume it is still current.
3. Do not run Wine/StarCraft. For any game-dependent acceptance test, provide
   the owner the exact command/setup and mark it pending owner verification.

### B. Gateway train failure: diagnose before changing more guards
The reported log has `OrderSink.train failed for #138 Gateway` and an
`ArrayIndexOutOfBoundsException`, but the stack trace is emitted by the catch in
`BwapiOrderSink.issue()` and is not the original exception location. The text
`: 5` in the heading has not been authoritatively mapped to a BWAPI error code.
Earlier claims about its cause were not proven. Treat the root cause as OPEN.

1. Inspect the exact current branch/source and `git blame` for the Gateway
   selector, train guard, and `BwapiOrderSink.issue()`.
2. Capture the actual throwable type, message, and original stack at the sink
   boundary (or a safe diagnostic equivalent); do not infer cause from the
   synthetic `Thread.dumpStack()` trace.
3. Determine whether code 5 is a command error or whether the displayed `5` is
   unrelated text; use the vendored JBWAPI/OpenBW source as evidence.
4. Check completed/free/training-queue state for the reported Gateway ID before
   calling `train`. In particular, verify whether `isTrainingAnyUnit()` and
   `Select.ourOneNotTrainingUnits()` agree for completed Gateways with queued
   work. Do not add broad exception swallowing or an unverified filter.
5. Add a focused regression test for the confirmed precondition/selection bug;
   run it red-before/green-after where feasible, then the fast suite.
6. Keep this defect separate from Cybernetics Core prerequisite scheduling
   unless evidence connects them. A failed Zealot train command does not alone
   prove why the Core is not built.

### C. Close production-v2 correctness gaps before declaring M6
Review `_AI/redesign/01_PRODUCTION.md` against code and tests, one invariant at a
 time. Add tests for each behavior before enabling/deleting legacy behavior.

1. **Repeated goals/stateless recomputation:** build-order rows are fed every
   frame. Confirm `BuildOrderGoals` emits only unsatisfied quantities, including
   units already completed, in production, or represented by a pending order.
   The current conversion appears to emit a row repeatedly; verify with a test
   and fix through an explicit satisfaction snapshot/port.
2. **Prerequisite readiness:** ensure an existing but unfinished building is
   represented as a facility available only at completion, while a completed
   building satisfies the prerequisite immediately. Test Cybernetics Core ->
   Dragoon and Gateway -> Zealot, plus a prerequisite already under construction.
3. **Atomic placement/resource commit:** scheduler currently allocates resources
   before the placement reservation is validated. A placement failure must not
   consume timeline funds. Reserve/validate first, then commit atomically; test
   failure leaves balances and later scheduling unchanged.
4. **Existing work and budget:** test pending construction/resource accounting
   against `ConstructionRequests`, training queues, completed units, and current
   stocks. Avoid double-counting resources already deducted by the game.
5. **Producer selection and uniqueness:** represent a real producer assignment
   (not only a producer type string); prevent one Gateway from receiving two
   commands in one frame and respect queued training/research/upgrade capacity.
6. **Supply timeline:** distinguish supply used, free supply, and future supply
   providers. Test Pylon-before-block, max-supply emergency provider, and no
   negative supply at any projected frame.
7. **Race/recipe completeness:** verify Protoss, Terran, and Zerg producibles,
   prerequisite chains, build durations, producer types, research and upgrade
   routing. Unknown producer/recipe must be explicitly unschedulable, not treated
   as available at frame 0.
8. **Dynamic goal quality:** compare workers/supply/expansion/army/tech goals to
   legacy policy with deterministic snapshots. Avoid speculative permanent
   goals that duplicate completed work or create unbounded expansions.
9. **Goal priority/count semantics:** test stable order for equal priorities,
   continuous goals, count > 1, producer limits, and `targetStartFrame`. Preserve
   requested future start frames rather than silently scheduling at frame 0.
10. **Execution idempotence:** plans are recomputed each frame, so dispatch must
    not reissue a command that is already queued or under construction. Test
    train, research, upgrade, and builder paths separately.

### D. OpenBW integration and M6 cutover
1. Continue from `_AI/PLAN-OPENBW.md` and `_AI/CHALLENGES/OpenBW.md`; no new
   transport guesses. The client/host lifecycle and shared-memory registry still
   need owner-run game verification.
2. Keep the legacy path as fallback while V2 is DRY_RUN. Compare requested
   goals/planned items/legacy orders in bounded, throttled logs.
3. Ask the owner to run the documented OpenBW test on a real map and return the
   bot/server logs plus the production observations. Never launch Wine/SC from
   here; `_AI/CONVENTIONS.md` §14.
4. Only after dry-run parity and owner confirmation, enable LIVE and test on
   OpenBW: opening Pylon/Gateway mineral thresholds, Cybernetics Core before
   Dragoon, repeated Gateway production, research/upgrade, supply-block recovery,
   builder death/reassignment, no duplicate dispatches.
5. Delete legacy `Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`, and
   `Construction/**` recovery code only after V2 LIVE passes those checks. Then
   run Java 8 compile, fast tests, ArchUnit; confirm frozen store is unchanged or
   shrunk. Do not expand the baseline to make a rule pass.
6. Update this status with exact test/game evidence after each cycle. Do not mark
   M6 DONE without an owner-verified OpenBW run; do not delete `_AI/STATUS.md`
   while any step in this section or the spec remains open.

### Resume point
Start with **A1-A2**, then **B1-B5** (Gateway failure is the immediate reported
production blocker). Continue with **C1** onward in small verified cycles. M6
remains partial until the owner verifies the LIVE OpenBW game.

- Resume status at handoff: M1-M5 reported done; M6 partial; M3 placements use
  the legacy finder and Pylon scoring is pure but not wired. Latest claimed fast
  suite count in the previous handoff was 256; rerun before relying on it.
- Gateway `OrderSink.train` root cause: **unconfirmed**. Existing alive/completed
  and busy-Gateway guards are present; gather original failure evidence before
  another production change.
- V2 LIVE OpenBW game: **not verified**. Do not claim the complete refactor.
- StarCraft/Wine launch: **forbidden unless owner explicitly requests it**.
  OpenBW game runs are owner-executed as documented in the OpenBW challenge.
- Tool/runtime constraints: commands must be bounded by 360 seconds; project
  targets Java 8 (`javac --release 8`), and all tests compile with production.
  Keep steps short, update this file at the end of every cycle, commit each
  verified logical change.
- Current user priority: finish `_AI/redesign/01_PRODUCTION.md`, diagnose the
  Gateway train failure from evidence, add regression tests, and keep updating
  this file. Do not ask to begin implementation; proceed in order above.
  Avoid speculative architecture additions and do not remove working legacy
  paths before V2 is proven in a game.
- User also requested a future sweep of oversized October comments; detailed
  list is not yet established. Do not mix that cleanup with production fixes;
  first inventory changed files against `feature/2025-12-t`, then shorten
  comments in small reviewable commits.
- `build-bot-jar.sh` is the canonical Linux/IDE jar builder. IntelliJ artifact
  generation should remain disabled; `scripts/run-wine-full.sh` exposes
  `JAR_OUT`, `BUILD`, and `LIVE_OUTPUT` at the top. Never assume IDE output and
  the jar being run are the same artifact.
- In `WorkerDefenceTest`/`WorkerSwarmTest`, source-pinned assertions exist because
  the stub world does not model real combat damage. Do not treat them as proof
  of in-game survival; OpenBW owner-run confirmation remains necessary.
- Uncommitted resource-assimilation work observed before this handoff may be in
  the working tree; inspect `git status --short` first and preserve it.
  Specifically, `GameStateSnapshot.buildTimeline()` was changed to deduct costs
  of not-started `ConstructionRequests` using clamped mineral/gas totals. Verify
  correctness (whether those resources are already reflected in current stocks)
  before accepting it; add a regression test rather than reverting blindly.
- Recent v2 components to audit against the spec: `ExistingItems`,
  `PullForwardGoals`, `TechProducible`, `UpgradeProducible`,
  `PylonPlacementScore`, `GameOrderDirector.researchOrUpgrade`, and
  `ProductionDispatcher`. Check no duplicate goals, no phantom facilities,
  frame monotonicity, solvency, single-frame uniqueness, and latency dispatch.
- Status counts/commit IDs earlier in this file are historical milestone
  evidence, not current truth; re-run tests and inspect `git log`/working tree
  before updating progress.
