package atlantis.production.orders.production.queue;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.History;
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
        boolean added = allOrders().add(index, productionOrder);

        if (added) {
//            System.err.println("productionOrder.whatToString() = " + productionOrder.whatToString());
            history.addNow(productionOrder.whatToString());
            clearCache();
//            System.err.println("history.size = " + history.size() + " / last:" + history.last());
        }
//        else {
//            A.errPrintln("Failed to add " + productionOrder.whatToString());
//        }

//        allOrders().print("Added");

        return added;
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

    public Orders nonCompletedNext30() {
        return cache.get(
            "nonCompletedNext30",
            CACHE_FOR_FRAMES,
            orders::nonCompletedNext30
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
