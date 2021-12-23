package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ATargetingForSpecificUnits {

    public static AUnit target(AUnit unit) {
        if (unit.isArchon() || unit.isUltralisk()) {
            return furthestTargetInRange(unit);
        }

        return null;
    }

    // =========================================================

    private static AUnit furthestTargetInRange(AUnit unit) {
        return Select.enemyRealUnits().canBeAttackedBy(unit, 0).mostDistantTo(unit);
    }

}
