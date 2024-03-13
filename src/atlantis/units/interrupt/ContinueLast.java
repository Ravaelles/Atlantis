package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.ShouldRetreat;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ContinueLast extends Manager {
    public ContinueLast(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.isStopped()) return false;
        if (unit.isRunning()) return false;
        if (A.everyNthGameFrame(5)) return false;
        if (ShouldRetreat.shouldRetreat(unit)) return false;
        if (unit.isRanged() && unit.meleeEnemiesNearCount(1.7) >= 1) return false;
//        if (unit.lastActionMoreThanAgo(3)) return false;

        return unit.isAttacking()
            || (unit.isMoving() && unit.distToTargetMoreThan(2));
    }

    public Manager handle() {
        return usedManager(this);
    }
}
