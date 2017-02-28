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
            int defensiveBuildings = AConstructionManager.countExistingAndPlannedConstructions(
                    AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND
            );
            for (int i = defensiveBuildings; i < 2; i++) {
                ARequests.getInstance().requestDefensiveBuildingAntiLand(null);
            }
        }
        
        // === Tech ========================================
        
        if (enemyStrategy.isGoingHiddenUnits()) {
            ARequests.getInstance().requestDetectorQuick(null);
        }
        
        if (enemyStrategy.isGoingAirUnitsQuickly()) {
            if (AGame.getTimeFrames() % 28 == 0) {
                ARequests.getInstance().requestAntiAirQuick(null);
            }
        }
    }
    
}
