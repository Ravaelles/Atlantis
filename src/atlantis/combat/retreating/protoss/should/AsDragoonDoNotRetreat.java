package atlantis.combat.retreating.protoss.should;

import atlantis.units.AUnit;

public class AsDragoonDoNotRetreat {
    public static boolean doNotRetreat(AUnit unit) {
        if (!unit.isDragoon()) return false;
        if (unit.hp() <= 33) return false;

        if (unit.cooldown() <= 7 && unit.distToMain() <= 7) return true;

//        return unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6);

        return false;
    }
}
