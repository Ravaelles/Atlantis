package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ContinueMoving extends Manager {
    public ContinueMoving(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (!unit.isDragoon()) return false;
        if (!unit.isMoving()) return false;
        if (unit.isStopped()) return false;
        if (A.everyNthGameFrame(77)) return false;
        if (unit.lastActionMoreThanAgo(37)) return false;
        if (unit.isLeader()) return false;
        if (unit.lastPositionChangedMoreThanAgo(10)) return false;
        if (A.now() % 16 == 0 && unit.lastActionMoreThanAgo(14)) return false;

        if (dontApplyDuringMissionSparta()) return false;
        if (unit.isScout() || unit.isSquadScout()) return false;

        return unit.targetPosition() != null
            && unit.lastActionLessThanAgo(unit.enemiesNear().empty() ? 14 : 5)
            && unit.distTo(unit.targetPosition()) >= 0.5;

//        if (!unit.isDragoon()) return false;
//
//        return (unit.isMoving() && !unit.isBraking() && !unit.isStopped())
//            && !unit.isAttackingOrMovingToAttack()
//            && unit.lastActionLessThanAgo(2)
//            && unit.targetPosition() != null;
    }

    private boolean dontApplyDuringMissionSparta() {
        return unit.isMissionSparta()
            && unit.isRanged();
//            && !unit.isRunning()
//            && unit.meleeEnemiesNearCount(1.3) == 0;
    }

    public Manager handle() {
//        unit.paintCircleFilled(26, Color.Purple);
        return usedManager(this);
    }
}
