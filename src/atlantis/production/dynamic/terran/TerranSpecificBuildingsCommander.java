package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.terran.bunker.HaveBunkerAtMainChoke;
import atlantis.production.dynamic.terran.bunker.HaveBunkerAtNaturalChoke;
import atlantis.production.dynamic.terran.bunker.TerranReinforceBasesWithBunkers;

public class TerranSpecificBuildingsCommander extends Commander {
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
