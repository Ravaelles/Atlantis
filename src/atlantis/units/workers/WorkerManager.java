package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.production.constructing.builders.BuilderManager;
import atlantis.terran.repair.DynamicRepairsNearby;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class WorkerManager extends Manager {
    public WorkerManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isScout() && !RepairAssignments.isRepairerOfAnyKind(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidCriticalUnits.class,
            WorkerDefenceManager.class,
            AvoidEnemies.class,

            BuilderManager.class,
            DynamicRepairsNearby.class,

            GatherResources.class,
        };
    }
}
