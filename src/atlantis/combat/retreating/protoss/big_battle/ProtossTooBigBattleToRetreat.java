package atlantis.combat.retreating.protoss.big_battle;

import atlantis.units.AUnit;

public class ProtossTooBigBattleToRetreat {
    public static boolean PvP_doNotRetreat(AUnit unit) {
        if (unit.friendsNear().atMost(3)) return false;

        if (unit.isDragoon()) {
            if (
                unit.hpPercent() >= 60
//                    && unit.cooldown() <= 9
                    && unit.meleeEnemiesNearCount(1.8 + unit.woundPercent() / 50.0) == 0
            ) return true;

            if (unit.hp() <= 40 && unit.enemiesNear().ranged().inRadius(7, unit).notEmpty()) return false;

            return false;
//            if (true) return true;

//            if (unit.enemiesNear().inRadius(3.5, unit).empty()) return true;
//
//            if (unit.cooldown() >= 10) {
//                if (unit.meleeEnemiesNearCount(1.6) > 0) return false;
//                if (dragoonInDangerOfRanged(unit)) return false;
//            }
//
//            if (unit.friendsNear().combatUnits().inRadius(8, unit).atMost(2)) return false;
//
////            if (unit.lastAttackFrameMoreThanAgo(30 * 3)) return true;
//            if (unit.shields() >= 40 && unit.enemiesNear().inRadius(3.2, unit).empty()) return true;
        }

        if (unit.isZealot()) {
            if (ProtossTooBigBattleToRetreat_asZealot.doNotRetreat(unit)) {
                System.out.println("PvP Zealot dont");
                return true;
            }
//            if (zealotShouldSupportDragoons(unit)) return true;
//            if (zealotShouldKeepZealotLine(unit)) return true;
        }

        return unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6);
    }

    private static boolean dragoonInDangerOfRanged(AUnit unit) {
        return unit.hp() <= 80
            && (unit.hp() <= 59 || unit.cooldown() >= 15)
            && unit.enemiesNear().ranged().inRadius(12.7, unit).empty();
    }
}
