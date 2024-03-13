package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;

public class ProtossExpandNow {
    public static void requestNewBase() {
        ProductionOrder productionOrder = AddToQueue.maxAtATime(AtlantisRaceConfig.BASE, ProtossExpansionCommander.maxBasesAtATime());

        if (productionOrder != null && Count.bases() <= 1) {
            productionOrder.setModifier("NATURAL");
        }
    }
}
