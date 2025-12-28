package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.terran.bunker.HaveBunkerAtMainChoke;
import atlantis.production.dynamic.terran.bunker.HaveBunkerAtNaturalChoke;
import atlantis.production.dynamic.terran.bunker.TerranReinforceBasesWithBunkers;

public class TerranSpecificBuildingsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            TerranNewGasBuildingCommander.class,

            HaveBunkerAtMainChoke.class,
            HaveBunkerAtNaturalChoke.class,
            TerranReinforceBasesWithBunkers.class,
        };
    }
}
