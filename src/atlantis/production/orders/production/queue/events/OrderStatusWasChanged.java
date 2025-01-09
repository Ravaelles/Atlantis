package atlantis.production.orders.production.queue.events;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;

public class OrderStatusWasChanged {
    public static void update(ProductionOrder order) {
        OrderStatus status = order.status();

//        if (order.is(AUnitType.Protoss_Pylon)) {
//            A.errPrintln(A.now() + " Protoss_Pylon CHANGED STATUS TO: " + status + " / " + order);
////            A.printStackTrace(A.now() + " Protoss_Pylon CHANGED STATUS TO: " + status + " / " + order);
//        }

        if (status.isReady()) order.makeSureResourcesAreReserved();
        else if (status.isInProgress() && order.isUnit()) order.makeSureResourcesAreReserved();
        else if (status.isFinished() && order.isUnit() && Queue.get() != null) {
            Queue.get().markAsProducedAndForget(order.unitType());
        }

        if (status.isInProgress()) {
            removeSameTechFromQueue(order, status);
        }
        if (status.isFinished()) {
            removeSameTechFromQueue(order, status);
        }
    }

    private static void removeSameTechFromQueue(ProductionOrder order, OrderStatus newStatus) {
        if (order.tech() != null) markAllAs(sameTypeTech(order), newStatus);
        else if (order.upgrade() != null) markAllAs(sameTypeUpgrade(order), newStatus);
//        if (order.tech() != null) markAllAsCompleted(sameTypeTech(order));
//        if (order.upgrade() != null) markAllAsCompleted(sameTypeUpgrade(order));
    }

    private static Orders sameTypeTech(ProductionOrder order) {
        if (Queue.get() == null) return new Orders();

        return Queue.get().allOrders().techType(order.tech()).exclude(order);
    }

    private static Orders sameTypeUpgrade(ProductionOrder order) {
        if (Queue.get() == null) return new Orders();

        return Queue.get().allOrders().upgradeType(order.upgrade()).exclude(order);
    }

    private static void markAllAs(Orders orders, OrderStatus newStatus) {
        for (ProductionOrder order : orders.list()) {
            order.setStatus(newStatus);
        }
    }
}
