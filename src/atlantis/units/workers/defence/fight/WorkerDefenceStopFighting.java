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
        if (unit.isAttacking() || unit.action().isAttacking()) {
            return WorkerDoNotFight.doNotFight(unit);
        }

        return false;
    }

    @Override
    protected Manager handle() {
        if ((new GatherResources(unit)).invokedFrom(this)) return usedManager(this);
        if (unit.moveToSafety(Actions.MOVE_SAFETY, "StopFighting")) return usedManager(this);

        return null;
    }
}
