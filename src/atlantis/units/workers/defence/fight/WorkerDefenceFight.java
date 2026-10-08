package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.util.We;

public class WorkerDefenceFight extends Manager {
    public WorkerDefenceFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isBuilder()
            && unit.enemiesNear().notEmpty()
            && (!We.terran() || !unit.isRepairerOfAnyKind())
            && !unit.isSpecialAction();
//            && !WorkerDoNotFight.doNotFight(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            WorkerFightEnemyProxyBuilding::new,
            WorkerDefenceStopFighting::new,
            WorkerDefenceFightCombatUnits::new,
            WorkerDefenceFightWorkers::new,
        };
    }
}
