package atlantis.production.constructions;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConstructionRequests {
    /**
     * List of all unfinished (started or pending) constructions.
     */
    public static ConcurrentLinkedQueue<Construction> constructions = new ConcurrentLinkedQueue<>();

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
            if (construction.status() == ConstructionOrderStatus.NOT_STARTED
                && construction.buildingType().equals(type)) {
                total++;
            }
        }

        // =========================================================
        // Special case for Overlord
        if (We.zerg() && type.equals(AUnitType.Zerg_Overlord)) {
            total += Select.ourUnfinished().ofType(type).count();
        }

        return total;
    }

    public static int countNotFinishedWithHighPriority() {
        int total = 0;
        for (Construction construction : constructions) {
            if (construction.productionOrder().priority().isHighOrHigher() && construction.notFinished()) {
                total++;
            }
        }

        return total;
    }

    public static int countAllOfType(AUnitType type) {
        int total = 0;
        for (Construction construction : constructions) {
            if (construction.buildingType().equals(type)) {
                total++;
            }
        }

        return total;
    }

    public static Construction getNotStartedOfType(AUnitType type) {
        for (Construction construction : constructions) {
            if (
                construction.status() == ConstructionOrderStatus.NOT_STARTED
                    && construction.buildingType().equals(type)
            ) {
                return construction;
            }
        }

        return null;
    }

    public static Construction getNotFinishedOfType(AUnitType type) {
        for (Construction construction : constructions) {
            if (
                construction.status() == ConstructionOrderStatus.IN_PROGRESS
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
            if (construction.status() == ConstructionOrderStatus.NOT_STARTED
                && construction.buildingType().equals(type)
                && construction.positionToBuildCenter() != null
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

    public static int countNotFinishedOfTypeInRadius(AUnitType type, double radius, HasPosition position) {
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
            if (construction.status() == ConstructionOrderStatus.IN_PROGRESS
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

//    public static int countExistingAndExpectedInNearFuture(AUnitType type, int amongNTop) {
//        if (!type.isBuilding()) {
//            throw new RuntimeException("Can only use it for buildings: " + type);
//        }
//
//        return Select.ourOfType(type).count()
//            + ProductionQueue.countInQueue(type, amongNTop);
//    }

    public static int countExistingAndNotFinished(AUnitType type) {
        if (!type.isABuilding()) {
            throw new RuntimeException("Can only use it for buildings: " + type);
        }

        return Select.ourWithUnfinished(type).count()
            + countNotFinishedOfType(type);
    }

    public static int countExistingAndPlannedInRadius(AUnitType type, double radius, HasPosition position) {
        if (!type.isABuilding()) {
            throw new RuntimeException("Can only use it for buildings: " + type);
        }

        return Select.ourWithUnfinished(type).inRadius(radius, position).count()
            + countNotFinishedOfTypeInRadius(type, radius, position);
    }

    /**
     * If we requested to build building A and even assigned worker who's travelling to the building site,
     * it still doesn't count as unitCreated. We need to manually count number of constructions and only
     * then, we can e.g. "get not started Terran Barracks constructions".
     */
    public static ArrayList<Construction> notStartedOfType(AUnitType type) {
        ArrayList<Construction> notStarted = new ArrayList<>();
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.NOT_STARTED
                && (type == null || construction.buildingType().equals(type))) {
                notStarted.add(construction);
            }
        }
        return notStarted;
    }

    public static ArrayList<Construction> notFinishedOfType(AUnitType type) {
        ArrayList<Construction> notStarted = new ArrayList<>();
        for (Construction construction : constructions) {
            if (construction.status() != ConstructionOrderStatus.FINISHED
                && (type == null || construction.buildingType().equals(type))) {
                notStarted.add(construction);
            }
        }
        return notStarted;
    }

    public static ArrayList<Construction> notStarted() {
        ArrayList<Construction> notStarted = new ArrayList<>();
        for (Construction construction : constructions) {
            if (construction.status() == ConstructionOrderStatus.NOT_STARTED) {
                notStarted.add(construction);
            }
        }
        return notStarted;
    }

    public static ArrayList<Construction> notFinished() {
        ArrayList<Construction> notFinished = new ArrayList<>();
        for (Construction construction : constructions) {
            if (construction.status() != ConstructionOrderStatus.FINISHED) {
                notFinished.add(construction);
            }
        }
        return notFinished;
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
            mineralsNeeded += construction.buildingType().mineralPrice();
            gasNeeded += construction.buildingType().gasPrice();
        }
        int[] result = {mineralsNeeded, gasNeeded};
        return result;
    }

    public static void removeOrder(Construction construction) {
        if (construction != null) {
            constructions.remove(construction);
        }
    }

    /**
     * Top-priority request.
     */
    public static void removeAllNotStarted(String reason) {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();
            if (!construction.hasStarted()) {
                construction.cancel(reason);
            }
        }
    }

    public static boolean hasNotStarted(AUnitType building) {
        return !notStartedOfType(building).isEmpty();
    }

    public static boolean hasNotStartedNear(AUnitType building, HasPosition position, double inRadius) {
        for (Construction order : notStartedOfType(building)) {
            if (order.buildPosition() != null && position.distToLessThan(position, inRadius)) {
                return true;
            }
        }
        return false;
    }

    public static APosition nearestOfTypeTo(AUnitType building, HasPosition position, double max) {
        assert position != null;

        for (Construction order : notFinishedOfType(building)) {
            if (
                order != null
                    && order.buildPosition() != null
                    && position.distToLessThan(order.buildPosition(), max)
            ) {
                return order.buildPosition();
            }
        }
        return null;
    }

    public static boolean alreadyExists(Construction newConstructionOrder, boolean allowPrint) {
        for (Construction construction : constructions) {
            if (
                !construction.equals(newConstructionOrder)
                    && construction.sameAs(newConstructionOrder)
                    && construction.positionToBuildCenter().distTo(newConstructionOrder.positionToBuildCenter()) <= 6
            ) {
                if (allowPrint) {
//                    A.errPrintln("Cancel same construction: " + construction.buildingType());
//                    A.errPrintln("A (old): " + construction);
//                    A.errPrintln("B (new): " + newConstructionOrder);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isBeingBuilt(AUnitType type) {
        return !notFinishedOfType(type).isEmpty();
    }
}
