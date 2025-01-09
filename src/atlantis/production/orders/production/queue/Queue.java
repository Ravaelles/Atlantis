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
        return orders.readyToProduce();

//        return cache.get(
//            "readyToProduceOrders",
//            CACHE_FOR_FRAMES,
//            orders::readyToProduce
//        );
    }

    public Orders forCurrentSupply() {
        return cache.get(
            "forCurrentSupply",
            CACHE_FOR_FRAMES,
            orders::forCurrentSupply
        );
    }

    public Orders finishedOrInProgress() {
        return orders.inProgress().addAll(orders.finished().list());
    }

    public Orders inProgressOrders() {
        return orders.inProgress();

//        return cache.get(
//            "inProgressOrders",
//            CACHE_FOR_FRAMES,
//            orders::inProgress
//        );
    }

    public Orders notStarted() {
        return orders.notStarted();

//        return cache.get(
//            "notStarted",
//            CACHE_FOR_FRAMES,
//            orders::notStarted
//        );
    }

    public Orders notFinished() {
        return orders.notFinished();
    }

    public Orders notFinishedNext30() {
        return cache.get(
            "nonFinishedNext30",
            CACHE_FOR_FRAMES,
            orders::nonFinishedNext30
        );
    }

    public Orders finishedOrders() {
        return orders.finished();
    }

    // =========================================================

    public void removeOrder(ProductionOrder order) {
//        A.printStackTrace("REMOVE ORDER " + order);

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
