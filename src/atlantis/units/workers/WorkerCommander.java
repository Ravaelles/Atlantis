package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.units.buildings.GasBuildingsCommander;

/**
 * Manages all worker (SCV, Probe, Drone) actions.
 */
public class WorkerCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            GasBuildingsCommander::new,
            WorkerTransferCommander::new,
            WorkerHandlerCommander::new,

            CrucialRepairsNearbyCommander::new,
        };
    }
}
