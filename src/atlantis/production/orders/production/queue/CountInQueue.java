package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

public class CountInQueue {
    public static int count(AUnitType type, int numberOfNextOrdersToCheck) {
        return Queue.get().nextOrders(numberOfNextOrdersToCheck).ofType(type).size();
    }

    public static int count(TechType type, int numberOfNextOrdersToCheck) {
        return Queue.get().nextOrders(numberOfNextOrdersToCheck).techType(type).size();
    }

    public static int count(UpgradeType type, int numberOfNextOrdersToCheck) {
        return Queue.get().nextOrders(numberOfNextOrdersToCheck).upgradeType(type).size();
    }

    public static int countOrdersWithPriorityAtLeast(ProductionOrderPriority priority) {
        return Queue.get().nonCompleted().priorityAtLeast(priority).size();
    }
}