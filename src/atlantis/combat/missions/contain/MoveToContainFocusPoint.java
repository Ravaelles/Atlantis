package atlantis.combat.missions.contain;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MoveToFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MoveToContainFocusPoint extends MoveToFocusPoint {

    protected static final double BASE_DIST_TO_FOCUS_POINT = 6.5;

//    protected static final double MARGIN = 0.6;

    public static boolean move(AUnit unit, AFocusPoint focusPoint) {
        MoveToContainFocusPoint.unit = unit;
        MoveToContainFocusPoint.focusPoint = focusPoint;
        fromSide = focusPoint.fromSide();

        optimalDist = optimalDist();
        distUnitToFocus = unit.distTo(focusPoint);
        distUnitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        distFocusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());

        if (advance()) {
            return true;
        }

        if (unit.enemiesNear().isEmpty()) {
            return wrongSideOfFocus() || tooCloseToFocusPoint();
        }

        return false;
    }

    // =========================================================

    protected static double optimalDist() {
        int workersComeThroughBonus = workersComeThroughBonus();

        double ourUnitsNearBonus = Select.our().inRadius(2, unit).count() / 20.0;

        return BASE_DIST_TO_FOCUS_POINT
                + (unit.isTank() ? 3.8 : 0)
                + (unit.isMedic() ? -0.8 : 0)
//                + (unit.isMarine() ? 2 : 0)
                + workersComeThroughBonus
//                + (unit.isMelee() ? 3 : 0)
                + ourUnitsNearBonus;
    }

    private static int workersComeThroughBonus() {
        if (unit.mission() != null && unit.isMissionDefend()) {
            return Select.enemy().inRadius(5, unit).isEmpty()
                    && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                    ? 4 : 0;
        }

        return 0;
    }

}
