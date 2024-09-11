package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.AbundanceCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnitType;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Vulture;
import static atlantis.units.AUnitType.Terran_Wraith;

public class TerranAbundance extends AbundanceCommander {
    @Override
    public boolean applies() {
        return We.terran()
            && A.hasMinerals(800)
            && (A.hasMinerals(1000) || Queue.get().forCurrentSupply().nonCompleted().size() <= 10);
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
