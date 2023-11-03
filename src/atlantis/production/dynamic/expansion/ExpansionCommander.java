package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ExpansionCommander extends Commander {
    public ExpansionCommander() {
    }

    @Override
    protected void handle() {
//        System.err.println("ExpansionCommander.handle() @ " + A.now());
        if (ShouldExpand.shouldBuildNewBase()) prepareForNewBase();
    }

    protected void prepareForNewBase() {
        if (newExpansionIsSecured()) {
            requestNewBase();
        }
    }

    protected boolean newExpansionIsSecured() {
        if (!We.terran()) return true;

        return NewBaseIsSecured.newBaseIsSecured();
    }

    private static void requestNewBase() {
        // ZERG case
        if (We.zerg()) {
            AddToQueue.withStandardPriority(AtlantisRaceConfig.BASE, Select.naturalOrMain());
        }

        // TERRAN + PROTOSS
        else {
            ProductionOrder productionOrder = AddToQueue.withHighPriority(AtlantisRaceConfig.BASE);
            if (productionOrder != null && Count.bases() <= 1) {
                productionOrder.setModifier("NATURAL");
            }
        }
    }
}
