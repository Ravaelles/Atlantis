package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;

public class ObserverDontAvoidEnemy {
    public static Decision dontAvoid(AUnit unit) {
        if (!unit.isObserver()) return Decision.INDIFFERENT;

        if (!unit.effUndetected()) return Decision.FALSE;
        if (unit.enemiesNear().detectors().inRadius(12, unit).notEmpty()) return Decision.FALSE;

        double safetyMargin = 2 + unit.woundPercent() / 18.0;
        if (unit.enemiesNear().havingAntiAirWeapon().canAttack(unit, safetyMargin).notEmpty()) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }
}
