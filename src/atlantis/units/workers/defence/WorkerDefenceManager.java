package atlantis.units.workers.defence;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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

    /** Construction owns assigned workers; the defence chain never takes them over. */
    @Override
    public boolean applies() {
        if (!unit.isWorker()) return false;
        if (unit.isBuilder() || unit.isConstructing()) return false;
        if (A.isUms() && !Have.main()) return false;
        return (!We.terran() || !unit.isRepairing())
            && !unit.isSpecialMission();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            WorkerDefenceHelpCannon::new,
            WorkerHelpCombatUnitsFight::new,
            WorkerDefenceRun::new,
            WorkerDefenceStopFighting::new,
            WorkerDefenceFight::new,
            BuddyRepair::new,
            WorkerAvoidManager::new,
        };
    }

}
