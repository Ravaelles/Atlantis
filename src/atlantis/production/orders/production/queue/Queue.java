package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.QueueRefresher;

public class Queue extends AbstractQueue {
    private static Queue instance = null;

    // =========================================================

    protected Queue() {
    }

    // =========================================================

    public void refresh() {
        clearCache();
        (new QueueRefresher(this)).refresh();
        clearCache();
    }

    // =========================================================

    public boolean addNew(int index, ProductionOrder productionOrder) {
        boolean result = allOrders().add(index, productionOrder);

        clearCache();
//        allOrders().print("Added");

        return result;
    }

    // =========================================================

    public Orders allOrders() {
        return cache.get(
            "allOrders",
            CACHE_FOR_FRAMES,
            () -> orders
        );
    }

    public Orders readyToProduceOrders() {
        return cache.get(
            "readyToProduceOrders",
            CACHE_FOR_FRAMES,
            orders::readyToProduce
        );
    }

    public Orders forCurrentSupply() {
        return cache.get(
            "forCurrentSupply",
            CACHE_FOR_FRAMES,
            orders::forCurrentSupply
        );
    }

    public Orders inProgressOrders() {
        return cache.get(
            "inProgressOrders",
            CACHE_FOR_FRAMES,
            orders::inProgress
        );
    }

    public Orders nonCompleted() {
        return cache.get(
            "nonCompleted",
            CACHE_FOR_FRAMES,
            orders::nonCompleted
        );
    }

    public Orders completedOrders() {
        return cache.get(
            "completedOrders",
            CACHE_FOR_FRAMES,
            orders::completed
        );
    }

    // =========================================================

    public void removeOrder(ProductionOrder order) {
        orders.remove(order);

        clearCache();
    }

    // =========================================================

    public static Queue get() {
        return instance;
    }

    public static void set(Queue instance) {
        Queue.instance = instance;
    }
}
