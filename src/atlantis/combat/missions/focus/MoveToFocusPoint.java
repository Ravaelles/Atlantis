package atlantis.combat.missions.focus;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public abstract class MoveToFocusPoint {

    protected static final double MARGIN = 0.15;

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

        if (joinSquad()) {
            return true;
        }

        if (unit.lastActionLessThanAgo(6, Actions.MOVE_FORMATION)) {
            return true;
        }

        unitToFocus = unit.distTo(focus);
        optimalDist = optimalDist();

        if (unit.isMissionDefendOrSparta() && unit.enemiesNear().inRadius(5, unit).notEmpty()) {
            unit.addLog("DoNotWithdraw");
            return false;
        }

        if (unitToFocus > (optimalDist + MARGIN)) {
            String dist = A.dist(unitToFocus);
            if (unit.lastActionMoreThanAgo(35, Actions.MOVE_FOCUS)) {
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

    public boolean isOnValidSideOfChoke(AUnit unit, AFocusPoint focus) {
        if (focus == null || !focus.isAroundChoke()) {
            return true;
        }

        APosition choke = focus.choke().center();
        double unitToChoke = unit.distTo(choke);
        double focusToChoke = focus.distTo(choke);

        if (unitToChoke < focusToChoke) {
            return false;
        }

        double unitToFromSide = unit.distTo(focus.fromSide());
        double focusToFromSide = focus.distTo(focus.fromSide());

        return unitToFromSide <= focusToFromSide;
    }

    /**
     * Unit is too far from its focus point and/or is on the wrong side of it (most evident on ramps).
     */
    public boolean handleWrongSideOfFocus(AUnit unit, AFocusPoint focus) {
        if (unit.isAir()) {
            return false;
        }

        APosition withdrawTo = focus.fromSide();

        if (!focus.isAroundChoke() || withdrawTo == null) {
//            System.out.println("fromSide = " + fromSide);
//            System.out.println("FOCUS POINT = " + focus.toString());
//            System.out.println("isAroundChoke = " + focus.isAroundChoke());
//            System.out.println("focus.fromSide() = " + focus.fromSide());
            return false;
        }

        double distToFocusPoint = unit.distToFocusPoint();
//        double optimalDist = unit.mission().optimalDist();

        boolean onValidSideOfChoke = isOnValidSideOfChoke(unit, focus);
//        APainter.paintCircleFilled(unit, 6, onValidSideOfChoke ? Color.Green : Color.Red);

        if (!onValidSideOfChoke && distToFocusPoint <= 7) {
            makeFriendsHelpWithdraw(unit, focus);

            unit.move(withdrawTo, Actions.MOVE_FOCUS, "Withdraw", true);
            return true;
        }

//        if (!isOnValidSideOfChoke(unit, focus) && distToFocusPoint <= 7) {
////        if (
////            distToFocusPoint < optimalDist
////                || (!isOnValidSideOfChoke(unit, focus) && distToFocusPoint <= 7)
////        ) {
//        if (makeFriendsHelpWithdraw(unit, focus)) return true;
//
//            unit.move(focus.fromSide(), Actions.MOVE_FOCUS, "Withdraw", true);
//            return true;
//        }

        return false;
    }

    private static boolean makeFriendsHelpWithdraw(AUnit unit, AFocusPoint focus) {
        if (unit.enemiesNear().combatUnits().empty()) {
            for (AUnit friend : unit.friendsNear().inRadius(0.3, unit).combatUnits().list()) {
                APosition withdrawFriendTo = friend.translateTilesTowards(1, focus.fromSide());
                if (friend.move(withdrawFriendTo, Actions.MOVE_FOCUS, "HelpWithdraw", true)) {
                    friend.setTooltip("HelpWithdraw", true);
                }
                return true;
            }
        }
        return false;
    }

    // =========================================================

    protected boolean isTooClose() {
        return unitToFocus < (optimalDist - MARGIN);
    }

    private boolean isTooFar() {
        return unitToFocus >= (optimalDist + MARGIN);
    }

    /**
     * Unit is too close to its focus point.
     */
    protected boolean tooCloseToFocusPoint() {
        if (!isAroundChoke()) {
            return false;
        }

        if (unit.isMelee() && unit.hp() <= 18 && unitToFocus <= 3.5) {
            if (unit.moveAwayFrom(focus, 2, "InjuredSafety", Actions.MOVE_FOCUS)) {
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

            APosition fromSide = focus.fromSide();
            if (fromSide != null) {
                unit.move(focus, Actions.MOVE_FORMATION, "TooClozze" + dist);
                return true;
            }

            unit.moveAwayFrom(focus, 0.1, "TooCloze" + dist, Actions.MOVE_FOCUS);
            return true;
        }

        return false;
    }

    protected boolean tooFarBack() {
        if (isTooFar()) {
            boolean isTooFar = isTooFar();
            APosition goTo = isTooFar ? focus : unit.translateTilesTowards(0.1, focus);

            if (goTo.isWalkable()) {
                unit.move(goTo, Actions.MOVE_FOCUS, "TooFar", true);
                return true;
            }
        }

        return false;
    }

    private boolean isAroundChoke() {
        return focus != null && focus.isAroundChoke();
    }

}
