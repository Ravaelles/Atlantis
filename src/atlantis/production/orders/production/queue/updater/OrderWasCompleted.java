package atlantis.production.orders.production.queue.updater;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;

public class OrderWasCompleted {
    public static void update(ProductionOrder order, Queue queue) {
        queue.removeOrder(order);
        queue.completedOrdersHistory().add(order);
        queue.clearCache();
    }
}
