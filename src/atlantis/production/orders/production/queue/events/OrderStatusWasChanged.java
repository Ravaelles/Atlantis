package atlantis.production.orders.production.queue.events;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;

public class OrderStatusWasChanged {
    public static void update(ProductionOrder order, OrderStatus status) {
//        A.errPrintln("OrderStatusWasChanged(" + order + ", " + status + ")");

        if (status.ready()) order.makeSureResourcesAreReserved();
        else if (status.inProgress() && order.isUnit()) order.makeSureResourcesAreReserved();
//        else order.makeSureToClearReservedResources();

        removeSameTechFromQueue(order);

        if (order.isCompleted() && order.isUnit() && Queue.get() != null) {
            Queue.get().markAsProducedAndForget(order.unitType());
        }
    }

    private static void removeSameTechFromQueue(ProductionOrder order) {
        if (order.tech() != null) markAllAsCompleted(sameTypeTech(order));
//        if (order.upgrade() != null) markAllAsCompleted(sameTypeUpgrade(order));
    }

    private static Orders sameTypeTech(ProductionOrder order) {
        if (Queue.get() == null) return new Orders();

        return Queue.get().allOrders().techType(order.tech()).exclude(order);
    }

    private static Orders sameTypeUpgrade(ProductionOrder order) {
        return Queue.get().allOrders().upgradeType(order.upgrade()).exclude(order);
    }

    private static void markAllAsCompleted(Orders orders) {
        for (ProductionOrder order : orders.list()) {
            order.setStatus(OrderStatus.COMPLETED);
        }
    }
}
