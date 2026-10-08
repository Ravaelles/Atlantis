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

## What is still open

1. **The cutover has not been verified in a real game.** `PRODUCTION_V2=LIVE` is
   the only policy when set, but no LIVE run has been accepted as correct. The
   legacy tree stays as the fallback until one is.
2. **The plan re-plans instead of remembering (the live blocker).** First OpenBW
   run of LIVE: the same Pylon was scheduled at a **later frame every frame**
   (`Pylon@557-1007`, then `@746-1196`, then `@974-1424`) and `issued:` never
   listed it - so the dispatcher was not re-issuing it, the *scheduler* was
   re-planning it against a timeline that never records what was already
   committed. Fixing the goal layer's "count work already ordered" (done for
   workers and supply) removed the thousands-of-orders symptom but not this one.
   This is the "repeated goals / stateless recomputation" gap and it is why the
   bot does not build on the live run.
3. **Full dynamic-goal parity**: tech, race-specific army composition and
   strategic (Play) contributions are still simplified.
4. **The economic model is constants**: `EconomyModel` holds the rates and the
   first-trip delay, but worker/gas rates are not measured.
5. **Legacy deletion** (`Queue/**`, `ProductionOrder`, `PreventDuplicateOrders`,
   `Construction/**` recovery) after LIVE passes. A previous count said **149
   production files** reference the queue, so this is not a mechanical delete:
   the dynamic commanders must be replaced by their v2 goals first.
6. **The Gateway `OrderSink.train failed ... : 5` report**: unconfirmed. The `: 5`
   text was never mapped to a BWAPI error code and the trace came from the catch
   boundary, not the throw site. Do not add speculative guards for it.

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
