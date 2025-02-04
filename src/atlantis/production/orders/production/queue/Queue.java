package atlantis.production.orders.production.queue;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.History;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.QueueRefresher;
import atlantis.units.AUnitType;

import java.util.List;

import static atlantis.units.AUnitType.Terran_Engineering_Bay;

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

    public Orders completedOrInProgress() {
        return orders.inProgress().addAll(orders.completed().list());
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

    public Orders nonCompleted() {
        return orders.nonCompleted();

//        return cache.get(
//            "nonCompleted",
//            CACHE_FOR_FRAMES,
//            orders::nonCompleted
//        );
    }

    public Orders nonCompletedNext30() {
        return cache.get(
            "nonCompletedNext30",
            CACHE_FOR_FRAMES,
            orders::nonCompletedNext30
        );
    }

    public Orders completedOrders() {
        return orders.completed();

//        return cache.get(
//            "completedOrders",
//            CACHE_FOR_FRAMES,
//            orders::completed
//        );
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
