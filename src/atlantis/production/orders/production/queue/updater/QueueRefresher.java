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
//        A.errPrintln("@ " + A.now() + " - REFRESH QUEUE -----------------------------");
//        noMoreNewReadyOrdersFromNowOn = false;

//        ArrayList<ProductionOrder> completed = new ArrayList<>();

        for (ProductionOrder order : queue.allOrders().list()) {
            updateOrderStatus(order);

//            if (order.isCompleted()) completed.add(order);
        }

//        cleanUpCompleted(completed);

//        queue.clearCache();
//        queue.allOrders().print("ALL");
//        ReservedResources.print();
    }

//    private static void cleanUpCompleted(ArrayList<ProductionOrder> completed) {
//        if (A.now() < 300) return;
//
//        // Iterate over all orders using iterator and remove those that are completed
//        for (ProductionOrder order : completed) {
//            order.cancel();
//        }
//        Queue.get().clearCache();
//    }

    private OrderStatus updateOrderStatus(ProductionOrder order) {
        if (IsOrderCompleted.isCompleted(order, existingCounter)) return markAsComplete(order);

        if (IsOrderInProgress.check(order)) return markAsInProgress(order);

        return tryChangingStatusToReady(order);
    }

    private OrderStatus tryChangingStatusToReady(ProductionOrder order) {
//        if (noMoreNewReadyOrdersFromNowOn) return markAsNotReady(order);

        // Ready to produce
        if (IsReadyToProduceOrder.isReadyToProduce(order)) return markAsReadyToProduce(order);

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
