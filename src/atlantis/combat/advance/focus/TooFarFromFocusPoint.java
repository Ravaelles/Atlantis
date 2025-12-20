package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class TooFarFromFocusPoint extends MoveToFocusPoint {
    public TooFarFromFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (focus == null || !focus.isValid()) return false;
//        if (!unit.isLeader()) return false;
        if (unit.isAir() && !unit.isAlphaSquad()) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.lastCommandIssuedAgo() <= 2) return false;
        if (unit.lastCommandIssuedAgo() >= 30) return false;
        if (unit.lastUnderAttackLessThanAgo(50)) return false;
        if (unit.enemiesNear().canAttack(unit, 5).notEmpty()) return false;
        if (unit.lastActionLessThanAgo(40, Actions.LOAD)) return false;
        if (unit.distToLeader() > 6) return false;
        if (unit.squadSize() >= 3 && unit.friendsInRadiusCount(0.8) == 0) return false;
        if (unit.enemiesThatCanAttackMe(4).count() > 0) return false;

        if (Enemy.terran()) {
            if (unit.enemiesNear().tanksSieged().countInRadius(11.5, unit) > 0) return false;
        }

//        if (unit.distToFocusPoint() > 4.5) return true;
//        System.err.println("dist = " + unit.distToDigit(focus) + ", optimalDist = " + optimalDist(focus));
        if (unit.distTo(focus) > optimalDist(focus) + 0.1) return true;

//        evaluateDistToFocusPointComparingToLeader();
//
//        if (distFromFocus == DistFromFocus.TOO_FAR || fallbackTooFarFromFocus()) {
//            if (unit.isTank() && unit.hasSiegedOrUnsiegedRecently()) return false;
//
//            return true;
//        }

        return false;
    }

    protected Manager handle() {
        if (focus == null) return null;

        if (
            unit.isMoving()
                && unit.isActiveManager(this)
                && unit.lastCommandIssuedAgo() <= (unit.enemiesNear().empty() ? 300 : 25)
        ) {
            return usedManager(this);
        }

        if (act()) return usedManager(this);

        return null;
    }

    private boolean fallbackTooFarFromFocus() {
        return unit.distToFocusPoint() >= 12;
    }

    private double optimalDist(AFocusPoint focusPoint) {
        return OptimalDistanceToFocusPoint.forUnit(unit, focusPoint);
    }

    protected boolean act() {
        if (focus == null) return false;
//        if (!unit.looksIdle()) return false;

        double distToFocus = unit.distTo(focus);
        HasPosition goTo = distToFocus <= 2.5
            ? goToWhenNotSoFarFromFocus()
            : goToWhenFarFromFocus();

//        if (unit.isDragoon()) A.errPrintln("distToFocus = " + distToFocus + " / " + goTo + " / " + unit);

        if (goTo != null) {
//            if (unit.isDragoon()) A.errPrintln("TOO FAR = " + unit.distToFocusPoint() + " / " + unit);

            if (!goTo.isWalkable()) goTo = goTo.makeWalkable(5, 1, null);
            if (goTo == null) {
                ErrorLog.printMaxOncePerMinute("TFFP: Can't make walkable " + goTo + " for " + unit);
                return false;
            }

            if (goTo.isWalkable()) {
                if (!unit.isMoving() || A.everyNthGameFrame(5)) {
                    unit.move(goTo, Actions.MOVE_FOCUS, "TooFar", true);
                    return true;
                }
            }
            else {
                ErrorLog.printMaxOncePerMinute("Unwalkable focus " + focus + " for " + unit);
            }
        }

        return false;
    }

    private HasPosition goToWhenFarFromFocus() {
        if (!focus.isAroundChoke()) return focus;
        if (unit.distToFocusPoint() >= 10) return focus;

        APosition goTo = focus.translateTilesTowards(-3, focus.choke().center());
        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        goTo = focus.translateTilesTowards(-1.7, focus.choke().center());
        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        return focus;
    }

    private HasPosition goToWhenNotSoFarFromFocus() {
        APosition goTo = unit.translateTilesTowards(0.15, focus);

        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        return focus;
    }
}

