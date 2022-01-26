package atlantis.production.constructing;

import atlantis.AGame;
import atlantis.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.position.APosition;
import atlantis.production.ProductionOrder;
import atlantis.production.Requirements;
import atlantis.production.orders.ProductionQueue;
import atlantis.production.orders.ProductionQueueRebuilder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConstructionRequests {

    /**
     * List of all unfinished (started or pending) constructions.
     */
    protected static ConcurrentLinkedQueue<ConstructionOrder> constructionOrders = new ConcurrentLinkedQueue<>();

    /**
     * Issues request of constructing new building. It will automatically find position and builder unit for
     * it.
     */
    public static void requestConstructionOf(AUnitType building) {
        requestConstructionOf(building, null, null);
    }

    public static boolean requestConstructionOf(ProductionOrder order) {
        return requestConstructionOf(order.unitType(), order.atPosition(), order);
    }

    /**
     * Find a place for new building and assign a worker.
     *
     * WARNING: passed order parameter can later override nearTo parameter.
     */
    public static boolean requestConstructionOf(AUnitType building, HasPosition near, ProductionOrder order) {

        // Validate
        if (!building.isBuilding()) {
            throw new RuntimeException("Requested construction of not building!!! Type: " + building);
        }

        if (ASpecificConstructionManager.handledAsSpecialBuilding(building, order)) {
            return true;
        }

        if (!Requirements.hasRequirements(building)) {
            return handleNoRequirementsFullfilledFor(building);
        }

        // =========================================================
        // Create ConstructionOrder object, assign random worker for the time being

        ConstructionOrder newConstructionOrder = new ConstructionOrder(building);
        newConstructionOrder.setProductionOrder(order);
        newConstructionOrder.setNearTo(near);
        newConstructionOrder.setMaxDistance(order != null ? order.maximumDistance() : -1);
        newConstructionOrder.assignRandomBuilderForNow();

        if (newConstructionOrder.builder() == null) {
            if (AGame.supplyUsed() >= 7 && Count.bases() > 0 && Count.workers() > 0) {
                System.err.println("Builder is null, got damn it!");
            }
            return false;
        }

        // =========================================================
        // Find place for new building

        APosition positionToBuild = newConstructionOrder.findPositionForNewBuilding();

        // =========================================================
        // Successfully found position for new building

        if (positionToBuild != null) {

            // Update construction order with found position for building
            newConstructionOrder.setPositionToBuild(positionToBuild);

            // Assign optimal builder for this building
            newConstructionOrder.assignOptimalBuilder();

            // Add to list of pending orders
            constructionOrders.add(newConstructionOrder);

            // Rebuild production queue as new building is about to be built
            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();

            return true;
        }

        // Couldn't find place for building! That's bad, print descriptive explanation.
        else {
            System.err.print("Can't find place for `" + building + "`, " + order);
            if (AbstractPositionFinder._CONDITION_THAT_FAILED != null) {
                System.err.print("(reason: " + AbstractPositionFinder._CONDITION_THAT_FAILED + ")");
            } else {
                System.err.print("(reason not defined - bug)");
            }
            System.err.println();

            newConstructionOrder.cancel();
//            throw new RuntimeException("Can't find place for `" + building + "` ");
            return false;
        }
    }

    private static boolean handleNoRequirementsFullfilledFor(AUnitType building) {
        if (We.protoss() && AGame.supplyTotal() <= 10) {
            return false;
        }

        AUnitType requiredBuilding = building.getWhatIsRequired();
        if (countExistingAndNotFinished(requiredBuilding) == 0) {
            ConstructionRequests.requestConstructionOf(requiredBuilding);
            return true;
        }

        System.err.println("Uhmmm... shouldn't reach here.");
        System.err.println(building + " // " + requiredBuilding);
        return false;
    }

    /**
     * Returns ConstructionOrder object for given builder.
     */
    public static ConstructionOrder constructionOrderFor(AUnit builder) {
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (builder.equals(constructionOrder.builder())) {
                return constructionOrder;
            }
        }

        return null;
    }

    public static boolean hasRequestedConstructionOf(AUnitType type) {
        return countNotFinishedOfType(type) > 0;
    }

    /**
     * If we requested to build building A and even assigned worker who's travelling to the building site,
     * it's still doesn't count as unitCreated. We need to manually count number of constructions and only
     * then, we can e.g. "count unstarted barracks constructions".
     */
    public static int countNotStartedOfType(AUnitType type) {
        int total = 0;
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                    && constructionOrder.buildingType().equals(type)) {
                total++;
            }
        }

        // =========================================================
        // Special case for Overlord
        if (type.equals(AUnitType.Zerg_Overlord)) {
            total += Select.ourUnfinished().ofType(type).count();
        }

        return total;
    }

    public static int countNotStartedOfTypeInRadius(AUnitType type, double radius, APosition position) {
        int total = 0;
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                    && constructionOrder.buildingType().equals(type)
                    && position.distTo(constructionOrder.positionToBuildCenter()) <= radius) {
                total++;
            }
        }

        // =========================================================
        // Special case for Overlord
        if (type.equals(AUnitType.Zerg_Overlord)) {
            total += Select.ourUnfinished().ofType(type).count();
        }

        return total;
    }

    public static int countNotFinishedOfType(AUnitType type) {
        return Select.ourUnfinished().ofType(type).count()
                + countNotStartedOfType(type);
    }

    public static int countNotFinishedOfTypeInRadius(AUnitType type, double radius, APosition position) {
        return Select.ourUnfinished().ofType(type).inRadius(radius, position).count()
                + countNotStartedOfTypeInRadius(type, radius, position);
    }

    /**
     * Returns how many buildings (or Overlords) of given type are currently being produced (started, but not
     * finished).
     */
    public static int countPendingOfType(AUnitType type) {
        int total = 0;
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                    && constructionOrder.buildingType().equals(type)) {
                total++;
            }
        }

        // =========================================================
        // Special case for Overlord
        if (type.equals(AUnitType.Zerg_Overlord)) {
            total += Select.ourUnfinished().ofType(AUnitType.Zerg_Overlord).count();
        }

        return total;
    }

    public static int countExistingAndExpectedInNearFuture(AUnitType type, int amongNTop) {
        if (!type.isBuilding()) {
            throw new RuntimeException("Can only use it for buildings: " + type);
        }

        return Select.ourOfType(type).count()
                + ProductionQueue.countInQueue(type, amongNTop);
    }

    public static int countExistingAndNotFinished(AUnitType type) {
        if (!type.isBuilding()) {
            throw new RuntimeException("Can only use it for buildings: " + type);
        }

        return Select.ourOfTypeIncludingUnfinished(type).count()
                + countNotFinishedOfType(type);
    }

    public static int countExistingAndPlannedInRadius(AUnitType type, double radius, APosition position) {
        if (!type.isBuilding()) {
            throw new RuntimeException("Can only use it for buildings: " + type);
        }

        return Select.ourOfTypeIncludingUnfinished(type).inRadius(radius, position).count()
                + countNotFinishedOfTypeInRadius(type, radius, position);
    }

    /**
     * If we requested to build building A and even assigned worker who's travelling to the building site,
     * it's still doesn't count as unitCreated. We need to manually count number of constructions and only
     * then, we can e.g. "get not started Terran Barracks constructions".
     */
    public static ArrayList<ConstructionOrder> notStartedOfType(AUnitType type) {
        ArrayList<ConstructionOrder> notStarted = new ArrayList<>();
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                    && (type == null || constructionOrder.buildingType().equals(type))) {
                notStarted.add(constructionOrder);
            }
        }
        return notStarted;
    }

    public static ArrayList<ConstructionOrder> notStarted() {
        ArrayList<ConstructionOrder> notStarted = new ArrayList<>();
        for (ConstructionOrder constructionOrder : constructionOrders) {
            if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                notStarted.add(constructionOrder);
            }
        }
        return notStarted;
    }

    /**
     * Returns every construction order that is active in this moment. It will include even those buildings
     * that haven't been started yet.
     *
     * @return
     */
    public static ArrayList<ConstructionOrder> all() {
        return new ArrayList<>(constructionOrders);
    }

//    public static ArrayList<HasPosition> allConstructionOrdersIncludingCached() {
//        ArrayList<HasPosition> positions = new ArrayList<>();
//        for (ConstructionOrder order : ConstructionRequests.constructionOrders) {
//            positions.add(order.positionToBuild());
//        }
//        positions.addAll(APositionFinder.cache.values());
//
//        return positions;
//    }

    /**
     * @return first int is number minerals, second int is number of gas required.
     */
    public static int[] resourcesNeededForNotStarted() {
        int mineralsNeeded = 0;
        int gasNeeded = 0;
        for (ConstructionOrder constructionOrder : notStartedOfType(null)) {
            mineralsNeeded += constructionOrder.buildingType().getMineralPrice();
            gasNeeded += constructionOrder.buildingType().getGasPrice();
        }
        int[] result = {mineralsNeeded, gasNeeded};
        return result;
    }

    protected static void removeOrder(ConstructionOrder constructionOrder) {
        constructionOrders.remove(constructionOrder);
    }

    /**
     * Top-priority request.
     */
    public static void removeAllNotStarted() {
        for (Iterator<ConstructionOrder> iterator = ConstructionRequests.constructionOrders.iterator(); iterator.hasNext(); ) {
            ConstructionOrder constructionOrder =  iterator.next();
            if (!constructionOrder.hasStarted()) {
                constructionOrder.cancel();
            }
        }
    }

    public static boolean hasNotStartedNear(AUnitType building, HasPosition position, double inRadius) {
        for (ConstructionOrder order : notStartedOfType(building)) {
            if (order.positionToBuild() != null && position.distToLessThan(position, inRadius)) {
                return true;
            }
        }
        return false;
    }
}
