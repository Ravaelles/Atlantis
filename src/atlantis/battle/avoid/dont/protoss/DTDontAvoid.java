package atlantis.battle.avoid.dont.protoss;

import atlantis.units.AUnit;

public class DTDontAvoid {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDT()) return false;
        if (unit.effVisible() && unit.isWounded()) return false;
        if (unit.lastUnderAttackLessThanAgo(90)) return false;

        return unit.enemiesNear().detectors().inRadius(13.5, unit).empty();
    }
}
