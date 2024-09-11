package atlantis.production.orders.production.queue;

import atlantis.production.orders.build.ABuildOrder;

public class CurrentQueue {
    private static Queue currentInstance = null;
    private static ABuildOrder basedOnBuildOrder;

    // =========================================================

    public static Queue get() {
        return currentInstance;
    }

    public static void setBasedOnBuildOrder(Queue queue, ABuildOrder basedOnBuildOrder) {
        currentInstance = queue;
        CurrentQueue.basedOnBuildOrder = basedOnBuildOrder;
    }

    public ABuildOrder basedOnBuildOrder() {
        return basedOnBuildOrder;
    }
}
