package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.terran.abundance.TerranAbundance;
import atlantis.production.dynamic.terran.units.*;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;

public class TerranDynamicUnitsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran()
            && ReservedResources.minerals() <= 900
            && Queue.get().nonCompleted().size() <= 10;
    }

    @Override
    protected void handle() {
        ProduceScienceVessels.scienceVessels();

        int dynamicOrders = CountInQueue.countDynamicOrders();

        if (dynamicOrders <= 3 || A.hasMinerals(700)) {
            ProduceWraiths.wraiths();

            TerranDynamicFactoryUnits.handleFactoryProduction();
        }

        if (dynamicOrders <= 8 || A.hasMinerals(700)) {
            if (Count.infantry() <= 14 || (Enemy.protoss() && Count.tanks() >= 4) || A.hasMinerals(500)) {
                ProduceGhosts.ghosts();
                ProduceMedicsAndFirebats.medics();
                ProduceMarines.marines();
            }
        }

        (new TerranAbundance()).invoke();
    }

    // =========================================================

}
