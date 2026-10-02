package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.protoss.ProtossSpecificBuildingsCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsCommander;
import atlantis.production.dynamic.terran.TerranSpecificBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergNewGasBuildingCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.util.We;

public class DynamicBuildingsCommander extends Commander {
    @Override
    public boolean applies() {
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected CommanderFactory[] subcommanders() {
        CommanderFactory[] generic = new CommanderFactory[]{
            ProtossSpecificBuildingsCommander::new,

            TerranSpecificBuildingsCommander::new,

            ZergNewGasBuildingCommander::new,

            ExpansionCommander::new,
        };

        CommanderFactory[] raceSpecific = new CommanderFactory[0];

        if (We.terran()) raceSpecific = new CommanderFactory[]{
            TerranDynamicBuildingsCommander::new,
        };
        else if (We.protoss()) raceSpecific = new CommanderFactory[]{
            ProtossDynamicBuildingsCommander::new,
        };
        else if (We.zerg()) raceSpecific = new CommanderFactory[]{
            ZergDynamicBuildingsCommander::new,
        };

        return mergeCommanders(generic, raceSpecific);
    }

    public static Commander get() {
        if (We.terran()) return (new TerranDynamicBuildingsCommander());
        if (We.protoss()) return (new ProtossDynamicBuildingsCommander());
        if (We.zerg()) return (new ZergDynamicBuildingsCommander());
        return null;
    }
}
