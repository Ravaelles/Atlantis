package atlantis.production.orders.production;

import atlantis.information.strategy.AStrategy;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Current production queue. Based on Build Order loaded from file, with some dynamic changes applied.
 */
public abstract class ProductionQueue {
    /**
     * Ordered list of next units we should build.
     * It gets rebuild whenever new unit is created.
     */
    private static ArrayList<ProductionOrder> nextInQueue = new ArrayList<>();

    /**
     * Number of minerals reserved to produce some units/buildings.
     */
    protected static int mineralsNeeded = 0;

    /**
     * Number of gas reserved to produce some units/buildings.
     */
    protected static int gasNeeded = 0;

    // =========================================================

    public static int size() {
        return nextInQueue.size();
    }

    public static void addOrder(ProductionOrder order) {
        nextInQueue.add(order);
        sortQueueBySupplyNeeded();
    }

    public static void addToQueue(int index, ProductionOrder productionOrder) {
        nextInQueue.add(index, productionOrder);
        sortQueueBySupplyNeeded();

//        printQueue("Added to queue: " + productionOrder);
    }

    public static void removeOrder(ProductionOrder order) {
        nextInQueue.remove(order);
    }

    protected static void setQueue(ArrayList<ProductionOrder> queue) {
        nextInQueue = queue;
    }

    public static boolean isAtTheTopOfQueue(AUnitType type, int amongNTop) {
        for (int i = 0; i < amongNTop && i < nextInQueue.size(); i++) {
            if (type.equals(nextInQueue.get(i).unitType())) {
                return true;
            }
        }
        return false;
    }

    public static ProductionOrder nextOrderFor(AUnitType type, int amongNTop) {
        for (int i = 0; i < amongNTop && i < nextInQueue.size(); i++) {
            if (type.equals(nextInQueue.get(i).unitType())) {
                return nextInQueue.get(i);
            }
        }
        return null;
    }

    public static int countInQueue(AUnitType type, int amongNTop) {
        int count = 0;
        for (int i = 0; i < amongNTop && i < nextInQueue.size(); i++) {
            if (type.equals(nextInQueue.get(i).unitType())) {
                count++;
            }
        }
        return count;
    }

    public static int countInQueue(TechType type, int amongNTop) {
        int count = 0;
        for (int i = 0; i < amongNTop && i < nextInQueue.size(); i++) {
            if (type.equals(nextInQueue.get(i).tech())) {
                count++;
            }
        }
        return count;
    }

    public static int countInQueue(UpgradeType type, int amongNTop) {
        int count = 0;
        for (int i = 0; i < amongNTop && i < nextInQueue.size(); i++) {
            if (type.equals(nextInQueue.get(i).upgrade())) {
                count++;
            }
        }
        return count;
    }

    // === Getters =============================================

    public static void useBuildOrderFrom(AStrategy strategy) {
        CurrentBuildOrder.set(strategy.buildOrder());
        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
    }

    /**
     * Number of minerals reserved to produce some units/buildings in the build order that according to it
     * should be produced right now (judging by the supply used).
     */
    public static int mineralsReserved() {
        return mineralsNeeded;
    }

    /**
     * Number of gas reserved to produce some units/buildings in the build order that according to it
     * should be produced right now (judging by the supply used).
     */
    public static int gasReserved() {
        return gasNeeded;
    }

    public static int mineralsNeeded() {
        return mineralsNeeded;
    }

    public static void setMineralsNeeded(int mineralsNeeded) {
        ProductionQueue.mineralsNeeded = mineralsNeeded;
    }

    public static int countOrdersWithPriorityAtLeast(ProductionOrderPriority priority) {
        int total = 0;
        for (ProductionOrder order : nextInQueue) {
//            if (type.equals(order.getUnitOrBuilding())) {
            if (order.priority().compareTo(priority) >= 0) {
                total++;
            }
        }
        return total;
    }

    public static int positionInQueue(AUnitType type) {
        int current = 0;
        for (int i = 0; i < nextInQueue.size(); i++) {
            if (type.equals(nextInQueue.get(i).unitType())) {
                return current;
            }

            current++;
        }

        return -1;
    }

    public static void printQueue(String message) {
        if (message == null) {
            message = "Currently in queue";
        }

        System.out.println("=== " + message + " (" + nextInQueue.size() + ") ===");
        for (ProductionOrder order : nextInQueue) {
            System.out.println(order);
        }
    }

    // =========================================================

    public static void sortQueueBySupplyNeeded() {
//        nextInQueue.sort((o1, o2) -> o1.minSupply() < o2.minSupply());
        nextInQueue.sort(Comparator.comparing(ProductionOrder::minSupply));
    }

//    public static void sortBySupplyNeeded() {
//        Comparator.comparingInt(ProductionOrder::minSupply);
//    }

    // =========================================================

    @SuppressWarnings("unchecked")
    public static ArrayList<ProductionOrder> nextInQueue() {
        return (ArrayList<ProductionOrder>) nextInQueue.clone();
    }
}
