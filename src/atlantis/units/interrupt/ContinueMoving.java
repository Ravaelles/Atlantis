package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ContinueMoving extends Manager {
    public ContinueMoving(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (!unit.isDragoon()) return false;

        return unit.isMoving()
            && unit.lastActionLessThanAgo(2)
            && unit.targetPosition() != null;

//        if (!unit.isDragoon()) return false;
//
//        return (unit.isMoving() && !unit.isBraking() && !unit.isStopped())
//            && !unit.isAttackingOrMovingToAttack()
//            && unit.lastActionLessThanAgo(2)
//            && unit.targetPosition() != null;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
