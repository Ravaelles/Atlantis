package atlantis.combat.running.to_building;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.Strategy;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class ShouldRunTowardsBase {
    public static boolean check(AUnit unit, HasPosition runAwayFrom, Action action) {
        if (runAwayFrom == null) return false;

        AUnit main = Select.main();
        if (main == null) return false;

        if (unit.hp() <= A.whenEnemyProtoss(35, 30)) return false;

        double distToMain = unit.groundDistToMain();
        if (distToMain <= 20) return false;
//        if (distToMain <= 50) return true;

        if (Enemy.protoss()) {
            if (!EnemyInfo.hasRanged()) {
                if (unit.cooldown() <= 8 && unit.meleeEnemiesNearCount(3) <= 1) return false;
                if (unit.meleeEnemiesNearCount(3) >= 2) return false;
            }
        }

        if (Enemy.zerg() && unit.meleeEnemiesNearCount(2.5) >= 4) return false;

        if (runAwayFrom instanceof AUnit) {
            AUnit enemy = (AUnit) runAwayFrom;
            if (enemy.isRanged()) {
                if ((enemy.groundDist(main) - 5) < distToMain) return false;
            }
        }

        if (true) return true;

        // =========================================================

        if (Actions.MOVE_DANCE_AWAY.equals(action)) return true;

        if (Enemy.zerg() && distToMain >= 30 && unit.hp() >= 42) return true;

        if (vsZergEarlyGame(unit, distToMain)) return true;

        if (We.protoss() && unit.hp() <= 40 && unit.lastUnderAttackLessThanAgo(30 * 4)) return false;

        if (unit.meleeEnemiesNearCount(1.5) >= 3) return false;

//        if (unit.meleeEnemiesNearCount(2.7) >= 3) return false;
//        if (
//            unit.meleeEnemiesNearCount(3) >= 2
//            && unit
//        ) return false;

//        if (distToMain <= 40) return true;
        if (unit.enemiesNear().buildings().notEmpty()) return true;
        if (unit.enemiesNear().combatBuildingsAnti(unit).atLeast(1)) return true;

        if (unit.meleeEnemiesNearCount(2.1) >= (unit.hp() <= 80 ? 2 : 3)) return false;

        if (tooCloseToEnemyBuildings(unit)) return true;

        if (runAwayFrom instanceof AUnit) {
            AUnit enemy = (AUnit) runAwayFrom;
            if (enemy.isDarkTemplar() && unit.distTo(enemy) >= 1.8) return Count.cannons() > 0;
        }

        if (unit.hp() >= 80 && A.s >= 60 * 6 && distToMain >= 30) return true;

        if (tooManyRangedNear(unit)) return false;

        if (earlyGamePvZ(unit)) return true;
        if (earlyGameTvP(unit)) return true;

        if (
            unit.enemiesNear().canAttack(unit, 4).count() >= 2
                && Chokes.enemyMainAndNaturalChokes().groundDistTo(unit) <= 5
        ) return true;

        if (nearestEnemyCloserToBaseThanUs(unit)) return false;

//        if (distToMain >= 40 && unit.isDragoon() && unit.hp() >= 41 && ) return true;

        if (We.protoss()) {
            if (Enemy.zerg()) {
                if (unit.lastUnderAttackLessThanAgo(30) || unit.hp() <= 35) return false;

                if (unit.distToBase() >= 10) return true;
                if (unit.distToCannon() >= 6) return true;
            }
        }

        if (!A.isUms() && unit.hp() >= 41 && Count.ourCombatUnits() <= 7) {
            if (!unit.isRanged() || !Enemy.protoss() || unit.rangedEnemiesCount(8) > 0) return true;
        }

        if (forbidAsManyEnemiesNear(unit)) return false;

        if (Enemy.protoss()) {
            if (
                unit.isDragoon()
                    && unit.enemiesNear().ranged().empty()
            ) {
                if (
                    unit.shotSecondsAgo(2) && unit.enemiesNear().countInRadius(2.7, unit) <= 0
                ) return true;

                if (unit.enemiesNear().countInRadius(5, unit) >= 3) return false;
            }
        }

        if (Enemy.zerg() && unit.isDragoon() && unit.eval() <= 0.6) return false;

        if (unit.isMissionAttack() && unit.isGroundUnit() && unit.enemiesNear().buildings().notEmpty()) return true;
        if (unit.isScout() && !unit.isDragoon() && unit.enemiesNear().buildings().notEmpty()) return true;

        if (Enemy.zerg()) {
            if (A.s <= 320) return true;
        }

        if (unit.meleeEnemiesNearCount(1.4) > 0) return false;
        if (unit.meleeEnemiesNearCount(1.5) >= 2) return false;

        if (unit.isDragoon() && (Alpha.count() <= 25 || unit.shields() <= 40)) return false;

        if (unit.isSquadScout() && A.seconds() <= 500) return true;

        if (A.seconds() >= 550) return false;

        if (A.seconds() <= 400 && unit.isRetreating() && unit.distToMain() >= 60) return true;
        if (unit.isMarine() && unit.isHealthy() && unit.distToBase() >= 30) return true;

        if (unit.isFlying()) return false;
        if (unit.hp() <= 20) return false;
        if (unit.isDragoon() || unit.isTank()) return false;

        if (unit.distTo(runAwayFrom) < 2.1) return false;
        if (unit.meleeEnemiesNearCount(1.8) > 0) return false;

        if (unit.isAir() && main.distTo(unit) > 12) return true;

        if (Strategy.get().isRushOrCheese() && A.seconds() <= 300) return false;
        if (!unit.hasPathTo(main)) return false;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(4);
        if (distToMain >= 40 || (distToMain > 7 && meleeEnemiesNearCount == 0 && unit.isMissionDefend()))
            return true;

        if (A.seconds() >= 380) return false;

        if (unit.isScout()) return false;

        // If already close to the base, don't run towards it, no point
        if (distToMain < 50) return false;

        if (meleeEnemiesNearCount >= 1) return false;

        // Only run towards our main if our army isn't too numerous, otherwise units gonna bump upon each other
        if (Count.ourCombatUnits() > 10) return false;

        if (unit.lastStartedRunningLessThanAgo(30) && unit.lastStoppedRunningLessThanAgo(30))
            return false;

        if (Count.ourCombatUnits() <= 10 || unit.isNearEnemyBuilding()) {
            if (unit.meleeEnemiesNearCount(3) == 0) {
                return true;
            }
        }

        return false;

//        AUnit mainOrAnyBuilding = Select.mainOrAnyBuilding();
//        if (mainOrAnyBuilding == null) return false;
//
//        return unit.distTo(mainOrAnyBuilding) >= 20
//            && unit.meleeEnemiesNearCount(1.7) == 0;
    }

    private static boolean vsZergEarlyGame(AUnit unit, double distToMain) {
        if (!Enemy.zerg()) return false;

        if (A.s <= 60 * 8) {
            if (distToMain >= 12) return true;
//            if (unit.hp() <= 40) return false;
        }

        return false;
    }

    private static boolean tooCloseToEnemyBuildings(AUnit unit) {
        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        if (enemyBuilding == null) return false;

        if (unit.meleeEnemiesNearCount(2.5) > 0) return false;

        return unit.distTo(enemyBuilding) <= 20;
    }

    private static boolean earlyGameTvP(AUnit unit) {
        if (!We.terran()) return false;
        if (!Enemy.protoss()) return false;
        if (!unit.isMarine()) return false;

        if (unit.rangedEnemiesCount(7) >= 2) return true;

        return unit.hp() >= 30 && unit.meleeEnemiesNearCount(2.4) == 0;
    }

    private static boolean earlyGamePvZ(AUnit unit) {
        if (!We.protoss()) return false;
        if (!Enemy.zerg()) return false;

        return unit.hp() >= 35 && unit.squadSize() <= 12;
    }

    private static boolean tooManyRangedNear(AUnit unit) {
        return unit.rangedEnemiesCount(2 + unit.woundPercent() / 30.0)
            >= (unit.woundPercent() >= 40 ? 2 : 3);
    }

    private static boolean nearestEnemyCloserToBaseThanUs(AUnit unit) {
        AUnit enemy = unit.nearestEnemy();
        if (enemy == null) return false;

        AUnit main = Select.main();
        if (main == null) return false;

        return enemy.groundDist(main) < unit.distTo(main);
    }

    private static boolean forbidAsManyEnemiesNear(AUnit unit) {
        if (!We.protoss()) return false;

        int minEnemiesToForbid = unit.hp() <= 80 ? 2 : 4;
        double radius = 1.5 + unit.woundPercent() / 25.0;

        if (unit.enemiesNear().canAttack(unit, radius).count() >= minEnemiesToForbid) return true;

        return false;
    }

    public static AUnit position() {
        return Select.mainOrAnyBuilding();
    }
}
