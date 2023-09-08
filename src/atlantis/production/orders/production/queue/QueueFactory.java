package atlantis.production.orders.production.queue;

import atlantis.production.orders.build.ABuildOrder;

public class QueueFactory {
    public static Queue fromBuildOrder(ABuildOrder buildOrder) {
        Queue queue = new Queue();
        queue.orders().addAll(buildOrder.productionOrders());
        return queue;
    }
}
