package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.protoss.ProtossDynamicTechResearch;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander;
import atlantis.production.dynamic.terran.TerranDynamicTechResearch;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTechResearch;
import atlantis.production.dynamic.zerg.ZergDynamicUnitsCommander;
import atlantis.util.HasReason;
import atlantis.util.We;

public class DynamicUnitAndTechProducerCommander extends Commander implements HasReason {
    @Override
    protected CommanderFactory[] subcommanders() {
        CommanderFactory[] raceSpecific = null;

        if (We.protoss()) {
            raceSpecific = new CommanderFactory[]{
                ProtossDynamicTechResearch::new,
                ProtossDynamicUnitProductionCommander::new,
            };
        }
        if (We.terran()) {
            raceSpecific = new CommanderFactory[]{
                TerranDynamicTechResearch::new,
                TerranDynamicUnitsCommander::new,
            };
        }
        if (We.zerg()) {
            raceSpecific = new CommanderFactory[]{
                ZergDynamicTechResearch::new,
                ZergDynamicUnitsCommander::new,
            };
        }

        CommanderFactory[] generic = new CommanderFactory[]{
//            AutoProduceWorkersCommander.class
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
