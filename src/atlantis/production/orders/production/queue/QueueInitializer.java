package atlantis.production.orders.production.queue;

import atlantis.information.strategy.Strategy;
import atlantis.production.orders.build.ABuildOrder;

public class QueueInitializer {
    public static void initializeProductionQueue() {
        ABuildOrder buildOrder = Strategy.get().buildOrder();

        Queue queue = QueueFactory.fromBuildOrder(buildOrder);
        Queue.set(queue);
    }
}
