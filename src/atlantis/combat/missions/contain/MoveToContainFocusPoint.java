package atlantis.combat.missions.contain;

import atlantis.combat.advance.Advance;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MoveToContainFocusPoint extends Advance {
    public MoveToContainFocusPoint(AUnit unit) {
        super(unit);
    }

//    public boolean move(AUnit unit, AFocusPoint focusPoint) {
//        if (unit.isRunning() || unit.enemiesNear().groundUnits().inRadius(7, unit).notEmpty()) {
//            return false;
//        }
//
//        if (unit.lastActionLessThanAgo(11, Actions.MOVE_FORMATION)) {
//            return true;
//        }
//
//        this.unit = unit;
//        this.focus = focusPoint;
//        fromSide = focusPoint.fromSide();
//
//        optimalDist = optimalDist(unit);
//        unitToFocus = unit.distTo(focusPoint);
//        unitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
//        focusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());
//
//        if (unit.enemiesNear().isEmpty()) {
//            if (unit.lastActionMoreThanAgo(5, Actions.MOVE_FORMATION)) {
//                return handleWrongSideOfFocus(unit, focusPoint) || tooCloseToFocusPoint();
//            }
//        }
//
////        if (spreadOut()) {
////            return true;
////        }
//
//        if (advance()) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public double optimalDist() {
//        double base = 7.2;
//        double ourUnitsNearBonus = Select.our().inRadius(2, unit).count() / 20.0;
//        int workersComeThroughBonus = workersComeThroughBonus();
//
//        return base
//            + (unit.isTank() ? 3.8 : 0)
//            + (unit.isMedic() ? -0.8 : 0)
////                + (unit.isMarine() ? 2 : 0)
//            + workersComeThroughBonus
////                + (unit.isMelee() ? 3 : 0)
//            + ourUnitsNearBonus;
//    }

    // =========================================================

    private int workersComeThroughBonus() {
        if (unit != null && unit.isMissionDefend()) {
            return Select.enemy().inRadius(5, unit).isEmpty()
                && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                ? 4 : 0;
        }

        return 0;
    }

}
