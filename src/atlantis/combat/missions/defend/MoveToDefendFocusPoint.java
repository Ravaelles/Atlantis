package atlantis.combat.missions.defend;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.focus.MoveToFocusPoint;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class MoveToDefendFocusPoint extends MoveToFocusPoint {

    public boolean move(AUnit unit, AFocusPoint focusPoint) {
        if (unit.isRunning()) {
            return true;
        }

        if (unit.lastActionLessThanAgo(2)) {
            unit.setTooltip(unit.tooltip() + ".");
            return true;
        }

        this.unit = unit;
        this.focus = focusPoint;

//        if (holdOnPerpendicularLine()) {
//            return true;
//        }

        fromSide = focusPoint.fromSide();
        optimalDist = optimalDist(unit);
        unitToFocus = unit.distTo(focus);
        unitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        focusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());

        if (spreadOut()) {
            return true;
        }

//        if (unit.lastActionMoreThanAgo(5, Actions.MOVE_FORMATION)) {
            if (focus.isAroundChoke()) {
    //            if (unit.debug())System.out.println("handleWrongSideOfFocus " + unit);
                if (
                    handleWrongSideOfFocus(unit, focusPoint) || tooFarBack() || tooCloseToFocusPoint()
                ) {
                    return true;
                }
            }
//        }

        return advance();
    }

    protected boolean advance() {
        focus = unit.mission().focusPoint();

        if (focus == null || !focus.hasPosition()) {
//            System.err.println("Null focus point for " + unit + " in MoveToFocusPoint");
            System.err.println("unit.mission() = " + unit.mission());
            A.printStackTrace("Null focus point for " + unit + " in MoveToFocusPoint");
            return false;
        }

//        if (unit.enemiesNear().inRadius(5, unit).notEmpty()) {
//            if (!unit.isZergling() || unit.hp() >= 20) {
//                unit.addLog("DontWithdraw");
//                return false;
//            }
//        }

        if (unitToFocus > (optimalDist + MARGIN)) {
            String dist = A.dist(unitToFocus);

            if (unit.lastActionMoreThanAgo(20, Actions.MOVE_FOCUS)) {
                APosition position =
                    (unit.distTo(focus) <= 6 ||
                    (
                        focus.region() != null && focus.region().equals(unit.position().region()))
                        ? focus.translatePercentTowards(unit, 40) : focus
                    );
                return unit.move(
                    position,
                    Actions.MOVE_FOCUS,
                    "ToFocus" + dist,
                    true
                );
            }
        }

        return false;
    }

    // =========================================================


    public double optimalDist(AUnit unit) {
//        if (unit.isZealot()) {
//            private final double SPARTA_MODE_DIST_FROM_FOCUS = 0.55;
//            return SPARTA_MODE_DIST_FROM_FOCUS + letWorkersComeThroughBonus();
//        }

        double base = 0.0;

        if (We.zerg() && Enemy.protoss()) {
            base = 0.6;
        }

        if (unit.isTerran()) {
            base += (unit.isTank() ? 2.5 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isFirebat() ? -1.5 : 0)
                + (unit.isRanged() ? 1 : 0)
                + (Select.our().inRadius(1.5, unit).count() / 25.0);

            if (We.zerg() && focus.isAroundChoke()) {
                base += (focus.choke().width() <= 3) ? 3.5 : 0;
            }
        }

        return Math.max(
            (unit.isRanged() ? 3.7 : 0),
            base
                + letWorkersComeThroughBonus(unit)
                + (unit.isDragoon() ? 1.7 : 0)
        );
    }

    private double letWorkersComeThroughBonus(AUnit unit) {
        if (We.protoss() && A.seconds() >= 150) {
            return 0;
        }

        return unit.enemiesNear().combatUnits().isEmpty()
                && Select.ourWorkers().inRadius(7, unit).atLeast(1)
                ? 3 : 0;
    }

}
