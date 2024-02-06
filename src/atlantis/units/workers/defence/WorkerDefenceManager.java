package atlantis.units.workers.defence;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.workers.defence.fight.WorkerDefenceFight;
import atlantis.units.workers.defence.run.WorkerDefenceRun;

public class WorkerDefenceManager extends Manager {
    public WorkerDefenceManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isWorker()) return false;
        if (unit.enemiesNear().isEmpty()) return false;

        return !unit.isRepairing()
            && !unit.isSpecialMission()
            && (unit.isWounded() || unit.enemiesNear().reavers().notEmpty());
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WorkerDefenceRun.class,
            BuddyRepair.class,
            WorkerDefenceFight.class,
        };
    }

}
