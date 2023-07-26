package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.protoss.*;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsManager;
import atlantis.production.dynamic.terran.TerranDynamicTech;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTech;
import atlantis.util.We;


public class DynamicUnitsProductionCommander extends Commander {

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] raceSpecific;

        if (We.terran()) {
            raceSpecific = new Class[] {
                TerranDynamicTech.class,
                TerranDynamicUnitsCommander.class,
                TerranDynamicBuildingsManager.class,
            };
        }
        else if (We.protoss()) {
            raceSpecific = new Class[] {
                ProtossDynamicTech.class,
                ProtossDynamicUnitsManager.class,
                ProtossDynamicBuildingsCommander.class,
            };
        }
        else {
            raceSpecific = new Class[] {
                ZergDynamicTech.class,
                ZergDynamicUnitsManager.class,
                ZergDynamicBuildingsManager.class,
            };
        }

        Class[] generic = new Class[] {
            DynamicTrainWorkersCommander.class
        };

        return mergeCommanders(raceSpecific, generic);
    }
}
