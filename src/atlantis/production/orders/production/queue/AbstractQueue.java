package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;

public abstract class AbstractQueue {
    protected final int CACHE_FOR_FRAMES = 0;

    protected static Cache<Orders> cache = new Cache<>();
    protected final Orders orders = new Orders();

    public void clearCache() {
        cache.clear();
    }

    public Orders nextOrders(int n) {
        return cache.get(
            "abstractNextOrders:" + n,
            CACHE_FOR_FRAMES,
            () -> orders.next(n)
        );
    }

    public boolean haveAmongNextOrders(AUnitType type, int numberOfNextOrdersToCheck) {
        return !nextOrders(numberOfNextOrdersToCheck).ofType(type).isEmpty();
    }

    public void markAsProducedAndForget(AUnitType type) {
        ProductionOrder forgetOrder = null;

        for (ProductionOrder order : orders.list()){
            if (order.isUnit() && order.unitType().equals(type)) {
                order.setIgnore(true);
                break;
            }
        }
    }
}
