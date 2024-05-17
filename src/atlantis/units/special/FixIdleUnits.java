package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FixIdleUnits extends Manager {
    public FixIdleUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        return unit.isCombatUnit()
            && (unit.isStopped() || unit.isIdle())
//            && A.fr % 4 == 0
            && unit.noCooldown()
            && unit.lastOrderWasFramesAgo() >= 12
            && unit.lastActionMoreThanAgo(12)
            && unit.enemiesNear().ranged().countInRadius(6, unit) == 0
            && (!unit.isRanged() || unit.enemiesNear().inRadius(4, unit).empty());
    }

    @Override
    public Manager handle() {
        if (attackEnemies()) return usedManager(this);
        if (movedToFocusPoint()) return usedManager(this);

        return null;
    }

    private boolean attackEnemies() {
        if (unit.isDragoon() && unit.enemiesNear().notEmpty()) {
            if ((new AttackNearbyEnemies(unit)).invokedFrom(this)) return true;
        }

        return false;
    }

    private boolean movedToFocusPoint() {
        AFocusPoint focusPoint = unit.focusPoint();
        if (focusPoint == null) return false;

        if (
            !unit.isMoving()
                && unit.distToFocusPoint() > 5
                && unit.move(focusPoint, Actions.MOVE_UNFREEZE, "FixIdleUnits")
        ) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - FixIdleUnits");
            return true;
        }

        return false;
    }
}
