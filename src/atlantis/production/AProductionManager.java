package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.production.orders.ABuildOrderManager;
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
        ArrayList<ProductionOrder> produceNow = ABuildOrderManager.getThingsToProduceRightNow(
                ABuildOrderManager.MODE_ALL_ORDERS
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
        
        ABuildOrderManager.getProductionQueueNext(20);
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
            ABuildOrderManager.getCurrentBuildOrder().produceWorker();
        } 

        // =========================================================
        // Non-worker so combat units and special units like Scarabs etc.
        else { 
            ABuildOrderManager.getCurrentBuildOrder().produceUnit(unitType);
        } 
    }

    private static void researchUpgrade(UpgradeType upgrade) {
        AUnitType buildingType = AUnitType.createFrom(upgrade.whatUpgrades());
//        System.out.println("Research " + upgrade + " in " + buildingType);
        if (buildingType != null) {
            AUnit building = (AUnit) Select.ourBuildings().ofType(buildingType).first();
//            System.out.println(upgrade + " level is " + AGame.getPlayerUs().getUpgradeLevel(upgrade));
            if (building != null && !building.isBusy()) {
//                System.out.println("   ISSUE");
                building.upgrade(upgrade);
            }
        }
    }

    private static void researchTech(TechType tech) {
        AUnitType buildingType = AUnitType.createFrom(tech.whatResearches());
        if (buildingType != null) {
            AUnit building = (AUnit) Select.ourBuildings().ofType(buildingType).first();
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
        AConstructionManager.requestConstructionOf(unitType, order, null);
    }
    
}
