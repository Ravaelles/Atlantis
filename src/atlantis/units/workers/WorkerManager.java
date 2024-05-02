package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.production.constructing.builders.BuilderManager;
import atlantis.terran.repair.DynamicRepairsNearby;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.workers.defence.WorkerDefenceManager;

public class WorkerManager extends Manager {
    public WorkerManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isWorker()) return false;
        if (unit.isSpecialMission()) return false;

        return !unit.isScout() && !RepairAssignments.isRepairerOfAnyKind(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidSpellsAndMines.class,

            AvoidCriticalUnits.class,
            WorkerDefenceManager.class,
            WorkerAvoidManager.class,
            AvoidEnemies.class,

            BuilderManager.class,
            DynamicRepairsNearby.class,

            GatherResources.class,

            IdleWorker.class,
        };
    }
}
