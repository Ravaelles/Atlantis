package atlantis.combat.retreating.protoss;

import atlantis.units.AUnit;

public class ProtossTooBigBattleToRetreat {
    public static boolean PvP_doNotRetreat(AUnit unit) {
        if (unit.friendsNear().atMost(3)) return false;

        if (unit.isDragoon()) {
            if (
                unit.hpPercent() >= 25
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
            if (zealotShouldSupportDragoons(unit)) return true;
            if (zealotShouldKeepZealotLine(unit)) return true;
        }

        return unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6);
    }

    private static boolean dragoonInDangerOfRanged(AUnit unit) {
        return unit.hp() <= 80
            && (unit.hp() <= 59 || unit.cooldown() >= 15)
            && unit.enemiesNear().ranged().inRadius(12.7, unit).empty();
    }

    private static boolean zealotShouldKeepZealotLine(AUnit unit) {
        return (unit.hp() >= 35 || unit.cooldown() <= 15)
            && unit.friendsNear().zealots().inRadius(2.5, unit).atLeast(3);
    }

    private static boolean zealotShouldSupportDragoons(AUnit unit) {
        return unit.eval() > 0.85
            && unit.friendsNear().dragoons().inRadius(4, unit).atLeast(1);
    }
}
