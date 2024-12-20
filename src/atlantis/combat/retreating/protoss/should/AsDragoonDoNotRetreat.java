package atlantis.combat.retreating.protoss.should;

import atlantis.units.AUnit;

public class AsDragoonDoNotRetreat {
    public static boolean doNotRetreat(AUnit unit) {
        if (!unit.isDragoon()) return false;
        if (unit.cooldown() >= 7) return false;
        if (unit.hp() < 60) return false;
        if (unit.combatEvalRelative() <= 1.4) return false;

        return unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6);
    }
}
