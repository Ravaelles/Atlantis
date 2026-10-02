package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.protoss.reinforce.ProtossExtraEarlyCannonCommander;

public class ProtossSpecificBuildingsCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            ProtossNewGasBuildingCommander::new,

            ProtossSecureBasesCommander::new,
            ProtossExtraEarlyCannonCommander::new,
        };
    }
}
