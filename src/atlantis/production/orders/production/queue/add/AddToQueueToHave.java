
package atlantis.production.orders.production.queue.add;

import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class AddToQueueToHave {
    public static boolean haveAtLeastOneWithTopPriority(AUnitType type, int howMany) {
        return haveAtLeastOneWithTopPriority(type, howMany, A.supplyUsed() / 2);
    }

    public static boolean haveAtLeastOneWithTopPriority(AUnitType type, int howMany, int atSupply) {
        if (Count.withPlanned(type) >= howMany) return false;

        for (int i = 1; i <= howMany; i++) {
            ProductionOrder order = AddToQueue.withTopPriority(type);
            if (order != null) order.setMinSupply(atSupply);
        }

        return howMany > 0;
    }
}
