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

    @Override
    public boolean applies() {
        if (!unit.isWorker()) return false;

        // A BUILDER is still a worker, and it must still defend itself. This
        // used to be a flat `if (unit.isBuilder()) return false;`, so the whole
        // defence chain - run, fight, avoid - was skipped for any worker with a
        // construction assigned. For Protoss that is most of the early game:
        // `BuilderManager.isBuilder()` returns true while the worker is merely
        // WALKING to a build site (`We.protoss() && !worker.isStopped()`), so a
        // Probe on its way to a Pylon stood and mined with Zealots on top of it.
        //
        // The owner's evidence (2026-10-07) is exactly this: five dead Probes,
        // each with `BuilderManager` as its first log entry and
        // `GatherResources` after it - and no WorkerDefenceManager anywhere,
        // because this line had removed it from the chain.
        //
        // A worker that is actually CONSTRUCTING cannot walk away (StarCraft
        // holds it in place) and must not be told to flee: BuilderManager owns
        // it and keeps its own under-attack handling. Everything else - walking
        // to a site, waiting for resources, gathering while assigned - is free
        // to run or fight.
        if (unit.isConstructing()) return false;

        if (A.isUms() && !Have.main()) return false;

        return (!We.terran() || !unit.isRepairing())
            && !unit.isSpecialMission();
//            && (unit.isWounded() || unit.enemiesNear().reavers().notEmpty());
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
