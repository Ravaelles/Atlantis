package atlantis.combat.advance.focus;

import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

//public abstract class MoveToFocusPoint extends HasUnit {
public abstract class MoveToFocusPoint extends MissionManager {
    protected static final double MARGIN = 0.15;


    protected double optimalDist;
    protected double unitToFocus;
    protected double unitToFromSide;
    protected double focusToFromSide;
    protected APosition fromSide;

    // =========================================================

    public MoveToFocusPoint(AUnit unit) {
        super(unit);

        fromSide = focusPoint.fromSide();
        optimalDist = optimalDist();
        unitToFocus = unit.distTo(focusPoint);
        unitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        focusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());
    }

    // =========================================================

    /**
     * Optimal distance to focus point or -1 if not defined.
     */
    public abstract double optimalDist();

//    public double optimalDist() {
//        A.printStackTrace("optimalDist - To be implemented");
//        return 0;
//    }

    protected boolean move() {
        focusPoint = unit.mission().focusPoint();

        if (focusPoint == null) {
//            System.err.println("Null focus point for " + unit + " in MoveToFocusPoint");
            System.err.println("unit.mission() = " + unit.mission());
            A.printStackTrace("Null focus point for " + unit + " in MoveToFocusPoint");
            return false;
        }

        if (joinSquad()) {
            return true;
        }

        if (unit.lastActionLessThanAgo(6, Actions.MOVE_FORMATION)) {
            return true;
        }

        unitToFocus = unit.distTo(focusPoint);
        optimalDist = optimalDist();

        if (unit.isMissionDefendOrSparta() && unit.enemiesNear().inRadius(5, unit).notEmpty()) {
            unit.addLog("DoNotWithdraw");
            return false;
        }

        if (unitToFocus > (optimalDist + MARGIN)) {
            String dist = A.dist(unitToFocus);
            if (unit.lastActionMoreThanAgo(35, Actions.MOVE_FOCUS)) {
                APosition position =
                    (focusPoint.distTo(unit) <= 6 || (focusPoint.region() != null && focusPoint.region().equals(unit.position().region())))
                        ? focusPoint.translatePercentTowards(unit, 40) : focusPoint;

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

    protected boolean spreadOut() {
        if (unit.lastActionLessThanAgo(11, Actions.MOVE_FORMATION)) {
            return true;
        }

        Selection friends = unit.friendsNear().groundUnits().inRadius(1, unit);
        if (friends.notEmpty()) {
            AUnit nearest = friends.nearestTo(unit);
            if (unit.distTo(nearest) <= 0.1) {
//                if (nearest.lastActionLessThanAgo(10, Actions.MOVE_FORMATION)) {
////                    boolean b = nearest.moveAwayFrom(unit, 0.1, "Separate", Actions.MOVE_FORMATION)
////                        || nearest.moveAwayFrom(unit, 2, "Separate", Actions.MOVE_FORMATION);
//                }
                if (
                    unit.lastActionLessThanAgo(11, Actions.MOVE_FORMATION)
                ) {
//                    boolean b = unit.moveAwayFrom(nearest, 0.1, "Separate", Actions.MOVE_FORMATION)
//                        || unit.moveAwayFrom(nearest, 2, "Separate", Actions.MOVE_FORMATION);
//                    return b;
                    APosition goTo = unit.makeFreeOfAnyGroundUnits(3, 0.2, unit);
                    if (goTo != null) {
                        return unit.move(goTo, Actions.MOVE_FORMATION, "Separate", false);
                    }
                }
            }
        }

        return false;
    }

    private boolean joinSquad(AUnit unit) {
        if (unit.distToLeader() >= 8 && unit.enemiesNear().isEmpty() && unit.friendsInRadius(2.1).atMost(2)) {
            unit.addLog("JoinSquad");
            return unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, "JoinSquad", false);
        }
        return false;
    }


    // =========================================================

    protected boolean isTooClose() {
        return unitToFocus < (optimalDist - MARGIN);
    }

    /**
     * Unit is too close to its focus point.
     */
    protected boolean tooCloseToFocusPoint() {
        if (!isAroundChoke()) {
            return false;
        }

        if (unit.isMelee() && unit.hp() <= 18 && unitToFocus <= 3.5) {
            if (unit.moveAwayFrom(focusPoint, 2, "InjuredSafety", Actions.MOVE_FOCUS)) {
                return true;
            }
        }

        if (unit.enemiesNear().inRadius(2, unit).isNotEmpty()) {
            return false;
        }

        if (unit.isZealot() && unit.enemiesNear().inRadius(6, unit).notEmpty()) {
            return false;
        }

        if (isTooClose()) {
            String dist = A.dist(unitToFocus);

//            if (distUnitToFromSide > 3) {
//                return unit.move(fromSide, Actions.MOVE_FOCUS, "TooClose" + dist, true);
//            }

            APosition fromSide = focusPoint.fromSide();
            if (fromSide != null) {
                unit.move(focusPoint, Actions.MOVE_FORMATION, "TooClozze" + dist);
                return true;
            }

            unit.moveAwayFrom(focusPoint, 0.1, "TooCloze" + dist, Actions.MOVE_FOCUS);
            return true;
        }

        return false;
    }


    private boolean isAroundChoke() {
        return focusPoint != null && focusPoint.isAroundChoke();
    }

}

