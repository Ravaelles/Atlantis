package atlantis.production.constructing;

import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.production.orders.production.ProductionQueueRebuilder;
import atlantis.production.orders.production.Requirements;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConstructionRequests {
    /**
     * List of all unfinished (started or pending) constructions.
     */
    public static ConcurrentLinkedQueue<Construction> constructions = new ConcurrentLinkedQueue<>();

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
     * <p>
     * WARNING: passed order parameter can later override nearTo parameter.
     */
    public static boolean requestConstructionOf(AUnitType building, HasPosition near, ProductionOrder order) {

        // Validate
        if (!building.isBuilding()) {
            throw new RuntimeException("Requested construction of not building!!! Type: " + building);
        }

        if (SpecificConstructionRequests.handledAsSpecialBuilding(building, order)) return true;

        if (!Requirements.hasRequirements(building)) {
            return handleRequirementsNotFullfilledFor(building);
        }

        // =========================================================
        // Create ConstructionOrder object, assign random worker for the time being

//        System.err.println("Requested CONSTRUCTION:");
//        System.err.println( building + " / " + near + " / " + order);
//        if (building.isBase()) {
//            A.printStackTrace("Requested BASE");
//        }

        Construction newConstructionOrder = new Construction(building);
        newConstructionOrder.setProductionOrder(order);
        newConstructionOrder.setNearTo(near);
        newConstructionOrder.setMaxDistance(order != null ? order.maximumDistance() : -1);
        newConstructionOrder.assignRandomBuilderForNow();

        if (newConstructionOrder.builder() == null) {
            if (AGame.supplyUsed() >= 7 && Count.bases() > 0 && Count.workers() > 0) {
                ErrorLog.printMaxOncePerMinute("Builder is null, got damn it!");
            }
            return false;
        }

        // =========================================================
        // Find place for new building

        APosition positionToBuild = newConstructionOrder.findPositionForNewBuilding();

        // =========================================================
        // Successfully found position for new building

        if (positionToBuild != null) {
            APositionFinder.clearCache();

            // Update construction order with found position for building
            newConstructionOrder.setPositionToBuild(positionToBuild);

            // Assign optimal builder for this building
            newConstructionOrder.assignOptimalBuilder();

            // Add to list of pending orders
            constructions.add(newConstructionOrder);

//            A.printStackTrace("AFTER ADDED");
//            System.out.println("# ADDED, constructions = ");
//            A.printList(constructions);

            // Rebuild production queue as new building is about to be built
            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();

            return true;
        }

        // Couldn't find place for building! That's bad, print descriptive explanation.
        if (AGame.supplyTotal() > 10) {
            ErrorLog.printMaxOncePerMinute("Can't find place for `" + building + "`, " + order);
//                A.printStackTrace("Can't find place for `" + building + "`, " + order);
            if (AbstractPositionFinder._CONDITION_THAT_FAILED != null) {
                ErrorLog.printMaxOncePerMinute("(reason: " + AbstractPositionFinder._CONDITION_THAT_FAILED + ")");
            }
            else {
                ErrorLog.printMaxOncePerMinute("(reason not defined - bug)");
            }
        }

        ErrorLog.printMaxOncePerMinute("Cancel " + building + " (Invalid place)");
        newConstructionOrder.cancel();
//            throw new RuntimeException("Can't find place for `" + building + "` ");
        return false;
    }

    private static boolean handleRequirementsNotFullfilledFor(AUnitType building) {
        if (We.protoss() && AGame.supplyTotal() <= 10) return false;

        AUnitType requiredBuilding = building.whatIsRequired();

        if (requiredBuilding == null) {
//            System.out.println("requiredBuilding NULL for " + building);
            return false;
        }

//        if (countExistingAndNotFinished(requiredBuilding) == 0) {
        if (Count.existing(requiredBuilding) == 0 && Count.inProductionOrInQueue(requiredBuilding) == 0) {
            ConstructionRequests.requestConstructionOf(requiredBuilding);
            return true;
        }

        ErrorLog.printMaxOncePerMinute(
            "Uhmmm... shouldn't reach here. "
                + "EXISTING_BUILDING=" + Count.existing(building)
                + ", IN_PROD_BUILDING" + Count.inProductionOrInQueue(building)
                + "EXISTING_REQ=" + Count.existing(requiredBuilding)
                + ", IN_PROD_REQ" + Count.inProductionOrInQueue(requiredBuilding)
        );
        ErrorLog.printMaxOncePerMinute(building + " // " + requiredBuilding);
        return false;
    }

    /**
     * Returns ConstructionOrder object for given builder.
     */
    public static Construction constructionFor(AUnit builder) {
        for (Construction construction : constructions) {
            if (builder.equals(construction.builder())) {
                return construction;
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
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                && construction.buildingType().equals(type)) {
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

    public static Construction getNotStartedOfType(AUnitType type) {
        for (Construction construction : constructions) {
            if (
                construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                    && construction.buildingType().equals(type)
            ) {
                return construction;
            }
        }

        return null;
    }

    public static int countNotStartedOfTypeInRadius(AUnitType type, double radius, HasPosition position) {
        int total = 0;
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                && construction.buildingType().equals(type)
                && position.distTo(construction.positionToBuildCenter()) <= radius) {
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
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                && construction.buildingType().equals(type)) {
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

        return Select.ourWithUnfinished(type).count()
            + countNotFinishedOfType(type);
    }

    public static int countExistingAndPlannedInRadius(AUnitType type, double radius, APosition position) {
        if (!type.isBuilding()) {
            throw new RuntimeException("Can only use it for buildings: " + type);
        }

        return Select.ourWithUnfinished(type).inRadius(radius, position).count()
            + countNotFinishedOfTypeInRadius(type, radius, position);
    }

    /**
     * If we requested to build building A and even assigned worker who's travelling to the building site,
     * it's still doesn't count as unitCreated. We need to manually count number of constructions and only
     * then, we can e.g. "get not started Terran Barracks constructions".
     */
    public static ArrayList<Construction> notStartedOfType(AUnitType type) {
        ArrayList<Construction> notStarted = new ArrayList<>();
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED
                && (type == null || construction.buildingType().equals(type))) {
                notStarted.add(construction);
            }
        }
        return notStarted;
    }

    public static ArrayList<Construction> notStarted() {
        ArrayList<Construction> notStarted = new ArrayList<>();
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                notStarted.add(construction);
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
    public static ArrayList<Construction> all() {
        return new ArrayList<>(constructions);
    }

//    public static ArrayList<HasPosition> allConstructionOrdersIncludingCached() {
//        ArrayList<HasPosition> positions = new ArrayList<>();
//        for (ConstructionOrder order : ConstructionRequests.constructions) {
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
        for (Construction construction : notStartedOfType(null)) {
            mineralsNeeded += construction.buildingType().getMineralPrice();
            gasNeeded += construction.buildingType().getGasPrice();
        }
        int[] result = {mineralsNeeded, gasNeeded};
        return result;
    }

    protected static void removeOrder(Construction construction) {
        constructions.remove(construction);
    }

    /**
     * Top-priority request.
     */
    public static void removeAllNotStarted() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();
            if (!construction.hasStarted()) {
//                System.out.println("Removing all non started buildings due to priority request");
                construction.cancel();
            }
        }
    }

    public static boolean hasNotStartedNear(AUnitType building, HasPosition position, double inRadius) {
        for (Construction order : notStartedOfType(building)) {
            if (order.buildPosition() != null && position.distToLessThan(position, inRadius)) {
                return true;
            }
        }
        return false;
    }
}
