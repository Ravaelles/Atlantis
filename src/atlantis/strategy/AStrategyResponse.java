package atlantis.strategy;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.position.APosition;
import atlantis.production.requests.ARequests;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AStrategyResponse {
    
    public static void update() {
        int defBuildingAntiLand = AConstructionManager.countExistingAndPlannedConstructions(AtlantisConfig.DEF_BUILDING_ANTI_LAND);
        if (defBuildingAntiLand < AStrategyInformations.needDefBuildingAntiLand) {
            ARequests.getInstance().requestDefBuildingAntiLand(null);
        }
    }
    
    // =========================================================

    protected static void updateEnemyStrategyChanged() {
        AEnemyStrategy enemyStrategy = AEnemyStrategy.getEnemyStrategy();
        
        // === Rush ========================================
        
        if (enemyStrategy.isGoingRush()) {
            handleRushDefense(enemyStrategy);
        }
        
        // === Tech ========================================
        
        if (enemyStrategy.isGoingHiddenUnits()) {
            if (AGame.getTimeFrames() % 19 == 0) {
                ARequests.getInstance().requestDetectorQuick(null);
            }
        }
        
        if (enemyStrategy.isGoingAirUnitsQuickly()) {
            if (AGame.getTimeFrames() % 28 == 0) {
                ARequests.getInstance().requestAntiAirQuick(null);
            }
        }
    }
    
    // =========================================================

    private static void handleRushDefense(AEnemyStrategy enemyStrategy) {
        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return;
        }
        
        if (AGame.playsAsTerran()) {
            int minBunkers = 1;

            // Anti-cheese
            if (enemyStrategy.isGoingCheese()) {
                minBunkers = 2;
            }

            AStrategyInformations.needDefBuildingAntiLandAtLeast(minBunkers);
        }
    }

    private static boolean shouldSkipAntiRushDefensiveBuilding(AEnemyStrategy enemyStrategy) {
        if (enemyStrategy == null) {
            return false;
        }
        
        if (AScoutManager.hasAnyScoutBeenKilled()) {
            return false;
        }
        
        // =========================================================
        
        if (!enemyStrategy.isGoingRush() && !enemyStrategy.isGoingCheese()) {
            return true;
        }
        
        return false;
    }
    
}
