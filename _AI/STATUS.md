# STATUS — Production redesign (Track 01, `_AI/redesign/01_PRODUCTION.md`)

> Living status for the production-v2 refactor. Update at the end of every work
> cycle. Spec first (`redesign/01_PRODUCTION.md`), checklist in
> `__01_PRODUCTION_TODO.md`, this file = milestone state.
> Owner priority 1 (2026-10-08): **finish this**.

## Milestones

| # | Milestone | State |
|---|---|---|
| M0 | Production blocker (engine throws instead of refusing an order) | **done** |
| M1 | Pure domain + `ResourceTimeline` | **done** |
| M2 | `ProductionScheduler` | **done** |
| M3 | Placement seam (`PlacementPlanner` / `LegacyPlacementPlanner`) | **done** |
| M4 | Dispatcher + `ProductionV2Mode` flag (OFF/DRY_RUN/LIVE) | **done** |
| M5 | Goal sources (`BuildOrderGoals`, `DynamicGoals`, `PullForwardGoals`) | **done** |
| M6 | Cutover: LIVE verified in a game, legacy tree deleted | **partial** |

## What exists (one line each)

- **Domain** (`atlantis.production.v2`): `ResourceCost`, `Producible`,
  `ProductionGoal`, `TargetPlacement`, `ProductionItem`, `ProductionPlan`,
  `ResourceTimeline`, `ScheduledProductionItem`, `ProducerFacility`,
  `ProducerFacilityRegistry`, `PlacementPlanner`, `PlacementReservation`.
  Tests: `ResourceTimelineTest`, `ProductionSchedulerTest`.
- **Scheduler**: stateless plan per pass; prerequisites recursive and gated to the
  completion frame (a Core under construction gates the Dragoon); earliest
  affordable frame; shift-forward instead of drop; one item per facility slot;
  `producerLimit`; recipe-cycle cut; placement validated **before** allocation;
  supply providers add supply at completion; total capped at 200.
- **Placement seam**: `LegacyPlacementPlanner` over `APositionFinder`, with
  `startPass()/endPass()` so two identical buildings in one frame do not get the
  same cached tile. `CandidateResolver` exists for ranking several candidates;
  the legacy finder returns one. `PylonPlacementScore` is pure and tested but
  deliberately **not wired** - ranking needs a finder that returns more than one
  validated tile.
- **Dispatcher**: `ProductionDispatcher` (latency window for units, due-now for
  buildings), `OrderDirector` + `GameOrderDirector` (frame-scoped de-dup,
  research detected by our own tech) + `DryRunOrderDirector`,
  `ProductionV2Mode` OFF/DRY_RUN/LIVE.
- **Goals**: `BuildOrderGoals` (stateless rows satisfied by what the game has -
  completed, building, queued, pending; `xN`; `@AREA`; legacy supply lookahead),
  `DynamicGoals` + `PullForwardGoals` (workers, one owner for the supply rule,
  army floor, expansion counting a base under construction).
- **Adapters**: `GameStateSnapshot` (the only game bridge: stocks, mining rate,
  facility registry, supply in production), `ProductionEngine` (composition
  root), `ExistingItems`, `CommittedWork`, `TechProducible`, `UpgradeProducible`,
  `UnitProducible`.
  Tests: `ProductionEngineSmokeTest`, `WorkerProductionTest`,
  `OpeningDoesNotHoardMineralsTest`, `GoalSourcesTest`,
  `TechAndUpgradeProducibleTest`, `PullForwardGoalsTest`, `PylonPlacementScoreTest`,
  `BuildOrderGoalsTest`.
- **M0 fix**: the engine **throws** when a production building cannot take an
  order (it walks its training queue past the end) rather than returning false;
  `BwapiOrderSink.issue()` caught it, logged once a minute and dropped the order,
  so a busy Gateway produced nothing forever. Guarded in `BwapiOrderSink.train`,
  `ProduceZealot.produceZealot` and `GatewayClosestToEnemy.get()`.
  Test: `TrainOrderGuardTest`.

## What M6 still needs

1. **A LIVE run verified by the owner.** `PRODUCTION_V2=LIVE` makes v2 the only
   policy, but the cutover has never been confirmed in a real game. The legacy
   path stays as the fallback until it is.
2. **Then** delete the legacy tree: `Queue/**`, `ProductionOrder`,
   `PreventDuplicateOrders`, and the `Construction/**` healing commanders, once
   no production/test reference remains (audit with `grep`, not by assumption -
   a previous count said 149 depending files).
3. ArchUnit store must shrink or stay unchanged; never grow it to pass.

**Blocker outside this track:** placement. Positioning a building is part of
production, and `PositionFinder` is scheduled for deletion and rewrite
(`_AI/POSITION-FINDER.md`, owner priority 2). Do not extend the legacy finder
while that is pending.

## Verification protocol

- Every cycle: `bash scripts/run-tests.sh` (fast scope, < 40 s, CONVENTIONS §12)
  + ArchUnit, store unchanged or shrunk.
- M4+: one real game run with the flag (DRY_RUN then LIVE), comparison log
  recorded here.
- M6 closes with an **owner-verified** LIVE game run and the legacy tree deleted.
- Never launch Wine/StarCraft from a model run (CONVENTIONS §14); the OpenBW
  recipe is owner-run (`PLAN-OPENBW.md` §8).

## Open items owned elsewhere

- The Gateway `OrderSink.train failed ... : 5` report: **unconfirmed**. The `: 5`
  text has never been mapped to a BWAPI error code and the trace comes from the
  catch boundary, not the throw site. Deferred in `__01_PRODUCTION_TODO.md`; do
  not add speculative guards for it.
- Tests that launch a game belong to the owner tier; models run stub-world and
  pure tests only.

## History

Milestone-by-milestone evidence (commit ids, suite counts, measured numbers) is in
the git history of this file and in the milestone lines of the commits. This file
keeps only the current state, so it does not drift into a journal.
