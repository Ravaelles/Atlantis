package atlantis.strategy;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AStrategyResponse {

    protected static void updateWhenEnemyStrategyChanged() {
        AEnemyStrategy enemyStrategy = AEnemyStrategy.getEnemyStrategy();
        
        // === Rush ========================================
        
        if (enemyStrategy.isGoingRush()) {
            int defensiveBuildings = countBuildingsFinishedAndPlanned(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
            
            for (int i = defensiveBuildings; i < 2; i++) {
                AConstructionManager.requestConstructionOf(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
            }
        }
    }
    
    // =========================================================

    private static int countBuildingsFinishedAndPlanned(AUnitType type) {
        return Select.ourOfType(type).count() + AConstructionManager.countNotFinishedConstructionsOfType(type);
    }
    
}
