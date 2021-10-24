package atlantis.interrupt;

import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class DontDisturbInterrupt {

    public static boolean dontInterruptImportantActions(AUnit unit) {

        // Don't INTERRUPT shooting units
        if (DontInterruptStartedAttacks.shouldNotInterrupt(unit)) {
            return true;
        }

        // Allow unit to load to shuttle
        if (allowUnitToLoadToTransport(unit)) {
            return true;
        }

        if (allowUnitToContinueRareRightClickActions(unit)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean allowUnitToLoadToTransport(AUnit unit) {
        return !unit.type().isTransport() && unit.lastActionLessThanAgo(13, UnitActions.LOAD);
    }

    private static boolean allowUnitToContinueRareRightClickActions(AUnit unit) {
        return unit.lastActionLessThanAgo(6, UnitActions.RIGHT_CLICK);
    }

}
