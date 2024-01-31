
package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.protoss.units.*;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossDynamicUnitProductionCommander extends Commander {
    @Override
    public boolean applies() {
        return We.protoss()
            && freeToSpendResources();
//            && !ProtossShouldExpand.needToSaveMineralsForExpansion();
    }

    private static boolean freeToSpendResources() {
        if (A.hasMinerals(550)) return true;

        if (keepSomeResourcesInLaterGamePhases()) return false;

        int reservedMinerals = ReservedResources.minerals();
        int reservedGas = ReservedResources.gas();
        int mineralsMargin = A.supplyUsed() < 40 ? 150 : 200;
        int gasMargin = A.supplyUsed() < 40 ? 100 : 150;

        if (reservedMinerals > 0 && !A.hasMinerals(mineralsMargin + reservedMinerals)) return false;
        if (reservedGas > 0 && !A.hasGas(gasMargin + reservedMinerals)) return false;

//        System.err.println(A.now() + " 2dyna produce: " + A.minerals() + "/" + reservedMinerals);

        return true;
    }

    private static boolean keepSomeResourcesInLaterGamePhases() {
        if (
            A.seconds() >= 450
                && !A.hasMinerals(500)
                && Count.basesWithUnfinished() <= 2
        ) return true;

        return !A.hasMinerals(320) && A.seconds() > 320;
    }

    protected void handle() {
        if (!AGame.everyNthGameFrame(7)) return;

        ProduceScarabs.scarabs();
        ProduceObservers.observers();
        ProduceArbiters.arbiters();
        ProduceCorsairs.corsairs();
        ProduceShuttles.shuttles();
        ProduceReavers.reavers();

        ProduceDragoon.dragoon();
        ProduceZealot.zealot();
    }
}
