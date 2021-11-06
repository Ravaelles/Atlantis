package atlantis.combat.micro.managers;

import atlantis.combat.missions.MissionUnitManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.We;

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
//        double optimalDist = 6.5 - unit.lastUnderAttackAgo() / (30 * 25.0);
        double optimalDist = 6 - unit.hpPercent() / 66.0;
        double distToFocusPoint = unit.distTo(focusPoint);
        double margin = Math.max(1.2, (unit.squadSize() - 6) / 10.0);
        boolean result;

        if (We.terran() && handleTerranAdvance(unit)) {
            return true;
        }

        // Too close
        if (
                allowTooClose
                && distToFocusPoint <= optimalDist - margin
                && (result = unit.moveAwayFrom(focusPoint, 2.5, "#Adv:TooClose(" + (int) distToFocusPoint + ")"))
        ) {
            return result;
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
            return unit.move(focusPoint, UnitActions.MOVE_TO_ENGAGE, "#Adv:Back(" + (int) distToFocusPoint + ")");
        }

        return false;
    }

    // =========================================================

    private static boolean handleTerranAdvance(AUnit unit) {
        if (Select.our().tanks().isEmpty()) {
            return false;
        }

        if (unit.isTank()) {
            return false;
        }

        double maxRadiusFromTank = 4 + Math.sqrt(Count.ourCombatUnits());
        int tanksNearby = Select.our().tanks().inRadius(maxRadiusFromTank, unit).count();

        if (tanksNearby >= 1) {
            return false;
        }

        unit.move(
                unit.position().translatePercentTowards(Select.our().tanks().nearestTo(unit), 30),
                UnitActions.MOVE_TO_FOCUS,
                "ToTank"
        );
        return true;
    }

}