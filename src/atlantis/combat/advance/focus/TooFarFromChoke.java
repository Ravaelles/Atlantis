package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TooFarFromChoke extends MoveToFocusPoint {
    public TooFarFromChoke(AUnit unit) {
        super(unit);
    }

    @Override
    public double optimalDist() {
        return 4;
    }

    public Manager handle() {
        if (act()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean isTooFar() {
        return unitToFocus >= (optimalDist + MARGIN);
    }

    protected boolean act() {
        if (isTooFar()) {
            boolean isTooFar = isTooFar();
            APosition goTo = isTooFar ? focusPoint : unit.translateTilesTowards(0.1, focusPoint);

            if (goTo.isWalkable()) {
                unit.move(goTo, Actions.MOVE_FOCUS, "TooFar", true);
                return true;
            }
        }

        return false;
    }
}

