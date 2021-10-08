package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.buildings.managers.AExpansionManager;
import atlantis.constructing.AConstructionManager;
import atlantis.production.orders.ABuildOrderManager;
import atlantis.units.AUnit;
import atlantis.units.Select;


public class ADynamicConstructionManager {

    public static void update() {
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AExpansionManager.requestNewBaseIfNeeded();
        
        // If number of bases is bigger than gas buildings, it usually makes sense to build new gas extractor
        buildGasBuildingsIfNeeded();
        
        // === Terran only ========================================
        
        if (AGame.isPlayingAsTerran()) {
            TerranDynamicConstructionManager.update();
        }
    }
    
    // =========================================================

    /**
     * Build Refineries/Assimilators/Extractors when it makes sense.
     */
    private static void buildGasBuildingsIfNeeded() {
        if (AGame.everyNthGameFrame(50)) {
            return;
        }
        
        // =========================================================
        
        int numberOfBases = Select.ourBases().count();
        int numberOfGasBuildings = Select.ourIncludingUnfinished().ofType(AtlantisConfig.GAS_BUILDING).count();
        if (
            numberOfBases >= 2
            && numberOfBases > numberOfGasBuildings && !AGame.canAfford(0, 350)
            && AConstructionManager.countNotStartedConstructionsOfType(AtlantisConfig.GAS_BUILDING) == 0
            && hasABaseWithFreeGeyser()
        ) {
            AConstructionManager.requestConstructionOf(AtlantisConfig.GAS_BUILDING);
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
