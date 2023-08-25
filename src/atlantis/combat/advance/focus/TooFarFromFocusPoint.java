package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TooFarFromFocusPoint extends MoveToFocusPoint {
    public TooFarFromFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMissionAttack()) return false;

        if (evaluateDistFromFocusPoint() == DistFromFocus.TOO_FAR) {
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
    public double optimalDist() {
        return 4;
    }

    protected boolean act() {
        APosition goTo = unit.distTo(focusPoint) <= 3 ? unit.translateTilesTowards(0.15, focusPoint) : focusPoint;

        if (goTo.isWalkable()) {
            unit.move(goTo, Actions.MOVE_FOCUS, "TooFar", true);
            return true;
        }

        return false;
    }
}

