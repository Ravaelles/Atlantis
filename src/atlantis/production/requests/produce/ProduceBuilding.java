package atlantis.production.requests.produce;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.DecisionDispatcher;
import atlantis.decisions.goals.protoss.BuildingQuestion;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.constructions.position.FindPosition;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.AConsole;
import atlantis.util.We;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class ProduceBuilding {
    // See ProduceWorker: the executor registers on first wrapper load,
    // game root or test alike.
    static {
        atlantis.production.ProductionDecisionExecutor.register();
    }

    public static Construction _lastConstruction = null;

    /**
     * Names the refusing guard when a ready building order dies before
     * siting (NEXT #148: 20 Forge wants vanished with zero diagnostics
     * because every exit below was silent). Pure twin, no behavior.
     *
     * <p>Throttled per (guard, order): a demand stuck refused for minutes
     * names itself once per 300 frames, not once per evaluation (measured:
     * 2187 identical rows for one order in a game). First occurrence
     * always logs, so pins and hunts keep their witness.</p>
     */
    private static final java.util.Map<String, Integer> lastRefusedAt = new java.util.HashMap<>();

    private static boolean refuse(AUnitType type, ProductionOrder order, String guard) {
        String key = guard + ":" + (order == null ? "-" : order.id());
        Integer last = lastRefusedAt.get(key);
        int now = A.now();
        if (last == null || now - last >= 300) {
            lastRefusedAt.put(key, now);
            EventLog.key(LogKey.BUILD_REFUSED)
                .param("type", type == null ? "-" : type.name())
                .param("guard", guard)
                .param("order", order == null ? "-" : order.toString())
                .param("supplyUsed", A.supplyUsed())
                .param("minerals", A.minerals())
                .log(A.now(), A.minSec());
        }
        return false;
    }

    public static void clearRefusedThrottleForTests() {
        lastRefusedAt.clear();
    }

    /**
     * Single-door concurrency cap for Pylons (Wine game: three concurrent
     * Pylon warps out of one supply block at 25 - BO#8, its dynamic mirror
     * and BO#11, each firing alone correctly and tripling together). Below
     * 40 supply used at most one Pylon may be unfinished: a second order
     * waits for the warp (owner: 10+ free supply above 25 from overlapping
     * BO + dynamic Pylons; the goal is never more than 8 free supply
     * there, which in practice means one Pylon at a time). At 40+ the
     * pipeline pair stays: overlapping builds is how late-game supply
     * keeps up. The cap counts warping + not-started, so a row whose need
     * is already being built waits instead of re-siting. Stuck sites do
     * not deadlock this: overdue constructions are cancelled elsewhere
     * and the next row proceeds after the cancel.
     */
    public static boolean tooManyPylonsInFlight() {
        int inFlight = ConstructionRequests.countNotFinishedOfType(AUnitType.Protoss_Pylon);
        if (atlantis.game.A.supplyUsed() < 40) return inFlight >= 1;
        return inFlight >= 2;
    }

    /**
     * The order's own construction with a live builder is live demand -
     * the builder is dispatched or walking, and no second site may follow
     * it. Pure over the link: the status changer owns the statuses,
     * the door only reads them.
     */
    public static boolean hasLiveConstruction(ProductionOrder order) {
        if (order == null) return false;
        Construction live = order.construction();
        if (live == null) return false;
        if (live.status() != ConstructionOrderStatus.NOT_STARTED
            && live.status() != ConstructionOrderStatus.STARTING
            && live.status() != ConstructionOrderStatus.IN_PROGRESS) return false;
        return live.builder() != null && !live.builder().isDead();
    }

    /**
     * True when an unfinished building of this type is already warping for
     * exactly this order - the order's site became a building, and no
     * second site may follow it. Sibling rows link different order
     * instances, so they never match here.
     */
    public static boolean orderHasWarpingBuilding(ProductionOrder order, AUnitType type) {
        if (order == null) return false;
        for (atlantis.units.AUnit unit : atlantis.units.select.Select.ourUnfinished()
            .ofType(type).list()) {
            if (order.equals(unit.productionOrder())) return true;
        }
        return false;
    }

    /**
     * PurpleWave rule (owner 2026-10-19): one unstarted Pylon at a time.
     * A NOT_STARTED or STARTING Pylon construction for another order (or
     * an orderless one) means supply is already on the way with no unit
     * to show for it yet - the engine has nothing arriving for hundreds
     * of frames, but the builder is committed. Ordering a second Pylon
     * behind it is the 30/48-toward-38/65 slide. Own traces are
     * excluded (this order replacing its own lost construction must
     * proceed); PREPARING picks are excluded (someone's aimed future -
     * blocking on them could park an earlier need behind a later row).
     * An IN_PROGRESS Pylon with no linked building yet counts too: the
     * advance fired on the order echo while the server-side warp has not
     * registered, so no unit exists to count (measured: 65f-apart warp
     * pair behind one travelling builder).
     * Stuck sites unblock by themselves: overdue cancels clear them
     * and the count drops.
     *
     * <p>Above 60 supply two may fly at once (owner 2026-10-19: production
     * outruns warp completion there, holding to one starves the pump) -
     * the second unstarted is allowed, the third is not. Package-visible
     * count as a seam: the status question pins here.</p>
     */
    public static boolean anotherUnstartedPylonExists(ProductionOrder order) {
        return countUnstartedPylonsForOtherOrders(order) >= (A.supplyTotal() > 60 ? 2 : 1);
    }

    public static int countUnstartedPylonsForOtherOrders(ProductionOrder order) {
        int total = 0;
        for (Construction construction : ConstructionRequests.constructions) {
            if (construction == null || !construction.buildingType().isPylon()) continue;
            if (construction.status() != ConstructionOrderStatus.NOT_STARTED
                && construction.status() != ConstructionOrderStatus.STARTING
                && !isEchoWithoutUnit(construction)) continue;
            if (order != null && order.equals(construction.productionOrder())) continue;
            total++;
        }
        return total;
    }

    /**
     * An IN_PROGRESS Pylon with no linked building yet: the advance fired
     * on the order echo while the server-side warp has not registered -
     * no unit exists, so neither the warping-unit check nor the
     * not-started set sees it. Same rule as the V2 lag term: an
     * unconfirmed warp-in-progress still blocks the next Pylon.
     */
    static boolean isEchoWithoutUnit(Construction construction) {
        return construction.status() == ConstructionOrderStatus.IN_PROGRESS
            && construction.buildingUnit() == null;
    }

    /**
     * Every building request walks through the single door: the question
     * answers want, the call site names the act (BO line vs dynamic rule),
     * the dispatcher logs and executes. Gates underneath are untouched -
     * Phase A (__PLAN-DECISIONS).
     */
    public static boolean produceBuilding(AUnitType type, ProductionOrder order) {
        ADecision want = new BuildingQuestion().ask(GoalBoardAdvisor.snapshot());
        if (want == null) return false;

        return DecisionDispatcher.issue(want.refine(
            type.name(),
            demandReason(order),
            order != null && !order.isDynamic()
                ? ADecision.Source.BUILD_ORDER : ADecision.Source.DYNAMIC,
            order).withOrderId(order == null ? null : order.id()).atFrame(A.now()));
    }

    /**
     * Bank already spoken for by earlier-queued unfinished buildings:
     * the sum of their still-unpaid prices. A warping building has
     * already paid (its minerals left the bank at warp start), so it
     * holds no reserve - counting it would serialize every later
     * building behind a full warp-in (measured UMS 7th_rav: Pylon
     * waiting on a warping Core at bank 288). Own construction and
     * orderless ones excluded (no sequence to compare). Pure over the
     * live list, pinned in stub.
     */
    public static int earlierUnfinishedMinerals(ProductionOrder order) {
        return earlierUnfinishedCost(order, true);
    }

    public static int earlierUnfinishedGas(ProductionOrder order) {
        return earlierUnfinishedCost(order, false);
    }

    private static boolean isAlreadyPaidFor(Construction construction) {
        AUnit building = construction.buildingUnit();
        return building != null && building.isAlive();
    }

    private static int earlierUnfinishedCost(ProductionOrder order, boolean minerals) {
        if (order == null) return 0;
        int sum = 0;
        for (Construction other : ConstructionRequests.notFinished()) {
            ProductionOrder otherOrder = other.productionOrder();
            if (otherOrder == null || otherOrder == order) continue;
            if (otherOrder.id() >= order.id()) continue;
            if (other.buildingType() == null) continue;
            if (isAlreadyPaidFor(other)) continue;
            sum += minerals ? other.buildingType().mineralPrice() : other.buildingType().gasPrice();
        }
        return sum;
    }

    /**
     * Why this order fires now, in the row itself: the order line plus the
     * demand-side state behind it - free supply, unfinished buildings of
     * this type, the order's previous link (cid:STATUS or link-null when a
     * cancel/unlink dropped it) and whether this issues a fresh site or
     * adopts the prepared one. A repeat row for one order then names its
     * own cause (e.g. a re-request after an unlink) instead of reading as
     * an identical twin of the first row. Pure over primitives so the
     * shape pins without a game.
     */
    static String demandReason(ProductionOrder order) {
        String line = order == null ? "build-want" : order.toString();
        int free = A.supplyFree();
        int unfinished = order == null || order.unitType() == null
            ? 0 : ConstructionRequests.countNotFinishedOfType(order.unitType());
        String prev = "link-null";
        String path = "new";
        if (order != null && order.construction() != null) {
            prev = "cid" + order.construction().id() + ":" + order.construction().status();
            if (order.construction().status() == ConstructionOrderStatus.PREPARING) path = "adopt";
        }
        return demandContext(line, free, unfinished, prev, path);
    }

    public static String demandContext(String line, int freeSupply, int unfinishedOfType,
                                       String prevLink, String path) {
        return line + " [free=" + freeSupply + " unfinished=" + unfinishedOfType
            + " prev=" + prevLink + " path=" + path + "]";
    }

    /**
     * The legacy building issuance (supply, adoption, wallet and priority
     * gates included) - now reachable only through the domain executor,
     * never directly. Phase A.
     */
    public static boolean requestBuilding(AUnitType type, ProductionOrder order) {
        assert type.isABuilding();

        if (type.isZerg()) {
            return ProduceZergUnit.produceZergBuilding(type, order);
        }
        else if (type.isAddon()) {
            return ProduceAddon.produceAddon(type);
        }
        else {
            // Owner 2026-10-16: no building starts below its supply on a
            // poor bank - revokes the old one-early rule (early claims
            // held builders while the worker pump starved). Below
            // minSupply only price+50 builds. Site picking (PREPARING,
            // two early, no builder) is unaffected.
            if (A.supplyUsed() < order.minSupply()
                && !A.canAfford(type.mineralPrice() + 50, type.gasPrice())) return refuse(type, order, "below-min-supply-and-poor");

            // NEXT #57: an adopted construction with a living builder needs
            // no new request - the builder is dispatched or walking, and
            // re-requesting every frame piled same-tile duplicates (121
            // accepts in one game, totalOfType pinned at 1). Wait for warp,
            // cancel, or builder death instead. Builderless adoptions still
            // fall through to a fresh request below (status quo). STARTING
            // is the travelling half of the same pair (owner 2026-10-19):
            // unnamed here it minted a second site behind the walking
            // builder (Wine oid-72 storm: every BUILD row a fresh cid).
            if (hasLiveConstruction(order)) return refuse(type, order, "live-construction");

            // Owner 2026-10-17: single-instance structures (Core) refuse
            // while ANY is coming for another order - a sited sibling or
            // a warping building. Creation-time caps miss the
            // finish-removal vs re-request race (measured: #32 requested
            // one frame after #9 warped, #9's site already gone from the
            // list). Own traces excluded, so a same-order re-request after
            // a genuine loss still proceeds.
            if (type.equals(AUnitType.Protoss_Cybernetics_Core)
                && atlantis.production.constructions.OutstandingBuildings
                    .excluding(order, type) >= 1) return refuse(type, order, "core-single-instance");

            // Owner 2026-10-17: one supply block, one Pylon at a time (two
            // in flight max) - BO rows, the fallback and the supply
            // commander each fire alone correctly and triple together
            // (measured: warps at 3:31/3:35/3:45 from BO#8 + mirror + BO#11
            // at 25/25). The cap counts warping + not-started, so a row
            // whose need is already being built waits instead of re-siting.
            if (type.isPylon() && tooManyPylonsInFlight()) return refuse(type, order, "pylon-cap");

            // Owner: below 40 supply never more than 8 free - any Pylon
            // row (BO included: At-29 fired at 25/33 into 12 free) that
            // would push free past that waits. Above 40 the old 12-cap
            // in the queue door still applies.
            if (type.isPylon() && A.supplyUsed() < 40 && A.supplyFree() > 8) {
                return refuse(type, order, "pylon-overstock");
            }

            // Owner 2026-10-19 (PurpleWave rule): one unstarted Pylon at a
            // time. A NOT_STARTED or STARTING Pylon construction for
            // ANOTHER order means supply is already on the way with no
            // unit to show for it yet - ordering a second one behind the
            // walking builder is how 30/48 slides toward 38/65. The new
            // row waits as a plain queued order instead. Own traces are
            // excluded, so this order replacing its own lost construction
            // still proceeds; PREPARING picks are someone's aimed future
            // (blocking on them could park an earlier need behind a later
            // row into a real block). Stuck sites unblock by themselves:
            // overdue cancels clear them and the count drops.
            if (type.isPylon() && anotherUnstartedPylonExists(order)) return refuse(type, order, "unstarted-pylon");

            // Owner 2026-10-16: the order's building already warps - wait
            // for completion, do not site a second one. Warp-finish
            // releases the builder, so the guard above reads "builderless"
            // and re-requests the same order onto a fresh site (measured:
            // row #2 warped [16,48] at 1:20, re-requested [18,44] at
            // 1:48+1:52, two builders churned on it to game end). The link
            // is the order instance itself, so a sibling row of the same
            // type still proceeds - only this order's own warp blocks it.
            if (orderHasWarpingBuilding(order, type)) return refuse(type, order, "warping-link");

            // Earlier demand is reserved first (owner): a building may not
            // spend what an earlier-queued unfinished building still needs.
            // Earlier = smaller queue order id (own construction excluded,
            // orderless ones skipped - no sequence to compare). Check comes
            // before the wallet so a later row never eats an earlier row's
            // reserve, even when the bank looks affordable in isolation.
            // Only when the bank already covers this building alone: below
            // its own price the old aim-while-poor path decides (a walking
            // builder is free, the reserve question is moot).
            if (order != null
                && A.canAfford(type.mineralPrice(), type.gasPrice())
                && !A.canAfford(
                    type.mineralPrice() + earlierUnfinishedMinerals(order),
                    type.gasPrice() + earlierUnfinishedGas(order))) {
                return refuse(type, order, "earlier-reserve");
            }

            // Wallet enforcement (NEXT #93): a fresh builder claim for a
            // board-owned type needs funding - otherwise the worker is held
            // for a building the bank cannot cover yet (the #53 shape:
            // held workers do not mine). Active-but-unfunded waits a frame;
            // inactive demand, goalless types and stale funding stay open.
            // Queue order outranks wallet funding (owner): a READY
            // build-order row that covers its own price plus earlier
            // unfinished demand is issued even when the wallet funds
            // something else (measured UMS 7th_rav: BO Core #5 PREPARING
            // from 2:00, bank 250-370, unfunded behind a dynamic Gateway
            // while the queue order sat). Dynamic demand still needs
            // funding; affordability (own + earlier reserve) is checked
            // first, so the #53 shape cannot return through this door.
            boolean queueOrdered = order != null && !order.isDynamic();
            boolean affordable = A.canAfford(
                type.mineralPrice() + earlierUnfinishedMinerals(order),
                type.gasPrice() + earlierUnfinishedGas(order));
            if (!GoalBoardFeeder.adoptionAllowed(type) && !(queueOrdered && affordable)) {
                return refuse(type, order, "funding");
            }

            // Owner 2026-10-15: a waiting Core outranks Pylon spending
            // while contended (bounded, self-releasing - see
            // PriorityWaiters.pylonYieldsToCore). The 100 minerals stay
            // saved for the Core instead of going into a forward Pylon.
            if (type.isPylon() && PriorityWaiters.pylonYieldsToCore()) return refuse(type, order, "core-priority");

            // Owner 2026-10-19 (Wine: the At-22 Gateway's builder started
            // while its At-21 Pylon was still PREPARING, then the bank sat
            // below 100 for the Pylon while the Gateway held a worker on
            // unpowered ground): a power-needing building with no standing
            // Pylon waits until the bank covers the Pylon plus itself -
            // never send the worker before the power is paid for.
            if (powerProviderNotFunded(type)) return refuse(type, order, "power-not-funded");

            // A PREPARING construction already holds a picked site: adopt it
            // (or keep waiting for a free builder) instead of searching anew.
            if (order.construction() != null
                && order.construction().status() == ConstructionOrderStatus.PREPARING) {
                return adoptPreparedSite(order);
            }

            validatePosition(order, type);

            _lastConstruction = NewConstructionRequest.requestConstructionOf(order);
            return _lastConstruction != null;
        }
    }

    /**
     * Power before workers: sending a builder for a power-needing building
     * while no Pylon stands spends its price and then holds the worker on
     * unpowered ground while the bank refills for the Pylon. Wait until
     * the bank covers both (Pylon price + own price). Pylons themselves
     * never need power (their needsPower is false, so no first-Pylon
     * deadlock); warping counts as not built, per the owner's rule.
     */
    private static boolean powerProviderNotFunded(AUnitType type) {
        if (!We.protoss() || !type.needsPower()) return false;
        if (Count.ofType(AUnitType.Protoss_Pylon) > 0) return false;
        return !A.hasMinerals(
            AUnitType.Protoss_Pylon.mineralPrice() + type.mineralPrice());
    }

    /**
     * Owner 2026-10-14: a PREPARING construction already holds a picked
     * site - assign a builder and start instead of searching anew. No
     * builder free yet: stay PREPARING, retry next frames.
     */
    private static boolean adoptPreparedSite(ProductionOrder order) {
        Construction prepared = order.construction();
        if (prepared == null || prepared.status() != ConstructionOrderStatus.PREPARING) return false;

        // Owner 2026-10-17 (NEXT #150): a site picked early can be stale by
        // adoption time - a sibling may have started warping onto it after
        // the pick (measured Wine: GW#3 [112,12] picked the frame GW#2
        // warped at [112,10], shared tile row; the pick-time guards are
        // blind to a same-frame warp). Re-pick instead of adopting a site
        // the engine will refuse at issue time. The same holds for a
        // sibling that FINISHED onto it since the pick (measured: Pylon
        // adopts on [119,12] cancelled same-frame against a standing
        // Pylon) - a covered site is never adopted, only re-sited.
        if (OverlapsOurUnfinishedBuilding.check(prepared.buildPosition(), prepared.buildingType())
            || OverlapsOurStandingBuilding.check(
                prepared.buildPosition(), prepared.buildingType())) {
            APosition fresh = prepared.findPositionForNewBuilding();
            if (fresh == null || OverlapsOurUnfinishedBuilding.check(fresh, prepared.buildingType())) {
                EventLog.key(LogKey.BUILD_REFUSED)
                    .param("type", prepared.buildingType().name())
                    .param("guard", "adopt-site-blocked")
                    .param("order", order.toString())
                    .param("supplyUsed", A.supplyUsed())
                    .param("minerals", A.minerals())
                    .log(A.now(), A.minSec());
                return false;
            }
            APosition prev = prepared.buildPosition();
            prepared.setPositionToBuild(fresh);
            EventLog.key(LogKey.POSITION_REFRESH)
                .param("type", prepared.buildingType().name())
                .param("result", "adopt-resited")
                .param("prevTile", prev == null ? "-" : prev.toString())
                .param("tile", fresh.toString())
                .log(A.now(), A.minSec());
        }

        // Owner 2026-10-19: re-check at send time, not just at pick time -
        // the bank may have dropped below Pylon+self price while the site
        // waited for a free builder. Same guard as the request door.
        if (powerProviderNotFunded(prepared.buildingType())) {
            EventLog.key(LogKey.BUILD_REFUSED)
                .param("type", prepared.buildingType().name())
                .param("guard", "power-not-funded")
                .param("order", order.toString())
                .param("supplyUsed", A.supplyUsed())
                .param("minerals", A.minerals())
                .log(A.now(), A.minSec());
            return false;
        }

        // Owner 2026-10-18: prefer the worker reserved at pick time - it
        // was free then, and skipping the search keeps adoption instant.
        // Fall back to the optimal search when it died, got claimed, or
        // was never reserved.
        AUnit reserved = prepared.reservedBuilder();
        if (reserved != null && reserved.isAlive() && !reserved.isBuilder()) {
            prepared.setBuilder(reserved);
        }
        else {
            prepared.assignOptimalBuilder();
        }
        if (prepared.builder() == null) {
            EventLog.key(LogKey.BUILD_REFUSED)
                .param("type", prepared.buildingType().name())
                .param("guard", "adopt-no-free-builder")
                .param("order", order.toString())
                .param("supplyUsed", A.supplyUsed())
                .param("minerals", A.minerals())
                .log(A.now(), A.minSec());
            return false;
        }

        prepared.setStatus(ConstructionOrderStatus.NOT_STARTED);
        _lastConstruction = prepared;
        EventLog.key(LogKey.BUILDING_STARTED)
            .param("type", prepared.buildingType().name())
            .param("cid", prepared.id())
            .param("oid", order == null ? null : order.id())
            .param("tile", prepared.buildPosition() == null ? "-"
                : "[" + prepared.buildPosition().tx() + "," + prepared.buildPosition().ty() + "]")
            .param("builder", prepared.builder().id())
            .param("supplyUsed", A.supplyUsed())
            .param("expectedSupply", order.minSupply())
            .param("supplyTotal", A.supplyTotal())
            .param("minerals", A.minerals())
            .log(A.now(), A.minSec());
        return true;
    }

    private static void validatePosition(ProductionOrder order, AUnitType type) {
        HasPosition atPosition = order.aroundPosition();
        if (atPosition == null) return;

        if (type.isBase() || type.isGasBuilding()) return;

        APosition newPosition = FindPosition.findForBuilding(
            FreeWorkers.get().nearestTo(atPosition), type, null, atPosition, order.maximumDistance()
        );

        if (newPosition != null) {
            order.setAroundPosition(newPosition);
        }
        else {
            AConsole.errPrintln("^^^^^^^^^^^^^^^^^^^ CAN'T FIND POSITION FOR " + type + " at " + atPosition);
        }
    }
}
