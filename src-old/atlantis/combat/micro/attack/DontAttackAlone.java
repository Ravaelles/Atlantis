package atlantis.combat.micro.attack;

import atlantis.units.AUnit;

public class DontAttackAlone {
    public static boolean isAlone(AUnit unit) {
        if (
            unit.isMelee()
                && !unit.isMissionSparta()
                && unit.friendsNear().inRadius(3, unit).empty()
        ) return true;

        return false;
    }
}
