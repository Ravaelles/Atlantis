package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.workers.GatherResources;

public class WorkerDefenceStopFighting extends Manager {
    public WorkerDefenceStopFighting(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAttacking() || unit.action().isAttacking() || unit.lastAttackFrameLessThanAgo(60)) {
            return WorkerDoNotFight.doNotFight(unit);
        }

        return false;
    }

    @Override
    protected Manager handle() {
//        if (unit.hp() >= 19) {
//            if ((new GatherResources(unit)).forceHandle() != null) return usedManager(this);
//        }

        AUnit enemy = unit.nearestEnemy();
        if (enemy != null) {
            if (unit.runOrMoveAway(enemy, 6)) return usedManager(this);
        }

        return null;
    }
}
