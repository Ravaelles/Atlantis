package atlantis.combat.missions.defend;

import atlantis.combat.missions.ProtossMissionAdjustments;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class MissionDefendAllowsToAttack {

    protected MissionDefend mission;

    public MissionDefendAllowsToAttack(MissionDefend missionDefend) {
        this.mission = missionDefend;
    }

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (mission.focusPoint == null || mission.main == null) {
            return true;
        }
        double distTo = unit.distTo(enemy);

        Selection ourBuildings = enemy.enemiesNear().buildings();
        if (unit.isAir()) {
            if (
                ourBuildings.atLeast(2) &&
                    ourBuildings.nearestTo(enemy).distTo(enemy) <= enemy.groundWeaponRange() + 0.1
            ) {
                return true;
            }
        }

        if (unit.isRanged()) {

            // Don't chase workers too far from base
            if (
                enemy.isWorker() && distTo > unit.weaponRangeAgainst(enemy) && ourBuildings.empty()
            ) {
                return false;
            }

            if (
                unit.hp() >= 20 && unit.isInWeaponRangeByGame(enemy)
                    && (!enemy.isRanged() || enemy.distToMoreThan(enemy, 2.8))
            ) {
                return true;
            }

            if (distTo <= 6) {
                return true;
            }

            if (unit.enemyWeaponRangeAgainstThisUnit(enemy) >= 6) {
                return true;
            }

            if (!unit.position().region().equals(enemy.position().region())) {
                System.err.println("Regions dont match");
                return false;
            }
        }

        // =========================================================

        if (unit.isZergling()) {
            if (Enemy.protoss() && unit.hp() <= 18) {
                return false;
            }

            if (A.seconds() <= 500 && Count.sunkens() > 0) {
                Selection sunkens = Select.ourOfType(AUnitType.Zerg_Sunken_Colony);

                if (
                    sunkens.inRadius(10, unit).notEmpty()
                        && sunkens.inRadius(enemy.isRanged() ? 3 : 6, unit).empty()
                ) {
                    unit.addLog("Trickster");
                    return false;
                }
            }
        }

        // =========================================================

        mission.main = Select.main();
        mission.focusPoint = mission.focusPoint();
        mission.focusPointToBase = mission.focusPoint.distTo(mission.main);
        mission.unitToEnemy = unit.distTo(enemy);
        mission.unitToBase = unit.groundDist(mission.main);
        mission.enemyToBase = enemy.groundDist(mission.main);
        mission.enemyToFocus = enemy.groundDist(mission.focusPoint);

        if (mission.unitToEnemy <= 3 && unit.isDragoon() && enemy.isZealot() && unit.hp() <= 18) {
            return false;
        }

//        if (
////                (unit.isMelee() && unit.hasWeaponRangeToAttack(enemy, 0.1))
//                (unit.isMelee() && unitToEnemy <= 1.09)
//                || (unit.isRanged() && unit.hasWeaponRangeToAttack(enemy, 2))
//        ) {
//            if (unit.cooldownRemaining() <= 3 || unit.lastAttackFrameMoreThanAgo(40)) {
//                return true;
//            }
//        }

//        if (notAllowedToAttackTooFar(unit, enemy)) {
//            return false;
//        }
//
//        if (unit.isMelee() && enemyDistToBase > unitToBase) {
//            return false;
//        }

        // Zealots vs Zealot fix
        if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
            return true;
        }

        if (mission.main != null) {
            if (Select.enemy().inRadius(18, mission.main).atLeast(1)) {
                return true;
            }

            if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
                return true;
            }
        }

//        if (focusPointDistToBase < enemyDistToBase || enemyDistToBase < unitToBase) {
//            return true;
//        }

        return false;
    }
}
