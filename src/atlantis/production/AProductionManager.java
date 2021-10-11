package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.orders.AProductionQueue;
import atlantis.production.orders.AProductionQueueManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.util.ArrayList;

public class AProductionManager {

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    protected static void update() {
        
        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode()) {
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
                researchUpgrade(upgrade);
            }

            // =========================================================
            // Produce TECH

            else if (order.getTech()!= null) {
                TechType tech = order.getTech();
                researchTech(tech);
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

    private static void researchUpgrade(UpgradeType upgrade) {
        AUnitType buildingType = AUnitType.createFrom(upgrade.whatUpgrades());
        if (buildingType != null) {
            AUnit building = (AUnit) Select.ourBuildings().ofType(buildingType).first();
            if (building != null && !building.isBusy()) {
                building.upgrade(upgrade);
            }
        }
    }

    private static void researchTech(TechType tech) {
        AUnitType buildingType = AUnitType.createFrom(tech.whatResearches());
        if (buildingType != null) {
            AUnit building = Select.ourBuildings().ofType(buildingType).first();
            if (building != null && !building.isBusy()) {
                building.research(tech);
            }
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
