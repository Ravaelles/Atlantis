package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.orders.AProductionQueue;
import atlantis.production.orders.AProductionQueueManager;
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
        
        // === Handle UMS ==========================================
        
        if (AGame.isUms()) {
            return;
        }
        
        // =========================================================
        
        // Get build orders (aka production orders) from the manager
        ArrayList<ProductionOrder> produceNow = AProductionQueueManager.getThingsToProduceRightNow(
                AProductionQueue.MODE_ALL_ORDERS
        );
        for (ProductionOrder order : produceNow) {

            // =========================================================
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
        
        // === Fix - refresh entire queue ==============================
        
        AProductionQueue.getProductionQueueNext(20);
    }

    // =========================================================
    // Hi-level produce
    
    private static void produceUnit(AUnitType unitType) {
        
        // Supply: OVERLORD / PYLON / DEPOT
        if (AGame.getSupplyFree() == 0 && !unitType.isSupplyUnit() && !unitType.isBuilding()) {
            // Supply production is handled by AtlantisSupplyManager
            return;
        }

        // =========================================================
        // Worker

        if (unitType.equals(AtlantisConfig.WORKER)) {
            AProductionQueue.getCurrentBuildOrder().produceWorker();
        } 

        // =========================================================
        // Non-worker so combat units and special units like Scarabs etc.

        else { 
            AProductionQueue.getCurrentBuildOrder().produceUnit(unitType);
        } 
    }

    // =========================================================
    // Lo-level produce
    
    private static void produceBuilding(AUnitType unitType, ProductionOrder order) {
        if (!unitType.isBuilding()) {
            System.err.println("produceBuilding has been given wrong argument: " + unitType);
        }
        AConstructionRequests.requestConstructionOf(unitType, order, null);
    }
    
}
