package atlantis.units.workers.defence.fight;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class WorkerDoNotFight {
    public static boolean doNotFight(AUnit unit) {
        if (unit.enemiesNear().empty()) return true;
//        if (unit.shieldWound() >= 16) return true;

//        if (A.s <= 60 * 6) return true;

        if (unit.hp() >= 50) return false;
        if (unit.enemiesNear().buildings().notEmpty()) return false;

        if (We.protoss() && unit.shields() <= 6 && A.s >= 60 * 6) return true;

        Selection enemiesNear = unit.enemiesNear().groundUnits().inRadius(AUnit.NEAR_DIST, unit);
        if (Enemy.protoss() && We.protoss()) {
            AUnit nearestMelee = enemiesNear.melee().nearestTo(unit);
            if (nearestMelee != null) {
                if (nearestMelee.enemiesNear().nonBuildings().countInRadius(3, unit) <= 1) return true;
            }

            if (Count.workers() >= 15 && enemiesNear.atMost(2) && (unit.id() % 2 != 1 || unit.shieldWounded()))
                return true;
        }

        AUnit enemy = unit.enemiesNear().groundUnits().nearestTo(unit);
        if (enemy == null) return true;

        if (Enemy.zerg()) {
            if (unit.hp() <= 25) return true;
            if (unit.meleeEnemiesNearCount(2.5) >= 3) return true;
        }
        else if (Enemy.protoss() && unit.hp() < minHpForProtoss(unit)) return true;
        else if (unit.hp() <= 20) return true;

        double distToBase = unit.distToBase();
        if (distToBase >= 7 && unit.enemiesNear().groundUnits().countInRadius(2, unit) == 0) return true;

        if (unit.shieldWound() <= 5 && Select.ourBases().distToNearest(enemy) <= 4) return false;

        if (distToBase <= 7 && Count.workers() <= 14 && unit.distTo(enemy) <= 5) return unit.id() % 4 == 0;

        if (Enemy.protoss()) {
            if (unit.meleeEnemiesNearCount(4) >= 3) return unit.isWounded();
            if (dontFightAsTooManyZealotsComparedToWorkers(unit)) return true;
            if (Count.workers() >= 19 && unit.id() % 3 == 0) return true;
        }

        if (Count.ourCombatUnits() >= 10) {
//            if (unit.hp() <= 29 || unit.friendsNear().combatUnits().inRadius(4.5, unit).empty()) return true;
            if (unit.hp() <= 34) return true;
        }

//        if (ProtossStickCombatToMainBaseEarly.should()) return true;

        if (distToBase >= 9 && !unit.isMiningOrExtractingGas()) {
//            if (enemy.enemiesNear().combatUnits().inRadius(2.8, enemy).empty()) return true;

            if (
                enemy.ourNearestBuildingDist() >= maxDistToBuilding()
                    && (distToBase >= 12 || unit.friendsNear().combatUnits().inRadius(2.5, unit).empty())
            ) return true;

            //        AUnit target = unit.target();
            //        if (target != null && !unit.isABuilding()) {
            //            return true;
            //        }

//            if (unit.lastUnderAttackLessThanAgo(120) && Count.ourCombatUnits() >= 2) {
//                return unit.friendsNear().combatUnits().inRadius(unit.hp() <= 25 ? 2.5 : 5, unit).empty();
//            }
        }

        if (!We.zerg() && (unit.isBuilder() || unit.isConstructing())) return true;

        return false;
    }

    private static boolean dontFightAsTooManyZealotsComparedToWorkers(AUnit unit) {
        int enemies = unit.enemiesNear().zealots().countInRadius(8, unit);

        if (enemies >= 5) return true;

        int ours = unit.friendsNear().workers().count() + Count.ourCombatUnits();

        return enemies * 0.32 >= ours;
    }

    private static int minHpForProtoss(AUnit unit) {
        return 35;
//        return unit.meleeEnemiesNearCount(2.6) <= 1 ? 18 : 36;
    }

    private static double maxDistToBuilding() {
        if (Enemy.zerg()) return 4.4;
        return 5;
    }
}
