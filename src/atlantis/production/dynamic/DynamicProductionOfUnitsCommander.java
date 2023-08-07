package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicTechResearch;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitsCommander;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsCommander;
import atlantis.production.dynamic.terran.TerranDynamicTechResearch;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTechResearch;
import atlantis.production.dynamic.zerg.ZergDynamicUnitsCommander;
import atlantis.util.We;

public class DynamicProductionOfUnitsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] raceSpecific = null;

        if (We.terran()) {
            raceSpecific = new Class[]{
                TerranDynamicTechResearch.class,
                TerranDynamicUnitsCommander.class,
                TerranDynamicBuildingsCommander.class,
            };
        }
        else if (We.protoss()) {
            raceSpecific = new Class[]{
                ProtossDynamicTechResearch.class,
                ProtossDynamicUnitsCommander.class,
                ProtossDynamicBuildingsCommander.class,
            };
        }
        else {
            raceSpecific = new Class[]{
                ZergDynamicTechResearch.class,
                ZergDynamicUnitsCommander.class,
                ZergDynamicBuildingsCommander.class,
            };
        }

        Class[] generic = new Class[]{
            AutoTrainWorkersCommander.class
        };

        return mergeCommanders(raceSpecific, generic);
    }
}
