package atlantis.production.orders.production;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.ArrayList;

/**
 * Current production queue
 */
public class CurrentProductionQueue {
    /**
     * Returns list of orders (units and upgrades) that we should produce now.
     * This method iterates over all production orders, taken from the active build orders
     * and returns those we can afford at this moment.
     * <p>
     * Notice that dynamic actions (like requesting a detector quickly) may insert
     * a unit dynamically with top priority.
     */
    public static ArrayList<ProductionOrder> get(ProductionQueueMode mode) {
        if (ProductionQueue.nextInQueue().size() >= 10) return ProductionQueue.nextInQueue();

        (new RebuildProductionQueue(mode)).rebuildQueue();

        return ProductionQueue.nextInQueue();
    }

    protected static boolean hasUnitInQueue(AUnitType type, ArrayList<ProductionOrder> queue) {
        for (ProductionOrder order : queue) {
            if (order.unitType() != null && order.unitType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static int[] resourcesReserved() {
        return new int[]{ProductionQueue.mineralsNeeded, ProductionQueue.gasNeeded};
    }

    public static void remove(ProductionOrder order) {
        ProductionQueue.removeOrder(order);
    }

    public static void print() {
        print(null);
    }

    public static void print(String message) {
        ArrayList<ProductionOrder> queue = CurrentProductionQueue.get(ProductionQueueMode.REQUIREMENTS_FULFILLED);
//        ArrayList<ProductionOrder> queue = CurrentProductionQueue.get(ProductionQueueMode.ENTIRE_QUEUE);
        int total = CurrentProductionQueue.get(ProductionQueueMode.ENTIRE_QUEUE).size();

        if (message != null) A.println(message);

        A.println("Current production queue (current: " + queue.size() + " / total: " + total + ")");
        for (ProductionOrder order : queue) {
            A.println(order.toString());
        }
    }
}
