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
    public double optimalDist() {
        return 4;
    }

    public Manager handle() {
        if (isTooFar()) {
            if (act()) return usedManager(this);
        }

        return null;
    }

    private boolean isTooFar() {
        return evaluateDistFromFocusPoint() == DistFromFocus.TOO_FAR;
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

