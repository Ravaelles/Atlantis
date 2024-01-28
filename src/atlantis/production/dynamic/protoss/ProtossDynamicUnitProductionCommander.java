
package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.protoss.units.*;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.util.We;

public class ProtossDynamicUnitProductionCommander extends Commander {
    @Override
    public boolean applies() {
        return We.protoss()
            && !needToSaveResources();
//            && !ProtossShouldExpand.needToSaveMineralsForExpansion();
    }

    private static boolean needToSaveResources() {
        int reservedMinerals = ReservedResources.minerals();
        int reservedGas = ReservedResources.gas();

        if (reservedMinerals <= 0 && reservedGas <= 0) return false;

        int mineralsMargin = A.supplyUsed() < 40 ? 150 : 200;
        int gasMargin = A.supplyUsed() < 40 ? 100 : 150;

        if (reservedMinerals > 0 && A.minerals() + mineralsMargin < reservedMinerals) return true;
        if (reservedGas > 0 && A.gas() + gasMargin < reservedGas) return true;

        return false;
    }

    protected void handle() {
        if (AGame.notNthGameFrame(7)) return;

        ProduceScarabs.scarabs();
        ProduceObservers.observers();
        ProduceArbiters.arbiters();
        ProduceCorsairs.corsairs();
        ProduceShuttles.shuttles();
        ProduceReavers.reavers();

        ProduceDragoon.dragoon();
        ProduceZealot.produce();
    }
}
