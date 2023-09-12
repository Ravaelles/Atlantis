package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.Counter;

public class QueueRefresher {
    private final Queue queue;
    private final Counter<AUnitType> existingCounter;
    private boolean allOrdersFromNowNotReady = false;

    public QueueRefresher(Queue queue) {
        this.queue = queue;
        this.existingCounter = new Counter<>();
    }

    public void refresh() {
//        A.printStackTrace();

        for (ProductionOrder order : queue.allOrders().list()) {
            if (allOrdersFromNowNotReady) break;

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

        if (allOrdersFromNowNotReady) {
            return markAsNotReady(order);
        }

//        System.err.println("supplyUsed = " + AGame.supplyUsed());
//        System.err.println("minerals = " + AGame.minerals());
//        System.err.println("order = " + order);
//        System.err.println("calculateIfHasWhatRequired() = " + order.calculateIfHasWhatRequired());

        // Ready to produce
        if (IsReadyToProduceOrder.isReadyToProduce(order)) {
            return markAsReadyToProduce(order);
        }

        allOrdersFromNowNotReady = true;
        return null;
    }

    // =========================================================

    private OrderStatus markAsInProgress(ProductionOrder order) {
        return order.setStatus(OrderStatus.IN_PROGRESS);
    }

    private OrderStatus markAsReadyToProduce(ProductionOrder order) {
        ReservedResources.reserveMinerals(order.mineralPrice());
        ReservedResources.reserveGas(order.gasPrice());

        return order.setStatus(OrderStatus.READY_TO_PRODUCE);
    }

    private OrderStatus markAsNotReady(ProductionOrder order) {
        return order.setStatus(OrderStatus.NOT_READY);
    }

    private OrderStatus markAsComplete(ProductionOrder order) {
        return order.setStatus(OrderStatus.COMPLETED);
    }
}
