package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.units.AUnit;

public class ObserverDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (unit.enemiesNear().detectors().inRadius(12, unit).notEmpty()) return false;
        if (!unit.effUndetected()) return false;

        return true;
    }
}
