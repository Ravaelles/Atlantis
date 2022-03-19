package atlantis.information.strategy.response;

import atlantis.combat.missions.Missions;
import atlantis.game.AGame;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.map.scout.AScoutManager;
import atlantis.production.requests.AntiAirBuildingManager;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.production.requests.zerg.ZergSporeColony;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.util.We;

public abstract class AStrategyResponse {

    public boolean update() {
        if (AGame.notNthGameFrame(17)) {
            return false;
        }

        // Anti-LAND
        if (antiLandManager().handleBuildNew()) {
            return true;
        }

        // Anti-AIR
        if (antiAirManager().handleBuildNew()) {
            return true;
        }

        return false;
    }

    public static AStrategyResponse get() {
        return AStrategyResponseFactory.forOurRace();
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
            Missions.forceGlobalMissionContain("Enemy is expanding, engage him");
        }

        // === Tech ========================================

        if (enemyStrategy.goingHiddenUnits()) {
//            if (!Enemy.terran()) {
//                Missions.forceGlobalMissionDefend("Enemy goes hidden units");
//            }

            OurStrategicBuildings.setAntiLandBuildingsNeeded(1);
//            ADetectorRequest.requestDetectorImmediately(null);
        }

        if (enemyStrategy.isAirUnits()) {
//            if (!Enemy.protoss()) {
//                Missions.setGlobalMissionDefend();
//            }

            handleAirUnitsDefence();
        }
    }

    // =========================================================

    protected boolean rushDefence(AStrategy enemyStrategy) {
        System.out.println("GENERIC RUSH - shouldn't be called, use race-specific");
        
        Missions.forceGlobalMissionDefend("Rush defence");

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) {
            return false;
        }
        
        OurStrategicBuildings.setAntiLandBuildingsNeeded(rushDefenseCombatBuildingsNeeded(enemyStrategy));
        return true;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    protected void handleAirUnitsDefence() {
//        OurStrategicBuildings.setAntiAirBuildingsNeeded(5);
//        AAntiAirBuildingRequests.requestAntiAirQuick(null);
    }

    // =========================================================

    protected boolean shouldSkipAntiRushCombatBuilding(AStrategy enemyStrategy) {
        if (enemyStrategy == null) {
            return false;
        }
        
        if (AScoutManager.hasAnyScoutBeenKilled()) {
            return false;
        }
        
        // =========================================================

        return !enemyStrategy.isRush() && !enemyStrategy.isGoingCheese();
    }

    // =========================================================

    private AntiLandBuildingManager antiLandManager() {
        if (We.zerg()) {
            return ZergSunkenColony.get();
        }

        return AntiLandBuildingManager.get();
    }

    private AntiAirBuildingManager antiAirManager() {
        if (We.zerg()) {
            return ZergSporeColony.get();
        }

        return AntiAirBuildingManager.get();
    }

}
