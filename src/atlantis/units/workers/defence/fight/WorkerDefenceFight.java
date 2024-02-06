package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class WorkerDefenceFight extends Manager {
    public WorkerDefenceFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isBuilder() && !unit.isRepairerOfAnyKind() && !unit.isSpecialAction();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WorkerDefenceFightCombatUnits.class,
            WorkerDefenceFightWorkers.class,
        };
    }
}
