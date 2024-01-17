
package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.dynamic.expansion.ShouldExpand;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.protoss.units.*;
import atlantis.util.We;

public class ProtossDynamicUnitProductionCommander extends Commander {
    @Override
    public boolean applies() {
        return We.protoss() && !ProtossShouldExpand.needToSaveMineralsForExpansion();
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
