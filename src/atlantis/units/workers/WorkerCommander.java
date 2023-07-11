package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.terran.repair.ARepairCommander;
import atlantis.units.AUnit;
import atlantis.units.buildings.AGasManager;
import atlantis.units.select.Select;
import atlantis.util.CodeProfiler;
import atlantis.util.We;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class WorkerCommander extends Commander {

    /**
     * Executed only once per frame.
     */
    public void handle() {
        CodeProfiler.startMeasuring(this);

        // === Handle assigning workers to gas / bases ============================

        AGasManager.handleGasBuildings();
        AWorkerTransferManager.transferWorkersBetweenBasesIfNeeded();

        // === Act individually with every worker =================================

        for (AUnit worker : Select.ourWorkers().list()) {
            AWorkerManager.update(worker);
        }

        // =========================================================

        if (We.terran()) {
            ARepairCommander.update();
        }

        CodeProfiler.endMeasuring(this);
    }

}
