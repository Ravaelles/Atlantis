package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.protoss.ProtossDynamicTechResearch;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitsCommander;
import atlantis.production.dynamic.terran.TerranDynamicTechResearch;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTechResearch;
import atlantis.production.dynamic.zerg.ZergDynamicUnitsCommander;
import atlantis.util.We;

public class DynamicUnitProducerCommander extends Commander {
    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] raceSpecific = null;

        if (We.terran()) {
            raceSpecific = new Class[]{
                TerranDynamicTechResearch.class,
                TerranDynamicUnitsCommander.class,
            };
        }
        else if (We.protoss()) {
            raceSpecific = new Class[]{
                ProtossDynamicTechResearch.class,
                ProtossDynamicUnitsCommander.class,
            };
        }
        else if (We.zerg()) {
            raceSpecific = new Class[]{
                ZergDynamicTechResearch.class,
                ZergDynamicUnitsCommander.class,
            };
        }

        Class[] generic = new Class[]{
            AutoProduceWorkersCommander.class
        };

        return mergeCommanders(raceSpecific, generic);
    }
}
