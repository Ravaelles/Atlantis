package atlantis.interrupt;

import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class DisturbInterruptManager {

    public static boolean dontInterrupt(AUnit unit) {
        if (unit.lastActionLessThanAgo(7, UnitActions.LOAD)) {
            return true;
        }

        return false;
    }

}
