package atlantis.information.strategy.response;

import atlantis.combat.micro.terran.bunker.TerranBunker;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.reinforce.terran.turrets.TerranMissileTurret;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.game.AGame;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.scout.ScoutCommander;
import atlantis.production.requests.AntiAirBuildingCommander;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.production.requests.zerg.ZergSporeColony;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.select.Count;
import atlantis.util.We;

public abstract class RaceStrategyResponse {
    public boolean update() {
        if (AGame.notNthGameFrame(17)) return false;

        // Anti-LAND
//        if (antiLandManager().requestToBuildNewAntiLandCombatBuilding()) return true;

        // Anti-AIR
        if (antiAirManager().requestToBuildNewAntiAirCombatBuilding()) return true;

        return false;
    }

    public static RaceStrategyResponse get() {
        return RaceStrategyResponseFactory.forOurRace();
    }

    // =========================================================

    public void updateEnemyStrategyChanged() {
        AStrategy enemyStrategy = EnemyStrategy.get();

        if (OurStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return;
        }

        // === Rush ========================================

        if (enemyStrategy.isRushOrCheese() && GamePhase.isEarlyGame()) {
            rushDefence(enemyStrategy);
        }

        // === Expansion ===================================

        if (enemyStrategy.isExpansion() && GamePhase.isEarlyGame() && Mission.get().isMissionDefendOrSparta()) {
//            Missions.forceGlobalMissionContain("Enemy is expanding, engage him");
            if (!Enemy.zerg() || Army.strength() >= 121) {
                Missions.forceGlobalMissionAttack("Enemy is expanding, engage him");
            }
        }

        // === Tech ========================================

        if (enemyStrategy.isGoingHiddenUnits()) {
            onEnemyGoesHiddenUnits();
        }

        // === Air units ===========================================

        if (enemyStrategy.isAirUnits()) {
//            if (!Enemy.protoss()) {
//                Missions.setGlobalMissionDefend();
//            }

            handleAirUnitsDefence();
        }
    }

    public abstract void onEnemyGoesHiddenUnits();

    // =========================================================

    protected boolean rushDefence(AStrategy enemyStrategy) {
        System.out.println("GENERIC RUSH - shouldn't be called, use race-specific");

        Missions.forceGlobalMissionDefend("Rush defence");

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return true;
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
