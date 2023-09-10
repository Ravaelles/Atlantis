package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.queue.order.Orders;
import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import bwapi.TechType;
import bwapi.UpgradeType;

public abstract class AbstractQueue {
    protected static Cache<Orders> cache = new Cache<>();

    protected final Orders orders = new Orders();

    public Orders nextOrders(int n) {
        return cache.get(
            "nextOrders:" + n,
            -1,
            () -> orders.next(n)
        );
    }

    public boolean haveAmongNextOrders(AUnitType type, int numberOfNextOrdersToCheck) {
        return !nextOrders(numberOfNextOrdersToCheck).ofType(type).isEmpty();
    }
}
