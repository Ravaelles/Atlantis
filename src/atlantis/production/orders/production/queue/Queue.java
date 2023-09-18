package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.QueueRefresher;
import atlantis.units.select.Select;

public class Queue extends AbstractQueue {
    private static Queue instance = null;

    // =========================================================

    protected Queue() {
    }

    // =========================================================

    public void refresh() {
        (new QueueRefresher(this)).refresh();
        clearCache();
    }

    public void clearCache() {
        cache.clear();
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
            -1,
            () -> orders
        );
    }

    public Orders readyToProduceOrders() {
        return cache.get(
            "readyToProduceOrders",
            -1,
            orders::readyToProduce
        );
    }

    public Orders forCurrentSupply() {
        return cache.get(
            "forCurrentSupply",
            -1,
            orders::forCurrentSupply
        );
    }

    public Orders inProgressOrders() {
        return cache.get(
            "inProgressOrders",
            -1,
            orders::inProgress
        );
    }

    public Orders nonCompleted() {
        return cache.get(
            "nonCompleted",
            -1,
            orders::notCompleted
        );
    }

    public Orders completedOrders() {
        return cache.get(
            "completedOrders",
            -1,
            orders::completed
        );
    }

    // =========================================================

    public void removeOrder(ProductionOrder order) {
        orders.remove(order);
    }

    // =========================================================

    public static Queue get() {
        return instance;
    }

    public static void set(Queue instance) {
        Queue.instance = instance;
    }
}
