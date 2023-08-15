package atlantis.production.constructing;

import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionQueueRebuilder;
import atlantis.production.orders.production.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class NewConstructionRequest {
    /**
     * Issues request of constructing new building. It will automatically find position and builder unit for it.
     */
    public static boolean requestConstructionOf(AUnitType building) {
        return requestConstructionOf(building, null, null);
    }

    public static boolean requestConstructionOf(AUnitType building, HasPosition near) {
        return requestConstructionOf(building, near, null);
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

//        if (ConstructionRequests.constructions.size() >= 15) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Too many constructions, can't add more: " + building);
//            return false;
//        }

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
        newConstructionOrder.setPositionToBuild(positionToBuild);

        // Couldn't find place for building! That's bad, print descriptive explanation.
        if (positionToBuild == null) {
            ErrorLog.printMaxOncePerMinute("Can't find place for `" + building + "`, " + order);
//                A.printStackTrace("Can't find place for `" + building + "`, " + order);
            if (AbstractPositionFinder._CONDITION_THAT_FAILED != null) {
                ErrorLog.printMaxOncePerMinute("(reason: " + AbstractPositionFinder._CONDITION_THAT_FAILED + ")");
            }
            else {
                ErrorLog.printMaxOncePerMinute("(reason not defined - bug)");
            }

            newConstructionOrder.cancel();
            return false;
        }

        // =========================================================
        // Successfully found position for new building

        APositionFinder.clearCache();

        // Assign optimal builder for this building
        newConstructionOrder.assignOptimalBuilder();

        // Add to list of pending orders
        if (ConstructionRequests.alreadyExists(newConstructionOrder)) {
            newConstructionOrder.cancel();
            return false;
        }
        else {
            ConstructionRequests.constructions.add(newConstructionOrder);
        }

//            A.printStackTrace("AFTER ADDED");
//            System.out.println("# ADDED, constructions = ");
//            A.printList(constructions);

        // Rebuild production queue as new building is about to be built
        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();

        return true;
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
            requestConstructionOf(requiredBuilding);
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
}
