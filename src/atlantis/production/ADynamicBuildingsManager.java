package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.buildings.managers.AExpansionManager;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.orders.ABuildOrderManager;
import atlantis.units.AUnit;
import atlantis.units.Select;


public class ADynamicBuildingsManager {

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

    protected static boolean canAfford(int minerals, int gas) {
        return AGame.canAfford(minerals + ABuildOrderManager.getMineralsReserved(), gas + ABuildOrderManager.getGasReserved()
        );
    }

    // =========================================================

    private static boolean hasABaseWithFreeGeyser() {
        for (AUnit base : Select.ourBases().listUnits()) {
            if (Select.geysers().inRadius(10, base).isNotEmpty()) {
                return true;
            }
        }

        return false;
    }

}
