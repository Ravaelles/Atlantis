package atlantis.production;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructionManager;
import atlantis.production.orders.AtlantisBuildOrdersManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AtlantisProductionManager {

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    protected static void update() {
        
        // === Handle UMT ==========================================
        
        if (AtlantisGame.isUmtMode()) {
            return;
        }
        
        // =========================================================
        
        // Get build orders (aka production orders) from the manager
        AtlantisBuildOrdersManager buildOrdersManager = AtlantisConfig.getBuildOrders();

        ArrayList<ProductionOrder> produceNow = buildOrdersManager.getThingsToProduceRightNow(
                AtlantisBuildOrdersManager.MODE_ALL_ORDERS
        );
        for (ProductionOrder order : produceNow) {
            System.out.println(order + ": " + order.getUpgrade());

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
                System.out.println("upgrade = " + upgrade);
                researchUpgrade(upgrade);
            }

            // =========================================================
            // Produce TECH
            else if (order.getTech()!= null) {
                TechType tech = order.getTech();
                researchTech(tech);
            }
        }
        
        // === Fix - refresh entire queue ==============================
        
        AtlantisBuildOrdersManager.getBuildOrders().getProductionQueueNext(20);
    }

    // =========================================================
    // Hi-level produce
    
    private static void produceUnit(AUnitType unitType) {
        
        // Supply: OVERLORD / PYLON / DEPOT
        if (AtlantisGame.getSupplyFree() == 0 && !unitType.isSupplyUnit() && !unitType.isBuilding()) {
            // Supply production is handled by AtlantisSupplyManager
            return;
        }

        // =========================================================
        // Worker
        if (unitType.equals(AtlantisConfig.WORKER)) {
            AtlantisBuildOrdersManager.getBuildOrders().produceWorker();
        } 

        // =========================================================
        // Non-worker so combat units and special units like Scarabs etc.
        else { 
            AtlantisBuildOrdersManager.getBuildOrders().produceUnit(unitType);
        } 
    }

    private static void researchUpgrade(UpgradeType upgrade) {
        System.err.println("Research " + upgrade);
        AUnitType buildingType = AUnitType.createFrom(upgrade.whatUpgrades());
        System.out.println("buildingType = " + buildingType);
        if (buildingType != null) {
            AUnit building = (AUnit) Select.ourBuildings().ofType(buildingType).first();
            System.out.println("building = " + building);
            if (building != null) {
                boolean result = building.upgrade(upgrade);
                System.out.println("result = " + result);
            }
        }
    }

    private static void researchTech(TechType tech) {
        AUnitType buildingType = AUnitType.createFrom(tech.whatResearches());
        if (buildingType != null) {
            AUnit building = (AUnit) Select.ourBuildings().ofType(buildingType).first();
            if (building != null) {
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
        AtlantisConstructionManager.requestConstructionOf(unitType, order, null);
    }
    
}
