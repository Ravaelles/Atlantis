package atlantis.production.orders.production.queue;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.build.ABuildOrder;

public class QueueInitializer {
    public static void initializeProductionQueue() {
//        AStrategy strategy = OurStrategy.get();

        ABuildOrder buildOrder = OurStrategy.get().buildOrder();
//        Queue queue = BuildOrderToQueue.fromBuildOrder(buildOrder);
        Queue queue = QueueFactory.fromBuildOrder(buildOrder);

//        CurrentQueue.setBasedOnBuildOrder(queue, buildOrder);
        Queue.set(queue);
    }
}
