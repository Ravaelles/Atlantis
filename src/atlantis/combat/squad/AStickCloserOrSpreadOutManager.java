package atlantis.combat.squad;

import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class AStickCloserOrSpreadOutManager {
    
    public static boolean handle(AUnit unit) {
        APosition medianUnit = unit.getSquad().getMedianUnitPosition();

        if (handleShoudSpreadOut(unit, medianUnit)) {
            return true;
        }

        double maxDistFromAnotherUnit = 2;
        if (handleShouldStickCloser(unit, medianUnit, maxDistFromAnotherUnit)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean handleShoudSpreadOut(AUnit unit, APosition medianPoint) {
        if (Select.ourCombatUnits().inRadius(3, unit).count() >= 6) {
            return unit.moveAwayFrom(medianPoint, 4, "Spread");
        }

        return false;
    }

    private static boolean handleShouldStickCloser(AUnit unit, APosition medianPoint, double maxDistFromAnotherUnit) {
        APainter.paintLine(unit, medianPoint, Color.Grey);

        if (unit.distanceTo(medianPoint) > 10) {
            return unit.move(medianPoint, UnitActions.MOVE, "Together");
        }
        else if (unit.distanceTo(medianPoint) <= 3 && unit.isMoving() && unit.getTargetPosition().equals(medianPoint)) {
            return false;
        }

        return false;
    }

}
