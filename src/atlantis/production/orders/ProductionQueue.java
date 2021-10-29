package atlantis.production.orders;

import atlantis.position.APosition;
import atlantis.production.ProductionOrder;
import atlantis.strategy.AStrategy;
import atlantis.units.AUnitType;

import java.util.ArrayList;

/**
 * Current production queue. Based on Build Order loaded from file, with some dynamic changes applied.
 */
public abstract class ProductionQueue {

    /**
     * Build order currently in use.
     * switchToBuildOrder(ABuildOrder buildOrder)
     */
    private static ABuildOrder currentBuildOrder = null;

    /**
     * Ordered list of next units we should build.
     * It gets rebuild whenever new unit is created.
     */
    protected static ArrayList<ProductionOrder> currentProductionQueue = new ArrayList<>();

    /**
     * Number of minerals reserved to produce some units/buildings.
     */
    protected static int mineralsNeeded = 0;

    /**
     * Number of gas reserved to produce some units/buildings.
     */
    protected static int gasNeeded = 0;

    // =========================================================

    /**
     * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
     */
    public static ArrayList<ProductionOrder> nextInProductionQueue(int howMany) {
        ArrayList<ProductionOrder> result = new ArrayList<>();

        for (int i = 0; i < howMany && i < currentProductionQueue.size(); i++) {
            ProductionOrder productionOrder = currentProductionQueue.get(i);
            result.add(productionOrder);
        }

        return result;
    }

    public static boolean isAtTheTopOfQueue(AUnitType type, int amongNTop) {
        for (int i = 0; i < amongNTop && i < currentProductionQueue.size(); i++) {
            if (type.equals(currentProductionQueue.get(i).getUnitOrBuilding())) {
                return true;
            }
        }
        return false;
    }

    public static int countInTopOfQueue(AUnitType type, int amongNTop) {
        int count = 0;
        for (int i = 0; i < amongNTop && i < currentProductionQueue.size(); i++) {
            if (type.equals(currentProductionQueue.get(i).getUnitOrBuilding())) {
                count++;
            }
        }
        return count;
    }

//    public static boolean hasNothingToProduce() {
//        return currentProductionQueue.isEmpty();
//    }

    // === Getters =============================================

    /**
     * Returns currently active build order.
     */
    public static ABuildOrder get() {
        return currentBuildOrder;
    }

    public static void useBuildOrderFrom(AStrategy strategy) {
        currentBuildOrder = strategy.buildOrder();
        ProductionQueueRebuilder.rebuildProductionQueue();
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

}
