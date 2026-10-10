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


### NEXT #47 progress: non-zero bot exit no longer becomes a scenario pass
A fresh V2 OpenBW run returned bot exit 124 at the 110-second inner cap; the
runner nevertheless ran `assert-openbw-scenario.sh`, which prints `SCENARIO PASSED`
when no optional expectations are set, then only returns 124 at the very end.
This made an aborted run look green in its transcript. `run-openbw-e2e.sh` now
exits immediately with the bot's non-zero code after printing its log tail, before
scenario assertions. Regression `LauncherBuildsTheJarTest.anUncleanOpenBwBotExitCannotBeReportedAsScenarioPassed`
pins ordering; its shell syntax check also covers the OpenBW script. Verification:
fast suite 346/346, ArchUnit 7/7. #47 remains open: Pylon/Gateway and survival
assertions have not passed.


### NEXT #47/#48: Pylon command failure measured on the live selected tile
Additional temporary diagnostics were removed after use; the deployed bot jar was
rebuilt from the clean source tree and freshness-checked (`87aa649a...`). On the
LIVE OpenBW run, an actual `Unit.build(Pylon, tile)` returned false even though
`AUnitOrders` reported `ErRoR:b` (the origin tile is buildable including
buildings). The focused probe sampled the Pylon's 2x2 footprint: every tile was
`MapTiles.isBuildableIncludeBuildings=true` and `Game.isBuildable(..., true)=true`,
there were no other units overlapping the footprint, and Atlantis could afford
it; nevertheless `Game.canBuildHere(tile, Pylon, builder)=false`. This shows the
failure is not explained by occupied/unbuildable terrain or low minerals, but the
available evidence does not yet identify which `canBuildHere` condition is false.
Do not add a map-grid fallback or alter `APositionFinder` based on these signals;
the exact live command is rejected. #47/#48 remain open pending a targeted
reproducer that identifies the builder-specific/site-specific rejection and a
completed bounded game verdict.


### Correction to earlier #48 manager-history inference
The earlier note said the alternating names in `managerLogs()` implied that
WorkerHelp/Gather hijacked the assigned builder. That inference is not established:
a later focused sample of the assigned Pylon worker (frame 5040) shows
`isBuilder=true`, Construction status `NOT_STARTED`, and only `BuilderManager` in
its active history, while the building still has not appeared. Do not change the
manager order on that basis. `PylonBuildReject` samples at the current candidate
show `canBuildHere=false` with no builder and with the assigned builder, while all
2x2 cells are map/engine-buildable and there are no overlapping units; the precise
engine precondition remains unidentified. Temporary probes were removed and the
deployed jar rebuilt/freshness-checked from clean source.


### Stardust reference check (2026-10-10; read-only)
Inspected `/sc-ai/Stardust/src/Builder/Builder.cpp` and
`Builder/BuildingPlacement.cpp` for the OpenBW construction symptom.

- Stardust does not treat "builder committed" as proof the building started:
  `issueOrders` checks the result of `builder->build(...)`; success and failure
  are counted separately. A failed command reads `BWAPI::Broodwar->getLastError()`;
  insufficient minerals/gas do not count as a placement failure, while other
  failures accumulate. The builder continues moving to the selected position.
- After 240 failed build-command frames, Stardust logs the failure, releases the
  builder, removes the no-go reservation, and removes that pending building from
  its queue. Construction start is detected separately by matching an owned,
  unfinished building's type and tile, then releasing the builder.
- Placement availability is a precomputed grid initialized from map walkability
  and BWAPI buildability, with adjacent margins and explicit base/resource
  reservations; catalogue candidates are not themselves evidence that the engine
  accepted a later build command.

This comparison supports improving failure observability and having a bounded
recovery path, but it does **not** explain Atlantis/OpenBW's current refusal:
Atlantis's latest probe saw `Game.canBuildHere=false` despite each footprint tile
being buildable. Stardust is native C++ and its error/command behavior cannot be
assumed equivalent to JBWAPI/OpenBW. No runtime code changed; verify whether the
JBWAPI surface exposes a comparable last-error reason before designing a port.

### ROOT CAUSE FOUND: the OpenBW `canBuildHere=false` is `checkExplored`

**Status 2026-10-10: solved by reading the engine source, no game run needed for the
diagnosis.** The unexplained `Unit.build(Pylon, tile) == false` on OpenBW is now
attributed to one specific precondition of `canBuildHere`, and it is the only one
the earlier probes did not measure. This corrects the "precise engine precondition
remains unidentified" line above.

**The exact call chain (all read from source, nothing inferred):**

1. `AUnitOrders.build` -> `BwapiOrderSink.build` -> `bwapi.Unit.build(type, tile)`.
2. `Unit.build` -> `UnitCommand.build` -> `issueCommand`.
3. `issueCommand` first calls `canIssueCommand` (`bwapi.Unit`, javap’d from
   `lib/JBWAPI-Rav.jar`) and **returns `false` when it is false** - so the
   client-side gate is what answers, not the engine directly.
4. `canIssueCommand` for `UnitCommandType.Build` -> `canBuild(type, tile, true,
   false, false)`.
5. `Unit.canBuild(..., checkCanBuildHere=true, ...)` ends with
   `game.canBuildHere(tile, type, this, /*checkExplored=*/true)` - note the
   **`true`**.
6. `bwapi.Game.canBuildHere` (same jar, bytecode read) delegates straight to
   OpenBW’s `Templates::canBuildHere`, whose tile loop is:
   ```cpp
   if ( !Broodwar->isBuildable(x, y) || ( checkExplored && !Broodwar->isExplored(x,y)) )
     return false;
   ```
   `.../bwapi/bwapi/Shared/Templates.h:186-192`.

So a Pylon is refused by the client whenever **any** of the 2x2 footprint tiles is
**explored==false**, independently of `isBuildable`, of units overlapping and of
minerals. Every earlier probe listed `isWalkable`, `isBuildable`,
`isBuildableIncludeBuildings`, occupancy and cost - **`isExplored` was never among
them**, which is exactly why the rejection looked impossible.

**Why `isExplored` alone explains each recorded symptom:**

- The probe that said "every footprint tile is buildable and empty yet
  `canBuildHere=false`" is consistent: `isBuildable` and `isExplored` are
  different engine queries, and only the second one gated the command.
- `MapTiles.tilesCoveredAreBuildable` (the OpenBW fallback) checks `isWalkable`
  and `isBuildableIncludeBuildings` but **not** `isExplored`
  (`src/atlantis/map/MapTiles.java:206-220`), so our own "would this stand here"
  answer says *yes* on a tile the engine's command path says *no* to. That is the
  two-sources-of-truth split `_AI/POSITION-FINDER.md` warns about, still present.
- `Game.canBuildHere(tile, type)` (the 2-arg overload, used by the diagnostic
  probes) defaults `checkExplored` to **false**, while `Unit.build` reaches the
  4-arg overload with `checkExplored=true`. Two engine answers to the same
  question differ by that one flag - so a probe built on the 2-arg overload can
  report a valid tile for a command that is in fact refused.
- A 2x2 footprint is four tiles; on an OpenBW run started with a single spawned
  Probe, the far corner of a marginal candidate can still be unexplored even when
  the near tiles are not. The catalogue/legacy planner accept such a candidate
  because they never ask `isExplored`.

**Verified against the measured run - the residual is settled.** The owner
confirmed that on this map everything within roughly 8 tiles of the base is
explored, so the question is not "is the base itself unexplored" but "does the
search wander outside the explored ring". It does, and the log says so:
main choke `[67,114]`, and the Pylon was assigned to tile **`[95,123]`** - about
23 tiles from the main, far beyond the explored radius - where worker `#118`
walked and `Unit.build` was rejected **190 times** while the map grid reported
`buildable:true`. The legacy finder's `nextPosition()` uses
`maxDistance = 37` (`PylonPosition.nextPosition`), expanding ring by ring, so
once the near rings are refused it commits to a tile the command can never
accept. That is the whole failure: a search with no explored-boundary condition
plus an oracle that never asked `isExplored`.

**Consequence, and what NOT to do:**

- This is **not** a map-data bug and **not** an `APositionFinder` bug: the engine
  is answering a question we never asked it correctly. Do **not** add a
  `isBuildable`/map fallback and do **not** patch `APositionFinder`
  (`_AI/PLACEMENT-CUTOVER-PLAN.md` "What must not happen" still stands).

**Implemented (this session):**

- `MapTiles.tilesCoveredAreBuildable` now requires `tile.isExplored()` for every
  covered tile, matching the engine’s per-tile loop - so our oracle and the command
  path agree, and a far/explored-less tile is no longer offered by the search.
- Regression test
  `OpenBWPlacementWithoutJbwebTest.anUnexploredFootprintIsNotPlaceableOnTheOpenBWPath`:
  fails with the old code (4/5) and passes now (5/5), so the guard is not vacuous.
- Verification: fast suite 346/346, acceptance class 5/5, ArchUnit 7/7, store
  unchanged. A bounded OpenBW run is still owed to close #47 point 2 (until then
  this is a fix verified by test, not by game).

**Still deliberately out of scope:** the `-7` offset in
`PylonPosition.nearToPositionForFirstPylon` (`base.translateTilesTowards(-7,
centerOfResources)` - 7 tiles *away* from the mineral line) is a gameplay-policy
constant, not a bug proven by this evidence: with the explored term in place the
search can no longer commit to an unexplored tile, so the offset only shifts which
*explored* tiles are tried. Changing it would be a doctrine change and needs the
owner, per CONVENTIONS §3.

### Bounded OpenBW run after the explored fix (2026-10-10)

Command: `PLACEMENT=catalogue PRODUCTION_V2=LIVE bash scripts/run-openbw-e2e.sh
"maps/cog/(3)TauCross1.1.scx" Protoss Zerg`, jar rebuilt and freshness-checked
first. The run attached and played; it ended on the outer `timeout` (the known
#48 path), so it is **not** a passing scenario - but its log answers the
placement question that mattered.

**The placement refusal is gone.** In `out/openbw/bot.log`:

- `Can't find place for Pylon` - **0 occurrences** (this was the constant
  failure on every earlier run).
- `ErRoR:b` and `ErRoR:NB` (the "build command rejected the tile" markers from
  `AUnitOrders.build`) - **0 and 0**.
- The Pylon appears at **distinct, increasing plan windows**: `557`, `750`,
  `1125`, `2224`, `3305` - the planner re-plans forward instead of jamming on
  one impossible tile.

**What still blocks a verdict is #48/M6, not placement.** The log shows the
Pylon window already behind the current frame and re-offered every frame:

```
@@3945 plan=2 [Probe@4395-4695 by#70 Pylon@3305-3755 ] issued: OK Pylon@3305 (builder committed)
```

The builder is assigned and holds the construction (`builder committed` every
frame, no error), but the item never becomes due - the same "startFrame in the
past -> permanently `isDue` -> re-offered forever" shape recorded in #48. It is
the arbitration/dispatch blocker, separate from the placement fix in #50.

**Two side facts measured, worth keeping:**

- `ENEMY_COUNT=0` (the "undisturbed economy" mode) **crashes the client** on this
  harness: `ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 2`
  at `bwapi.Game.init(Game.java:202)`, i.e. the engine has no enemy slot to map.
  It is not a placement signal - do not use it as the clean-economy run until
  that is fixed on the harness side.
- A run with 1 enemy can end in **Defeat at ~36 in-game seconds** (a lone Probe
  killed by lings), before any building was due; that is not a placement result
  either. The scenario needs the survival horizon from #47 point 5, not a short
  default.

### #48 root cause: the build order was never issued (2026-10-10)

The re-dispatch loop is **not** a dispatcher duplicate and **not** a stuck
builder. It is a control-flow deadlock in the legacy builder that the v2
dispatcher exposed, and it is measured, not inferred.

**Measured with a temporary probe on a live OpenBW run**
(`PLACEMENT=catalogue PRODUCTION_V2=LIVE`, Pylon at tile `[6,47]`), identical on
every sampled frame:

```
ISSUE_PROBE unit=Probe#76 pos=[6,47] tile=[6,47] dist=0.625
  engIncBuild=true engBuild=true explored=true occupied=false
  canAfford=true minerals=736 constructing=false lastCommandAgo=15
  lastActionGt20=false
```

Read it in order:

- the builder stands **on** its build tile (`dist=0.625`);
- the tile is valid by **every** measure - engine-terrain, engine-including-
  buildings, explored, not occupied (so the #50 explored fix is not the blocker
  here);
- minerals are ample; and
- **`lastActionGt20=false`, `lastCommandAgo` pinned at 15, forever**.

`lastActionGt20` is `unit.lastActionMoreThanAgo(20)` at
`IssueBuildOrder:89` - the guard that must be true before `unit.build(...)` is
called at line 101. It is **permanently false**, so the build command is never
issued at all. A second probe confirmed it: `BUILD_CALL` (inside
`AUnitOrders.build`) never printed once, while `ISSUE_PROBE` printed on schedule.

**Why it is permanently false:** `TravelToConstruct.travelWhenReady` returned
early at the top on

```java
if (asProtossMultiBuilderDoNotSwitchConstructions(builder)) return false;
```

which is `builder.lastActionLessThanAgo(20, Actions.MOVE_BUILD)`. The worker is
sitting on the site with the build never issued, so `TravelToConstruct` keeps
re-issuing MOVE_BUILD, which keeps the guard true, which keeps
`IssueBuildOrder` unreached - a closed loop. The construction therefore sits at
`NOT_STARTED`, `CancelTooLongConstructions` cancels it at the ~36 s timeout
(`Cancel constr of Pylon (Took too long) buildable:true`), the legacy
`AddToQueue.withHighPriority` re-requests it, v2 re-plans it next frame, and the
cycle repeats until the outer `timeout` kills the run. That is exactly the
observed "Pylon window slides +1/frame, `builder committed` every frame, game
never ends".

**Fixed:** the throttle is now consulted only when the builder is genuinely
still travelling, and only after the distance decision that already existed:

```java
boolean stillTravelling = isStillTravellingForTest(distanceToConstruction, minDistanceToIssueBuildOrder)
    && shouldMoveToConstruct(construction, distanceToConstruction, minDistanceToIssueBuildOrder);
if (stillTravelling && asProtossMultiBuilderDoNotSwitchConstructions(builder)) return false;
```

A builder at the site now falls through to `IssueBuildOrder`.

**Also fixed in the same pass:** `ProductionEngine.offerPendingConstructionsAgain`
used `construction.timeOrdered()` (the frame the request was *created*, often
hundreds of frames old) as the re-offered item's `startFrame`. That made the plan
show a window behind the present (`Pylon@2224-2674` while the game was at frame
2555), so `isDue` was permanently true and the plan could never show that the
construction was stuck rather than progressing. A pending construction is due
**now**; the item is now created at `frame`.

**Regression test:**
`TravelToConstructTest.aBuilderAlreadyOnItsTileIsNotThrottledByARecentMoveBuild`
pins the reordered distance decision; it fails (1/3) with the pre-fix behaviour
and passes (3/3) with the fix. Full fast suite: **347/347**.

### #48 second pass: the build command is now issued, but nothing is built (2026-10-10)

Fixing the `IssueBuildOrder` throttle (see above) **moved the failure one step
further** and produced the first `unit.build(...)` calls ever seen on this path.
Measured with a temporary probe (`BUILD_CALL`, removed after use):

```
BUILD_CALL unit=Probe#72 tile=[6,47] lastCommandAgo=1140 blockedByCommandDelay=false
BUILD_CALL unit=Probe#72 tile=[6,47] lastCommandAgo=0    blockedByCommandDelay=true
BUILD_CALL unit=Probe#74 tile=[6,47] lastCommandAgo=20   blockedByCommandDelay=false
BUILD_CALL unit=Probe#74 tile=[6,47] lastCommandAgo=0    blockedByCommandDelay=true
```

Three facts, all new:

1. **`unit.build(...)` is now reached** (4 calls). Before the guard fix it was
   never called at all - `BUILD_CALL` printed zero times across every earlier run.
   So the throttle fix is real and necessary.
2. **The command is still not effective.** `blockedByCommandDelay=false` means the
   call went past `AUnitOrders.build`'s own `lastCommandIssuedAgo() <= 1` gate and
   into `orderSink().build(...)`, yet no building appears on tile `[6,47]`.
3. **Two drivers are fighting over the same worker.** `lastCommandAgo` oscillates
   `1140 -> 0 -> 20 -> 0`: one driver issues a command every ~7 frames while the
   other waits for `lastActionMoreThanAgo(20, BUILD)`. The 20-frame guard therefore
   only opens intermittently, and `CancelTooLongConstructions` still fires at 36 s.

Also confirmed: the tile in this run is `[6,47]`, fully valid and affordable, and
the Pylon window now tracks the current frame (`startFrame = frame`, the
`offerPendingConstructionsAgain` fix) instead of sitting ~1000 frames behind -
so the plan display is honest now.

**Next step (not done):** find what issues a command to the assigned builder every
~7 frames and stop the double drive - the v2 dispatcher re-offering the tile while
the legacy `BuilderManager` also drives the same worker. Per CONVENTIONS §18 the
next diagnosis is the assigned worker's `managerLogs()` over the stall window plus
the `lastCommandIssued` writer, not another guess at the position logic.
