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

        if (handleShouldSpreadOut(unit, medianUnit)) {
            return true;
        }

//        if (handleShouldStickCloser(unit, medianUnit)) {
//            return true;
//        }

        return false;
    }

    // =========================================================

    private static boolean handleShouldSpreadOut(AUnit unit, APosition medianPoint) {
        if (
                Select.ourCombatUnits().inRadius(4.5, unit).count() >= 6
                || Select.ourCombatUnits().inRadius(2.5, unit).count() >= 3
        ) {
            return unit.moveAwayFrom(medianPoint, 2, "Spread out");
        }

        return false;
    }

//    private static boolean handleShouldStickCloser(AUnit unit, APosition medianPoint) {
//        if (
//                Select.ourCombatUnits().inRadius(2, unit).count() >= 0
//        ) {
//            return unit.moveAwayFrom(medianPoint, 2, "Spread out");
//        }
//
//        return false;
//    }

}
