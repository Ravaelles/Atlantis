package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.protoss.reinforce.ProtossExtraEarlyCannonCommander;
import atlantis.util.We;

public class ProtossSpecificBuildingsCommander extends Commander {
    @Override
    public boolean applies() {
        // Mirror of TerranSpecificBuildingsCommander: DynamicBuildingsCommander
        // lists this commander as generic, so Protoss-only buildings must not
        // be evaluated in Terran/Zerg games.
        return We.protoss();
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            ProtossNewGasBuildingCommander::new,

            ProtossSecureBasesCommander::new,
            ProtossExtraEarlyCannonCommander::new,
        };
    }
}
