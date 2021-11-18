package atlantis.combat.missions;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public abstract class MoveToFocusPoint {

    protected static final double MARGIN = 0.6;

    protected static double optimalDist;
    protected static double distUnitToFocus;
    protected static double distUnitToFromSide;
    protected static double distFocusToFromSide;
    protected static AUnit unit;
    protected static AFocusPoint focusPoint;
    protected static APosition fromSide;

    // =========================================================

    protected static boolean tooFar() {
        if (fromSide != null) {
            if ((distUnitToFocus + distUnitToFromSide) > distFocusToFromSide * 1.15) {
                return unit.move(fromSide, UnitActions.MOVE_TO_FOCUS, "Withdraw");
            }
        }

        if (distUnitToFocus > (optimalDist + MARGIN)) {
            return unit.move(focusPoint, UnitActions.MOVE_TO_FOCUS, "TooFar");
        }

        return false;
    }

    protected static boolean tooClose() {
        if (distUnitToFocus <= (optimalDist - MARGIN)) {
            return unit.moveAwayFrom(focusPoint, 0.2, "TooClose");
        }

        return false;
    }

}
