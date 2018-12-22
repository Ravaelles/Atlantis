package atlantis.production;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.buildings.managers.AExpansionManager;
import atlantis.constructing.AConstructionManager;
import atlantis.constructing.AConstructionManager;
import atlantis.production.orders.ABuildOrderManager;
import atlantis.production.requests.TerranRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ADynamicConstructionManager {

    public static void update() {
        
        // Check if we should automatically build new base, because we have shitload of minerals.
        AExpansionManager.requestNewBaseIfNeeded();
        
        // If number of bases is bigger than gas buildings, it usually makes sense to build new gas extractor
        buildGasBuildingsIfNeeded();
        
        // === Terran only ========================================
        
        if (AGame.playsAsTerran()) {
            TerranDynamicConstructionManager.update();
        }
    }
    
    // =========================================================

    /**
     * Build Refineries/ Assimilators/ Extractors when it makes sense.
     */
    private static void buildGasBuildingsIfNeeded() {
        if (AGame.getTimeSeconds() % 10 != 0) {
            return;
        }
        
        // =========================================================
        
        int numberOfBases = Select.ourBases().count();
        if (numberOfBases >= 2) {
            int numberOfGasBuildings = Select.ourIncludingUnfinished().ofType(AtlantisConfig.GAS_BUILDING).count();
            if (numberOfBases > numberOfGasBuildings && !AGame.canAfford(0, 350) 
                    && AConstructionManager.countNotStartedConstructionsOfType(AtlantisConfig.GAS_BUILDING) == 0) {
                AConstructionManager.requestConstructionOf(AtlantisConfig.GAS_BUILDING);
            }
        }
    }
    
    // =========================================================
    
    protected static boolean canAfford(int minerals, int gas) {
        return AGame.canAfford(minerals + ABuildOrderManager.getMineralsReserved(), gas + ABuildOrderManager.getGasReserved()
        );
    }
    
}
