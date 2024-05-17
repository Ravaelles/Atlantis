package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;

public class ObserverDontAvoidEnemy {
    public static Decision shouldAvoid(AUnit unit) {
        if (!unit.effUndetected()) return Decision.TRUE;
        if (unit.enemiesNear().detectors().inRadius(12, unit).notEmpty()) return Decision.TRUE;

        double safetyMargin = 2 + unit.woundPercent() / 18.0;
        if (unit.enemiesNear().havingAntiAirWeapon().canAttack(unit, safetyMargin).notEmpty()) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }
}
