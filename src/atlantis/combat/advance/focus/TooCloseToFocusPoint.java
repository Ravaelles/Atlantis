package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class TooCloseToFocusPoint extends MoveToFocusPoint {
    private DistFromFocus distFromFocus;

    public TooCloseToFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
//        if (unit.isMissionAttackOrGlobalAttack()) return false;
        if (unit.isSpecialMission() && unit.isMelee()) return false;
        if (unit.lastActionLessThanAgo(60, Actions.LOAD)) return false;
//        if (EnemyUnitBreachedBase.notNull()) return false;
        if (focusPoint == null || !focusPoint.isValid()) return false;

//        System.err.println("           " + evaluateDistFromFocusPoint() + " / " + unit);


        evaluateDistToFocusPointComparingToLeader();

//        System.err.println("unitToFocus = " + unitToFocus + " / " + distFromFocus);

        if (distFromFocus == DistFromFocus.TOO_CLOSE) {
            // Be brave with ChokeBlockersAssignments
//            if (unit.friendsNear().workers().specialAction().inRadius(7, unit).atLeast(2)) return false;

            if (unit.isTank() && unit.hasSiegedOrUnsiegedRecently()) return false;

            return true;
        }

        return false;
    }

    protected Manager handle() {
        if (focusPoint == null) return null;

        if (act()) return usedManager(this);

        return null;
    }

    @Override
    public double optimalDist(AFocusPoint focusPoint) {
        return OptimalDistanceToFocusPoint.forUnit(unit, focusPoint);
    }

    protected boolean act() {
//        if (asDragoon()) return true;
//
////        APosition goTo = isTooFar ? focusPoint : unit.translateTilesTowards(0.1, focusPoint);
//        if (goAway()) return true;
//        if (goAwayFromCenter()) return true;
//        if (goToMain()) return true;

        if (A.s % 4 <= 1) {
            if (goToMain()) return true;
        }

        if (goAway()) return true;
        if (goAwayFromCenter()) return true;

        return false;
    }

    private boolean goAway() {
//        if (A.fr % 50 <= 15) return unit.moveToMain(Actions.MOVE_FOCUS);

        return unit.moveAwayFrom(focusPoint, 0.1, Actions.MOVE_FOCUS, "TooCloseF");
    }

    private boolean asDragoon() {
        if (!unit.isDragoon()) return false;

//        System.err.println("TOO CLOSE = " + unit.distToFocusPoint() + " / " + unit);
//        if (unit.distToFocusPoint() <= 2.6) {

        if (Missions.isGlobalMissionDefendOrSparta()) {
            if (A.everyNthGameFrame(10)) unit.holdPosition("DragoonTooCloseA");
//            else unit.moveToMain(Actions.MOVE_FOCUS, "DragoonTooCloseB");
            else goAway();
        }
//        }

        return false;

//        if (unit.hp() >= 30) {
//            unit.holdPosition("DragoonHold");
//            return true;
//        }
//
//        return false;
    }

    private boolean goAwayFromCenter() {
        AChoke choke = focusPoint.choke();
        HasPosition goTo = choke;

        if (goTo == null) return false;

        goTo = goTo.translateTilesTowards(-0.2, choke);

        if (goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_FOCUS, "TooClose", true)) return true;
        }

        return false;
    }

    private boolean goToMain() {
//        if (unit.isDragoon()) {
//            if (A.everyNthGameFrame(3)) {
//                unit.holdPosition("SlowlyTooClose");
//            }
//            return false;
//        }

        HasPosition goTo = fromSide != null ? fromSide : Select.main();

        if (goTo != null && goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_FOCUS, "TooCloseM", true)) return true;
        }

        return false;
    }
}
