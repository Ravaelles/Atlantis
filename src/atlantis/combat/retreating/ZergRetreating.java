package atlantis.combat.retreating;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ZergRetreating {

    public static boolean shouldSmallScaleRetreat(AUnit unit, Selection enemies) {
        if (unit.isZergling()) {
            if (unit.friendsNearInRadius(2) <= 2 * unit.enemiesNearInRadius(2)) {
                return false;
            }
        }

        return false;
    }

}
