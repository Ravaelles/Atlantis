package atlantis.production.orders.production.queue.add;

import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;

public class IndexForNewOrder {
    public static int indexForPriority(ProductionOrderPriority priority) {
        if (priority.isStandard()) {
            return CountInQueue.countNextOrdersWithSupplyRequirementFilled(A.supplyUsed());
        }

        return CountInQueue.countOrdersWithPriorityAtLeast(priority);
    }
}
