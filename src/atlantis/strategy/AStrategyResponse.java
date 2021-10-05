package atlantis.strategy;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.production.requests.AAntiAirRequest;
import atlantis.production.requests.AAntiLandRequest;
import atlantis.production.requests.ADetectorRequest;
import atlantis.scout.AScoutManager;


public class AStrategyResponse {
    
    public static void update() {
        int defBuildingAntiLand = AConstructionManager.countExistingAndPlannedConstructions(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
        if (defBuildingAntiLand < AStrategyInformations.needDefBuildingAntiLand) {
            AAntiLandRequest.requestDefensiveBuildingAntiLand(null);
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
            if (AGame.everyNthGameFrame(19)) {
                AStrategyInformations.needDefBuildingAntiLandAtLeast(1);
                ADetectorRequest.requestDetectorQuick(null);
            }
        }
        
        if (enemyStrategy.isGoingAirUnitsQuickly()) {
            if (AGame.everyNthGameFrame(21)) {
                AAntiAirRequest.requestAntiAirQuick(null);
            }
        }
    }
    
    // =========================================================

    private static void handleRushDefense(AEnemyStrategy enemyStrategy) {
        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return;
        }
        
        if (AGame.isPlayingAsTerran()) {
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
