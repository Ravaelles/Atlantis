package atlantis.workers;

import atlantis.buildings.managers.AGasManager;
import atlantis.repair.ARepairCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class AWorkerCommander {

    /**
     * Executed only once per frame.
     */
    public static void update() {

        // === Handle assigning workers to gas / bases ============================
        
        AGasManager.handleGasBuildings();
        AWorkerTransferManager.transferWorkersBetweenBasesIfNeeded();

        // === Act individually with every worker =================================

        for (AUnit worker : Select.ourWorkers().listUnits()) {
            AWorkerManager.update(worker);
        }

        // =========================================================

        if (We.terran()) {
            ARepairCommander.update();
        }
    }

}
