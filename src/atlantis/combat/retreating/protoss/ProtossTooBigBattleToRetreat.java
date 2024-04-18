package atlantis.combat.retreating.protoss;

import atlantis.units.AUnit;

public class ProtossTooBigBattleToRetreat {
    public static boolean doNotRetreat(AUnit unit) {
        if (unit.isDragoon()) {
            if (unit.isHealthy()) return true;
            if (unit.hp() <= 40) return false;
            if (unit.cooldown() >= 12) return false;
            if (unit.cooldown() >= 5 && unit.meleeEnemiesNearCount(1.2) > 0) return false;
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
