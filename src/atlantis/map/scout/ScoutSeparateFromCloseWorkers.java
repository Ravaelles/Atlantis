package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ScoutSeparateFromCloseWorkers extends Manager {
    private AUnit worker;

    public ScoutSeparateFromCloseWorkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
            && unit.isScout()
            && unit.hp() <= 38
            && (worker = worker()) != null
            && unit.enemiesThatCanAttackMe(3).nonWorkers().empty();
    }

    @Override
    public Manager handle() {
        if (unit.runOrMoveAway(worker, 6)) {
            return usedManager(this);
        }

        if (unit.moveToSafety(Actions.MOVE_AVOID)) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit worker() {
        Selection workers = unit.enemiesNear().havingAntiGroundWeapon().inRadius(6, unit);
        if (workers.count() < (unit.hp() >= 33 ? 2 : 1)) return null;

        return workers.nearestTo(unit);
    }
}
