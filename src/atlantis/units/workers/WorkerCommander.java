package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.units.buildings.GasBuildingsCommander;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class WorkerCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            GasBuildingsCommander.class,
            WorkerTransferCommander.class,
            WorkerHandlerCommander.class,

            CrucialRepairsNearbyCommander.class,
        };
    }
}
