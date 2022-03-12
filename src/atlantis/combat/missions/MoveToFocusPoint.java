package atlantis.combat.missions;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public abstract class MoveToFocusPoint {

    protected static final double MARGIN = 0.4;

    protected double optimalDist;
    protected double unitToFocus;
    protected double unitToFromSide;
    protected double focusToFromSide;
    protected AUnit unit;
    protected AFocusPoint focus;
    protected APosition fromSide;

    // =========================================================

    public abstract double optimalDist(AUnit unit);

//    public double optimalDist(AUnit unit) {
//        A.printStackTrace("optimalDist - To be implemented");
//        return 0;
//    }

    protected boolean advance() {
        focus = unit.mission().focusPoint();

        if (focus == null) {
//            System.err.println("Null focus point for " + unit + " in MoveToFocusPoint");
            System.err.println("unit.mission() = " + unit.mission());
            A.printStackTrace("Null focus point for " + unit + " in MoveToFocusPoint");
            return false;
        }

        unitToFocus = unit.distTo(focus);
        optimalDist = optimalDist(unit);

        if (unit.enemiesNear().inRadius(5, unit).notEmpty()) {
            unit.addLog("DontWithdraw");
            return false;
        }

        if (unitToFocus > (optimalDist + MARGIN)) {
            String dist = A.dist(unitToFocus);
            return unit.move(
                focus.translatePercentTowards(unit, 40),
                Actions.MOVE_FOCUS,
                "ToFocus" + dist,
                true
            );
        }

        return false;
    }

    // =========================================================

    public boolean isOnValidSideOfChoke(AUnit unit, AFocusPoint focus) {
        if (focus == null || !focus.isAroundChoke()) {
            return true;
        }

        double unitToFromSide = focus.fromSide() == null ? -1 : unit.distTo(focus.fromSide());
        double focusToFromSide = focus.fromSide() == null ? -1 : focus.distTo(focus.fromSide());

        return unitToFromSide <= focusToFromSide;
    }

    /**
     * Unit is too far from its focus point and/or is on the wrong side of it (most evident on ramps).
     */
    public boolean handleWrongSideOfFocus(AUnit unit, AFocusPoint focus) {
        if (!focus.isAroundChoke() || focus.fromSide() == null) {
//            System.out.println("fromSide = " + fromSide);
//            System.out.println("FOCUS POINT = " + focus.toString());
//            System.out.println("isAroundChoke = " + focus.isAroundChoke());
//            System.out.println("focus.fromSide() = " + focus.fromSide());
            return false;
        }

        if (!isOnValidSideOfChoke(unit, focus) && unit.distToFocusPoint() <= 7) {
            if (unit.enemiesNear().combatUnits().empty()) {
                for (AUnit friend : unit.friendsNear().inRadius(7, unit).list()) {
                    friend.move(focus.fromSide(), Actions.MOVE_FOCUS, "HelpWithdraw", true);
                    friend.setTooltip("HelpWithdraw", true);
                }
            }

            return unit.move(focus.fromSide(), Actions.MOVE_FOCUS, "Withdraw", true);
        }

        return false;
    }

    // =========================================================

    /**
     * Unit is too close to its focus point.
     */
    protected boolean tooCloseToFocusPoint() {
        if (!isAroundChoke()) {
            return false;
        }

        if (unit.enemiesNear().inRadius(2, unit).isNotEmpty()) {
            return false;
        }

        if (unit.isZealot() && unit.enemiesNear().inRadius(6, unit).notEmpty()) {
            return false;
        }

        if (unitToFocus <= (optimalDist - MARGIN)) {
            String dist = A.dist(unitToFocus);

//            if (distUnitToFromSide > 3) {
//                return unit.move(fromSide, Actions.MOVE_FOCUS, "TooClose" + dist, true);
//            }

            return unit.moveAwayFrom(focus, 0.15, "TooCloze" + dist, Actions.MOVE_FOCUS);
        }

        return false;
    }

    private boolean isAroundChoke() {
        return focus != null && focus.isAroundChoke();
    }

}
