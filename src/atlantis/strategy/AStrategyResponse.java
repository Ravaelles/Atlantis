package atlantis.strategy;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.map.AMap;
import atlantis.production.requests.AAntiAirRequest;
import atlantis.production.requests.AAntiLandRequest;
import atlantis.production.requests.ADetectorRequest;
import atlantis.production.requests.ARequests;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;


public class AStrategyResponse {
    
    public static void update() {
        int defBuildingAntiLand = AConstructionRequests.countExistingAndPlannedConstructions(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
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
                ADetectorRequest.requestDetectorImmediately(null);
            }
        }
        
        if (enemyStrategy.isGoingAirUnitsQuickly()) {
            if (AGame.everyNthGameFrame(21)) {
                AAntiAirRequest.requestAntiAirQuick(null);
            }
        }
    }

    public static void hiddenUnitDetected(AUnit enemyUnit) {
        if (enemyUnit.isVisible()) {
            return;
        }

        if (enemyUnit.isType(AUnitType.Protoss_Dark_Templar)) {
            ARequests.getInstance().requestDetectorQuick(
                    AMap.getChokepointForMainBase().getCenter()
            );
            ARequests.getInstance().requestDetectorQuick(
                    AMap.getChokepointForNaturalBase(Select.mainBase().getPosition()).getCenter()
            );
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

        return !enemyStrategy.isGoingRush() && !enemyStrategy.isGoingCheese();
    }
    
}
