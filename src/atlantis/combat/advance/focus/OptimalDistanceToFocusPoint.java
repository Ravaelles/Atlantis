package atlantis.combat.advance.focus;

import atlantis.units.AUnit;

public class OptimalDistanceToFocusPoint {
    public static double forUnit(AUnit unit) {
        if (unit.isDragoon()) return 2.8;
        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;
        return 4;
    }
}
