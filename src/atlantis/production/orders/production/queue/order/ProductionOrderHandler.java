package atlantis.production.orders.production.queue.order;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.tech.ATechRequests;
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
        if (order.isConsumed()) {
            A.errPrintln("Order " + order + " is already consumed!");
            order.cancel();
            return;
        }

        // =========================================================

        // Produce UNIT
        if (order.unitType() != null) {
            AUnitType unitType = order.unitType();

            if (order.construction() != null) {
                ErrorLog.printMaxOncePerMinute("Construction already begun for " + order);
                return;
            }

            if (unitType.isABuilding()) {
                if (ProduceBuilding.produceBuilding(unitType, order)) {
                    order.consume();
                }
            }
            else {
                if (ProduceUnit.produceUnit(unitType, order)) {
                    order.consume();
                }
            }
        }

        // =========================================================
        // Produce UPGRADE

        else if (order.upgrade() != null) {
            UpgradeType upgrade = order.upgrade();
            if (ATechRequests.researchUpgrade(upgrade)) {
                order.consume();
            }
        }

        // =========================================================
        // Produce TECH

        else if (order.tech() != null) {
            TechType tech = order.tech();
            if (ATechRequests.researchTech(tech)) {
                order.consume();
            }
        }

        // =========================================================
        // Mission CHANGE

        else if (order.mission() != null) {
            if (TIMES_MISSION_ENFORCED <= 2) {
                Missions.setGlobalMissionTo(order.mission(), "Build Order enforced: " + order.mission());
                TIMES_MISSION_ENFORCED++;

                order.consume();
            }
        }

        // === Nothing! ============================================

        else {
            ErrorLog.printMaxOncePerMinute(order + " was not handled at all!");
        }
    }
}
