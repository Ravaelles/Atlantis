package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.ProductionOrder;

public class QueueUpdater {
    private final Queue queue;

    public QueueUpdater(Queue queue) {
        this.queue = queue;
    }

    public void update() {
        updateInProgress();
    }

    private void updateInProgress() {
        for (ProductionOrder order : queue.inProgressOrders()) {
            if (order.isInProgress()) {
                queue.inProgressOrders().add(order);
            }
        }
    }
}
