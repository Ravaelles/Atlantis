package atlantis.production;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.production.strategies.AtlantisProductionStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

import bwapi.UnitType;
import bwapi.UpgradeType;

public class AtlantisProduceUnitManager {

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    protected static void update() {
        AtlantisProductionStrategy productionStrategy = AtlantisConfig.getProductionStrategy();

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
//    public static boolean isCurrentlyProducing(AUnitType unitType) {
//
//    }
    private static void produceUnit(AUnitType unitType) {
        
        // Supply: OVERLORD / PYLON / DEPOT
        if (AtlantisGame.getSupplyFree() == 0 && !unitType.isSupplyUnit() && !unitType.isBuilding()) {
            return;
        }

        // Worker
        if (unitType.equals(AtlantisConfig.WORKER)) {
            produceWorker();
        } 

        // =========================================================
        // Infantry
        else if (unitType.isOrganic()) { //replaces  isInfantry()
            produceInfantry(unitType);
        } 

        // =========================================================
        // We don't know what to do
        else {
            System.err.println("UNHANDLED UNIT TYPE TO PRODUCE: " + unitType);
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
    
    private static void produceWorker() {
        AtlantisConfig.getProductionStrategy().produceWorker();
    }

    private static void produceInfantry(AUnitType infantryType) {
        AtlantisConfig.getProductionStrategy().produceInfantry(infantryType);
    }

}
