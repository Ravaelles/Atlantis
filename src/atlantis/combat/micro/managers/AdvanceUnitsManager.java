package atlantis.combat.micro.managers;

import atlantis.combat.missions.MissionUnitManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class AdvanceUnitsManager extends MissionUnitManager {

//    private Mission mission;
//
//    public boolean updateUnit(AUnit unit) {
//        unit.setTooltip("#Adv");
//
//        if (unit.distanceTo(mission.focusPoint()) > 6) {
//            unit.move(mission.focusPoint(), UnitActions.MOVE_TO_ENGAGE, "#MA:Forward!");
//            return true;
//        }
//
//        return false;
//    }

    public static boolean attackMoveToFocusPoint(AUnit unit, APosition focusPoint) {
        return moveToFocusPoint(unit, focusPoint, false, false);
    }

    public static boolean moveToFocusPoint(AUnit unit, APosition focusPoint) {
        return moveToFocusPoint(unit, focusPoint, true, true);
    }

    // =========================================================

    private static boolean moveToFocusPoint(
            AUnit unit, APosition focusPoint, boolean allowTooClose, boolean allowCloseEnough
    ) {
        double optimalDist = 6.5;
        double distToFocusPoint = unit.distTo(focusPoint);
        double margin = Math.max(0, (unit.squadSize() - 6) / 10);

        // Too close
        if (
                allowTooClose
                && distToFocusPoint <= optimalDist - margin
                && unit.moveAwayFrom(focusPoint, 2.5, "#Adv:Too close(" + (int) distToFocusPoint + ")")
        ) {
            return true;
        }

        // Close enough
        else if (allowCloseEnough && distToFocusPoint <= optimalDist + margin) {
            if (unit.isMoving()) {
                unit.stop("#Adv:Good(" + (int) distToFocusPoint + ")");
            }
            return true;
        }

        // Too far
        else if (distToFocusPoint > optimalDist + margin) {
            unit.move(focusPoint, UnitActions.MOVE_TO_ENGAGE, "#Adv(" + (int) distToFocusPoint + ")");
            return true;
        }

        return false;
    }

}