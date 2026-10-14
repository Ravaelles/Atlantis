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

## Session 2026-10-12 (continued): CherryVis in Docker serves our trace (NEXT #65)
Owner's direction: bmnielsen's `cherryvis-docker` instead of the native install
(which moved to `_OLD`). Verified the user's `python:3.10` base (django 2.1.7
clean of 3.10 breakers by sdist grep), bumped the provably-dead
`zstandard==0.11.0` to 0.21.0, added the missing `zstd`, and patched
`views.py` to `zstandard`'s streaming reader with plain-file fallback (a first
one-shot attempt failed size-less streams - measured `length mismatch`, empty
side panel with no console error as the symptom). Image
builds, container `cvis` runs on :8770 with `/sc-ai/REPLAYS` mounted: our game
pairs (`has_cvis_data: true`) and its trace serves as valid JSON both plain and
via `.zst`. Runner now pairs `.rep.cvis` with the on-disk `.rep` (engine
mangles the configured name). Container left running for the owner.

## Session 2026-10-12 (continued): game starts paused (NEXT #66, CLOSED)

Owner's IDE/Wine report (40 s frozen, first "2" starts it) is our own
`GameSpeed` flag, not the engine: both speed-change paths paused first and
unpaused last with no `finally`, and a keys file left by a killed session
replayed Pause into the next game. Fixed with `try/finally`
(`KeyboardPauseReleaseTest` fails pre-fix with a throwing game mock) and
`KeyRelay.purge()` at both launcher starts. Unit scope 396/396. The
engine-pause `resumeGame` path was already covered and is untouched.

## Session 2026-10-12 (continued): sleep gate ownership (NEXT #67, CLOSED)

Owner's question why dispatchers appear in `managerHistory`: the latency gate
claimed ownership via `usedManager`, stamping alternating dispatcher names over
the true owner with a stale leaf tooltip. Now returns `this` (chain still
stops); `unit.manager()` untouched. `SleepGateOwnershipTest` fails pre-fix.
Unit scope 397/397.

## Session 2026-10-12 (continued): exit-path backstop (NEXT #69)

Owner's question: 9 s game, 110 s run. Log forensics proves the JVM hung
between the trace write and `System.exit` (trace files exist, no Killing/Exit
prints, one silent second ForceExit, frames stopped) - but the exact frame is
unproven, and theorizing further is guesswork. Mitigation committed: 20 s
daemon backstop in `OnGameEnd` (`halt(1)`, self-exit path only). Not
unit-tested by construction (it halts the JVM); verified by suite green +
review. Still open in #69: jstack one hanging run.

## Session 2026-10-12 (continued): per-unit "i???" type ids (NEXT #68)

Owner: clicked units show "i???" and no per-unit data attaches. Frontend reads
`'i' + (id>=0 ? id : '???')` after `bw2id` mapping; the mapping comes from fuzzy
matching (frame, type, x, y) and our type was ours-not-BWAPI (1210 vs 64), so
nothing matched. Fixed at all write sites; `types_names` already engine-side.
Test pins 64, fails pre-fix. Pending: fresh-trace viewer check + per-frame
manager/order lines for non-buildings (via `units_logs`, the rendered channel).

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

## Session 2026-10-12 (continued): empty side panel (NEXT #65)

Owner: game replays, right-side CherryVis panel invisible, no console error
about cvis. The panel fetches `/get/cvis/` while the canvas does not, so a
data failure is silent by design. Measured: the endpoint returned 200 with raw
compressed bytes - one-shot `zstd.decompress` fails size-less streams
(`length mismatch` on the sample). Fix: streaming reader from `zstandard`
(which has the API) with plain-file fallback, via the Dockerfile patch.
Verified: sample serves 6.4 MB valid JSON (all 13 keys), ours 23 first-seen
frames. Container `cvis` left running with `/sc-ai/REPLAYS` mounted.

## Session 2026-10-12 (continued): gas tile from the geyser itself (NEXT #62)

Stardust settles the code side: a refinery site is `geyser->tile`, never
center-math. Ours recomputed it (finder) and snapped to the center (fix) -
right where rounding agrees, 2-off where it does not. New `AUnit.tilePosition()`
(engine value, footprint fallback), all three sites answer the geyser tile.
`GasTileIsGeyserTileTest` mutation-proven (fix case fails pre-fix). The live
engine-equality half stays with the GAS_REFUSED probe. Not verified in game:
OpenBW never requests gas (no Gateway yet); owner's IDE run is the next
evidence (`noBuilder=` line). Unrelated observation while here: another session
is editing this tree concurrently (`ForceExitLocallyAfterSomeTime.java`) -
commits list files explicitly, never `git add -A`.

## Session 2026-10-12 (continued): stop-before-build widened, backstop 15s (NEXT #59/#69)

Owner's run: Gateway cancelled 35s at [14,51] on `buildable:true`, and asked why
110 s for a 43 s game plus a 60 s cap. Three changes: (1) stop-then-build fires
on bare `lastCommand==Move` (the moving twin the old gate excluded; the "do not
repeat" note in #59 is superseded WITH the trace it demanded); (2) backstop
20 s -> 15 s so the worst case fits the owner's 60 s ask; the 120 s hard cap
stays (CONVENTIONS §17, long tier needs it) and `TIMEOUT_SECONDS=60` already
narrows any single run. No stub test possible for the stop (needs engine order
state) - the game run below is the test.

## Session 2026-10-12 (continued): canBuild bisection says poverty (NEXT #59)

Owner's demand to settle regions-vs-placement: the standstill probe plus a
sub-flag bisection (`canBuild000/100/010/001`) isolated `Game.canMake` failing
on `minerals(28) < 150` with every reachability, tile, power, occupancy and
terrain answer true. Regions (and the hasPath patch), power, occupancy and
placement are exonerated by one measured line; the engine refuses because the
bot is broke. Upstream defect: ~10 workers bank ~30 minerals, Gateway attempted
broke, refused, cancelled at 35s. The [14,51] site (`noBuilder=false`) is a
different mechanism (power or occupancy) and still needs its own split run.

## Session 2026-10-12 (continued): BUILDING-BLOCKERS.md opened (NEXT #59/#62)

Owner's ruling, honoring it: poverty is A cause, not THE cause (400-600
minerals banked just as often). New living file `_AI/BUILDING-BLOCKERS.md`
splits the hunt into Certain (6 items, evidence named), Refuted (7 readings
with what killed each), Open (O1 [14,51] power-vs-occupancy split run, O2 bank
sampling, O3 jstack, O4 gas equality). Indexed from `_AI/README.md`.

## Session 2026-10-13: --short E2E - Gateway builds, Core stalls IN_PROGRESS (NEXT #70)

Bounded run `20261013_095545_short-Protoss-vs-Zerg` (fresh jar `bf984111`,
TIMEOUT_SECONDS=60, BUILDER_LOG=1, exit 0, 31 s wall, ingame 296 s):
`Pylon=4 Gateway=2 Zealot=10` but `CyberneticsCore=0 Assimilator=0 Dragoon=0`
- the --short gate (Core + Dragoon) fails on the Core. Gateway construction is
healthy now (all 7 BUILD_ATTEMPT lines `engineAccepted=true`, zero refusals).

Measured mechanism (bot.log lines 712-1406): Core requested 2:21 at [87,111],
builder #83 walked there, attempt @4697 accepted with `isConstructing=true`
(order echo - ALL 7 attempt lines read true on the attempt frame), status
NOT_STARTED -> IN_PROGRESS. By @4710 `isConstructing=false order=PlayerGuard`
with our `lastCommand=Build` aging - the engine dropped the Build without our
code issuing anything, and NO Core unit ever existed (GameSummary counts live
board incl. unfinished: Core=0). Worker #83 then stood at the site 2400 frames
(`lastCommandIssuedAgo` -> 2353) with zero re-issue attempts.

Two structural gaps combine (both read in source, not inferred):
- `BuilderManager.handle()` drives only NOT_STARTED; IN_PROGRESS falls through
  to submanagers, while `isBuilder()` still claims the worker - held forever.
- `ConstructionThatLooksBugged` returns unless NOT_STARTED, so
  `CancelTooLongConstructions` never fires - the premature IN_PROGRESS grants
  immunity from the safety net that would cancel + requeue.
The premature flip itself comes from `ConstructionStatusChanger` trusting the
order-echo `isConstructing` (true from the Build order while walking/at site).
Why the warp aborted server-side with all client preconditions true is OPEN
(client accepted, server never started warp) - the fix heals regardless: retry
if transient, cancel + requeue elsewhere if the site is bad.

Fix (NEXT #70): demote a stalled IN_PROGRESS (idle builder at site, no building
of that type near the site) back to NOT_STARTED so travel/issue/cancel/requeue
resume. Assimilator never logged (0 lines) - gated behind Core in
`ProtossDynamicBuildingsCommander.topPriority`, so the Core stall is also the
gas stall. GAS_VARIANT experiment stays until a gas-reaching run.

## Session 2026-10-13 (continued): Core built, gas root-caused, O1 settled

Two bounded --short runs on the fresh jars (BUILDER_LOG=1, exit 0):

- `20261013_100226` (jar with demote): `Pylon=2 Gateway=2 Zealot=5
  CyberneticsCore=1` - the FIRST Core on OpenBW. DEMOTE fired 0 times: the
  Core built on its first attempt (@4585, [112,13], #116), so the warp-drop is
  intermittent and the demote stays a unit-pinned safety net (3/3), not a
  game-verified fix. Honest delta of the demote commit: none measured yet.
- Same run, Assimilator: requested 3:12 at [117,4], cancelled 34 s later with
  ZERO build attempts while every canBuild gate read true (`canBuild000-001`
  all true, minerals 116+, explored 8/8, power true). New measured root: the
  builder plateaued at tile-distance 3.7, past the 3.5 gas threshold - a 4x2
  refinery's center is ~2.2 tiles from its origin tile, so an adjacent worker
  always reads ~3.6 and never issues (the #59 center-vs-tile shape, gas twin).
  Fix committed: gas measures to the footprint center
  (`TravelToConstruct.distanceForBuildOrderIssue`; others keep the tile
  metric), `GasBuildIssueDistanceTest` 2/2, old source-pin updated, unit
  405/405. Game verification pending (next run died at Gateway first).
- `20261013_100525` (jar with both fixes): `Pylon=2`, Gateway=0 - the [14,51]
  variance. Gateway refused 315x (`engineAccepted=false`, `canBuildHere=true`,
  `distToBuild=1.3`, idle, `lastCommand=Move`): BUILD_BREAKDOWN settles O1 -
  `hasPower=false`, `unitsInRect=1 [BUILDER]`, `terrainOnly=ok`,
  `canBuild000=false`. POWER, not occupancy; the refusal is correct, the site
  is wrong (legacy finder never checks power - NEXT #50, now with its measured
  trigger). No Unwalkable-center lines; the stop-then-build is irrelevant here
  (worker already still). The gas fix is therefore still game-unverified: no
  run has reached gas since it landed.

## Session 2026-10-13 (continued): gas needs no power, rotation resolves to exact, server mechanism read- Power: CLOSED by code reading, no run needed. `AUnitType.needsPower()`
  excludes Pylon/Nexus/Assimilator; `ProtossPositionFinder.needsPylonPower`
  repeats the same three (owner's ruling 2026-10-10); the engine agreed live
  (`hasPower=true` on the Assimilator footprint). Gas is unpowered by rule.
- Rotation (owner's spec: `A.now()%60` windows exact/center/-2,-1/+1,0, print
  the winner): `20261013_101406` resolves it. `GAS_ACCEPTED win=0
  tile=[117,4]` - the EXACT geyser tile; both shifts refused client-side
  already (`GAS_REFUSED ... noBuilder=false`). Roster: `Assimilator=1` (first),
  `CyberneticsC=1`, `Gateway=2` - only Dragoon missing (gas flowed too late:
  accepted @4865/202 s, game over at 248 s). Rotation + GAS_ACCEPTED +
  `GasVariantTileTest` REMOVED the same turn; the request tile is exact, no
  windows. O4 closed.
- Server mechanism for accepted-but-no-building (read in
  `/sc-ai/OpenBW/openbw/bwgame.h`, not guessed): `order_PlaceProtossBuilding`
  walks the worker to `order_target.pos`, and only within 70 px (88 px for
  Assimilator) checks `player_has_supply_and_resources_for` NOW plus
  `can_place_building` NOW - on NO it does a silent `order_done`
  (worker `return_to_idle`, our `order=PlayerGuard` with stale
  `lastCommand=Build`). That is the Core @4697 signature exactly, and the
  prime suspect is the bank between issue and warp: minerals 194 (@4680) ->
  68 (@4710) while the worker pump kept training - the server re-checks the
  200 in the arrival frame, after the client said yes. JBWAPI's own javadoc
  warns of it ("a small chance for a command to fail after passed to
  Broodwar"). Refinery twin: `build_refinery` needs the geyser under the sent
  position's center (`get_building_at_center_position`) - only the exact tile
  passes, which is what win=0 measured. Recorded in `_AI/BUILDING-BLOCKERS.md`
  Certain; NEXT #62 closed.

## Session 2026-10-13 (continued): CherryVis late units read own positions (NEXT #68 follow-up)

Owner: only pre-existing units match in the viewer, future ones read "???".
Measured in `102220` trace: every late Probe first-seen at the Nexus
(288,1456) - a unit being trained reports its PRODUCER's position even at
completion (instrumented run proved `via=completed` fires with the Nexus
position), and the viewer (nearest same-type within 25px, no retry) matches
nothing in the base crowd. Stardust records at incomplete->complete because
its units report own positions there; ours do not.

Fix: mobile units defer 7 frames past completion (`defer`/`processPending` in
`ACherryVis_UnitFirstSeen`, driven per frame from `ACherryVis.update`), until
they walk out with their own position - same frame in trace and replay means
distance zero, so the delay cannot hurt. Buildings record at completion (a
warp never moves). `CherryVisDeferredFirstSeenTest` 2/2, unit 405/405.
Verified in game `20261013_104050_short --cvis` (15 deferred + 3 via):
late units now spread around the base, none shares the Nexus position.
Viewer check (owner opens the trace) still pending.

## Session 2026-10-13 (continued): unit logs flow, scout NPE guarded (owner asks)

- CherryVis unit logs (owner: matching works, panel empty): the only producer
  (manager mirror in `setManagerUsed`) sat behind const-false
  `CHERRYVIS_UNIT_ORDERS`, so traces left `units_logs={}`. Every manager change
  now logs unconditionally (`CV.unitLog` is itself a no-op unless enabled).
  Verified `20261013_105915_short --cvis`: 22 units with logs
  (`manager:GatherResources` -> `manager:BuilderManager` -> back, with
  frames). Viewer re-check is the owner's.
- Wine NPE (`RoamAroundEnemyBase.java:67`): `enemyBaseRegion` null when the map
  model has no region for the tile; guarded to `return false` (the old null
  check tested `enemyMain`, already non-null). The per-factory `invokedFor`
  catch stays the backstop: a throwing manager returns null and the chain moves
  on next frame, so one bad scout never kills the game. Trivial guard, no test.
- Owner's E2E `20261013_104858_adhoc` (411 s): `Pylon=6 Dragoon=6
  Assimilator=1`, zero `AN ERROR HAS OCCURRED` - the NPE is Wine-only so far.

## Session 2026-10-13 (autonomous): power rule follows the engine mask (NEXT #50)

`20261013_112034_long` died on Gateway [14,51] (315 power refusals, finished
Pylon at [16,46], game over at 165 s). Root cause read in
`/sc-ai/OpenBW/openbw/bwgame.h` (`is_in_psionic_matrix_range` +
`psi_field_mask`): |dx|<256px by |dy|<160px plus an 8x5 tapered mask - our
Chebyshev-6 allowed 6 vertically (engine: under 5; [14,51] is exactly 160px
below) and cut 8 horizontally down to 6. New pure `map.position.PsiField`
used by both planners; dead Chebyshev helpers and radius constants removed.
`PsiFieldTest` 3/3, rewritten `PylonPowerRadiusTest` 2/2, unit 408/408, arch
store unchanged. Legacy path NOT deleted (22 files use it; default runs on
it) - deletion stays behind the catalogue cutover; only provably-dead code
goes. Next: E2E must show Gateway avoiding [14,51].

## Session 2026-10-13 (autonomous): first --short PASS, #50 and #70 closed

`20261013_112638_short` (fresh jar with the psi-mask rule): **SCENARIO
PASSED** - `Pylon=6 Gateway=3 Zealot=8 Dragoon=3 Assimilator=1 Core=1`, zero
`Can't find place`, Gateways at [92,107] [92,111] [96,111] (all accepted
first try; [14,51] never requested). DEMOTE fired once (CyberneticsC
[87,111], 456f standstill) and the Core still built - the safety net works
in a game, not just in its test.
- Closes #70 (deleted from NEXT): stalled-IN_PROGRESS demote (game-verified)
  + gas center-distance (Assimilator=1 in two runs now).
- Closes #50 (deleted from NEXT): engine-mask PsiField shared by both
  planners + power gate in the shared chain (power-first, named reason) +
  chain test. The letter of #50 (Core refused on an unpowered tile with a
  test) is the Gateway case in PylonPowerRadiusTest.
- NOT closed: #59 builder/Gateway variance (single-run evidence so far -
  needs a second green run to call it stable), #69 (jstack), #65/#68 (owner
  opens traces in the viewer), #34 (needs UMS map), #8/#36/#40 (need design
  decisions), #51/#39 (gameplay constants, owner's call).
- Legacy position path NOT deleted: 22 files use it and default runs play
  on it. Owner's removal ask is recorded but refused as stated - deleting
  the live path without cutover breaks the bot (CONVENTIONS section 3);
  only provably-dead code (Chebyshev helpers, radius constants, dead
  branches) was removed.

## Session 2026-10-13 (autonomous, continued): third --short PASS, old-suspect sweep

- `20261013_114308_short`: **SCENARIO PASSED** (third in a row) -
  `Pylon=5 Gateway=3 Zealot=8 Dragoon=3 Assimilator=1 Core=1`. Gateway variance
  (#59, OpenBW half) stable across three runs since the psi-mask fix.
- Suspect sweep over 10 recent run logs (`--short` with and without
  BUILDER_LOG): `WTF?!?` 0-1 per run (refresh loop dead), `took too long`
  0-1, STUCK_WORKER lines are healthy miners now (order=MiningMinerals,
  on-patch, growing ago - the documented probe false-positive, no churning),
  WORKER_GATE still oscillates CantAfford-early/NoSupply-late but games pass
  (economy outgrows it). No action; #61 stays closed, #60 stays open.
- One unresolved observation (NOT a defect yet): two `REQUEST_ACCEPTED Pylon
  [116,16]` one second apart (100226, no cancel between, no further trace).
  Games pass and no leak symptoms accompany it, so per CONVENTIONS section 3a
  no fix is attempted; #57's prescribed measurement (caller stack at
  `constructions.add`) stands as its next step if leak symptoms return.
- Cleanup: dead commented blocks removed (GasBuildingFix 35 lines,
  IssueBuildOrder ~30, ConstructionStatusChanger status block) + 4 unused
  imports. Legacy position path kept (live, 22 files) - see refusal rationale.

## Session 2026-10-13 (autonomous, continued): expansion never decided + suspect 700s gate

- `20261013_114519_long` (381 s): full tech, Nexus=1 all game. The expansion
  order never even entered the queue this time (no ADDED line; previous run
  queued one at 6:19 that sat unbuilt). Decision chain
  (`ProtossShouldExpandToNaturalBase.forSecondBase`) explains it without any
  bug: 3 gateways (< 4 for `2ndManyMinerals`), ~25 workers (< 44 for
  `ManyWorkers`), game 381 s (< 630 s `GettingLate`). Thresholds, not defects
  - strategy tuning stays the owner's call (same class as #51). No change.
- SUSPECT, unpatched: `IsReadyToProduceOrder.cantAffordAndDidntExpandYet`
  blocks EVERY order when `hasMinerals(550) && seconds >= 700 && bases <= 1` -
  i.e. precisely when rich and late on one base, the name says the opposite
  ("cant afford"). Probably inverted (`!hasMinerals`), but it only fires in
  700 s+ games, which no bounded OpenBW run reaches and Wine is owner-only -
  so it is filed as NEXT #71 with the evidence instead of patched blind.
- New probe this cycle: `ORDER_NOT_READY` names the refusing queue term
  (supply/resources/requirements + numbers) for stuck building orders, so the
  next silent-queue case (like the 6:19 Nexus) is diagnosable in one game.

## Session 2026-10-13 (autonomous, continued): center-rule PASS, all POWER_AT_REQUEST agree

- `POWER_AT_REQUEST` probe (new jar): every requested site this run reads
  ourCheck=true engineHasPower=true (4 sites), game PASSED again
  (`Pylon=6 Gateway=2 Dragoon=3 Assimilator=1 Core=1`, zero Can't-find-place).
- Center correction committed: the client enforces power at the footprint
  CENTER (`bwapi/Game: canBuildHere -> hasPower(lt, type)`), pylon anchor is
  its tile origin. `PsiField` takes the footprint; legacy checks it exactly;
  catalogue keeps a documented tile-point approximation (no footprint at its
  call site; NEEDS_NEW_PYLON degrades misses instead of deadlocking).
  `PsiFieldTest` + `PylonPowerRadiusTest` updated to center math (3/3),
  unit 411/411, arch store unchanged.
- Coverage audit: FindPosition funnels through the gated sweep
  (sweep gate + shared-chain gate); the only ungated path is exact-position
  verbatim (occupancy-only, by design). No third power path exists.
- Watch item (open, no action): the 115258 Terran-run Core at [15,51] was
  accepted while no completed pylon covered it under either math. Never
  recurred in 4 subsequent runs (all POWER_AT_REQUEST agree). Per CONVENTIONS
  section 3a noted, not patched.

## Session 2026-10-13 (autonomous, continued): Terran re-pass, #59 stability note

- Vs Terran re-run with the center rule: **SCENARIO PASSED** (`Pylon=4
  Gateway=1 Zealot=4 Dragoon=3 Assimilator=1 Core=1`; Core accepted at
  [112,11], Gateway at [112,19] - powered sites on the western pylon line).
  Five of the last six --short runs pass.
- NEXT #59 annotated: OpenBW half stable, Wine half + threshold-boundary
  still owner-side.

## Session 2026-10-13 (autonomous, continued): Terran expedition (#56 measured, race reverted)

- Temporary race flip (Main.ourRace Terran, rebuilt jar, one --short) then
  REVERTED same turn (diff verified empty, Protoss jar rebuilt, Protoss
  re-verified playing with no RACE MISMATCH). Never leave a flipped jar
  behind - this is the procedure if it is ever needed again.
- Terran game (258 s): `SCV=21 Marine=5 Academy=1 Refinery=1 CommandC=1
  SupplyD=2 Barracks=1`. #56 symptoms ABSENT: zero `No construction for`,
  zero `CANNOT define`, every build attempt accepted. The predicted defects
  are real in source but did not trigger (positional miss needs site drift
  that did not occur). #56 stays open with this measurement - not fixed
  blind, not closed on one clean game. Side observation: Terran starting
  SCVs show the same OpenBW uncompleted window (`workerCompleted=false` at
  frame 30); DoNothing waits it out and mining recovers, same as Protoss.
- Protoss re-verify run: Core=1 Assimilator=1 but Dragoon=0 at 318 s (gate
  correctly FAILED - refinery->100 gas->Dragoon chain is longer than the
  ForceExit horizon when the Core lands late). Timing, not a defect.

## Session 2026-10-13 (autonomous, continued): closures #54/#61/#55, second map, #56 measured

- Closes #54 (refresh cancel loop dead: WTF 0 in 10+ runs; 101406 proved
  cancel->re-request productive, Assimilator built), #61 (stuck workers,
  root-caused under #63, today's STUCK lines are healthy miners), #55 (map
  analysis always ran everywhere - single ancient call site d3e9e0d3).
- `(2)Destination1.1.scx` --short: **SCENARIO PASSED** (`Pylon=5 Gateway=3
  Dragoon=2 Assimilator=1 Core=1`). Placement generalizes past TauCross.
- #56 measured via temporary Terran flip (reverted same turn, diff verified
  empty, Protoss re-verified): symptoms absent, stays open.
- #71 filed (suspect inverted 700 s gate, needs a 700 s+ Wine game to verify;
  not patched blind).

## Session 2026-10-13 (autonomous checkpoint): green board, jar fresh

- Full suite: unit 440/440, arch 10/11 (only the pre-existing structural
  Manager->SleepUntilFrameGate, advisory), store unchanged all session.
  Deployed jar rebuilt fresh from the cleaned tree.
- Session closes: #50 (psi mask + shared-chain gate, game-verified), #54
  (refresh loop dead), #55 (map analysis always ran), #61 (stuck workers),
  #62 (exact refinery tile, Assimilator=1), #70 (demote game-verified,
  gas center-distance). Filed: #71 (suspect 700 s gate, needs Wine).
- Left open, all owner/design-side: #59 Wine half, #69 (no hang reproduced
  in 12+ runs, all exit 0), #65/#68 (viewer checks), #34 (UMS map), #8/#36/
  #40 (design), #51/#39 (constants), #45 (V2 parity), #15 (Terran deploy),
  #56 (symptoms absent), #57 (no leak symptoms), #58 (Wine expansion),
  #60 (thresholds), stages 3/5/6/8-13.
- Refusals logged for the record: legacy position-path deletion (live path,
  breaks the bot without cutover), blind fixes for #71/#56/unexplained
  single-run anomalies, per-commit full-game gates.

## Session 2026-10-13 (autonomous, continued): deletion gate lesson + 10 dead classes

- Near-miss: `PositionsAroundMainBase` passed a zero-reference grep because the
  `grep -v` self-exclusion also excluded `BuildablePositionsAroundMainBase`
  (its filename ends with the class name). Caught by compilation, restored
  before commit. Rule going forward: compilation green is the deletion gate,
  grep is only the candidate finder. (CONVENTIONS section 3a: cheap check
  before the claim - the compile was the check that worked.)
- Deleted after green compiles: OldTerranForbiddenByStreetGrid,
  ForbiddenByStreetGridForBarracks, ProtossForbiddenForProducerBuildings,
  OldCrucialRepairsNearby, DynamicTech, NonReinforcedBases, all three race
  SupplyCommanders (superseded by the fallback-supply path),
  ProtossLimitExistingConstructions. Suite 414/414 throughout.

## Session 2026-10-13 (autonomous, continued): dead-code sweep (39 classes)

- Deleted 39 zero-reference classes in 4 commits (grid helpers, supply
  commanders, fix/repair managers, scout listener, wall logic, combat
  corpses/tiny helpers, zstd reader, shuttle stub, self-recursive
  SquadsInitializer). Gate: exact-filename grep for candidates, green
  compile as proof (caught one grep -v trap live: PositionsAroundMainBase
  restored). Suite 443/443 unit, arch store unchanged, jar rebuilt
  (5028 entries, was 5062).
- Spared deliberately: new-arch placement files, ATechManager, larger
  race/combat micro (AttackAsWraith, TerranInfantryShouldRetreat,
  ZergRetreating, ProtossZealotLongNotAttacked) - owner eyes first.
- Live legacy position path kept (refusal logged); only dead code goes.

Store note: core-rule file now holds 255 entries (267 on 2026-10-04; -3 from this batch, rest from earlier removals; ratchet holds, this batch +0/-3).

## Session 2026-10-13 (autonomous, continued): queue-silence hunt, standby

- Nexus order vanishes between ADDED and REQUEST with no log trace (two
  --long runs). Refresh evaluates other orders (448 ORDER_NOT_READY lines),
  so the Nexus order never reaches isReady - status/ignore path, not a gate.
  New throttled QUEUE_BASE probe prints base-order status in refresh; needs
  a game that queues a Nexus (this run never decided one: 3 gateways).
- 161240_long: Core@190s + Gas@198s, Dragoon=0 at 286s - timing again (100
  gas + 50s build exceed the horizon), gate correctly failed. Plus late
  supply cap 33/33 (Pylon search stalls late game - same family as #59).
- Board: unit 414/414 (select-package) / 440+ full, arch store untouched,
  tree clean, jar fresh. Nothing further advances without owner-side
  evidence (Wine BUILDER_LOG game, jstack hang, viewer checks, 700 s+ game)
  - standing by for the Wine files.

## Session 2026-10-13 (autonomous, continued): re-place before cancel (owner ask)

- `CancelTooLongConstructions.tryReplaceWithFreshSite`: a timed-out
  construction first gets a fresh request with the old one still alive (the
  duplicate guard sees it); only a genuinely different site (footprint
  separation) cancels the old, with the queue order transferred. Same site or
  no site keeps the old - placement blocking us is status quo, as specified.
  Also null-safe `Construction.sameAs` (order-less V2 constructions NPEd the
  duplicate check; found by the new test). `ReplaceStuckConstructionTest`
  3/3, unit 417/417.
- 163040_short: no timeouts, no REPLACED (nothing stuck - healthy run),
  Core@178s Gas@198s, Dragoon=0 at 285s - timing again, gate correctly
  failed. Re-place awaits a game with a genuinely stuck construction.

## Session 2026-10-13: Wine log auto-copy (owner ask)

- `run-wine-full.sh` copies `out/wine/client.log` to `WINE_LOG_COPY` from the
  bot ENV on every exit (first step of `cleanup()`). Guards: non-empty path +
  `GAME_LAUNCHER=WINE` in the same file (Wine-only, never OpenBW); candidates
  in the bot's own ENV resolution order (a stale scbw ENV shadowing the local
  one was caught by the standalone test and removed from the list). Live path
  set in both git-ignored machine ENVs; tracked LOCAL-EXAMPLE documents the
  key empty. Next Wine game lands at `/sc-ai/E2E OpenBW LOGS/last-wine.log`.

## Session 2026-10-13 (owner asks): Core reservation + grid yield, verified in game

- Owner's Wine log (manual.log, 6:55, 49/49 cap): Pylon search dead late,
  Core never reserved, builders ping-pong Builder/Gather on every site.
- Committed: (1) queued Cybernetics Core pauses the worker pump below 250
  (joins Robo/Cannon in ProtossCriticalStuffInQueue - existing machinery, no
  new bookkeeping); (2) Pylon street grid yields at zero free supply
  (aesthetics yield to correctness; minerals/occupancy/power still apply).
  Tests: CriticalStuffCoreTest + GridYieldsWhenSupplyBlockedTest 2/2 each,
  unit 425/425, arch store unchanged.
- Verified `--short` with both live: PASS (`Core=1 Dragoon=3`), Core attempts
  at 125 s (was ~190 s), 86 CriticalStuff pump pauses in log. No regression.
- Declined with reasons (CONVENTIONS section 3): fallback threshold table
  (early Pylons build fine - 5 by 49 supply - the miss is late-search, not
  timing) and producer-early-creation (queueing Core at 18 without bank
  reserves nothing - reservations attach at READY - so it needs a redesign
  of reserve timing with Wine-length A/B, not a one-line change).

## Session 2026-10-13 (autonomous tuning round)

- Hold-IN_PROGRESS fix (Builder/Gather theft): committed with rule test,
  verified no-regression in game (PASS). Awaits a Wine game (the log it was
  built from) for the steal-gone confirmation.
- Owner asks delivered: Core critical reservation + grid yield at hard block
  (both game-verified PASS, Core attempts at 125 s), live Wine log tee
  (crash/IDE-kill safe, tested standalone + pinned).
- Declined with reasons: fallback threshold table, producer-early-creation
  (needs reserve-timing redesign + Wine-length A/B).
- Cleanup: ~200 dead comment lines + 7 unused imports + 39 dead classes
  (compile-gated, one live near-miss caught and restored).
- Board: unit 425/425, arch store untouched since the -3, jar fresh,
  6/8 recent --short PASS (2 timing misses, gates honest).

## Session 2026-10-13 (continued): builderless demote + hold verified in game

- Root cause closed for the Wine Builder/Gather alternation: a refused
  attempt's order echo flips IN_PROGRESS; on Wine setStatus releases the
  builder on the flip, leaving IN_PROGRESS + no builder + no building, and
  the old rule then released every stopped worker to Gather. Fix: no-building
  IN_PROGRESS keeps its worker (only a warping building releases it), and a
  builderless stall older than 90 f demotes to NOT_STARTED so StatusChanger
  assigns a new builder. Non-Protoss paths unchanged.
- Game proof (fresh jar): PASS with Dragoon=4 (best yet), DEMOTE fired on a
  stalled Pylon, zero took-too-long cancels. The safety net heals before the
  cancel path is reached.

## Session 2026-10-13 (autonomous: 3 NEXT items)

1. **#57 creation-path measurement**: single `addConstruction` door + temporary
   caller stacks. Result: both Cores via NewConstructionRequest (V2 path
   silent, V2 off) - first refused 6x, refresh-cancelled, rebuilt elsewhere,
   PASS. Cancel+retry working, not the leak. Door kept, print removed.
2. **Case study**: power-refusal saga (mask, center, withdrawn readings) into
   CASE-STUDIES.md.
3. **Cleanup**: 80 dead commented gates in Protoss building producers.
   Suite 426/426 throughout, tree clean.

## Session 2026-10-13 (autonomous: 3 more NEXT-adjacent items)

1. **E2E teeth (#34/IDEA-E2E-TESTS §3.3)**: absurd expectation (99 Gateways)
   fails correctly; deliberately broken build (grid refuses everything:
   Pylon=0, all zero) SCENARIO FAILED; reverted same turn (diff verified
   empty), jar rebuilt, re-verified PASS. Both halves of "red against
   broken" now proven. Mirror matchup (Protoss) also PASSES.
2. **Filed #72**: orphaned Singularity Charge researcher (wire or delete,
   owner call).
3. **Sweep**: game/information/application trees show no more dead classes
   (one candidate triaged, kept deliberately).

## Session 2026-10-13 (autonomous: DECISIONS.md + 3 items)

- New `_AI/DECISIONS.md` (indexed in README): D1 legacy deletion, D2 fallback
  table, D3 early-creation redesign, D4 Singularity, D5 thresholds+expansion,
  D6 ADR seam, D7 cache/intent seams, D8 Terran target. Deleted on approve.
- (1) game/information/application sweep: only ATechManager zero-ref -
  triaged live (sole Singularity path, filed #72) instead of deleted.
- (2) 180949_long (429 s): Dragoon=7, still Nexus=1 - expansion is
  threshold-gated (3 gateways, 429 s < 630), not broken. Folded into D5.
- (3) 313 dead comment lines removed in game listeners, suite green.

## Session 2026-10-13 (autonomous: 4 more NEXT items)

1. **WO-13 re-triage (#13)**: all 60 remaining information-rule entries are
   live strategy-response logic (race responses, queue/mission/tech calls) -
   nothing mechanical left. Triage stands, no change.
2. **#40 intent test + NPE guard**: `BuilderlessConstructionTest` 2/2 pins
   cancel-not-adopt for builderless NOT_STARTED (intended? still owner call)
   and covers the order-less V2 shape that NPEd `sameAs`'s neighborhood
   (`productionOrder().cancel` guarded).
3. **Cleanup**: dead commented methods in `Decision`, 89-line batch earlier.
4. **E2E stability**: another PASS (`Pylon=5 Gateway=3 Dragoon=3`), 7/9
   overall, both misses timing with honest gates.

## Session 2026-10-13 (autonomous: 4 closed-line deletions with re-verification)

- Deleted CLOSED lines #64 (guard present, PylonFallbackNoRecursionTest 1/1),
  #67 (SleepGateOwnershipTest 1/1), #66 (KeyboardPauseReleaseTest 1/1), #42
  (Assimilator built in 5+ runs since, incl. Terran Refinery). Full narratives
  stay in git history per process. Corrected stale #68 text (manager mirror
  unconditional since 2026-10-13, not flag-gated).

## Session 2026-10-13 (autonomous: 4 more closes + #73)

- Deleted CLOSED lines #29 (UnitStatsTableTest 5/5, correction table empty),
  #38 (debug.object dir + kryo/reflectasm/minlog gone), #53
  (OrphanedConstructionTest 2/2). #35 NOT deleted: reciprocity holds but its
  class is red on unrelated stale sunken pins - see #73.
- Filed #73: acceptance tier red on 11 pre-existing failures (proven on a
  clean pre-session worktree, removed after). Also fixed NEXT structure:
  #71-73 had landed past the appendix, moved with the items.

## Session 2026-10-13 (autonomous: 4 more closes)

- Deleted CLOSED lines #28 (no test-harness imports in production, build
  asserts), #47 (E2E loop proven by ~15 runs), #48 (465 LIVE plans, zero
  duplicate buildings), #53 (OrphanedConstructionTest green).
- #52 slide is BACK under V2 LIVE (Pylon@557 @2, @777 @360, @979 @660, exit
  124) despite PRIORITY_OPENING_SUPPLY - new V2_GOALS probe (bands per goal,
  builderLog-gated) names the next measurement: classification vs allocation
  order. #35 kept (reciprocity holds, class red on unrelated stale pins -
  filed #73 with pre-session-tree proof).

## Session 2026-10-13 (autonomous: #35 closed, #59/#46 split off)

- Closes #35 (deleted): reciprocity re-verified, not just re-read - all 5
  `assertReciprocal` tests in `CombatEvaluatorTest` pass
  (`relativeScoreIsAReciprocalPair`, `fourMarinesBeatOneHydralisk`,
  `threeMarinesAgainstTwoHydralisksScoreOurWay`, `marinesAndMedicBeatOneHydralisk`,
  `oneMarineAgainstOneEnemyMarine` - the mirror test, twice) plus
  `AtlantisJfapTest` 2/2; B-18 hedge `OUR_SIDE_HEDGE = 0.3` confirmed our-side-only
  (`CombatEvalScale:65`, applied `AUnit:2578`, skipped for enemies `:2564`).
  The class's only red is the 3 known pre-existing pin failures, tracked in #73
  (label corrected: two sunken-colony + one direction pin, not three sunken).
- Retires #59's line, files #74: fixed halves re-verified (12/12 unit green -
  requeue, strategy orders, move-debounce, gas distance, travel; cancel-before-count
  `CancelTooLongConstructions:192-194`, `lastCommandIssuedAgo` `IssueBuildOrder:102`,
  bare-`Move` stop `:184-185`, `BUILD_BREAKDOWN` `BuilderDiagnostics:105`).
  #74 keeps the Wine-only remainder (Builder/Gather alternation, 1.4-tile boundary).
- Retires #46's line, files #75: init-before-chooser verified
  (`OnGameStarted:115` before `:118`, `StrategyBuildOrderTest` 5/5 pins the missing
  set as exactly the three tracked). #75 keeps the owner-policy remainder.
- Not touched: #52 (another session has the fix in flight in its dirty tree -
  `DynamicGoals`/`PullForwardGoals`/`GameStateSnapshot`/`GoalSourcesTest`).

## Session 2026-10-13 (autonomous: #60 closed, #58 measured, E2E step 3 done)

- Closes #60 (deleted): the narrow reading settled by 239 same-line WORKER_GATE
  lines (`20261013_193330_long`, 409 s, exit 0, jar `505e914b`) - 0/50 CantAfford
  with same-line minerals>=100 (affordability gate reads clean); 8 NoSupply with
  minerals<100 all at genuine 17/17 or 33/33 (both gates closed at once, the label
  is check order, not a false reading). Defect half refuted; the threshold half
  already lives in D5. Watchdogs quiet: took-too-long 0, Last resort 0,
  is-overdue 0, DEMOTE 1 (Pylon [79,114], healed and completed).
- #58 recorded, kept open: 0 `Nexus ADDED TO QUEUE`, 0 `QUEUE_BASE`, 13 distinct
  accepts, 0 cancels - plus the instrumentation gap (legacy path emits no queue
  markers at all, so QUEUE_BASE cannot distinguish "never queued" from "never
  logged"). No expansion in 409 s undisturbed reads as D5 thresholds, not a queue
  bug. Next: legacy-path queue logging or a V2-path run where markers exist.
- IDEA-E2E-TESTS §3.5 step 3 marked done (Pylon fixed, green many in a row,
  red-against-broken proven both halves). Step 4 (7-min survival) still open:
  this run 409 s / 0 kills vs 420 s / 12 kills.
- #57 health: 1 REFUSED_DUPLICATE (dedupe working as designed), 13 distinct
  accepts, 0 cancels - no leak symptoms; the tripwire stays.
- Disclosure: the run jar contained the other session's uncommitted V2-goals
  edits; the run used the default legacy path (no PLACEMENT/PRODUCTION_V2),
  where that code is inert. Nothing of theirs staged, touched or claimed.

## Session 2026-10-13 (owner asks: Wine worker stream, cvis log shape, 90-unmatched)

- Wine worker root cause (last-wine.log, 3513 lines, @0-@9720): `CriticalStuff`
  from frame 0 at 50 minerals (39/39 early gates), first worker ordered only at
  ~242 minerals (@1110-@1140, 242→208 after the 50 payment). Mechanism: the
  queue is pre-seeded with the whole build order at init, so a far-future
  not-started Core read as "save up" for the entire early game; the bank had
  to touch 250 before the gate short-circuited. Fix: critical items count only
  when imminent (explicit TOP request, or supply gate met - the PrioritizeCore
  predicate). The existing `CriticalStuffCoreTest` caught the over-narrow first
  cut (a TOP request must stay imminent); green after. New
  `WorkerPumpEarlyGameTest` 3/3: far-future Core doesn't pause, frame-0-shape
  stream gate opens, Probe 300-frame engine pin (the <=10-frame re-queue is the
  asked <3%). Suite 461/0, arch advisory with only the known
  SleepUntilFrameGate failure.
- Cvis log shape (owner spec): manager lines `-GatherResources@1128` (dash =
  active manager), engine orders `ATTACK@1128` via a small mapper
  (attackUnit→ATTACK etc., uppercase fallback); outcome suffix dropped per spec
  (refusals stay in bot.log BUILD_ATTEMPT). No test pinned the old strings.
- `cvis_dbg_matching.js` console.logs removed (Stardust repo, separate commit).
- 90-unmatched analysis (newest trace 19:42, 107 seeds + 52 late units): clean
  on all fixed axes, so 38+ of the 90 must be frame-1 seeds - the seed batch
  itself never matched (no-retry matcher), not trace content. Filed #76
  (direct unit_ids pairs); the per-type console breakdown is gone with the
  logs, the offline numbers stand in its place.

## Session 2026-10-13 (owner asks: cherryvis-docker setup + #76 in game)

- Closes #76 (deleted): `unit_ids` pairs emitted at every first-seen write and
  verified in a real trace (`20261013_201745 --short --cvis`, exit 124 on the
  engine-handoff cap - trace written before it: `CVIS OK`) - 139 pairs, 100%
  first_seen coverage, engine ids only, Probe-21/Mineral-93/Nexus-1 plausible.
  `CherryVisUnitIdsTest` 2/2 pins the shape. Suite 463/0 after.
- Viewer reloaded: `cvis_dbg_matching.js` console.logs removed live AND baked
  as `cvis-matching-no-console.diff` (repo pattern); first rebuild failed on
  the diff path prefix (`src/` stripped), second passed detached (pip ~90 s -
  over the 120 s foreground shape, background + poll). Image `42123ae9`,
  container restarted with identical args, served js grep-clean, homepage 200,
  newest pair listed. Owner: hard-refresh the browser (cached js).
- New `_AI/CHERRYVIS-DOCKER.md` (setup we use + links + patch pattern),
  indexed in `_AI/README.md`. The missing setup file the owner asked for.

## Session 2026-10-13 (autonomous: #73 split, #58 markers, legend agreement)

- Retires #73, files #77: the 4 NPEs are fixed by a null-guard in
  `AUnit.isTrainingAnyUnit` (a handle-less unit trains nothing - same shape
  as the neighboring accessors; `ProtossDragoonProductionTest` 2/2,
  `protoss.ProtossDynamicUnitProductionTest` 3/3 green, zero NPEs left in the
  tier). #77 keeps the 7 pins (3 numeric eval + 4 boolean behavioral - code
  or pins, owner's call). Suite 464/0 after.
- #58 instrumented, kept open: buildings now log `<Type> ADDED TO QUEUE` at
  birth (`AddToQueue`), so the next stuck construction leaves a first trace;
  the hunt still needs its Nexus-queuing game.
- `CherryVisUpdatesLegendAgreementTest` (#65's actionable half): one shared
  runtime table feeds `units_updates` and `custom_names` (verified single
  instance in the writer); the test pins every referenced id resolving in
  legend order. Viewer-click check stays the owner's.

## Session 2026-10-13 (autonomous: #56 Terran symptoms, #69 watchdog, combat sweep)

- #56 measured, kept open: second Terran game (temporary flip, reverted +
  Protoss re-verified playing same turn) shows the stall behaviorally - two
  SCVs Move-loop at successive Barracks sites, zero attempts, 0 Barracks,
  minerals hoarded - with all literal markers absent. Neither predicted defect
  confirmed as written; next is reading `TerranNewBuilding` vs the fixed
  Protoss path, not a third measurement run.
- #69 instrumented, kept open: background jstack catcher in
  `run-openbw-e2e.sh` (log stalled 30 s + java alive → one `jstack -l` dump
  to the run dir, then stands down; skipped if jstack missing; `bash -n`
  clean). Still needs its hang.
- Combat sweep: deleted `combat/eval/Evaluate.java` (@Deprecated, all-private,
  zero refs); `ZergRetreating` RESTORED after deletion - zero refs but on the
  owner-eyes-first spared list, which the sweep rule does not overrule.
  Suite green after.

## Session 2026-10-13 (owner's OpenBW run: roster key, dragoons, order watcher)

- Roster "false readings" settled (owner's 331 s run, ForceExit 41/41):
  Pylon 4/4, Assimilator 1/1, Core 1/1 completed==roster; Gateway 2 completed
  vs 3 roster - the +1 ordered 5:16, 15 s before exit (warping at count time).
  Counts read the engine list 1:1 with unfinished INCLUDED (2026-10-12 ruling)
  - not desync, a reinstall changes nothing. Filed D9 (keep / completed-only
  / split board-vs-gates). E2E gates read the same line (training passes).
- Reinstall OpenBW: NO - evidence above, plus Wine/OpenBW gaps are our code
  paths (regions, attach timing, finder), not engine corruption.
- Dragoons at "25 gas": premise wrong - a Dragoon costs 125/50, the gate is
  gas>=50 (`ProduceDragoon:33-34`), then dragoons<=4 produces immediately;
  "60+" observed = 50-gate + timing. No bug (`AllowProduceDragoon` adds only
  184+ supply gates).
- Pylon timing nuance: Pylon-1 queued from frame 0, completed at supply 9 (BO:
  At 8) - on-time in supply terms; the 400-bank-near-cap matches late Pylons
  3,4 (ADDED 4:44-4:53, min 191-501). "9/9" in ORDER_NOT_READY is
  used/minSupply, not used/total. A game-time for the 400-bank moment would
  settle whether anything was late early.
- Correction: the log shows Gatewayx2 + Core completed - Assimilator was the
  only LATE building, not the only non-Pylon one.
- Order watcher implemented as observer, not manager (a manager would perturb
  the chain it reports on) + game-verified: 2397 lines, full trails
  (Nothing→MoveToMinerals→MiningMinerals→Move→PlaceBuilding), 82
  dash-managers interleaved, unit_ids coverage still 100%. Suite 464/0.

## Session 2026-10-13 (autonomous: FIVE fixes - hover, Nexus, supply, #56, pins)

1. Terran hover FIXED + game-verified: the move target is the tile origin
   now (the footprint-center target was unreachable by the tile-origin
   decision metric for 4x3). Terran run: Barracks BUILD_ATTEMPTs 2/2
   accepted (was 0 forever), Barracks=2, Marine=9. Protoss re-verify PASSED
   (Gateway=2, Pylon=4, Core+Dragoon gate). Refresh-drift ruled out (0
   refresh lines; re-site came from fresh request after cancel).
2. Nexus save-up gate + deterministic tests (`SaveForNexus` below 450 while
   queued; caught + fixed an order-dependent test along the way -
   ShouldExpand globals don't belong in unit tests). No bounded game-verify
   possible (expansion thresholds); Wine will tell.
3. Supply bands restored to ASupplyManager levels (2/4/5/8/10/14/19, measured
   against the deleted source) + public threshold tests; Protoss run PASSED
   with them (PylonAt8 visible, first Pylon @1152 at 8/9). D2 executed and
   deleted. Capped-with-bank windows persist (reservations/placement next,
   not order timing).
4. Closes #56 (deleted): orphan fallback fixed+tested, hover
   fixed+game-verified, predicted defect 2 refuted (IN_PROGRESS is correct at
   creation). Remainder filed as #82 (off-site completion, unobserved).
5. OccupiedTile test aligned to the #49 ruling (worker blocks w/o builder,
   builder excluded); #77 narrowed to 6. Zealot guard tried and falsified by
   measurement (claim persists; stub-world proximity selector flaps for
   static geometry - open harness-vs-production question, do not gate on it).
   Terran pins = We.terran/Main.ourRace harness question (owner).

   NOTE: another session is active in the tree (`BuildOrderSettings`
   supply threshold touched while this session changed supply bands -
   overlap to reconcile before either side claims supply behavior).

## Session 2026-10-13 (owner overrides: completed-only, Pylon-at-8, #form)

- Counting pipeline EXONERATED by raw-engine dump (28/28 exists+completed,
  1:1 with roster - temporary probe, removed after reading). Our counts do
  not invent units; #78 rewritten to the replay-pairing suspect (stale `.rep`
  opened for the logged game?) - needs the owner: which `.rep` timestamp,
  does it match the run dir? Completed-only roster implemented anyway (D9
  executed and deleted) - gates honest from now on.
- Pylon-at-8 gate implemented + 3 tests (`PylonAt8` reason): supply>=8, no
  Pylon at all, bank<150 → workers wait; released by warping Pylon or 150.
  Suite 467/0 (incl. new tests). Other races untouched, Zealot/Dragoon
  impossible at 8 anyway (no Gateway).
- Watcher switched to owner spec `#<lastCommandName>@<frame>` (engine's own
  getLastCommand, null-safe) + game-verified: 120 `#`-lines (#Move 42,
  #Stop 30, #Gather 25, #NO_COMMAND 17, #Build 6), dash-managers intact.
- Admission stands: "unfinished explains everything" was wrong (Zealot=6 at
  zero gateways killed it), file-confusion dismissals were wrong. The remaining
  open question is owned explicitly in #78 instead of explained away.

## Session 2026-10-13 (desync verdict + @minSec + idle-workers solved)

- OpenBW #27 confirmed real ("openbw creates desynced replay", 2019,
  unassigned, second Linux confirmation 2021): bot log correct, replays
  desync early. Verdict: LIVE is truth (roster, gates, traces), replays are
  position-viewing only. #78 rewritten accordingly; engine replay-writer fix
  out of scope (no upstream fix to update to; reinstall pointless).
- Pylon-2 "refusal" NOT reproduced: owner's snippet (#Build→-Gather→#Gather
  in 3-4f) is the normal warp-and-release signature, and no run shows an
  engine Pylon refusal (all BUILD_ATTEMPT accepted). Newest run Pylon=4.
- Idle fresh workers SOLVED, not desync: 19/19 probes idle EXACTLY 302f
  (#NO_COMMAND→#Gather, zero Move between) = training time - the watcher sees
  units still inside the Nexus (training units are listed incomplete), then
  they pop out and mine. Uniform, self-healing, engine-not-ready (#63 class).
  Military never-gatherers expected.
- Unit logs now `@minSec()` (owner spec): `#Move@0:12`, `-GatherResources@0:12`
  - watcher + manager mirror. Suite gate next.

## Session 2026-10-13 (engine question: what fixing OpenBW replay desync takes)

- Filed #79 (owner priority, separate model): replay = header (frame count,
  map, players, hardcoded seed) + compressed per-frame ACTION history
  (`replay_saver.h: add_action`) + map data; playback re-simulates, so
  desync = missing/misordered actions, nondeterminism, or initial-state
  mismatch. Cheap-first plan recorded in the item (no-bot repro,
  first-divergent-frame, vendored-vs-upstream diff) before any surgery.

## Session 2026-10-13 (PurpleWave latency comparison + LATCOM line)

- PurpleWave: `setLatCom(false)` at start (`With.scala:131`) + manual
  latency compensation everywhere; Steamhammer/McRave: `setLatCom(true)`;
  Stardust tests: false; we: JBWAPI default ON, never set, never printed.
  JBWAPI auto-sets `setLatCom(!async)`; LatCom predicts unit state on issue
  (javadoc) - our order-timing sagas live in these semantics. LATCOM line
  now logs latCom/latencyFrames/remaining at every game start. Filed #80
  (one bounded setLatCom(false) experiment, reverted after).

## Session 2026-10-13 (dragoon ASAP test, LatCom A/B kept, #81 filed)

- Dragoon verdict: no production defect existed - the Wine log never reached
  50 gas (Assimilator online ~3:58, ~32 gas by the 4:57 end), so the price
  gate never opened; gates were always price-correct. `DragoonProductionAsapTest`
  pins first-call ordering at exactly 125/50. Suite 468/0 (1 skipped).
- LatCom A/B kept per owner instruction (latCom=false on the LATCOM line):
  Pylon timing SAME (@1301 at 9/9), flapping SAME (none), Dragoon=0
  confounded by #81 (25/25 cap from 3:02). Verdict inconclusive, not harmful.
  Closes #80 (deleted).
- Filed #81: late Pylon search refuses the corner neighborhood (1953x
  physical + grid modulo); Pylon-1 in the same run was poverty-held
  (40-88 min, all reachability/power true) - consistent with PylonAt8.

## Session 2026-10-14 (autonomous: #52 closed LIVE, #57 triple, flap verdict)
- #57 watch datum, not a verdict: `REQUEST_ACCEPTED Pylon [11,51]` x3 at
  4:45/4:47/4:50 in `20261013_233256`, `totalOfType=1` every time (counter
  blind to siblings), zero cancel/refusal/replace/demote for the tile. The
  duplicate shape without the builder/bank proof - the item's add-site stack
  trace still names the path. Do not patch blind.
- Zealot flap verdict: stub-world `enemiesNear().inRadius(3)` flips
  true/false for STATIC geometry on both units (383 probe lines) - per-unit
  caches are instance fields, so ownership narrows to stub-side
  membership/proximity evaluation, exact mechanism open. Production
  membership is event-driven (no such churn source); still, no production
  code gates on proximity for this - the tried guard is reverted, zero
  behavior delta. #77 keeps the item with this note.
- Suite 476/0; tree holds only the foreign BuildOrderSettings line (untouched).

## Session 2026-10-14 (autonomous: #81 visibility + healthy long game)

- #81 attribution fixed (zero behavior): `PylonTooFarFromBaseEarly` names its
  kills now - part of the 1953 "physical" was mislabeled doctrine victims.
  Pinned in `RequestBuildingNearTest` (6/6); suite 479/0.
- Long game 368 s: placement 6/6, no failed searches, so no TRUE-split data -
  #81 stays open for the corner-stall game. NoSupply 21 polls total (down
  from 38+; reserved>bank = earmarked, not starved). DEMOTE 1 (CyberneticsC,
  healed). E2E FAIL on long-tier content (2<3 gateways etc.), not on error.

## Session 2026-10-14 (Wine supply blocks: timing fix verified)
- Fix: fallback bands +one warp headroom (8/10/12/16/16/22, opening bands
  unchanged); Stardust comparison done (their `shiftForSupply` pulls Pylon
  completion before predicted block frames over a horizon - our legacy path
  has no timeline, so this is reactive-with-headroom; full prediction
  belongs to V2 parity, #45).
- Verified: suite 476/0; --short PASS with Core+Dragoon=2; non-BO Pylons
  ordered at 14/17, 19/25, 26/33 (3-7 free) instead of at-cap; NoSupply 3
  hits (max bank 248+300 reserved); Can't-find-place 0, took-too-long 0,
  DEMOTE 3 all healed. 41/41 + 33/33 with bank remain (next: #83
  reservations, #81 placement).

## Session 2026-10-14 (full Pylon solution: burn-calm verified, #83 refuted)

- Spending autopsy rewrote the story: the 25-cap bank crashes (92->8,
  88->4) are the Pylon's OWN attempts (@4791/@4974, accepted) - each spends
  ~100, the warp drops server-side, demote, retry spends again. Workers stop
  (31/31 NoSupply), no units/upgrades/Forge lines anywhere. 41-cap: zero
  spends, monotonic 172->348 (pure warp wait). So #83-as-arbitration was the
  wrong direction (near-main orders bypass resource checks anyway) - deleted
  with this refutation.
- Fix: warp-window pump calm (IN_PROGRESS + no building + bank<250 pauses
  workers; linked warps and rich banks unaffected) + 3 tests. Verified:
  suite 479/0; --long 251 s: bank monotonic 28->584 through both caps, ZERO
  Pylon attempts inside NoSupply episodes, no sawtooth. Burn cycles gone.
- Revealed next layer (not this turn): 3rd Pylon placement-fails 3:02
  (1953 physical, corner base), never retried, bank grows into the hoard
  tripwire at 4:11. #81 territory (corner attribution + retry cadence).

## Session 2026-10-14 (owner asks: Nexus corner drive, Pylon retry, Nexus-before-1000)

- Center-target + footprint thresholds (owner call): workers walk to the
  footprint CENTER again (corner drive was the origin-target from the hover
  fix), decision reads distTo(center) with threshold max(1.4,
  center-offset) - Pylon ~1.41, Gateway/Barracks 2.5, Nexus 2.83, gas 3.5
  and Bunker 8 unchanged. Metric and target agree by construction, so the
  hover cannot return through this split.
- Verified BOTH races: Protoss PASS (Core+Dragoon=2, 10/10 accepted, no
  Move-loop); Terran Barracks 2/2 accepted and BUILT (Marine=9) - hover
  stays fixed under center geometry. Protoss re-verified PASS after revert.
  Suite 485/0 (incl. rewritten center pins).

## Session 2026-10-14 (demote-hold fix + overlap live, 2-Dragoon PASS)

- Demote misfire fixed: the standstill clock counted the pre-attempt saving
  wait, demoting 7f after an accepted attempt (103230 Pylon-3). Exemption:
  constructions attempted <150f ago hold (timestamp stamped at both
  `unit.build()` sites; `logBuildAttempt` untouched - it is builderLog-gated
  and must not carry behavior). Test: recent-attempt holds, old/default
  still demotes. Suite 482/0.
- Verified in game (`20261014_103638`, PASS Core+Dragoon=2): 8/8 attempts
  accepted, 0 demotes, 0 placement failures, 0 took-too-long. Bank-wait
  standstills (250f+ at 0.0 dist, canAfford=false) now read as correct
  saving, not stuckness. Overlap armed but untriggered (requests stayed
  sequential); no spam.
- Watch item (not a fix): `Exit was requested manually` ended that game at
  220 s (Escape path). In headless no one presses keys - suspect a stale
  key-relay file a la #66 (purge exists at launcher starts). If it recurs
  with short wall time, the purge path needs re-reading.

## Session 2026-10-14 (owner ask: 2nd + 3rd Nexus tests, bank hoard)

- `Count.existing()` for primary bases counted ONLY Zerg Hatchery/Lair/Hive -
  Protoss Nexus and Terran CC always read 0. Measured: 2 finished Nexuses
  read as 0 existing, so `bases` never exceeded 1 and the third-base logic
  was unreachable - this is the "2nd yes, then stuck" shape. Fixed (Zerg
  morph branch kept, others count normally); 4 base-count call sites
  reviewed, all assume true counts. Suite 485/0, no other test moved.
- Div0 guard in `genericThirdLotsOfMinerals` (min/(400x0) with nothing in
  production threw, caught upstream as silent no-expand). Behavior for n=0
  unchanged (still no), minus the exception.
- `SecondAndThirdNexusTest` 3/3 (2x runs): LimitedBases decision, approved
  order queued, third-base decision. Caught two harness traps along the way
  (Terran-race default breaks BASE counting; pre-seeded queue reservations
  need a reset for affordability paths) - documented in the tests.
- Bank-hoard mechanism status: Pylon conversion (5:01 order unbuilt in 38 s)
  still needs a BUILDER_LOG=1 run to resolve (builder? placement? attempt?) -
  owner runs without the flag. Expansion pipeline now tested; thresholds
  stay D5.

## Session 2026-10-14 (owner asks: corner drive, Pylon retry, Nexus-before-1000)

- Center-target + footprint thresholds DONE (owner call) + game-verified
  both races (Protoss PASS 10/10 accepted; Terran Barracks 2/2 built, hover
  stays fixed). Corner drive was the origin-target from the hover fix.
- Pylon retry: NO CODE NEEDED - failed searches already re-run at producer
  cadence (READY orders every frame via ProductionOrdersCommander:53-56;
  readiness re-evaluated every 19/71f), exceeding the asked 5 s. The Wine-41
  gap was never-requested, not never-retried - and its telemetry
  (ORDER_NOT_READY) was commented out at HEAD, now restored behind
  BUILDER_LOG (was: silent queue). Next log names the gate.
- ENV-source startup line: the Wine bot played on silent defaults (no
  builder logging despite the flag file) - CWD-relative resolution misses on
  Wine. One unconditional line now logs source + builderLog every game.
- Nexus-before-1000 verdict: pre-1000 absence IS the criteria (natural gates
  fail at 2 gates/23 workers/348s/combat; LimitedBases needs 777) - no
  criteria change per owner, so nothing to fix there. Post-5:48 flapping
  (READY⇄IN_PROGRESS, bank 927+) without issue needs builder/order data -
  blocked until the ENV-fixed run lands.

## Session 2026-10-14 (owner asks: order IN_PROGRESS truth + scout rewrite analysis)

- Order statuses fixed: NO code path ever marked a building order FINISHED
  (`IsOrderCompleted` answered tech/upgrades only) - every building order
  sat IN_PROGRESS forever, clogging counts and the production log the owner
  reads. Fix: building order FINISHED iff its linked building stands
  completed (units untouched). `BuildingOrderCompletionTest` 2/2 (built
  completes; warping does not). Suite 487/0.
- Verified in game (PASS Core+Dragoon=2): 11/11 attempts accepted, one
  self-healed DEMOTE, zero stuck-IN_PROGRESS churn, no mass re-requests.
- Scout verdict: REPAIR, not rewrite (analysis vs PurpleWave in STATUS -
  same assigner+priority shape; the hang is a fall-through hole: all
  managers decline at once, no terminal fallback, no abandonment/handoff).
  Patch points named (roam timeout-advance, terminal fallback+release,
  x>=3200 guard); awaiting owner decision before touching.

## Session 2026-10-14 (owner asks: finish the scout repair)

- Implemented the three named patch points (owner approved the repair):
  roam timeout-advance (`RoamAroundEnemyBase`: progress tracking via
  `roamLastDistToPoint`/`roamLastProgressFrame`, forced polygon advance
  after 300 frames without progress - the old code advanced only on
  arrival within 1.8, so a blocked point returned the same target and
  `move()` suppressed the re-issue), terminal fallback+release
  (`ScoutCommander`: a null chain result names `scoutToRelease`, freed
  next frame by `releasePendingScout()`, never mid-loop), x>=3200 guard
  removed (was already out of the tree in the started work, kept out).
  Plus `ScoutState.reset()` wired in `OnGameStarted` next to the other
  per-game resets. Tests: `ScoutRoamStuckTest` 4/4, `ScoutReleaseTest`
  3/3 (incl. reset). Suite 494/494, arch 6/7 with only the known
  pre-existing `Manager->SleepUntilFrameGate` failure, store unchanged.
  Game verification pending (needs a scout-hang game, owner-side).
