package atlantis.production.orders.production.queue.events;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.util.AConsole;

public class OrderStatusWasChanged {
    public static void update(ProductionOrder order) {
        OrderStatus status = order.status();

//        if (order.is(AUnitType.Protoss_Pylon)) {
//            AConsole.errPrintln(A.now() + " Protoss_Pylon CHANGED STATUS TO: " + status + " / " + order);
////            AConsole.printStackTrace(A.now() + " Protoss_Pylon CHANGED STATUS TO: " + status + " / " + order);
//        }

        if (status.isReady()) {
            // Owner 2026-10-16: earmark only at/after the row's supply -
            // a Gateway row reserving 150 at supply 9 starved the worker
            // pump to a 200 bank while the Gateway itself waited. Below
            // minSupply the row waits unreserved; spending checks add back
            // own reservations, so issuance at minSupply works unchanged.
            if (atlantis.game.A.supplyUsed() >= order.minSupply()) {
                order.makeSureResourcesAreReserved();
            }
        }
        else if (status.isInProgress() && order.isUnit()) order.makeSureResourcesAreReserved();
        else if (status.isFinished() && order.isUnit() && Queue.get() != null) {
            order.releasedReservedResources();
            Queue.get().markAsProducedAndForget(order.unitType());
        }
        // NEXT #89: IN_PROGRESS buildings keep holding (builder en route,
        // money about to warp); every other exit from earmarked releases.
        else if (!status.isInProgress()) order.releasedReservedResources();

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
