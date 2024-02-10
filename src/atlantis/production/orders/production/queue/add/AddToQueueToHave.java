package atlantis.production.orders.production.queue.add;

import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class AddToQueueToHave {
    public static boolean haveAtLeastOneWithTopPriority(AUnitType type, int howMany) {
//        cancelPreviousNonStartedOrdersOf(type);
//        Count.clearCache();

        if (Count.withPlanned(type) >= howMany) return false;

        for (int i = 1; i <= howMany; i++) {
            AddToQueue.withTopPriority(type);
        }

        return howMany > 0;
    }

    private static boolean cancelPreviousNonStartedOrdersOf(AUnitType type) {
        for (ProductionOrder order : Queue.get().nextOrders().ofType(type).list()) {
            if (order.isInProgress()) return true;
            order.setIgnore(true);

            if (order.construction() != null) {
                order.construction().setBuilder(null);
            }

            order.cancel();
            return true;
        }

        return false;
    }
}
