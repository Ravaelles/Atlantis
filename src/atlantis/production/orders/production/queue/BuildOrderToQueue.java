package atlantis.production.orders.production.queue;

import atlantis.production.orders.build.ABuildOrder;

public class BuildOrderToQueue {

    public static Queue fromBuildOrder(ABuildOrder buildOrder) {
        Queue queue = new Queue(buildOrder.productionOrders());

        return queue;
    }
}
