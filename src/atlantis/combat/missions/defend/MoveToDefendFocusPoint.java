package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MoveToFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MoveToDefendFocusPoint extends MoveToFocusPoint {

    protected static final double MARGIN = 0.6;

    public static boolean move(AUnit unit, AFocusPoint focusPoint) {
        MoveToDefendFocusPoint.unit = unit;
        MoveToDefendFocusPoint.focusPoint = focusPoint;
        fromSide = focusPoint.fromSide();

        optimalDist = optimalDist();
        distUnitToFocus = unit.distTo(focusPoint);
        distUnitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        distFocusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());

        if (tooFar() || tooClose()) {
            return true;
        }

        unit.holdPosition("DefendHere", true);
        return true;
    }

    // =========================================================

    protected static double optimalDist() {
        int letWorkersComeThroughBonus = Select.enemy().inRadius(5, unit).isEmpty()
                && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                ? 4 : 0;
        double ourUnitsNearbyBonus = Select.our().inRadius(2, unit).count() / 20.0;

        return 0.7
                + (unit.isTank() ? 3 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isMarine() ? 2 : 0)
                + letWorkersComeThroughBonus
                + (unit.isRanged() ? 3 : 0)
                + ourUnitsNearbyBonus;
    }

}
