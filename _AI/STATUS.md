# STATUS — Production V2 (`_AI/redesign/01_PRODUCTION.md`)

> Owner priority 1. Spec: `redesign/01_PRODUCTION.md`. This file is the whole
> status - the separate `__01_PRODUCTION_TODO.md` was folded in and deleted
> (2026-10-08) because most of it was done.

## Milestones

| # | Milestone | State |
|---|---|---|
| M0 | Production blocker (engine throws instead of refusing an order) | **done** |
| M1 | Pure domain + `ResourceTimeline` | **done** |
| M2 | `ProductionScheduler` | **done** |
| M3 | Placement seam | **done** (now served by the rewritten planner, see Placement) |
| M4 | Dispatcher + `ProductionV2Mode` (OFF/DRY_RUN/LIVE) | **done** |
| M5 | Goal sources | **done** |
| M6 | Cutover: LIVE verified in a game, legacy tree deleted | **partial** |

## Session 2026-10-09 (later): exit-path crash, worker tweaks, NEXT sweep

### Fixed: stack traces on the game-exit path (owner report)
A run that ended through `ForceExitLocallyAfterRealSeconds` (started from the IDE)
printed a full `java.io.IOException: Cannot run program "pkill"` traceback, through
`ProcessHelper.killWineGameProcesses` -> `killWineProcesses` ->
`killWineProcessesIfOnWine`. `executeInCommandLine`/`executeInCommandLineDetached`
called `err.printStackTrace()` for failures that are **expected** on the exit path
(no `taskkill` on Linux; the JVM cannot name Wine processes from the IDE; the
process is exiting anyway). They now log a one-line note instead.
- Test: `ProcessHelperExitPathTest` (3) - captures `System.err` and asserts no
  `IOException` header, no `\tat ` frame, at most a few note lines.
- Commit: `49f1c9dd`. Fast suite 342/0 (was 339), ArchUnit 7/7, store unchanged.

### Applied: owner's `WorkerAvoidManager` (worker gather-hide under a crowd)
The owner supplied the finished `runTowardsMineralsToBecomeTransparent` body
(the branch stub returned `false`); applied verbatim plus the missing
`Select` import. `MoveUnitsFromConstructionPlace` radius `6 -> 3.8` and the
`GatherFallback` import cleanup were already in the branch (`25ab3618` "Worker
tweaks"). Suite green.

### NEXT #49 closed: `OnGameEnd._executed` latch (was one-way, and asymmetric)
- `_executed` is now reset per game: new `OnGameEnd.reset()`, called from
  `OnGameStarted.execute()` next to the other per-game resets. Without it the
  latch leaked between games in one JVM (same class as
  `UnitsArchive.reset()`/`ReservedResources.reset()`).
- The `Env.isTesting()` branch no longer returns without setting the latch - it
  now sets `_executed = true` before `exitGame`, so a first spurious `onEnd`
  cannot leave the guard disarmed.
- Seam `hasExecuted()` / `markExecutedForTest()` so the rule is assertable
  without entering the `System.exit` path. Test: `OnGameEndLatchTest` (3).
- Commit: (this session). Suite green, ArchUnit 7/7, store unchanged.

### NEXT #48 - re-dispatch loop: root cause narrowed to `GameOrderDirector.buildAt`
Investigated without a game run (the LIVE path is the blocker, and a bounded run
is the gate). The plan item being **re-offered every frame is by design** (pinned
by `ProductionDispatcherTest.committedBuildingIsReofferedEveryFrameUntilItIsFinished`),
so re-offer is not the bug. The defect is that the re-offer is not **idempotent**:
`GameOrderDirector.buildAt` de-dupes a pending construction only on
`buildingType + exact tile` (lines 79-86), while `LiveExistingItems` counts a
building as "coming" on `ConstructionRequests.countNotStartedOfType(type)` -
which ignores the tile. When the planner hands a different tile each frame, a
**second `Construction` for the same building is created**, and the same type
gets dispatched again next frame. Not yet fixed; the next step is a regression
that drives `buildAt` twice with two tiles and asserts one pending construction.

## Prior session: verify the interrupted worker-defense / E2E cycle

## What exists

- **Domain** (`atlantis.production.v2`): `ResourceCost`, `Producible`,
  `ProductionGoal`, `TargetPlacement`, `ProductionItem`, `ProductionPlan`,
  `ResourceTimeline`, `ScheduledProductionItem`, `ProducerFacility`,
  `ProducerFacilityRegistry`, `PlacementPlanner`, `PlacementReservation`,
  `CommittedWork`, `ExistingItems`, `EconomyModel`.
- **Scheduler**: stateless plan per pass; prerequisites recursive and gated to the
  completion frame; earliest affordable frame; shift-forward instead of drop; one
  item per facility slot; `producerLimit`; recipe-cycle cut; placement validated
  before allocation; supply providers add supply at completion; total capped 200.
- **Dispatcher**: `ProductionDispatcher` (latency window for units, due-now for
  buildings), `OrderDirector` + `GameOrderDirector` (frame-scoped de-dup) +
  `DryRunOrderDirector`, `ProductionV2Mode` OFF/DRY_RUN/LIVE.
- **Goals**: `BuildOrderGoals`, `DynamicGoals` + `PullForwardGoals` (workers, one
  owner for the supply rule, army floor, expansion, and the Protoss cannon
  fortification policy from `atlantis.placement.policy`).
- **Adapters**: `GameStateSnapshot` (the only game bridge), `ProductionEngine`
  (composition root), `TechProducible`, `UpgradeProducible`, `UnitProducible`.
- **M0**: the engine throws when a production building cannot take an order;
  guarded in `BwapiOrderSink.train`, `ProduceZealot.produceZealot`,
  `GatewayClosestToEnemy.get()`. Test: `TrainOrderGuardTest`.

## Current work: verify the interrupted worker-defense / E2E cycle
The owner reports the manual StarCraft run builds both Pylon and Gateway. This is
owner-observed behavior, not an OpenBW artifact; preserve that distinction.

1. **Builder ownership rule implemented and tested.** `WorkerManager` gives
   `BuilderManager` first priority; `BuilderManager` retains an assigned
   `NOT_STARTED` construction even when `TravelToConstruct` cannot issue a move
   in that frame, after its own safety submanagers. `WorkerDefenceManager` excludes
   assigned builders and `WorkerHelpCombatUnitsFight` declines them. Regression
   drives the real `WorkerManager` chain and asserts `unit.managerLogs()` contains
   BuilderManager but not WorkerHelp/Gather. Focused `WorkerDefenceTest`: 12/12.
   Commit: `b8f6a611`.
2. **#48 duplicate building goals.** `BuildOrderGoals` emits only the earliest
   unsatisfied occurrence per type; `ProductionScheduler` deduplicates identical
   building type+placement across sources. Regression test pins one Pylon and one
   Gateway despite duplicate supply intent.
3. **#47 actual-result assertions.** `GameSummary` reports Protoss Pylon/Gateway
   counts; runner checks structures, time, kills, balance and placement refusal.
   Offline self-test rejects the 0/0-building fixture.
4. **Jar callback crash report.** Owner saw
   `NoClassDefFoundError: atlantis/combat/squad/AssignUnitToSquad`. Exact crashed
   artifact unavailable; current jar is fresh, contains both callback classes,
   and the builder asserts they are packaged. Remains unreproduced.
5. **Verification (2026-10-09):** focused `WorkerDefenceTest` 12/12; latest full
   suite 339/0 (8.5 s); ArchUnit 7/7 (2.8 s). Deployed jar rebuilt and
   freshness-checked; callback class entries are present. The full-game OpenBW
   runner refused to start because the owner's StarCraft/ChaosLauncher is active.
   The fast-suite host smoke warned that no registry appeared in its 1 s smoke
   window; that is not an E2E result. Do not terminate the owner's game.
6. **Owner manual-SC confirmation (not OpenBW):** Pylon and Gateway both built.
   Record separately from engine-test evidence.
7. **#47 remains open:** resume bounded OpenBW after the owner game ends. Require
   Pylon>=1, Gateway>=1, game>=420s, kills>=12 and <=40, balance>=-200, and no
   placement refusal. Inspect the assigned builder’s `managerLogs()` if it stalls.

### Reverted: NEXT #9 wall-clock extraction attempt
The `A` date/time formatter split into a new `ATime` was started and **reverted**:
repeated text substitution edits consumed the budget without a behaviour change.
`A` owns both methods again (clean tree, suite green). Recorded in `_AI/NEXT.md`
under #9 so nobody repeats it blindly.

### Blocked: NEXT #8 — Production race strategy pilot
ADR 0007 (`d12fd53b`) is explicitly Proposed. The ADR convention and the ADR's
own consequence require owner acceptance before implementing its Production V2
instance-injection seam. #8 must not start until that design decision is made.

### Closed: NEXT #7
`DOCS/adr/0007-race-strategy-seams.md` defines context-local ports (not a global
RaceStrategy), documents the existing static Production V2 engine and its
concrete composition change for #8, limits the pilot to five type identities,
preserves the old goal-generation behavior, and sets tests/branch-count evidence.
Cross-checked against the context map, placement strategy precedent,
ProductionEngine, GameStateSnapshot, and goal generators; no runtime code changed.
Commit `d12fd53b`; ADR remains Proposed pending owner approval for #8.

### Blocked: NEXT #5 — per-frame query service
`Select` exposes static methods backed by static caches, and the current
`FramePipeline`/bootstrap does not construct or pass a per-frame query service.
An instance-owned `core.world` cache cannot replace a static Select cache without
threading a service through its static call graph; adding another static holder
would violate the ownership intent and CONVENTIONS §5. #5 needs an explicit
application-owned service lifecycle (or a decision on temporary resolution from
the static facade) before a behavior-neutral migration is possible. Do not
relabel a new static cache as a service.

### Blocked: NEXT #34 — real-opponent scenario follow-ups
The current OpenBW harness accepts exactly one BWAPI client; its opponent comes
from the engine auto-menu and is race-only. A second Java bot cannot attach, and a
scripted rusher requires a UMS map with units/triggers that is not present in this
workspace. Wine/scbw opponent runs belong to the owner and cannot be launched by
this model. A completed single-client OpenBW game vs engine AI verifies attachment
but is not the requested 4pool/9pool real-opponent twin. Resume #34 when a scripted
UMS map is supplied or the harness/owner provides the intended opponent runner.

### Partial: NEXT #15 — deployed Protoss jar; Terran target unresolved
`/sc-ai/BOTS/AtlantisP/AI/Atlantis.jar` rebuilt canonically; freshness fingerprint
`94e518…` matches and bytecode major is 52 (Java 8). OpenBW completed: client
attached, exit 0, `Total time: 843 seconds`, no exception. The log reports
`Can't find place for Pylon` at 0:39, so this verifies the jar was played, not
that placement works. The checked Terran output `/sc-ai/BOTS/AtlantisT/AI/Atlantis.jar`
does not exist; scbw manages downloaded opponent caches. Workspace listing outside
`/sc-ai/Atlantis` was refused by the boundary tool; did not attempt a workaround.
#15 remains open until the owner identifies/approves the Terran deployment path.
Evidence: `out/openbw/bot.log`, `out/openbw/server.log`. Commits:
`bd451818` (Java 8 test fixture repair), `30f4d354` (partial jar verification).

### Closed: NEXT #41
`ProtossMissionDefendAllowsToAttack` now requires `hasEnemyForEval()` for the
`eval() >= 1.3` shortcut. The earlier independent recent-attack/focus override
is unchanged. Regression test `EvalHasReadingTest.defendDoesNotChaseAnUnmeasuredTargetOnTheQuietEvalValue`
failed with the old guard (3/4; expected INDIFFERENT but got TRUE) and passes
with the guard (4/4). Full fast suite: 335 passed, 0 failed; ArchUnit: 7/7.
No numeric threshold changed. Commit: `164c377f`.

### Blocked: NEXT #42 diagnostic evidence
Reason diagnostics are committed (`7cbe31fe`) and tests/build pass, but OpenBW
attempts did not complete a qualifying game with a finished Cybernetics Core and
no Assimilator. One game lost the only Probe before the condition; another yielded
no captured verdict. Do not change eligibility gates or close #42 until the
blocking reason is captured in a completed qualifying game.

### Completed partial cycle: NEXT #42 instrumentation
Added reason labels for producer rejections (`AlreadyHaveAssimilator`,
`AlreadyQueued`, `ExpansionSupplyGate`, `NoCyberneticsCore`,
`CyberneticsCoreNotNearCompletion`, `QueueRejected`) and an `applies()` report for
the first blocking commander gate (`Throttle`, `MineralsOrQueueSize`,
`StandardResourcesOrExpansion`, `CriticalQueueResources`), plus earlier
short-circuiting producers (`FallbackPylon` / `CyberneticsCore`) that can prevent
reaching the Assimilator producer. No eligibility gate was intentionally changed.
Fast suite: 335 passed, 0 failed; ArchUnit: 7/7; canonical jar build passed with
Java 8 bytecode and a fresh source fingerprint. A 90-second OpenBW game attached
and completed but lost its only Probe at 48:28 game time, before the diagnostic
precondition. A second invocation yielded no captured verdict; no bot/host process
remained and the log contains no usable end state. #42 remains open pending a
completed run that reaches the historic Core/no-Assimilator state and captures
the blocking gate.

### Deferred: NEXT #40 — construction recovery branch characterization
`ConstructionThatLooksBugged` has a branch that cannot run: it returns unless
status is `NOT_STARTED`, then checks for a status other than `NOT_STARTED`
before assigning a builder. The current fallback cancels a request without a
builder. NEXT explicitly requires an owner-intent test before changing either
behavior; no test can resolve that policy question without an agreed expected
outcome.

### Blocked: NEXT #36 — BaseUnderAttack cache/seam
The backlog proposes a per-frame cache of `check()` (or an injectable seam) to
avoid repeated base/enemy queries across workers. A new static production cache
would violate CONVENTIONS §5, which explicitly forbids new static caches. The
cache approach is therefore not being implemented. The non-static seam requires
threading a shared query service through the worker manager graph; that is larger
than the item’s “cheapest honest version” and needs a design decision. Revisit #36
when a per-frame query service exists or the owner approves a seam design.

### Still open: Production V2 resource-arbitration livelock (M6 blocker)
A deterministic scheduler probe reproduced the old relative-frame slide: with a
60-mineral bank, a 50-mineral Probe goal and a 100-mineral Pylon goal, planned
Pylon start moved 449, 450, 451, 452 as the timeline origin advanced. Proposed
fixes were reverted: subtracting `ExistingItems.availableFrom` suppresses worker
production because it includes existing workers; raising only the dynamic supply
goal does not address a Pylon from a build-order row. The attempted OpenBW run
produced no verifiable game result: its log ends in repeated `No server proc ID`
for stale PID 2785905. Historical STATUS evidence remains the live-run finding
that Pylon start advanced +1/frame through frame 4500. A fresh, captured run and
full goal-source/dispatch trace are still required before changing arbitration.

## What is still open
1. **The cutover has not been verified in a real game.** `PRODUCTION_V2=LIVE` is
   the only policy when set, but no LIVE run has been accepted as correct. The
   legacy tree stays as the fallback until one is.
2. **The plan livelocks when an item is never affordable (THE blocker, measured
   2026-10-08).** On a live OpenBW run the Pylon's scheduled start frame advanced
   by **exactly one frame per frame** - `Pylon@979`, `@980`, `@981`, `@982` - so it
   was never due and never built, while the game ran to frame 4500. Cause: the
   Probe's training keeps the minerals below the Pylon's cost, so
   `findEarliestAffordableFrame` answers "16 frames from now" every frame. The item
   is always just out of reach and the plan slides forever. This is a **timeline /
   arbitration** problem: something must decide that a cheaper, higher-priority item
   (the Probe at 50 minerals) starving a needed building (the Pylon at 100) is a
   livelock, not a schedule - likely a bounded look-ahead or a "reserve for the
   higher-priority goal" rule. It is the last thing between us and a bot that
   develops on OpenBW.
3. **Full dynamic-goal parity**: tech, race-specific army composition and
   strategic (Play) contributions are still simplified.
4. **The economic model is constants**: `EconomyModel` holds the rates and the
   first-trip delay, but worker/gas rates are not measured.
5. **Legacy deletion** (`Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`,
   `Construction/**` recovery) after LIVE passes. A previous count said **149
   production files** reference the queue, so this is not a mechanical delete.
6. **The Gateway `OrderSink.train failed ... : 5` report**: unconfirmed, and no
   speculative guards for it.

### Fixed in this round (so nobody re-hunts them)

- **The scheduler re-planned buildings forever.** `scheduleGoal` had no
  "do we already have this" step, so a fresh building was planned every frame at a
  sliding earliest-affordable frame. Buildings are now subtracted against what the
  game already has or has coming; units are deliberately left to their goal
  generators (worker goal = existing + in production).
- **The demand loops in the goal layer** (measured first as 2883 ordered Pylons
  and thousands of Probes): the worker goal counted existing workers only, and the
  supply goal ignored Pylons already building. Both count what is on the way now.
- **Placement works in a live game** with `PLACEMENT=catalogue` - the
  `Can't find place for Pylon` failure is gone. See Placement below.
- **The Pylon cancel that is left** (`took too long (36s) / buildable:true`) is a
  *consequence* of (2): the construction is cancelled because the order never
  became due, not because the builder or the tile is wrong. `buildable:true` in
  that line is the placement working.

## Blocker outside this track

**Placement.** Putting a building on the map is part of production, and the
placement planner is freshly rewritten (`_AI/redesign/03_PLACEMENT.md`, whose top
section lists what is unfinished). Do not extend the legacy `APositionFinder`
while that is pending.

## How to verify

- Every cycle: `bash scripts/run-tests.sh` (fast, < 40 s) + ArchUnit, store
  unchanged or shrunk.
- LIVE runs on OpenBW: `PLACEMENT=catalogue PRODUCTION_V2=LIVE bash
  scripts/run-openbw-e2e.sh "<map>" Protoss Protoss` (see
  `_AI/IDEA-E2E-TESTS.md`). Never launch Wine/StarCraft (CONVENTIONS §14).
- M6 closes with an owner-verified LIVE run and the legacy tree deleted.


## Resumed NEXT work (2026-10-09, autonomous continuation)

### NEXT #49 closure recorded
The `OnGameEnd` reset/one-way latch fix is in commit `3d5c6dac`; `OnGameEndLatchTest`
exercises the latch reset and one-way invariant, and the fast suite (345 passing,
0 failing) plus ArchUnit (7/7) passed at the current tree. Removed #49 from `NEXT.md`
per CONVENTIONS §7. This is independent from the outstanding OpenBW blocker.

### NEXT #47/#48: fresh OpenBW evidence
Rebuilt `/sc-ai/BOTS/AtlantisP/AI/Atlantis.jar` with the canonical builder and
confirmed freshness before the runs. The bounded 7-game-minute default-path run
attached but failed the expected Pylon/Gateway/kills/balance assertions and logged
`Can't find place for Pylon`; its log was subsequently overwritten by the V2 run,
so no more detailed claim is based on that artifact.

The bounded `PRODUCTION_V2=LIVE PLACEMENT=catalogue` run attached but hit the
runner's 110-second bot cap (outer `timeout 120`; exit 124), before a verdict. Its
log shows Pylon dispatch at a start frame already behind the current frame and
repeated `builder committed` responses, with no completed Pylon/Gateway summary.
The specific worker diagnostic (`BuilderManager` log in `out/openbw/bot.log`) shows
worker #118 assigned to a Pylon at `[120,14]`, status `NOT_STARTED`; its retained
manager history alternates `BuilderManager` and `WorkerHelpCombatUnitsFight` /
`GatherResources` on successive frames. This is measured evidence that the simple
unit test of `WorkerManager` does not yet explain the live manager-chain behavior.
Per CONVENTIONS §18, inspect that worker's history before changing manager ordering.
No placement or builder patch is justified yet; #47 and #48 remain open.
