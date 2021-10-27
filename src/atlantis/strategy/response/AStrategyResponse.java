package atlantis.strategy.response;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.AMap;
import atlantis.production.requests.AAntiAirBuildingRequests;
import atlantis.production.requests.AAntiLandBuildingRequests;
import atlantis.production.requests.ADetectorRequest;
import atlantis.production.requests.ARequests;
import atlantis.scout.AScoutManager;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.Enemy;
import atlantis.util.Us;


public class AStrategyResponse {
    
    public static boolean update() {
        if (AGame.notNthGameFrame(10)) {
            return false;
        }

        // Anti-LAND
        if (AAntiLandBuildingRequests.handle()) {
            return true;
        }

        // Anti-AIR
        if (AAntiAirBuildingRequests.handle()) {
            return true;
        }

        return false;
    }
    
    // =========================================================

    public static void updateEnemyStrategyChanged() {
        AStrategy enemyStrategy = EnemyStrategy.get();

        // === Rush ========================================
        
        if (enemyStrategy.isRushOrCheese()) {
            rushDefense(enemyStrategy);
            Missions.setGlobalMissionDefend();
        }

        // === Expansion ===================================

        if (enemyStrategy.isExpansion()) {
            Missions.setGlobalMissionContain();
        }

        // === Tech ========================================

        if (enemyStrategy.isHiddenUnits()) {
            if (!Enemy.terran()) {
                Missions.setGlobalMissionDefend();
            }

            AStrategyInformations.antiLandBuildingsNeeded(1);
            ADetectorRequest.requestDetectorImmediately(null);
        }

        if (enemyStrategy.isAirUnits()) {
            if (!Enemy.protoss()) {
                Missions.setGlobalMissionDefend();
            }

            AStrategyInformations.antiAirBuildingsNeeded(3);
            AAntiAirBuildingRequests.requestAntiAirQuick(null);
        }
    }

    public static void hiddenUnitDetected(AUnit enemyUnit) {
        if (enemyUnit.effVisible()) {
            return;
        }

        if (enemyUnit.isType(AUnitType.Protoss_Dark_Templar)) {
            AStrategyInformations.detectorsNeeded(1);
            ARequests.getInstance().requestDetectorQuick(
                    AMap.getChokepointForMainBase().getCenter()
            );
            ARequests.getInstance().requestDetectorQuick(
                    AMap.getChokepointForNaturalBase(Select.mainBase().getPosition()).getCenter()
            );
        }
    }
    
    // =========================================================

    protected static boolean rushDefense(AStrategy enemyStrategy) {
        if (Us.isTerran() && TerranStrategyResponse.rushDefense(enemyStrategy)) {
            return true;
        }
        else if (Us.isTerran() && ProtossStrategyResponse.rushDefense(enemyStrategy)) {
            return true;
        }

        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }
        
        AStrategyInformations.antiLandBuildingsNeeded(rushDefenseDefensiveBuildings(enemyStrategy));
        return true;
    }

    protected static int rushDefenseDefensiveBuildings(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    protected static boolean shouldSkipAntiRushDefensiveBuilding(AStrategy enemyStrategy) {
        if (enemyStrategy == null) {
            return false;
        }
        
        if (AScoutManager.hasAnyScoutBeenKilled()) {
            return false;
        }
        
        // =========================================================

        return !enemyStrategy.isRush() && !enemyStrategy.isGoingCheese();
    }
    
}
