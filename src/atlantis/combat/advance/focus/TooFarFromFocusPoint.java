package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class TooFarFromFocusPoint extends MoveToFocusPoint {
    public TooFarFromFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.lastActionLessThanAgo(40, Actions.LOAD)) return false;
        if (EnemyWhoBreachedBase.get() != null) return false;
        if (unit.isMissionAttackOrGlobalAttack()) return false;

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
    public double optimalDist(AFocusPoint focusPoint) {
        return 4;
    }

    protected boolean act() {
        if (focusPoint == null) return false;

        double distToFocus = unit.distTo(focusPoint);
        HasPosition goTo = distToFocus <= 4
            ? goToWhenNotSoFarFromFocus()
            : goToWhenFarFromFocus();

        if (goTo != null && goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_FOCUS, "TooFar", true)) return true;
        }

        return false;
    }

    private HasPosition goToWhenFarFromFocus() {
        if (!focusPoint.isAroundChoke()) return focusPoint;

        APosition goTo = focusPoint.translateTilesTowards(-3, focusPoint.choke());

        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        return null;
    }

    private HasPosition goToWhenNotSoFarFromFocus() {
        APosition goTo = unit.translateTilesTowards(0.15, focusPoint);

        if (goTo != null && goTo.isWalkable()) {
            return goTo;
        }

        return Select.mainOrAnyBuilding();
    }
}

