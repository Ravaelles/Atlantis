package atlantis.units.workers.defence.fight;

import atlantis.combat.missions.defend.protoss.ProtossStickCombatToMainBaseEarly;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class WorkerDoNotFight {
    public static boolean doNotFight(AUnit unit) {
        if (unit.enemiesNear().empty()) return true;
//        if (unit.shieldWound() >= 16) return true;

//        if (A.s <= 60 * 6) return true;

        AUnit enemy = unit.enemiesNear().combatUnits().groundUnits().nearestTo(unit);
        if (enemy == null) return true;

        if (Enemy.zerg() && unit.hp() <= 24) return true;
        else if (Enemy.protoss() && unit.hp() < minHpForProtoss(unit)) return true;
        else if (unit.hp() <= 20) return true;

        if (Enemy.protoss()) {
            if (unit.meleeEnemiesNearCount(4) >= 3) return unit.isWounded();
        }

        if (Count.ourCombatUnits() >= 10) {
//            if (unit.hp() <= 29 || unit.friendsNear().combatUnits().inRadius(4.5, unit).empty()) return true;
            if (unit.hp() <= 34) return true;
        }

//        if (ProtossStickCombatToMainBaseEarly.should()) return true;

        double distToBase = unit.distToBase();
        if (distToBase >= 8) {
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

        Selection enemiesNear = unit.enemiesNear().groundUnits().inRadius(15, unit);
        if (!Enemy.protoss()) {
            if (enemiesNear.atMost(1) && (unit.id() % 2 != 1 || unit.shieldWounded())) return true;
//            if (A.s <= 400 && enemiesNear.atMost(2) && unit.friendsNear().combatUnits().atLeast(5)) return true;
        }

        if (unit.isBuilder() || unit.isConstructing()) return true;

        return false;
    }

    private static int minHpForProtoss(AUnit unit) {
        return unit.meleeEnemiesNearCount(2.6) <= 1 ? 18 : 36;
    }

    private static double maxDistToBuilding() {
        if (Enemy.zerg()) return 4.4;
        return 5;
    }
}
