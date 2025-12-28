package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.protoss.reinforce.ProtossExtraEarlyCannonCommander;

public class ProtossSpecificBuildingsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProtossNewGasBuildingCommander.class,

            ProtossSecureBasesCommander.class,
            ProtossExtraEarlyCannonCommander.class,
        };
    }
}
