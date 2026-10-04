package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.terran.bunker.HaveBunkerAtMainChoke;
import atlantis.production.dynamic.terran.bunker.HaveBunkerAtNaturalChoke;
import atlantis.production.dynamic.terran.bunker.TerranReinforceBasesWithBunkers;
import atlantis.util.We;

public class TerranSpecificBuildingsCommander extends Commander {
    @Override
    public boolean applies() {
        // Measured 2026-10-04 (15:26:55 Steamhammer.log): without this gate the
        // whole Terran subtree evaluated in a Protoss game, and
        // HaveBunkerAtMainChoke dereferenced the missing main choke on every
        // frame - the frame died in OnEveryFrame, production stalled at 2
        // zealots while minerals piled up. DynamicBuildingsCommander lists this
        // commander as generic, so the race gate lives here, next to the
        // self-guarded gas commanders below it.
        return We.terran();
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            TerranNewGasBuildingCommander::new,

            HaveBunkerAtMainChoke::new,
            HaveBunkerAtNaturalChoke::new,
            TerranReinforceBasesWithBunkers::new,
        };
    }
}
