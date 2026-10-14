package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Count;

public class ProtossExpandNow {
    public static void requestNewBase() {
        // NEXT #58: maxAtATime counts nextOrders(), which skips IN_PROGRESS -
        // a Nexus being worked on is invisible to the cap, so the expansion
        // producer added a second Nexus 9 s after the first (measured in game
        // 20261014_121406: ADDED 6:36 + 6:45, first IN_PROGRESS throughout).
        // Bases take minutes; an unfinished one always counts. (Units keep the
        // old counting: a training unit plus a queued next one is normal.)
        if (
            Queue.get().notFinished().ofType(AtlantisRaceConfig.BASE).size()
                >= ProtossExpansionCommander.maxBasesAtATime()
        ) return;

        ProductionOrder productionOrder = AddToQueue.maxAtATime(
            AtlantisRaceConfig.BASE, ProtossExpansionCommander.maxBasesAtATime(),
            ProductionOrderPriority.HIGH);

        if (productionOrder != null && Count.bases() <= 1) {
            productionOrder.setModifier("NATURAL");
        }
    }
}
