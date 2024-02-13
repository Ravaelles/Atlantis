package atlantis.production.orders.production.queue.order;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.information.tech.ATechRequests;
import atlantis.production.orders.production.queue.add.PreventDuplicateOrders;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.production.requests.produce.ProduceUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ProductionOrderHandler extends Commander {
    private static int TIMES_MISSION_ENFORCED = 0;

    private ProductionOrder order;

    public ProductionOrderHandler(ProductionOrder order) {
        this.order = order;
    }

    @Override
    protected void handle() {
        if (isAlreadyConsumed()) {
//            A.errPrintln("Order " + order + " is already consumed!");
            order.setStatus(OrderStatus.COMPLETED);
            return;
        }

        // =========================================================

        // Produce UNIT
        if (order.unitType() != null) {
            AUnitType unitType = order.unitType();

            if (order.construction() != null) {
//                ErrorLog.printMaxOncePerMinute("Construction already begun for " + order);
                return;
            }

            if (unitType.isABuilding()) {
                if (ProduceBuilding.produceBuilding(unitType, order)) {
                    order.consume();
                    order.setStatus(OrderStatus.IN_PROGRESS);
                }
            }
            else {
                if (ProduceUnit.produceUnit(unitType, order)) {
                    order.consume();
                    order.setStatus(OrderStatus.IN_PROGRESS);
                }
            }
        }

        // =========================================================
        // Produce UPGRADE

        else if (order.upgrade() != null) {
            UpgradeType upgrade = order.upgrade();
            if (ATechRequests.researchUpgrade(upgrade)) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                order.consume();

                PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(upgrade);
            }
        }

        // =========================================================
        // Produce TECH

        else if (order.tech() != null) {
            TechType tech = order.tech();
            if (ATechRequests.researchTech(tech)) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                order.consume();

                PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(tech);
            }
        }

        // =========================================================
        // Mission CHANGE

        else if (order.mission() != null) {
            if (TIMES_MISSION_ENFORCED <= 2) {
                Missions.setGlobalMissionTo(order.mission(), "Build Order enforced: " + order.mission());
                TIMES_MISSION_ENFORCED++;

                order.setStatus(OrderStatus.COMPLETED);
                order.consume();
                order.cancel();
            }
        }

        // === Nothing! ============================================

        else {
            ErrorLog.printMaxOncePerMinute(order + " was not handled at all!");
        }
    }

    private boolean isAlreadyConsumed() {
        if (!order.isConsumed()) return false;

        if (
            order.isBuilding() && (
                order.construction() == null
                    || order.construction().buildingUnit() == null
                    || order.construction().buildingUnit().hp() <= 0
                    || order.construction().buildingUnit().isDead()
            )
        ) return false;

//        if (order.isBuilding()) {
//            A.errPrintln("Building " + order + " is already consumed! Const = " + order.construction());
//        }

        return true;
    }
}
