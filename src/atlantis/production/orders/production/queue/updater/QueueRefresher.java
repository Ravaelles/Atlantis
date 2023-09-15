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
    private boolean noMoreNewReadyOrdersFromNowOn;

    public QueueRefresher(Queue queue) {
        this.queue = queue;
        this.existingCounter = new Counter<>();
    }

    public void refresh() {
//        A.errPrintln("@ " + A.now() + " - REFRESH QUEUE -----------------------------");
        noMoreNewReadyOrdersFromNowOn = false;

        for (ProductionOrder order : queue.allOrders().list()) {
            updateOrderStatus(order);
        }
    }

    private OrderStatus updateOrderStatus(ProductionOrder order) {
        if (!IsOrderNotCompleted.check(order, existingCounter)) return markAsComplete(order);

        if (IsOrderInProgress.check(order)) return markAsInProgress(order);

        return tryChangingStatusToReady(order);
    }

    private OrderStatus tryChangingStatusToReady(ProductionOrder order) {
        if (noMoreNewReadyOrdersFromNowOn) return markAsNotReady(order);

//        System.err.println("supplyUsed = " + AGame.supplyUsed());
//        System.err.println("minerals = " + AGame.minerals());
//        System.err.println("CHECK order = " + order);
//        System.err.println("calculateIfHasWhatRequired() = " + order.calculateIfHasWhatRequired());
//        ReservedResources.print();

        // Ready to produce
        if (IsReadyToProduceOrder.isReadyToProduce(order)) return markAsReadyToProduce(order);

        if (!IsReadyToProduceOrder.canAffordWithReserved(order)) noMoreNewReadyOrdersFromNowOn = true;
//        return null;

//        System.err.println("\n@@@@@@@@ FROM NOW ON NOT READY @@@@@");
//        ReservedResources.print();

//        noMoreNewReadyOrdersFromNowOn = true;
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

//        OrderWasCompleted.update(order, queue);

        return order.status();
    }
}
