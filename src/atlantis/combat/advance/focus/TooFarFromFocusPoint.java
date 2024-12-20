package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TooFarFromFocusPoint extends MoveToFocusPoint {
    public TooFarFromFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isLeader()) return false;
        if (focusPoint == null || !focusPoint.isValid()) return false;

        if (
            focusPoint.distTo(unit) >= 11
                && focusPoint.nameContains("Third", "Expansion")
//                && unit.enemiesNear().canBeAttackedBy(unit, 2.6).notEmpty()
        ) return true;

        if (unit.lastActionLessThanAgo(40, Actions.LOAD)) return false;
//        if (EnemyUnitBreachedBase.get() != null) return false;
//        if (unit.isMissionAttackOrGlobalAttack()) return false;

        evaluateDistToFocusPointComparingToLeader();

        if (distFromFocus == DistFromFocus.TOO_FAR) {
            if (unit.isTank() && unit.hasSiegedOrUnsiegedRecently()) return false;

            return true;
        }

        return false;
    }

    protected Manager handle() {
        if (act()) return usedManager(this);

        return null;
    }

    @Override
    public double optimalDist(AFocusPoint focusPoint) {
        return OptimalDistanceToFocusPoint.forUnit(unit, focusPoint);
    }

    protected boolean act() {
        if (focusPoint == null) return false;
//        if (!unit.looksIdle()) return false;

        double distToFocus = unit.distTo(focusPoint);
        HasPosition goTo = distToFocus <= 2.5
            ? goToWhenNotSoFarFromFocus()
            : goToWhenFarFromFocus();

//        if (unit.isDragoon()) A.errPrintln("distToFocus = " + distToFocus + " / " + goTo + " / " + unit);

        if (goTo != null) {
//            if (unit.isDragoon()) A.errPrintln("TOO FAR = " + unit.distToFocusPoint() + " / " + unit);

            if (goTo.isWalkable()) {
                if (unit.move(goTo, Actions.MOVE_FOCUS, "TooFar", true)) return true;
            }
            else {
                A.errPrintln("Unwalkable focus " + focusPoint + " for " + unit);
            }
        }

        return false;
    }

    private HasPosition goToWhenFarFromFocus() {
        if (!focusPoint.isAroundChoke()) return focusPoint;
        if (unit.distToFocusPoint() >= 10) return focusPoint;

        APosition goTo = focusPoint.translateTilesTowards(-3, focusPoint.choke().center());
        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        goTo = focusPoint.translateTilesTowards(-1.7, focusPoint.choke().center());
        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        return focusPoint;
    }

    private HasPosition goToWhenNotSoFarFromFocus() {
        APosition goTo = unit.translateTilesTowards(0.15, focusPoint);

        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        return focusPoint;
    }
}

