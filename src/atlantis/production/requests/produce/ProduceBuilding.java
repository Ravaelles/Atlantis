package atlantis.production.requests.produce;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.DecisionDispatcher;
import atlantis.decisions.goals.protoss.BuildingQuestion;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.Construction;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.constructions.position.FindPosition;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.AConsole;
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
     * Single-door concurrency cap for Pylons (Wine game: three concurrent
     * Pylon warps out of one supply block at 25 - BO#8, its dynamic mirror
     * and BO#11, each firing alone correctly and tripling together). At
     * most two unfinished Pylons (warping + not-started): the
     * pipeline-overlap doctrine number, enforced for every author here
     * instead of per path. A row whose need is already being built waits
     * as a plain queued order and fires when a warp completes.
     */
    public static boolean tooManyPylonsInFlight() {
        return ConstructionRequests.countNotFinishedOfType(AUnitType.Protoss_Pylon) >= 2;
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
            order == null ? "build-want" : order.toString(),
            order != null && !order.isDynamic()
                ? ADecision.Source.BUILD_ORDER : ADecision.Source.DYNAMIC,
            order).atFrame(A.now()));
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
                && !A.canAfford(type.mineralPrice() + 50, type.gasPrice())) return false;

            // NEXT #57: an adopted construction with a living builder needs
            // no new request - the builder is dispatched or walking, and
            // re-requesting every frame piled same-tile duplicates (121
            // accepts in one game, totalOfType pinned at 1). Wait for warp,
            // cancel, or builder death instead. Builderless adoptions still
            // fall through to a fresh request below (status quo).
            Construction live = order.construction();
            if (live != null
                && (live.status() == ConstructionOrderStatus.NOT_STARTED
                    || live.status() == ConstructionOrderStatus.IN_PROGRESS)
                && live.builder() != null && !live.builder().isDead()) return false;

            // Owner 2026-10-17: single-instance structures (Core) refuse
            // while ANY is coming for another order - a sited sibling or
            // a warping building. Creation-time caps miss the
            // finish-removal vs re-request race (measured: #32 requested
            // one frame after #9 warped, #9's site already gone from the
            // list). Own traces excluded, so a same-order re-request after
            // a genuine loss still proceeds.
            if (type.equals(AUnitType.Protoss_Cybernetics_Core)
                && atlantis.production.constructions.OutstandingBuildings
                    .excluding(order, type) >= 1) return false;

            // Owner 2026-10-17: one supply block, one Pylon at a time (two
            // in flight max) - BO rows, the fallback and the supply
            // commander each fire alone correctly and triple together
            // (measured: warps at 3:31/3:35/3:45 from BO#8 + mirror + BO#11
            // at 25/25). The cap counts warping + not-started, so a row
            // whose need is already being built waits instead of re-siting.
            if (type.isPylon() && tooManyPylonsInFlight()) return false;

            // Owner 2026-10-16: the order's building already warps - wait
            // for completion, do not site a second one. Warp-finish
            // releases the builder, so the guard above reads "builderless"
            // and re-requests the same order onto a fresh site (measured:
            // row #2 warped [16,48] at 1:20, re-requested [18,44] at
            // 1:48+1:52, two builders churned on it to game end). The link
            // is the order instance itself, so a sibling row of the same
            // type still proceeds - only this order's own warp blocks it.
            if (orderHasWarpingBuilding(order, type)) return false;

            // Wallet enforcement (NEXT #93): a fresh builder claim for a
            // board-owned type needs funding - otherwise the worker is held
            // for a building the bank cannot cover yet (the #53 shape:
            // held workers do not mine). Active-but-unfunded waits a frame;
            // inactive demand, goalless types and stale funding stay open.
            if (!GoalBoardFeeder.adoptionAllowed(type)) return false;

            // Owner 2026-10-15: a waiting Core outranks Pylon spending
            // while contended (bounded, self-releasing - see
            // PriorityWaiters.pylonYieldsToCore). The 100 minerals stay
            // saved for the Core instead of going into a forward Pylon.
            if (type.isPylon() && PriorityWaiters.pylonYieldsToCore()) return false;

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
     * Owner 2026-10-14: a PREPARING construction already holds a picked
     * site - assign a builder and start instead of searching anew. No
     * builder free yet: stay PREPARING, retry next frames.
     */
    private static boolean adoptPreparedSite(ProductionOrder order) {
        Construction prepared = order.construction();
        if (prepared == null || prepared.status() != ConstructionOrderStatus.PREPARING) return false;

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
        if (prepared.builder() == null) return false;

        prepared.setStatus(ConstructionOrderStatus.NOT_STARTED);
        _lastConstruction = prepared;
        EventLog.key(LogKey.BUILDING_STARTED)
            .param("type", prepared.buildingType().name())
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
