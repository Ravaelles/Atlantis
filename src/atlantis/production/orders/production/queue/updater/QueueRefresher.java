package atlantis.production.orders.production.queue.updater;

import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
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
        for (ProductionOrder order : queue.notFinished().list()) {
            if (order.shouldIgnore()) continue;

            // Diagnostic: base orders vanish silently between ADDED TO QUEUE
            // and any REQUEST/cancel (measured twice: 6:19 order, nothing
            // after). Throttled; shows whether refresh even sees the order.
            if (order.unitType() != null && order.unitType().isBase()) {
                atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                    "QUEUE_BASE " + order.unitType().name()
                        + " status=" + order.status()
                        + " min=" + atlantis.game.A.minerals()
                        + " supply=" + atlantis.game.A.supplyUsed() + "/" + atlantis.game.A.supplyTotal());
                atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.QUEUE_BASE)
                    .param("type", order.unitType().name())
                    .param("status", order.status() == null ? "-" : order.status().name())
                    .param("minerals", atlantis.game.A.minerals())
                    .param("supplyUsed", atlantis.game.A.supplyUsed())
                    .param("supplyTotal", atlantis.game.A.supplyTotal())
                    .log(atlantis.game.A.now(), atlantis.game.A.minSec());
            }

            updateOrderStatus(order);

            PrepareConstructionSite.prepareIfDue(order);
        }

        PrepareConstructionSite.dropOrphanedPreparing();

        // BATTLE CUTOVER: BO mission enforcement is deleted - DEFEND holds until step 5.
    }

    private OrderStatus updateOrderStatus(ProductionOrder order) {
        if (IsOrderCompleted.isCompleted(order, existingCounter)) return markAsComplete(order);
        if (IsOrderInProgress.isInProgress(order)) return markAsInProgress(order);

        return tryChangingStatusToReady(order);
    }

    private OrderStatus tryChangingStatusToReady(ProductionOrder order) {

        // Ready to produce
        if (IsReadyToProduceOrder.isReady(order)) return markAsReadyToProduce(order);


        return null;

    }

    // =========================================================

    private OrderStatus markAsInProgress(ProductionOrder order) {
        return order.setStatus(OrderStatus.IN_PROGRESS);
    }

    private OrderStatus markAsReadyToProduce(ProductionOrder order) {
        return order.setStatus(OrderStatus.READY_TO_PRODUCE);
    }

    private OrderStatus markAsComplete(ProductionOrder order) {

        order.setStatus(OrderStatus.FINISHED);

        if (Queue.get() != null) Queue.get().clearCache();

        return OrderStatus.FINISHED;

    }
}
