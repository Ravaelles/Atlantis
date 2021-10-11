package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.buildings.managers.AExpansionManager;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.orders.AProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Count;
import atlantis.units.Select;


public abstract class ADynamicBuildingsManager {

    public static void update() {
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AExpansionManager.requestNewBaseIfNeeded();
        
        // If number of bases is bigger than gas buildings, it usually makes sense to build new gas extractor
        gasBuildingIfNeeded();
    }
    
    // =========================================================

    /**
     * Build Refineries/Assimilators/Extractors when it makes sense.
     */
    private static void gasBuildingIfNeeded() {
        if (AGame.everyNthGameFrame(50)) {
            return;
        }
        
        // =========================================================
        
        int numberOfBases = Select.ourBases().count();
        int numberOfGasBuildings = Select.ourIncludingUnfinished().ofType(AtlantisConfig.GAS_BUILDING).count();
        if (
            numberOfBases >= 2
            && numberOfBases > numberOfGasBuildings && !AGame.canAfford(0, 350)
            && AConstructionRequests.countNotStartedConstructionsOfType(AtlantisConfig.GAS_BUILDING) == 0
            && hasABaseWithFreeGeyser()
        ) {
            AConstructionRequests.requestConstructionOf(AtlantisConfig.GAS_BUILDING);
        }
    }

    // =========================================================

    protected static void buildToHaveOne(int minSupply, AUnitType type) {
        if (AGame.getSupplyUsed() >= minSupply) {
            buildToHaveOne(type);
        }
    }

    protected static void buildToHaveOne(AUnitType type) {
        if (Count.ofType(type) > 0) {
            return;
        }

        buildIfCanAfford(type, true, type.getMineralPrice(), type.getGasPrice());
    }

    protected static void buildIfCanAfford(AUnitType type) {
        buildIfCanAfford(type, true, type.getMineralPrice(), type.getGasPrice());
    }

    protected static void buildIfAllBusyButCanAfford(AUnitType type) {
        if (Select.ourOfType(type).areAllBusy()) {
            buildIfCanAfford(type, true, type.getMineralPrice(), type.getGasPrice());
        }
    }

    protected static void buildIfCanAfford(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!canAfford(hasMinerals, hasGas)) {
            return;
        }

        if (onlyOneAtTime && AConstructionRequests.hasRequestedConstructionOf(type)) {
            return;
        }

        AConstructionRequests.requestConstructionOf(type);
    }

    protected static boolean canAfford(int minerals, int gas) {
        return AGame.canAfford(minerals + AProductionQueue.getMineralsReserved(), gas + AProductionQueue.getGasReserved()
        );
    }

    // =========================================================

    private static boolean hasABaseWithFreeGeyser() {
        for (AUnit base : Select.ourBases().listUnits()) {
            if (Select.geysers().inRadius(8, base).isNotEmpty()) {
                return true;
            }
        }

        return false;
    }

}
