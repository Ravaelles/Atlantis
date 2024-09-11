package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicTechResearch;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsCommander;
import atlantis.production.dynamic.terran.TerranDynamicTechResearch;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTechResearch;
import atlantis.production.dynamic.zerg.ZergDynamicUnitsCommander;
import atlantis.util.HasReason;
import atlantis.util.We;

public class DynamicUnitAndTechProducerCommander extends Commander implements HasReason {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] raceSpecific = null;

        if (We.terran()) {
            raceSpecific = new Class[]{
                TerranDynamicTechResearch.class,
                TerranDynamicUnitsCommander.class,
            };
        }
        if (We.protoss()) {
            raceSpecific = new Class[]{
                ProtossDynamicTechResearch.class,
                ProtossDynamicUnitProductionCommander.class,
            };
        }
        if (We.zerg()) {
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

    public static Commander get() {
        if (We.terran()) return (new TerranDynamicUnitsCommander());
        if (We.protoss()) return (new ProtossDynamicUnitProductionCommander());
        if (We.zerg()) return (new ZergDynamicUnitsCommander());
        return null;
    }

    @Override
    public String reason() {
        return "ToBeImplemented";
    }
}
