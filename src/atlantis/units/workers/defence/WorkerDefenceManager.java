package atlantis.units.workers.defence;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.workers.defence.fight.WorkerDefenceFight;
import atlantis.units.workers.defence.fight.WorkerDefenceHelpCannon;
import atlantis.units.workers.defence.fight.WorkerDefenceStopFighting;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
import atlantis.units.workers.defence.special.BuddyRepair;

public class WorkerDefenceManager extends Manager {
    public WorkerDefenceManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isWorker()) return false;

        return !unit.isRepairing()
            && !unit.isSpecialMission()
            && (unit.isWounded() || unit.enemiesNear().reavers().notEmpty());
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WorkerDefenceHelpCannon.class,
            WorkerDefenceRun.class,
            WorkerDefenceStopFighting.class,
            BuddyRepair.class,
            WorkerDefenceFight.class,
        };
    }

}
