package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.ShouldExpand;
import atlantis.production.dynamic.protoss.ProtossDynamicTechResearch;
import atlantis.production.dynamic.protoss.ProtossDynamicUnitsCommander;
import atlantis.production.dynamic.terran.TerranDynamicTechResearch;
import atlantis.production.dynamic.terran.TerranDynamicUnitsCommander;
import atlantis.production.dynamic.zerg.ZergDynamicTechResearch;
import atlantis.production.dynamic.zerg.ZergDynamicUnitsCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.util.We;

public class DynamicUnitProducerCommander extends Commander {
    @Override
    public boolean applies() {
//        return A.minerals() >= 500;
        return true;
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        Class[] raceSpecific = null;

        System.err.println("@ " + A.now() + " - DynamicUnitProducerCommander " + A.minerals());

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
