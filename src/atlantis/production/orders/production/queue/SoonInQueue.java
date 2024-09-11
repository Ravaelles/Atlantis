package atlantis.production.orders.production.queue;

import atlantis.units.AUnitType;

public class SoonInQueue {
    public static boolean have(AUnitType type) {
        return Queue.get().haveAmongNextOrders(type, 4);
    }

    public static boolean have(AUnitType type, int amongNextOrders) {
        return Queue.get().haveAmongNextOrders(type, amongNextOrders);
    }
}
