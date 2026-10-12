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


## Session 2026-10-12 (autonomous): own-algorithm cutover step 1 + short-tier honesty

### Committed
- `DeprecatedCodeException` (unchecked fail-fast for banned engine shortcuts) +
  `AUnit.hasPathTo` x2 routed through `ReachableTiles.between`, mirroring
  `APosition.hasPathTo`. Raw `u.hasPath` answered false for every pair on OpenBW;
  the port answers from terrain when regions are absent. No new ArchUnit
  violation (units -> map root is allowed); the one arch failure
  (`Manager -> SleepUntilFrameGate`) reproduces on the clean tree, so it
  pre-exists. Unit scope 389/389 green.
- `--short` honesty: `EXPECT_MIN_ASSIMILATORS=1` in the short tier and
  `Core>=1 (completed)` conjoined into `ForceExitWhenOpeningProven.applies()`,
  which promised "Core + Dragoon" but exited on Dragoons alone. A gas-less
  "proven opening" can no longer pass. Assertion self-test OK, unit 389/389.

### Investigated, not patched (refutations recorded in NEXT #62/#63)
- Assimilator: the request tile equals the engine geyser tile by construction
  (the -64/-32 offset is the centre-to-top-left conversion for 4x2); `d=0.0` is
  correct edge-distance; the JBWAPI terrain fallback errs towards reachable, so
  the builder-path passes. The refusal is elsewhere - the refusal-time probe
  (requestTile vs geyser tile equality + Java `canBuildHere` components) is
  named in #62 and needs one bounded run.
- Worker stall past ~16: unproven; candidates are #60 (gate oscillation) and
  #61 (flag self-deception). One bounded `--short` run with `BUILDER_LOG=1`
  (`STUCK_WORKER` + `WORKER_GATE`) decides it (#63).

### Next
Gas-refusal probe in `IssueBuildOrder` (gas-only, throttled), then a bounded
`--short` OpenBW run reading GAS tile-equality + `STUCK_WORKER`/`WORKER_GATE`.

## Session 2026-10-12 (continued): StackOverflowError in the Pylon fallback (NEXT #64)

Owner's log tail showed the bot dying in mutual recursion
`findStandardPositionFor:189` <-> `findWithRelaxedBoundaryMargin:206`. Root cause,
fully in hand: the relaxed-margin second pass re-entered the same method, whose
Pylon-fallback branch had no re-entry guard - any sweep failing twice (even the
relaxed one) recursed to overflow instead of returning null. Fix is one condition
(`&& !relaxedBoundaryMargin`); regression `PylonFallbackNoRecursionTest` fails
with `StackOverflowError` on the old code and passes on the new, flag reset
included. Closes #64. Next: read the STUCK_WORKER engine answers from the
15:53 run for #63.

## Session 2026-10-12 (continued): stuck workers root cause (NEXT #63)

The engine-answers probe named it: stuck Probe `workerCompleted=false` ->
`workerCanCommand=false` (JBWAPI `canCommand` false until completion), both
orders refused, `order=Nothing`, while our action flag claimed gathering.
Fix committed: both mining managers decline `!isCompleted()`
(`UncompletedWorkerIsLeftAloneTest` mutation-proven). The 10-frame probe
false-positives on real miners (`MiningMinerals`, accepted Gather, on the patch)
- probe issue, noted in #63. Still to verify in a game: minerals from minute one,
zero `NO_COMMAND` stalls; then close #63. `FreeWorkers`/builder exposure noted
as the follow-up check.

## Session 2026-10-12 (continued): same root in the builder path (NEXT #63)

Verified run showed Gateway=2/Zealot=5 (was 0/0) but still 131 NO_COMMAND lines:
all now `DoNothing` or `BuilderManager` on `workerCompleted=false` units - the
gather churn is gone (no small-ago retries, `ago` grows), confirmed by grep.
Measured #118: assigned builder uncompleted, never stuck, holding a Gateway site.
Fixed in cycle: `FreeWorkers.get()` is `onlyCompleted()` (25 consumers all pick
units to command something with), `BuilderManager.shouldReplaceBuilder` adds
`!isCompleted()` to the `isStuck` rule (existing `assignOptimalBuilder` heals).
Tests: `BuilderManagerShouldReplaceTest` 2/2, FreeWorkers case in the acceptance
test (fails pre-fix); unit scope 391/391. Game verification of #63 still open:
minerals from minute one, zero churning stalls.

## Session 2026-10-12 (continued): Closes #63 - verified in game

Final bounded `--short` run: zero churning stalls (no small-ago `NO_COMMAND` with a
claiming manager - every remaining such line is `DoNothing` on a genuinely
uncompleted unit, `ago` growing), mining recovers from the 0-mineral start
(`Pylon=3`, 22 Probes), Gateway/Zealot production resumes. Mechanism + withdrawn
readings archived in `CASE-STUDIES.md` (#63), engine-side note (fifth field:
`isCompleted` at start) in `CHALLENGES/OpenBW.md`. Residual early-minerals window
is engine attach latency, waited out - not churned. Gateway variance between runs
(2 vs 0, same far site [14,51], moving-refusal) stays with #59.

## Session 2026-10-12 (continued): CherryVis per-unit first-seen (NEXT #65)

Owner's report: whole-bot CherryVis worked, per-unit never did. Compared our
`trace.json` writer against Stardust's `Instrumentation/CherryVis.cpp` and the
documented format (zstd JSON trace; Stardust keeps per-unit LOG FILES + path
references, we inline everything). Found and fixed: `ACherryVis_UnitFirstSeen`
indexed its frame-keyed map by loop counter (NPE on any real game, killing the
whole trace write) and emitted one object per frame (same-frame discoveries
lost). Rewritten frame-to-list with id dedup; dead `...FirstSeenOld` deleted.
`CherryVisUnitFirstSeenTest` pins the exact JSON with no frontend running.
Unit scope 393/393. Still open in #65: empty `units_logs` and the
runtime-vs-hardcoded manager-id table behind `custom_names`.

## Session 2026-10-12 (continued): CherryVis legend follows runtime (NEXT #65)

`custom_names.manager` hardcoded one manager order while ids assign at runtime -
fixed to build the legend from the runtime strings table
(`CherryVisCustomNamesTest`). Unit scope 394/394. Left in #65: empty
`units_logs` (Stardust writes per-unit log files; we write none) and an
end-to-end viewer check, plus the per-line debug flags the owner suggested.

## Session 2026-10-12 (continued): CherryVis per-unit logs (NEXT #65)

`units_logs` now renders per-unit free-text lines (`CV.unitLog`, stored per unit
id, inline legacy shape, `CherryVisUnitLogsTest` pins the string). First
producers: build issued/refused in `IssueBuildOrder` behind const-false
`DebugFlags.CHERRYVIS_BUILDER_ACTIONS` - zero cost when off, per-line opt-in as
requested. Unit scope 395/395. Left in #65: an end-to-end viewer check of a real
trace.json (needs the CherryVis frontend + a game with the flag on).

## Session 2026-10-12 (continued): CherryVis E2E --cvis (NEXT #65)

Owner found the load-bearing defect: `zstd-jni` was never packaged, so `onGameEnd`
died with `NoClassDefFoundError` and the game-end report died with it - no run
could ever leave a trace. Fixed in three places: lib packaged (build asserts it),
writer path guarded in `ACherryVis.finish()`, plain JSON to `trace.json` with the
zstd copy as `trace.json.zst` (plus the backslash-in-filename Linux bug in the
summary writer). Runner: `--cvis` sets ENV `GENERATE_CHERRYVIS_REPLAY`, collects
into the run dir and `<name>.rep.cvis/` next to `--save-replay` output (default
`/sc-ai/REPLAYS`), fails a clean run with no trace; `--self-test-cvis` pins the
checks with no game. Verified: both self-tests green, unit 395/395. A proving
run (clean exit -> real trace.json) still pending.

## Session 2026-10-12 (continued): CherryVis first-seen seed (NEXT #65)

First proving run wrote a valid trace.json (all 14 keys) but `units_first_seen`
was empty with 16 logged units: our listener only recorded `UNIT_DISCOVERED`,
which never fires for our own units. Fixed Stardust-style: record at creation
(`OnUnitCreated`) + seed sweep of ours and minerals at game start
(`OnGameStarted`); dedup by id. Test `CherryVisSeedCurrentUnitsTest`. Unit
395/395, arch unchanged (same single pre-existing failure). Left in #65: the
viewer end-to-end check the owner runs locally (install notes below).

## Session 2026-10-12 (continued): CherryVis E2E proven in game (NEXT #65)

Clean 5 s `--short --cvis` run exits 0 with `CVIS OK`: trace.json parses, 22
first-seen frames (107 units seeded at frame 0), 21 units with manager updates.
Install notes for the local viewer added to `PLAN-OPENBW.md` (`--cvis` section:
django venv, emscripten, `server.py`, single-file vs partitioned caveat).
Left in #65: the owner opening a real trace in the viewer (needs the frontend),
and wider per-unit call-site coverage behind `CHERRYVIS_BUILDER_ACTIONS`.

## Session 2026-10-12 (continued): viewer 500 + --run-only + viewable pair (NEXT #65)

Owner installed the viewer; every page returned HTTP 500 - root cause
`import zstd` in `replays/views.py` while only `zstandard` was installed
(different packages). `install_cherryvis.sh` installs both now and gained
`--run-only` (prints "Starting CherryVis server without installation..." and
execs the server, failing fast with install hints when parts are missing).
Verified HTTP 200. Proving run `--short --cvis --save-replay` leaves
`/sc-ai/REPLAYS/<name>.rep` (39 KB) + `<name>.rep.cvis/` (valid trace.json, 23
first-seen frames, 16 updated units) - the file pair the editor opens. Left in
#65: the owner clicking through the viewer.
