package atlantis.strategy.response;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.MapChokes;
import atlantis.production.requests.AAntiAirBuildingRequests;
import atlantis.production.requests.AAntiLandBuildingRequests;
import atlantis.production.requests.ARequests;
import atlantis.scout.AScoutManager;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public abstract class AStrategyResponse {

    public boolean update() {
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

    public void updateEnemyStrategyChanged() {
        AStrategy enemyStrategy = EnemyStrategy.get();

        // === Rush ========================================
        
        if (enemyStrategy.isRushOrCheese()) {
            rushDefence(enemyStrategy);
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

            AStrategyInformations.setAntiLandBuildingsNeeded(1);
//            ADetectorRequest.requestDetectorImmediately(null);
        }

        if (enemyStrategy.isAirUnits()) {
            if (!Enemy.protoss()) {
                Missions.setGlobalMissionDefend();
            }

            handleAirUnitsDefence();
        }
    }

    // =========================================================

    public void updateHiddenUnitDetected(AUnit enemyUnit) {
        if (enemyUnit.effVisible()) {
            return;
        }

        if (enemyUnit.isType(AUnitType.Protoss_Dark_Templar, AUnitType.Zerg_Lurker)) {
            AStrategyInformations.setDetectorsNeeded(1);
            ARequests.getInstance().requestDetectorQuick(
                    MapChokes.mainBaseChoke().getCenter()
            );
            ARequests.getInstance().requestDetectorQuick(
                    MapChokes.chokeForNaturalBase(Select.mainBase().position()).getCenter()
            );
        }
    }

    // =========================================================

    protected boolean rushDefence(AStrategy enemyStrategy) {
        System.out.println("GENERIC RUSH - shouldn't be called");
        Missions.setGlobalMissionDefend();

        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }
        
        AStrategyInformations.setAntiLandBuildingsNeeded(rushDefenseDefensiveBuildingsNeeded(enemyStrategy));
        return true;
    }

    protected int rushDefenseDefensiveBuildingsNeeded(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    protected void handleAirUnitsDefence() {
        AStrategyInformations.setAntiAirBuildingsNeeded(5);
        AAntiAirBuildingRequests.requestAntiAirQuick(null);
    }

    // =========================================================

    protected boolean shouldSkipAntiRushDefensiveBuilding(AStrategy enemyStrategy) {
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
