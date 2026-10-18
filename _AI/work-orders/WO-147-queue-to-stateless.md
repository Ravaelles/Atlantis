# WO-147 — queue and order types to stateless demand/schedule (NEXT.md #147, part: the spec)

## Goal

V2 LIVE by default with parity numbers, then `Queue/**` + order types
move to `src-old/`. LAST: a bot that cannot queue cannot play. This
order is the spec + landing checklist; the behavior lands through
WO-144 (demand) and WO-145 (siting) first.

## Non-goals (do not touch)

- `Construction/**` (stays by design: V2 delegates builders to it via
  `GameOrderDirector`).
- The V2 pipeline itself (works; DRY_RUN compares, LIVE issues).
- Any bottom-up deletion: dynamic commanders go only after V2 covers
  tech + race army (#45 ordering).

## What the queue does today (measured 2026-10-18, replace don't remove)

| Piece | Files | V2 replacement (status) |
|---|---|---|
| Status machine NOT_READY → READY → IN_PROGRESS → FINISHED | `updater/QueueRefresher` + `IsOrderCompleted` / `IsOrderInProgress` / `IsReadyToProduceOrder` | scheduler `startFrame` + `CommittedConstructions.mergeInto` (walking-builder idempotence) + facility registry; LANDED |
| Single policy in LIVE | `Queue.refresh` no-ops when `PRODUCTION_V2=LIVE` (`QueueLiveGateTest`) | LANDED - the flip lever |
| Site preparation + orphan drop | `PrepareConstructionSite` (PREPARING + `dropOrphanedPreparing`) | `Construction/**` stays, so this stays until it moves, never before |
| Reservations | `ReservedResources` (+ `OrderReservations` shape) | `ResourceTimeline.allocate` (atomic, per-pass); readers migrate with #144 |
| Dedup | `PreventDuplicateOrders`, `OnlyOneAllowedOfType`, `TrimQueue` | single-frame plan has no duplicates by construction + inFlightFrom/standing-vs-inflight split + `orderHasWarpingBuilding`; LANDED |
| Recency (`lastRequestedAgo`, supply 60f veto) | `History`, `QueueLastStatus` | needs a named replacement before `SupplyCommander` dies (WO-144 delta) |
| Wallet/priority | `PriorityWaiters`, readiness gates | `GoalBoardWallet` + feeder funding (`adoptionAllowed`); LANDED (#93) |
| Status-flip reserve/release | `events/OrderStatusWasChanged` | source of the LIVE 200-300 leaked reservations ((b) Phase 5); dies with the queue, gated meanwhile |
| Order reads | `ProductionOrder` (581 lines: minSupply, aroundPosition, modifier, status, construction back-ref) | `ProductionGoal` + `PlacementReservation` + `ProductionItem`; readers migrate per row below |

## The reader inventory (the real reason this is LAST)

92 files outside `orders/production/queue` reference `ProductionOrder`
(measured 2026-10-18). Groups, in migration order (never bottom-up):

1. **Dynamic producers** (`dynamic/**`, ~40 files): die with WO-144
   (demand through the board/V2 goals, one author each).
2. **Site-finding** (`constructions/position/**`, door files): die with
   WO-145 (funnel only, then the chain moves).
3. **Construction execution** (`constructions/**` builders/commanders,
   `ProtossWarping`, `OutstandingBuildings`): stays - but its
   `productionOrder()` back-refs must be re-homed (order link becomes
   plan-item link) before the type dies.
4. **Listeners/recorders** (`game/listeners/*`, `WarpedBuildingRecorder`,
   `debug/painter`): read-only; re-point to the ledger/plan last.
5. **Strategy responses** (`information/strategy/response/*`,
   `information/tech/ATech`, battle siting): die with stages E/H, never
   through the queue.

## Steps (one commit per step, in order)

1. WO-144 + WO-145 exits hold (their own game proofs, their own commits).
2. **Parity A/B, legacy vs V2 LIVE** (long runs, same map): warps/min,
   tech coverage (Core/Forge/gas/Singularity), no-block runs at
   17/17 + 25/25, no duplicate warps. The 2026-10-18 gap is 0.63 vs
   0.83 buildings/min, thin on no-Dragoons + no-Nexus-2 (demand-side).
3. **Default flip** (`PRODUCTION_V2=LIVE` default): one long run, same
   bars. The single-policy gate already holds; the flip only widens it.
4. **Move `Queue/**` + order types** (not `Construction/**`): the 92
   readers verified re-homed first, then the move. Suite green,
   ArchUnit store shrinks or holds.
5. Close #45 and #147 together per §7 with run ids.

## Verification

- Steps 2-3: run ids + warps/min + tech checklist in the commit.
- Step 4: `grep -rn "production.orders.production.queue" src/atlantis`
  empty outside history comments, suite + ArchUnit green.

## Stop rules

- Parity gap open (thin V2, missing tech) → stop, the flip waits, never
  widen tolerance to make a thin run pass.
- Any of the 92 readers still needs the type → stop, it migrates with
  its track (#144/#145/E/H), not here.
- Any urge to move `Construction/**` with the queue → stop. It stays
  by design (#45).
