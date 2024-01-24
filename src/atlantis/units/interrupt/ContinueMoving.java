package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ContinueMoving extends Manager {
    public ContinueMoving(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;

        return (unit.isMoving() && !unit.isBraking() && !unit.isStopped())
            && !unit.isAttackingOrMovingToAttack()
            && unit.lastActionLessThanAgo(3)
            && unit.targetPosition() != null;
//            && unit.distTo(unit.targetPosition()) >= 1;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
