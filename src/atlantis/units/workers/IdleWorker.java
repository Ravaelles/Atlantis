package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.workers.gather.GatherResources;
import atlantis.util.log.ErrorLog;

public class IdleWorker extends Manager {
    public IdleWorker(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
//            && !unit.isGatheringMinerals()
//            && !unit.isGatheringGas()
//            && unit.lastActionMoreThanAgo(60)
            && (unit.isIdle() || unit.isStopped())
            && !unit.isConstructing()
            && !unit.isBuilder()
            && !unit.isRepairing()
            && !unit.isSpecialAction();
    }

    public Manager handle() {
//        if (unit.lastActionMoreThanAgo(51)) {
        if ((new GatherResources(unit)).forceHandle() != null) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - IdleWorker -> GatherResources");
            return usedManager(this);
        }

//        ErrorLog.debug(
//            unit
//            + " not managed to GatherResources! Falling back to nearest mineral. Last moved: "
//            + unit.lastPositionChangedAgo()
//        );

        AUnit mineral = Select.all().minerals().nearestTo(unit);
        if (mineral != null && unit.gather(mineral)) {
            return usedManager(this);
        }

        return null;
    }
}
