package atlantis.production.orders;

import atlantis.AGame;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.util.ArrayList;

/**
 * Current production queue. Based on Build Order loaded from file, with some dynamic changes applied.
 */
public abstract class AProductionQueue {

    public static final int MODE_ALL_ORDERS = 1;
    public static final int MODE_ONLY_UNITS = 2;

    /**
     * Build order currently in use.
     * switchToBuildOrder(ABuildOrder buildOrder)
     */
    protected static ABuildOrder currentBuildOrder = null;

    /**
     * Ordered list of production orders as initially read from the file. It never changes
     */
    protected static ArrayList<ProductionOrder> initialProductionQueue = new ArrayList<>();

    /**
     * Ordered list of next units we should build. It is re-generated when events like "started
     * training/building new unit"
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
    public static ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
        ArrayList<ProductionOrder> result = new ArrayList<>();

        for (int i = 0; i < howMany && i < currentProductionQueue.size(); i++) {
            ProductionOrder productionOrder = currentProductionQueue.get(i);
            result.add(productionOrder);
        }

        return result;
    }

    // === Getters =============================================

    /**
     * Returns currently active build order.
     */
    public static ABuildOrder getCurrentBuildOrder() {
        return currentBuildOrder;
    }
    
    /**
     * Number of minerals reserved to produce some units/buildings in the build order that according to it
     * should be produced right now (judging by the supply used).
     */
    public static int getMineralsReserved() {
        return mineralsNeeded;
    }

    /**
     * Number of gas reserved to produce some units/buildings in the build order that according to it
     * should be produced right now (judging by the supply used).
     */
    public static int getGasReserved() {
        return gasNeeded;
    }    

}
