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

## Current work
- **NEXT #42 — first Assimilator diagnostics, no policy change.** Added reason
  labels for producer rejections (`AlreadyHaveAssimilator`, `AlreadyQueued`,
  `ExpansionSupplyGate`, `NoCyberneticsCore`, `CyberneticsCoreNotNearCompletion`,
  `QueueRejected`) and an `applies()` report for the first blocking commander
  gate (`Throttle`, `MineralsOrQueueSize`, `StandardResourcesOrExpansion`,
  `CriticalQueueResources`), plus earlier short-circuiting producers
  (`FallbackPylon` / `CyberneticsCore`) that can prevent reaching the Assimilator
  producer. The diagnostic preserves producer order and short-circuit semantics;
  no eligibility gate was intentionally changed.
  Fast suite: 335 passed, 0 failed; ArchUnit: 7/7; canonical jar build passed
  with Java 8 bytecode and a fresh source fingerprint. A 90-second OpenBW game
  attached and completed but lost its only Probe at 48:28 game time; it did not
  meet the diagnostic precondition, so no Assimilator diagnostic line was
  expected. A second runner invocation was still active after the command channel's
  six-minute limit and yielded no captured verdict; a check for the exact host
  and bot process names found neither still running, and the log contains no
  usable game-end or Assimilator-gate evidence. The attempted game is
  inconclusive. #42 remains open until a completed run reaches the historic
  Core/no-Assimilator state and captures the blocking gate. The current cycle
  only adds and builds the instrumentation; it does not close #42.

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
