package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicBuildingsCommander;
import atlantis.util.We;

public class DynamicBuildingsCommander extends Commander {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] generic = new Class[]{
            ExpansionCommander.class,
            NewGasBuildingCommander.class,
        };

        Class[] raceSpecific = new Class[0];

        if (We.terran()) raceSpecific = new Class[]{
            TerranDynamicBuildingsCommander.class,
        };
        else if (We.protoss()) raceSpecific = new Class[]{
            ProtossDynamicBuildingsCommander.class,
        };
        else if (We.zerg()) raceSpecific = new Class[]{
            ZergDynamicBuildingsCommander.class,
        };

        return mergeCommanders(generic, raceSpecific);
    }
}
