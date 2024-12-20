package atlantis.production.orders.production.queue.updater;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.Counter;

public class QueueRefresher {
    private final Queue queue;
    private final Counter<AUnitType> existingCounter;
//    private boolean noMoreNewReadyOrdersFromNowOn;

    public QueueRefresher(Queue queue) {
        this.queue = queue;
        this.existingCounter = new Counter<>();
    }

    public void refresh() {
        for (ProductionOrder order : queue.nonCompleted().list()) {
            if (order.shouldIgnore()) continue;

            updateOrderStatus(order);
        }
    }

    private OrderStatus updateOrderStatus(ProductionOrder order) {
        if (IsOrderCompleted.isCompleted(order, existingCounter)) return markAsComplete(order);

        if (IsOrderInProgress.check(order)) return markAsInProgress(order);

        return tryChangingStatusToReady(order);
    }

    private OrderStatus tryChangingStatusToReady(ProductionOrder order) {
//        if (noMoreNewReadyOrdersFromNowOn) return markAsNotReady(order);

        // Ready to produce
        if (IsReadyToProduceOrder.check(order)) return markAsReadyToProduce(order);

//        if (
//            !A.hasMinerals(550) && !IsReadyToProduceOrder.canAffordWithReserved(order)
//        ) noMoreNewReadyOrdersFromNowOn = true;

        return null;

    }

    // =========================================================

    private OrderStatus markAsInProgress(ProductionOrder order) {
        return order.setStatus(OrderStatus.IN_PROGRESS);
    }

    private OrderStatus markAsReadyToProduce(ProductionOrder order) {
        return order.setStatus(OrderStatus.READY_TO_PRODUCE);
    }

    private OrderStatus markAsNotReady(ProductionOrder order) {
        return order.setStatus(OrderStatus.NOT_READY);
    }

    private OrderStatus markAsComplete(ProductionOrder order) {
//        if (order.unitType() != null && order.unitType().isGasBuilding()) {
//            A.errPrintln("########################### Gas building completed: " + order.construction());
//        }

        order.setStatus(OrderStatus.COMPLETED);
//        order.setUnitType(null);
//        order.setModifier(null);
//        order.forceSetPosition(null);

        if (Queue.get() != null) Queue.get().clearCache();

        return OrderStatus.COMPLETED;

//        OrderStatus status = order.status();
//        order.cancel();
//
//
//        return status;
    }
}
