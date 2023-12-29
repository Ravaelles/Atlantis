package atlantis.production.orders.production.queue.order;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.tech.ATechRequests;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.production.requests.produce.ProduceUnit;
import atlantis.units.AUnitType;
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

        // Produce UNIT
        if (order.unitType() != null) {
            AUnitType unitType = order.unitType();

            if (unitType.isABuilding()) {
                ProduceBuilding.produceBuilding(unitType, order);
            }
            else {
                ProduceUnit.produceUnit(unitType, order);
            }
        }

        // =========================================================
        // Produce UPGRADE

        else if (order.upgrade() != null) {
            UpgradeType upgrade = order.upgrade();
            ATechRequests.researchUpgrade(upgrade);
        }

        // =========================================================
        // Produce TECH

        else if (order.tech() != null) {
            TechType tech = order.tech();
            ATechRequests.researchTech(tech);
        }

        // =========================================================
        // Mission CHANGE

        else if (order.mission() != null) {
            if (TIMES_MISSION_ENFORCED <= 2) {
                Missions.setGlobalMissionTo(order.mission(), "Build Order enforced: " + order.mission());

                TIMES_MISSION_ENFORCED++;
            }
        }

        // === Nothing! ============================================

        else {
            System.err.println(order + " was not handled at all!");
        }
    }
}
