package atlantis.combat.missions.contain;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.focus.MoveToFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MoveToContainFocusPoint extends MoveToFocusPoint {

    public boolean move(AUnit unit, AFocusPoint focusPoint) {
        this.unit = unit;
        this.focus = focusPoint;
        fromSide = focusPoint.fromSide();

        optimalDist = optimalDist(unit);
        unitToFocus = unit.distTo(focusPoint);
        unitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        focusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());

        if (advance()) {
            return true;
        }

        if (unit.enemiesNear().isEmpty()) {
            return handleWrongSideOfFocus(unit, focusPoint) || tooCloseToFocusPoint();
        }

        return false;
    }

    @Override
    public double optimalDist(AUnit unit) {
        int workersComeThroughBonus = workersComeThroughBonus();

        double ourUnitsNearBonus = Select.our().inRadius(2, unit).count() / 20.0;

        return 6.5
            + (unit.isTank() ? 3.8 : 0)
            + (unit.isMedic() ? -0.8 : 0)
//                + (unit.isMarine() ? 2 : 0)
            + workersComeThroughBonus
//                + (unit.isMelee() ? 3 : 0)
            + ourUnitsNearBonus;
    }

    // =========================================================

    private int workersComeThroughBonus() {
        if (unit.mission() != null && unit.isMissionDefend()) {
            return Select.enemy().inRadius(5, unit).isEmpty()
                && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                ? 4 : 0;
        }

        return 0;
    }

}
