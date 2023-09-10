package atlantis.production.orders.production.queue.updater;

import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.util.Counter;

public class QueueRefresher {
    private final Queue queue;
    private final Counter<AUnitType> existingCounter;

    public QueueRefresher(Queue queue) {
        this.queue = queue;
        this.existingCounter = new Counter<>();
    }

    public void refresh() {
        for (ProductionOrder order : queue.allOrders().list()) {
            updateOrderStatus(order);
        }
    }

    private OrderStatus updateOrderStatus(ProductionOrder order) {
        if (IsOrderNotCompleted.isOrderNotCompleted(order, existingCounter)) {
            return updateWhenNotCompleted(order);
        }

        return markAsComplete(order);
    }

    private OrderStatus updateWhenNotCompleted(ProductionOrder order) {
        // In progress
        if (IsOrderInProgress.check(order)) {
            return markAsInProgress(order);
        }

//        System.err.println("supplyUsed = " + AGame.supplyUsed());
//        System.err.println("minerals = " + AGame.minerals());
//        System.err.println("order = " + order);
//        System.err.println("calculateIfHasWhatRequired() = " + order.calculateIfHasWhatRequired());

        // Ready to produce
        if (IsReadyToProduceOrder.isReadyToProduce(order)) {
            return markAsReadyToProduce(order);
        }

        return null;
    }

    // =========================================================

    private OrderStatus markAsInProgress(ProductionOrder order) {
        return order.setStatus(OrderStatus.IN_PROGRESS);
//        queue.inProgressOrders().add(order);
    }

    private OrderStatus markAsReadyToProduce(ProductionOrder order) {
        return order.setStatus(OrderStatus.READY_TO_PRODUCE);
//        queue.readyToProduceOrders().add(order);
//        queue.completedOrders().remove(order);
    }

    private OrderStatus markAsComplete(ProductionOrder order) {
//        System.err.println("markAsComplete = " + order);
        return order.setStatus(OrderStatus.COMPLETED);
//        queue.completedOrders().add(order);
    }
}
