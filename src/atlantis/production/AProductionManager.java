package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.orders.*;
import atlantis.tech.ATechRequests;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.util.ArrayList;

public class AProductionManager {

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    protected static void update() {

        // Get sequence of units (Production Orders) based on current build order
        ArrayList<ProductionOrder> queue = CurrentProductionOrders.thingsToProduce(ProductionQueueMode.ONLY_WHAT_CAN_AFFORD);
        for (ProductionOrder order : queue) {
            handleProductionOrder(order);
        }
    }

    private static void handleProductionOrder(ProductionOrder order) {

        // Produce UNIT
        if (order.getUnitOrBuilding() != null) {
            AUnitType unitType = order.getUnitOrBuilding();
            if (unitType.isBuilding()) {
                produceBuilding(unitType, order);
            } else {
                produceUnit(unitType);
            }
        }

        // =========================================================
        // Produce UPGRADE

        else if (order.getUpgrade() != null) {
            UpgradeType upgrade = order.getUpgrade();
            ATechRequests.researchUpgrade(upgrade);
        }

        // =========================================================
        // Produce TECH

        else if (order.getTech()!= null) {
            TechType tech = order.getTech();
            ATechRequests.researchTech(tech);
        }

        // === Nothing! ============================================

        else {
            System.err.println(order + " was not handled at all!");
        }
    }

    // =========================================================
    // =========================================================
    // =========================================================

    public static boolean produceWorker() {
        return CurrentBuildOrder.get().produceWorker();
    }

    private static boolean produceUnit(AUnitType unitType) {
        assert !unitType.isBuilding();

        // Supply: OVERLORD / PYLON / DEPOT
//        if (AGame.supplyFree() == 0 && !unitType.isSupplyUnit()) {
//            // Supply production is handled by AtlantisSupplyManager
//            return false;
//        }

        // =========================================================
        // Worker

        if (unitType.equals(AtlantisConfig.WORKER)) {
            return produceWorker();
        } 

        // =========================================================
        // Non-worker

        else if (AGame.canAffordWithReserved(50 + unitType.getMineralPrice(), unitType.getGasPrice())) {
            return CurrentBuildOrder.get().produceUnit(unitType);
        }

        return false;
    }

    private static void produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isBuilding();

        AConstructionRequests.requestConstructionOf(order);
    }
    
}
