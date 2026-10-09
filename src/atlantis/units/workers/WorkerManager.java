package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.avoid.buildings.protoss.ProtossCombatBuildingClose;
import atlantis.combat.micro.avoid.special.protoss.ProtossAvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.production.constructions.builders.BuilderManager;
import atlantis.terran.repair.DynamicRepairsNearby;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.workers.defence.WorkerAvoidManager;
import atlantis.units.workers.defence.WorkerDefenceManager;
import atlantis.units.workers.gather.GatherResources;

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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            // An assigned builder's construction travel/avoid/cancel policies own
            // that worker before the general worker-defense chain can fight with it.
            BuilderManager::new,

            AvoidSpellsAndMines::new,
            ProtossAvoidCriticalUnits::new,
            ProtossCombatBuildingClose::new,

            WorkerDefenceManager::new,
            WorkerAvoidManager::new,
            ProtossAvoidEnemies::new,


            DynamicRepairsNearby::new,

            GatherResources::new,

            IdleWorker::new,
        };
    }
}
