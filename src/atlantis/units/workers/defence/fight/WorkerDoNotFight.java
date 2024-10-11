package atlantis.units.workers.defence.fight;

import atlantis.combat.missions.defend.protoss.ProtossStickCombatToMainBaseEarly;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class WorkerDoNotFight {
    public static boolean doNotFight(AUnit unit) {
        if (unit.enemiesNear().empty()) return true;
        if (unit.shieldWound() >= 10 && !unit.isAttacking()) return true;

        if (Enemy.protoss() && unit.hp() <= 33) return true;
        if (Enemy.zerg() && unit.hp() <= 26) return true;
        else if (Enemy.protoss() && unit.hp() <= 33) return true;
        else if (unit.hp() <= 19) return true;

        AUnit enemy = unit.enemiesNear().combatUnits().groundUnits().nearestTo(unit);
        if (enemy == null) return true;

        if (ProtossStickCombatToMainBaseEarly.should()) return true;

        if (unit.distToBase() >= 10) {
//            if (enemy.enemiesNear().combatUnits().inRadius(2.8, enemy).empty()) return true;

            if (
                enemy.ourNearestBuildingDist() >= maxDistToBuilding()
                    && unit.friendsNear().combatUnits().inRadius(2.5, unit).empty()
            ) return true;

            //        AUnit target = unit.target();
            //        if (target != null && !unit.isABuilding()) {
            //            return true;
            //        }

//            if (unit.lastUnderAttackLessThanAgo(120) && Count.ourCombatUnits() >= 2) {
//                return unit.friendsNear().combatUnits().inRadius(unit.hp() <= 25 ? 2.5 : 5, unit).empty();
//            }
        }

        return false;
    }

    private static double maxDistToBuilding() {
        if (Enemy.zerg()) return 4.4;
        return 5;
    }
}
