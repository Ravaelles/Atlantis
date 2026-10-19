# Code review: Production + Placement (SOLID)

Scope: `src/atlantis/production/**` (queue, requests, constructions,
dynamic, v2) and `src/atlantis/placement/**` plus the legacy siting chain
`src/atlantis/production/constructions/position/**` it is strangling.
Method: dependency scan per package (`grep ^import`), god-class skim by
size + responsibility count, seam/contract read of the two funnels
(single construction door, placement port). Normative rules are
CONVENTIONS §2 (SRP), §5 (boundaries), §21 (domain in domain packages);
NEXT #45/#144-147 own the deletion ordering, so nothing here proposes
bottom-up rewrites.

## R1 (DIP, fixed this session): placement performs production

`ProtossPlacementStrategy.requestAvailabilitySupport` (geometry domain)
writes straight into the legacy production queue
(`placement/race/ProtossPlacementStrategy.java:83`: `AddToQueue.withHighPriority(...)`),
then swallows **everything including `AssertionError`** (`catch (Throwable)`)
because "a unit test, a future v2-only build" has no queue. A void method
performing a cross-layer write that it then hides is the exact shape the
port interface (`production/v2/PlacementPlanner.java`) was built to prevent:
the planner (`placement/engine/CataloguePlacementPlanner.java:342`) cannot
see, test, or route the side effect.

Fix applied: support is a returned declaration now. The strategy answers
*what and where* (`placement.core.SupportRequest`, nullable = no channel),
the planner accumulates one pass worth (`drainSupportRequests`, cleared at
`startPass` like the tile reservations), and the composition roots forward
it - the V2 engine and the legacy catalogue adapter each into `AddToQueue`
(the coexistence-safe sink: queued providers are visible to V2 as
coming, so no cross-author duplicate). No signature change on the
planner port; no behavior change on the happy path (same HIGH Pylon at
the same tile). Test: planner-level stub pin of declare-collect-drain.
Incidental find fixed along the way: a caller-supplied grid was never
cleared of reservation marks, so every pass after the first saw an empty
catalogue (`clearUsed()` on the reuse path - the planner test needs
ctor + startPass to reach the support branch at all).

## R2 (dead code, fixed this session): two definitions of "free worker"

`Select.ourWorkersFreeToBuildOrRepair` duplicated the `FreeWorkers.get()`
contract minus a filter, and the last production caller went away with
the FreeWorkers sweep - leaving the definition, zero callers, zero
tests. Deleted. One definition of free survives (plus the deliberate
`isBuilder` carve-outs that name their reason at the call site).

## R3 (minor, not changed): stringly-typed placement contract

`RacePlacementStrategy` takes `String buildableTypeId` on four methods;
every race impl immediately re-parses it (`getByName`). The round trip
is total today, so this is readability, not a defect. If the contract
ever grows a fifth method, type it as `Producible`/`AUnitType` instead.

## R4 (god records, owned elsewhere - no change here)

`ProductionOrder` (581 lines: order data + status machine + pricing +
requirements + reservations + construction link + priority + position +
mission) and `Construction` (469: status + builder + siting + warp links)
each have several reasons to change. Both die on the #147 ordering
(stateless demand/schedule, then the queue moves to `src-old/`), never
bottom-up. No fix in this review beyond the ones already scheduled.

## R5 (coexistence, owned by #97 - no change here)

Two wall models live side by side: legacy `map/wall/*`
(`WallInAlgorithm`, still computed every frame by `OnEveryFrameHelper`
for the painter) and the new pure `map/analysis/*` (`WallAnchors`,
`WallPieces`, `WallKit`). Same for status tracking (legacy
`ConstructionStatusChanger` vs V2 `CommittedConstructions`, which is a
merge bridge, not a second machine). Both pairs are charter-owned
(#97 maplib, #144-147 migration). The only action is the standing rule:
no new reader may import the legacy side (ArchUnit + #147 grep gate).

## R6 (decided by owner, implemented)

`WorkerDefenceRun` no longer drafts builders: `isTheChosenHelper`
skipped only `isConstructing`, so a linked-but-waiting probe near a
victim was drafted - the builder hijack in reverse. New
`isEligibleHelper` seam (not the victim, healthy, idle, unassigned),
pinned by `WorkerDefenceSparesBuildersTest`. A genuinely overrun base
still has every other worker; the builder keeps its site.

## R7 (deliberate, not a violation)

`ProduceBuilding.requestBuilding` funnels ~15 guards through one door.
That reads like an SRP violation; it is the ratchet instead (NEXT #91:
one issuer, one log, no silent authors). Same for `requests/*`
importing `decisions.goals.protoss.*`: the Phase-A strangler pattern,
owned by the goal-board track, not accidental coupling.

## What was verified and left alone

- `ProductionDispatcher` stays pure (recording-fake tests); twins live
  at the engine edge. No change.
- `PylonPlacementScore` is intentionally unwired (in-code NOTE: wiring
  it needs a multi-candidate finder; inventing candidates is worse).
- `FindPosition.defineBuilder` / `GetOptimalBuilder` fallbacks now
  return null instead of stealing (FreeWorkers sweep); callers already
  handled null (adopt-no-free-builder twin, fresh-request retry).
