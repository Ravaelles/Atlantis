package atlantis.combat.micro.attack;

import atlantis.units.AUnit;

public class CanAttackCombatBuilding {
    public static boolean isAllowed(AUnit unit, AUnit target) {
        if (!target.isCombatBuilding()) return true;
        if (target.canAttackTarget(unit)) return true;

        return unit.friendsNear().inRadius(6.8, target).atLeast(7);
    }
}
