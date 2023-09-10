package atlantis.production.constructing;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
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

//    public static int countExistingAndExpectedInNearFuture(AUnitType type, int amongNTop) {
//        if (!type.isBuilding()) {
//            throw new RuntimeException("Can only use it for buildings: " + type);
//        }
//
//        return Select.ourOfType(type).count()
//            + ProductionQueue.countInQueue(type, amongNTop);
//    }

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
        if (construction != null) {
            constructions.remove(construction);
        }
    }

    /**
     * Top-priority request.
     */
    public static void removeAllNotStarted() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();
            if (!construction.hasStarted()) {
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

    public static boolean alreadyExists(Construction newConstructionOrder) {
        for (Construction construction : constructions) {
            if (construction.equals(newConstructionOrder)) {
//                ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Construction already exists: " + newConstructionOrder);
                return true;
            }
        }
        return false;
    }
}
