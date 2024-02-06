package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class IdleWorker extends Manager {
    public IdleWorker(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
            && !unit.isGatheringMinerals()
            && !unit.isGatheringGas()
            && unit.lastActionMoreThanAgo(60)
            && (!unit.recentlyMoved() || unit.isIdle())
            && !unit.isConstructing()
            && !unit.isBuilder()
            && !unit.isRepairing()
            && !unit.isSpecialAction();
    }

    public Manager handle() {
        if (unit.lastActionMoreThanAgo(51)) {
            (new GatherResources(unit)).forceHandle();
            return usedManager(this);
        }

        return null;
    }
}
