package atlantis.strategy;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.position.APosition;
import atlantis.production.requests.ARequests;
import atlantis.units.AUnit;
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
        
        if (enemyStrategy.isGoingRush() && !AGame.isEnemyProtoss()) {
            int defensiveBuildings = countBuildingsFinishedAndPlanned(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
            
            for (int i = defensiveBuildings; i < 2; i++) {
                requestDefensiveBuilding(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
            }
        }
        
        // === Tech ========================================
        
        if (enemyStrategy.isGoingHiddenUnits()) {
            ARequests.getInstance().requestDetectorQuick();
        }
        
        if (enemyStrategy.isGoingAirUnitsQuickly()) {
            ARequests.getInstance().requestAntiAirQuick();
        }
    }
    
    // =========================================================
    
    private static int countBuildingsFinishedAndPlanned(AUnitType type) {
        return Select.ourOfType(type).count() + AConstructionManager.countNotFinishedConstructionsOfType(type);
    }
    
}
