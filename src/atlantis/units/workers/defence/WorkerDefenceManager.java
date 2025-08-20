package atlantis.units.workers.defence;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.workers.defence.fight.WorkerDefenceFight;
import atlantis.units.workers.defence.fight.WorkerDefenceHelpCannon;
import atlantis.units.workers.defence.fight.WorkerDefenceStopFighting;
import atlantis.units.workers.defence.fight.WorkerHelpCombatUnitsFight;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
import atlantis.units.workers.defence.special.BuddyRepair;
import atlantis.util.We;

public class WorkerDefenceManager extends Manager {
    public WorkerDefenceManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isWorker()) return false;
        if (unit.isBuilder()) return false;
        if (A.isUms() && !Have.main()) return false;

        return (!We.terran() || !unit.isRepairing())
            && !unit.isSpecialMission();
//            && (unit.isWounded() || unit.enemiesNear().reavers().notEmpty());
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WorkerDefenceHelpCannon.class,
            WorkerHelpCombatUnitsFight.class,
            WorkerDefenceRun.class,
            WorkerDefenceStopFighting.class,
            WorkerDefenceFight.class,
            BuddyRepair.class,
            WorkerAvoidManager.class,
        };
    }

}
