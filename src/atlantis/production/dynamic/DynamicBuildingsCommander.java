package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.reinforce.ReinforceBasesCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.protoss.ProtossNewGasBuildingCommander;
import atlantis.production.dynamic.terran.TerranDynamicBuildingsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicBuildingsCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.util.We;

public class DynamicBuildingsCommander extends Commander {
    @Override
    public boolean applies() {
        return A.hasMinerals(450) || Queue.get().nonCompletedNext30().buildings().size() <= 1;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] generic = new Class[]{
            ReinforceBasesCommander.class,
            NewGasBuildingCommander.class,
            ProtossNewGasBuildingCommander.class,
            ExpansionCommander.class,
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

    public static Commander get() {
        if (We.terran()) return (new TerranDynamicBuildingsCommander());
        if (We.protoss()) return (new ProtossDynamicBuildingsCommander());
        if (We.zerg()) return (new ZergDynamicBuildingsCommander());
        return null;
    }
}
