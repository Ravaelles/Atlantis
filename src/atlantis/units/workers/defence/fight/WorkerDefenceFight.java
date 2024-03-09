package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class WorkerDefenceFight extends Manager {
    public WorkerDefenceFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return (We.protoss() || !unit.isBuilder())
            && !unit.isRepairerOfAnyKind()
            && !unit.isSpecialAction();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WorkerFightEnemyProxyBuilding.class,
            WorkerDefenceFightCombatUnits.class,
            WorkerDefenceFightWorkers.class,
        };
    }
}
