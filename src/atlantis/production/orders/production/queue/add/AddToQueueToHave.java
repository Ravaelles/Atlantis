package atlantis.production.orders.production.queue.add;

import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class AddToQueueToHave {
    public static boolean haveAtLeastOneWithTopPriority(AUnitType type, int howMany) {
        if (Count.withPlanned(type) >= howMany) return false;

        for (int i = 1; i <= howMany; i++) {
            AddToQueue.withTopPriority(type);
        }

        return howMany > 0;
    }
}
