package atlantis.game.listeners;

import atlantis.game.A;
import atlantis.game.event.Events;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.protoss.ProtossWarping;
import atlantis.production.constructions.terran.TerranNewBuilding;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.production.orders.production.queue.order.OrderStatus.IN_PROGRESS;

public class OnOurUnitCreated {
    public static void update(AUnit unit) {
        //        System.err.println("Our unit created: " + unit);

        Count.clearCache();
        Select.clearCache();

        // === Building ===========================================

        if (unit.isABuilding()) {
            ProtossWarping.updateNewBuildingJustWarped(unit);
            TerranNewBuilding.updateNewBuilding(unit);

            if (unit.isBase()) {
                ExpansionCommander.justExpanded();
                CancelNotStartedBases.cancelNotStartedOrEarlyBases(
                    unit, "New base created, remove not started ones"
                );
            }

            Construction construction = unit.construction();
            if (construction == null && !unit.type().isAddon() && !unit.type().isGasBuilding()) {
                A.errPrintln("No construction for " + unit);
            }
//            if (construction != null) {
//                construction.releaseReservedResources();
//            }

            ProductionOrder order = unit.productionOrder();
            if (order != null) order.releasedReservedResources();
            else if (construction != null) construction.releaseReservedResources();

            if (We.protoss() && unit.type().isPylon() && Select.countOurOfTypeWithUnfinished(AUnitType.Protoss_Pylon) == 1) {
                AUnit builder = unit.construction() == null ? null : unit.construction().builder();
                Events.dispatch("FirstPylonUnitCreated", unit, builder);
            }
        }

        // === Regular unit ===========================================

        else {
            // ---
        }

        // =========================================================

        updateProductionOrderToInProgress(unit);

        // =========================================================

        Queue.get().refresh();

        if (unit.isABuilding()) {
            if (unit.isBase()) OurClosestBaseToEnemy.clearCache();
        }

        // CENTER CAMERA ON THE FIRST BUNKER
//        if (unit.isBunker() && Env.isLocal() && Count.bunkers() == 0) CameraCommander.centerCameraOn(unit);
    }

    private static void updateProductionOrderToInProgress(AUnit unit) {
        Construction construction = unit.construction();
        if (construction != null) {
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        }
        else {
            if (unit.isABuilding()) System.err.println("No Construction for " + unit);
        }

        ProductionOrder order = unit.productionOrder();
        order = assignOrderToUnitIfRelationMissing(unit, order);

        if (order == null) {
            if (!unit.isWorker()) System.err.println("No ProductionOrder for " + unit);
            return;
        }
        order.setStatus(IN_PROGRESS);
    }

    private static ProductionOrder assignOrderToUnitIfRelationMissing(AUnit unit, ProductionOrder order) {
        if (unit.isWorker()) return null;

        if (order == null) {
            order = defineProductionOrderForUnit(unit, order);

            if (order != null) unit.setProductionOrder(order);
        }

        return order;
    }

    private static ProductionOrder defineProductionOrderForUnit(AUnit unit, ProductionOrder order) {
        if (unit.isABuilding()) {
            order = Queue.get().readyToProduceOrders().ofType(unit.type()).first();
            if (unit == null) order = Queue.get().inProgressOrders().ofType(unit.type()).first();
            if (unit == null) order = Queue.get().notStarted().ofType(unit.type()).first();
        }
        else {
            AUnitType parentType = unit.type().whatBuildsIt();
            if (parentType == null) return null;

            AUnit parent = Select.ourOfType(parentType).nearestTo(unit);
            if (parent == null) return null;

            // That object is the only way to pass argument from parent to unit
            order = parent.productionOrder();
        }

        return order;
    }
}
