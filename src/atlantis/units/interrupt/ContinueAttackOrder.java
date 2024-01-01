package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueAttackOrder extends Manager {
    public ContinueAttackOrder(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastActionLessThanAgo(unit.attackWaitFrames(), Actions.ATTACK_UNIT)
            && unit.hasTarget()
            && unit.target().hasPosition()
            && unit.target().isAlive();
    }

    public Manager handle() {
        return usedManager(this);
    }
}
