package atlantis.production.requests.produce;

import atlantis.game.A;
import atlantis.map.position.APosition;
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
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.AConsole;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class ProduceBuilding {
    public static Construction _lastConstruction = null;

    public static boolean produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isABuilding();

        if (type.isZerg()) {
            return ProduceZergUnit.produceZergBuilding(type, order);
        }
        else if (type.isAddon()) {
            return ProduceAddon.produceAddon(type);
        }
        else {
            // Owner 2026-10-15: never start more than one supply early -
            // assigning the builder is the economic cost (the probe stops
            // mining), so a row due at 28 waits even when everything else
            // is ready. Site picking (PREPARING, two early, no builder)
            // is unaffected.
            if (A.supplyUsed() < order.minSupply() - 1) return false;

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

        prepared.assignOptimalBuilder();
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
