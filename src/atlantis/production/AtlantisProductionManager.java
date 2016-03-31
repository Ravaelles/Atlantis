package atlantis.production;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.production.orders.AtlantisBuildOrders;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

import bwapi.UnitType;
import bwapi.UpgradeType;

public class AtlantisProductionManager {

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    protected static void update() {
        AtlantisBuildOrders productionStrategy = AtlantisConfig.getBuildOrders();

        ArrayList<ProductionOrder> produceNow = productionStrategy.getThingsToProduceRightNow(false);
        for (ProductionOrder order : produceNow) {

            // =========================================================
            // Produce UNIT
            if (order.getUnitType() != null) {
                AUnitType unitType = order.getUnitType();
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
        }
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
            AtlantisBuildOrders.getBuildOrders().produceWorker();
        } 

        // =========================================================
        // Non-worker so combat units and special units like Scarabs etc.
        else { 
            AtlantisBuildOrders.getBuildOrders().produceUnit(unitType);
        } 
    }

    private static void researchUpgrade(UpgradeType upgrade) {
        AUnitType buildingType = AUnitType.createFrom(upgrade.whatUpgrades()); //AUnitType.getUnitType(upgrade.getWhatUpgradesTypeID());
        if (buildingType != null) {
            AUnit building = (AUnit) Select.ourBuildings().ofType(buildingType).first();
            if (building != null) {
                building.upgrade(upgrade);
            }
        }
    }

    // =========================================================
    // Lo-level produce
    
    private static void produceBuilding(AUnitType unitType, ProductionOrder order) {
        if (!unitType.isBuilding()) {
            System.err.println("produceBuilding has been given wrong argument: " + unitType);
        }
        AtlantisConstructingManager.requestConstructionOf(unitType, order, null);
    }
    
}
