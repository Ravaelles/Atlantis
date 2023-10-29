package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.AbundanceCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.util.We;

public class TerranAbundance extends AbundanceCommander {
    @Override
    public boolean applies() {
        return We.terran()
            && ReservedResources.minerals() <= 900
            && Queue.get().nonCompleted().size() <= 10;
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            TerranAbundanceEarlyToMidGame.class,
            TerranAbundanceLateGame.class,
            TerranAbundanceTech.class,
        };
    }
}
