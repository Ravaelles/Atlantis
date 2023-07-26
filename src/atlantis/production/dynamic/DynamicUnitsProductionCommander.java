package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicTech;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitsManager;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsCommander;
import atlantis.production.dynamic.terran.TerranDynamicTech;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTech;
import atlantis.production.dynamic.zerg.ZergDynamicUnitsCommander;
import atlantis.util.We;


public class DynamicUnitsProductionCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] raceSpecific;
//        Class<? extends Commander>[] raceSpecific;

        if (We.terran()) {
            raceSpecific = new Class[]{
                TerranDynamicTech.class,
                TerranDynamicUnitsCommander.class,
                TerranDynamicBuildingsCommander.class,
            };
        }
        else if (We.protoss()) {
            raceSpecific = new Class[]{
                ProtossDynamicTech.class,
                ProtossDynamicUnitsManager.class,
                ProtossDynamicBuildingsCommander.class,
            };
        }
        else {
            raceSpecific = new Class[]{
                ZergDynamicTech.class,
                ZergDynamicUnitsCommander.class,
                ZergDynamicBuildingsCommander.class,
            };
        }

        Class[] generic = new Class[]{
            DynamicTrainWorkersCommander.class
        };

        return mergeCommanders(raceSpecific, generic);
    }
}
