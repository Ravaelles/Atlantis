package atlantis.production.orders.production.queue.order;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.information.tech.ATechRequests;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.production.requests.produce.ProduceUnit;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ProductionOrderHandler extends Commander {
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
            Missions.setGlobalMissionTo(order.mission(), "Build Order enforced: " + order.mission());
        }

        // === Nothing! ============================================

        else {
            System.err.println(order + " was not handled at all!");
        }
    }
}
