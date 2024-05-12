package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FixIdleUnits extends Manager {
    public FixIdleUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit()
            && (unit.isStopped() || unit.isIdle())
            && A.fr % 4 == 0
            && unit.noCooldown()
            && unit.lastOrderWasFramesAgo() >= 12
            && unit.lastActionMoreThanAgo(12);
    }

    @Override
    public Manager handle() {
        AFocusPoint focusPoint = unit.focusPoint();
        if (focusPoint == null) return null;

        if (unit.distToFocusPoint() > 5 && unit.move(focusPoint, Actions.MOVE_UNFREEZE, "FixIdleUnits")) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - FixIdleUnits");
            return usedManager(this);
        }

        return null;
    }
}
