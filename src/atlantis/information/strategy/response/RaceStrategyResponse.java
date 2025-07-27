package atlantis.information.strategy.response;

import atlantis.combat.micro.terran.bunker.TerranBunker;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.reinforce.terran.turrets.TerranMissileTurret;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.game.AGame;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.Strategy;
import atlantis.map.scout.ScoutCommander;
import atlantis.production.requests.AntiAirBuildingCommander;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.production.requests.zerg.ZergSporeColony;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public abstract class RaceStrategyResponse {
//    public boolean update() {
//        if (AGame.notNthGameFrame(17)) return false;
//
//        // Anti-LAND

    /// /        if (antiLandManager().requestToBuildNewAntiLandCombatBuilding()) return true;
//
//        // Anti-AIR
//        if (antiAirManager().requestToBuildNewAntiAirCombatBuilding()) return true;
//
//        return false;
//    }
    public static RaceStrategyResponse get() {
        return RaceStrategyResponseFactory.forOurRace();
    }

    // =========================================================

    public abstract boolean requestDetection(AUnit enemyUnit);

    // =========================================================

    public void updateEnemyStrategyChanged() {
        AStrategy enemyStrategy = EnemyStrategy.get();

//        if (Strategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
//            return;
//        }

        // === Rush ========================================

        if (enemyStrategy.isRushOrCheese() && GamePhase.isEarlyGame()) {
            rushDefence(enemyStrategy);
        }

        // === Expansion ===================================

        if (enemyStrategy.isExpansion() && GamePhase.isEarlyGame() && Mission.get().isMissionDefendOrSparta()) {
            if (!Enemy.zerg() || Army.strengthWithoutCB() >= 131) {
                Missions.forceGlobalMissionAttack("Enemy is expanding, engage him");
            }
        }

        // === Tech ========================================

        if (enemyStrategy.isGoingHiddenUnits()) {
            onEnemyGoesHiddenUnits();
        }

        // === Air units ===========================================

        if (enemyStrategy.isAirUnits()) {
            handleAirUnitsDefence();
        }
    }

    public abstract void onEnemyGoesHiddenUnits();

    // =========================================================

    protected boolean rushDefence(AStrategy enemyStrategy) {
        ErrorLog.printMaxOncePerMinute("GENERIC RUSH - shouldn't be called, use race-specific");

//        Missions.forceGlobalMissionDefend("Rush defence");
//
//        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return false;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    public void handleAirUnitsDefence() {
    }

    // =========================================================

    protected boolean shouldSkipAntiRushCombatBuilding(AStrategy enemyStrategy) {
        if (Count.cannonsWithUnfinished() >= 2) return true;

        if (enemyStrategy == null) return false;
        if (ScoutCommander.hasAnyScoutBeenKilled()) return false;

        return !enemyStrategy.isRush() && !enemyStrategy.isGoingCheese();
    }

    // =========================================================

    private AntiLandBuildingCommander antiLandManager() {
        if (We.zerg()) {
            return ZergSunkenColony.get();
        }
        else if (We.terran()) {
            return TerranBunker.get();
        }

        return AntiLandBuildingCommander.get();
    }

    private AntiAirBuildingCommander antiAirManager() {
        if (We.zerg()) {
            return ZergSporeColony.get();
        }
        else if (We.terran()) {
            return TerranMissileTurret.get();
        }

        return AntiAirBuildingCommander.get();
    }
}
