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

        if (dontApplyDuringMissionDefendOrSparta()) return false;

        return unit.isMoving()
            && unit.targetPosition() != null
            && unit.lastActionLessThanAgo(unit.enemiesNear().empty() ? 14 : 5)
            && unit.distTo(unit.targetPosition()) >= 0.5;

//        if (!unit.isDragoon()) return false;
//
//        return (unit.isMoving() && !unit.isBraking() && !unit.isStopped())
//            && !unit.isAttackingOrMovingToAttack()
//            && unit.lastActionLessThanAgo(2)
//            && unit.targetPosition() != null;
    }

    private boolean dontApplyDuringMissionDefendOrSparta() {
        return unit.isMissionDefendOrSparta()
            && unit.isRanged()
            && !unit.isRunning()
            && unit.meleeEnemiesNearCount(1.3) == 0;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
