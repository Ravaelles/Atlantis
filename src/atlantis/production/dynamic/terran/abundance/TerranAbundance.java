package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.AbundanceCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.util.We;

public class TerranAbundance extends AbundanceCommander {
    @Override
    public boolean applies() {
        return We.terran()
            && A.hasMinerals(800)
            && (A.hasMinerals(1000) || Queue.get().forCurrentSupply().notFinished().size() <= 10);
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
