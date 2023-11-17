package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class TooCloseToFocusPoint extends MoveToFocusPoint {
    public TooCloseToFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
        if (unit.isMissionAttackOrGlobalAttack()) return false;
        if (unit.lastActionLessThanAgo(60, Actions.LOAD)) return false;

        if (evaluateDistFromFocusPoint() == DistFromFocus.TOO_CLOSE) {
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
//        APosition goTo = isTooFar ? focusPoint : unit.translateTilesTowards(0.1, focusPoint);
        HasPosition goTo = fromSide != null ? fromSide : Select.main();

        if (goTo != null && goTo.isWalkable()) {
            unit.move(goTo, Actions.MOVE_FOCUS, "TooClose", true);
            return true;
        }

        return false;
    }

    @Override
    public double optimalDist() {
        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;
        return 4;
    }
}

