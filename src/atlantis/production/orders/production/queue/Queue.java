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
        // Single policy in LIVE: the v2 engine is the only producer, so the
        // legacy queue must not mature rows, prepare sites or twin readiness
        // on unit events (measured: orphan PREPARING with builders,
        // ORDER_NOT_READY rows and leaked reservations in LIVE games).
        // DRY_RUN is unaffected (legacy plays there).
        if (atlantis.config.env.Env.productionV2().isLive()) return;

        // No workers and no base, no production: on a workerless UMS map
        // the game-start BO load plus every unit event ran the full legacy
        // chain (QueueInitializer -> refresh -> prepareIfDue -> the
        // APositionFinder site search with its WUT stack and dirty-hack
        // fallbacks) for demand that can never build. The twin stays
        // silent for the same reason (IsReadyToProduceOrder). Gated on
        // baselessness too, so a base that lost its workers keeps the
        // adopt-and-wait path (PreparingConstructionSiteTest pins it).
        if (atlantis.units.select.Count.workers() == 0 && !atlantis.units.select.Have.base()) return;

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
//            AConsole.errPrintln("Failed to add " + productionOrder.whatToString());
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
    }

    public Orders notInProgress() {
        return orders.notInProgres();
    }

    public Orders statusNotReady() {
        return orders.notReady();
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
//        AConsole.printStackTrace("REMOVE ORDER " + order);

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
