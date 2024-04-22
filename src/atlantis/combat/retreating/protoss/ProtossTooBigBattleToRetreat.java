package atlantis.combat.retreating.protoss;

import atlantis.units.AUnit;

public class ProtossTooBigBattleToRetreat {
    public static boolean PvP_doNotRetreat(AUnit unit) {
        if (unit.isDragoon()) {
            if (unit.isHealthy()) return true;
            if (unit.enemiesNear().inRadius(3.7, unit).empty()) return true;

            if (unit.hp() <= 40) return false;
            if (unit.friendsNear().combatUnits().inRadius(8, unit).atMost(2)) return false;

            if (unit.lastAttackFrameMoreThanAgo(30 * 3)) return true;
            if (unit.shields() >= 40 && unit.enemiesNear().inRadius(3.2, unit).empty()) return true;

            if (unit.cooldown() >= 12 && unit.meleeEnemiesNearCount(1.6) > 0) return false;
        }

        if (unit.isZealot()) {
            if (zealotShouldSupportDragoons(unit)) return true;
        }

        return unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6);
    }

    private static boolean zealotShouldSupportDragoons(AUnit unit) {
        return unit.combatEvalRelative() > 0.85
            && unit.friendsNear().dragoons().inRadius(4, unit).atLeast(1);
    }
}
