package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
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
        if (unit.isMissionAttackOrGlobalAttack()) return false;
        if (unit.isSpecialMission()) return false;
        if (unit.lastActionLessThanAgo(60, Actions.LOAD)) return false;
        if (EnemyWhoBreachedBase.notNull()) return false;
        if (unit.isSquadScout()) return false;

        distFromFocus = evaluateDistFromFocusPoint();

        if (distFromFocus == DistFromFocus.TOO_CLOSE) {
            // Be brave with ChokeBlockersAssignments
//            if (unit.friendsNear().workers().specialAction().inRadius(7, unit).atLeast(2)) return false;

            if (unit.isTank() && unit.hasSiegedOrUnsiegedRecently()) return false;

            return true;
        }

        return false;
    }

    protected Manager handle() {
        if (act()) return usedManager(this);

        return null;
    }

    protected boolean act() {
        if (asDragoon()) return true;

//        APosition goTo = isTooFar ? focusPoint : unit.translateTilesTowards(0.1, focusPoint);
        if (goAwayFromCenter()) return true;
        if (goToMain()) return true;

        return false;
    }

    private boolean asDragoon() {
        if (!unit.isDragoon()) return false;

//        if (unit.distToFocusPoint() <= 2.6) {
        if (A.everyNthGameFrame(15)) unit.holdPosition("DragoonTooCloseA");
        else unit.moveToMain(Actions.MOVE_FOCUS, "DragoonTooCloseB");
//        }

        return true;

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
            unit.move(goTo, Actions.MOVE_FOCUS, "TooClose", true);
            return true;
        }
        return false;
    }

    private boolean goToMain() {
        if (unit.isDragoon()) {
            if (A.everyNthGameFrame(3)) {
                unit.holdPosition("SlowlyTooClose");
            }
            return false;
        }

        HasPosition goTo = fromSide != null ? fromSide : Select.main();

        if (goTo != null && goTo.isWalkable()) {
            unit.move(goTo, Actions.MOVE_FOCUS, "TooCloseB", true);
            return true;
        }
        return false;
    }

    @Override
    public double optimalDist() {
        return OptimalDistanceToFocusPoint.forUnit(unit);
    }
}
