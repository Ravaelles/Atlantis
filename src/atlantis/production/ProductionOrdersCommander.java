package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.information.tech.ATechRequests;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionQueueMode;
import atlantis.production.requests.ProduceUnitNow;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

public class ProductionOrdersCommander extends Commander {
    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    @Override
    public void handle() {
        // Get sequence of units (Production Orders) based on current build order
        ArrayList<ProductionOrder> queue = CurrentProductionQueue.thingsToProduce(ProductionQueueMode.ONLY_WHAT_CAN_AFFORD);
        for (ProductionOrder order : queue) {
            AUnitType base = AtlantisConfig.BASE;

            if (ConstructionRequests.countNotStartedOfType(base) > 0) {
                if (!A.hasMinerals(base.getMineralPrice() + order.mineralPrice())) {
                    return;
                }
            }

            try {
                handleProductionOrder(order);
            }
            catch (Exception e) {
                CurrentProductionQueue.remove(order);
                System.err.println("Cancelled " + order + " as there was a problem with it.");
                throw e;
            }
        }
    }

    private void handleProductionOrder(ProductionOrder order) {

        // Produce UNIT
        if (order.unitType() != null) {
            AUnitType unitType = order.unitType();
//            System.out.println("PRODUCE NOW unitType = " + unitType);
            if (unitType.isBuilding()) {
                ProduceUnitNow.produceBuilding(unitType, order);
            } else {
                ProduceUnitNow.produceUnit(unitType);
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
