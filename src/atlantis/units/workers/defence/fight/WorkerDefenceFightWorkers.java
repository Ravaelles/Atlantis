package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class WorkerDefenceFightWorkers extends Manager {
    public WorkerDefenceFightWorkers(AUnit unit) {
        super(unit);
    }


    @Override
    public boolean applies() {
        return unit.hp() >= 14
            && unit.enemiesNear().workers().notEmpty()
            && unit.enemiesNear().combatUnits().inRadius(7, unit).empty()
            && unit.distToBase() <= 16;
    }

    @Override
    public Manager handle() {
        if (fightEnemyWorkers()) return usedManager(this);

        return null;
    }

    private boolean fightEnemyWorkers() {
        AUnit nearestFucker = defineNearestFucker();
        if (nearestFucker != null) {
            unit.setTooltipTactical("NastyFucker!");
            unit.attackUnit(nearestFucker);
            return true;
        }

        return false;
    }

    private AUnit defineNearestFucker() {
        Selection enemyWorkers = unit.enemiesNear().workers();

        AUnit target = enemyWorkers.inRadius(1, unit).mostWounded();
        if (target != null) return target;

        return enemyWorkers.inRadius(fightIfWorkerAtMostAway(), unit).nearestTo(unit);
    }

    private double fightIfWorkerAtMostAway() {
        return 1.3 + (unit.lastUnderAttackLessThanAgo(30 * 2) ? 1.5 : 0);
    }
}
