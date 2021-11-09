package atlantis.interrupt;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class DontDisturbInterrupt {

    public static boolean dontInterruptImportantActions(AUnit unit) {

        // Allow to use tech
        if (unit.lastActionLessThanAgo(3, UnitActions.USING_TECH)) {
            return true;
        }

        // "Patrol" is used only for user manual actions and unit will not be interrupted
        if (unit.isPatrolling()) {
            unit.setTooltip("Manual control");
            return true;
        }

        // Don't INTERRUPT shooting units
        if (DontInterruptStartedAttacks.shouldNotInterrupt(unit)) {
//            System.out.println(A.now() + " SHOOT");
            APainter.paintRectangle(unit.position().translateByPixels(-5, 0), 10, 3, Color.Red);
            return true;
        }

        // Allow unit to load to shuttle
        if (allowUnitToLoadToTransport(unit)) {
            APainter.paintRectangle(unit.position().translateByPixels(-5, 0), 10, 3, Color.Blue);
//            System.out.println(A.now() + " TRANSP");
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
