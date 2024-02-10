package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class UnfreezeGeneric extends Manager {
    public UnfreezeGeneric(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hasCooldown()) return false;
        if (unit.isAccelerating()) return false;
        if (unit.lastPositionChangedLessThanAgo(22)) return false;
        if (unit.lastActionLessThanAgo(16)) return false;
//        if (unit.isAttacking()) return false;
        if (unit.lastStartedAttackLessThanAgo(20)) return false;
        if (unit.lastActionLessThanAgo(20, Actions.MOVE_DANCE_AWAY)) return false;

        if (isDragoonDuringSpartaMission()) return false;

        return true;
    }

    private boolean isDragoonDuringSpartaMission() {
        return unit.isDragoon()
            && unit.isMissionSparta()
            && unit.friendsNear().zealots().inRadius(3, unit).notEmpty();
    }

    @Override
    public Manager handle() {
        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
