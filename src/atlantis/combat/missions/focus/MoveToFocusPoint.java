package atlantis.combat.missions.focus;

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

    /**
     * Optimal distance to focus point or -1 if not defined.
     */
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

        if (joinSquad(unit)) {
            return true;
        }

        unitToFocus = unit.distTo(focus);
        optimalDist = optimalDist(unit);

        if (unit.isMissionDefendOrSparta() && unit.enemiesNear().inRadius(5, unit).notEmpty()) {
            unit.addLog("DoNotWithdraw");
            return false;
        }

        if (unitToFocus > (optimalDist + MARGIN)) {
            String dist = A.dist(unitToFocus);
            if (unit.lastActionMoreThanAgo(20, Actions.MOVE_FOCUS)) {
                APosition position =
                    (focus.distTo(unit) <= 6 || (focus.region() != null && focus.region().equals(unit.position().region())))
                    ? focus.translatePercentTowards(unit, 40) : focus;

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

    private boolean joinSquad(AUnit unit) {
        if (unit.distToSquadCenter() >= 8 && unit.enemiesNear().isEmpty()) {
            unit.addLog("JoinSquad");
            return unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, "JoinSquad", false);
        }
        return false;
    }

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

        double distToFocusPoint = unit.distToFocusPoint();
        if (
            distToFocusPoint < unit.mission().optimalDist(unit)
                || (!isOnValidSideOfChoke(unit, focus) && distToFocusPoint <= 7)
        ) {
            if (unit.enemiesNear().combatUnits().empty()) {
                for (AUnit friend : unit.friendsNear().combatUnits().inRadius(7, unit).list()) {
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
        if (unit.isMelee() && unit.hp() <= 18 && unitToFocus <= 3.5) {
            return unit.moveAwayFrom(focus, 2, "InjuredSafety", Actions.MOVE_FOCUS);
        }

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
