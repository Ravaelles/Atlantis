package atlantis.production.constructing;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
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
        if (!building.isABuilding()) {
            throw new RuntimeException("Requested construction of not building!!! Type: " + building);
        }

//        if (ConstructionRequests.constructions.size() >= 15) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Too many constructions, can't add more: " + building);
//            return false;
//        }

        if (SpecificConstructionRequests.handledAsSpecialBuilding(building, order)) return true;

        if (!Requirements.hasRequirements(building)) return handleRequirementMissingFor(building);

        // =========================================================
        // Create ConstructionOrder object, assign random worker for the time being

//        System.err.println("Requested CONSTRUCTION:");
//        System.err.println( building + " / " + near + " / " + order);
//        if (building.isBase()) {
//            A.printStackTrace("Requested BASE");
//        }

        Construction newConstruction = new Construction(building);
        newConstruction.setProductionOrder(order);
        newConstruction.setNearTo(near);
        newConstruction.setMaxDistance(order != null ? order.maximumDistance() : 66);
        newConstruction.assignRandomBuilderForNow();

        if (newConstruction.builder() == null) {
            if (AGame.supplyUsed() >= 7 && Count.bases() > 0 && Count.workers() > 0) {
                ErrorLog.printMaxOncePerMinute("Builder is null, got damn it!");
            }
            return false;
        }

        // =========================================================
        // Find place for new building

        APosition positionToBuild = DefineExactPositionForNewConstruction.exactPositionForNewConstruction(
            building, order, newConstruction
        );

        // =========================================================

        // Couldn't find place for building! That's bad, print descriptive explanation.
        if (positionToBuild == null) {
            return invalidNullPositionSoQuit(building, order, newConstruction);
        }

        // =========================================================
        // Successfully found position for new building

        APositionFinder.clearCache();

        // Assign optimal builder for this building
        newConstruction.assignOptimalBuilder();

        // Add to list of pending orders
        if (ConstructionRequests.alreadyExists(newConstruction)) {
//            ErrorLog.printMaxOncePerMinute("Cancel as construction already exists: " + newConstruction);
            newConstruction.cancel();
//            if (order.isBuilding() && !order.unitType().isMissileTurret()) {
//            }
            return false;
        }
        else {
            ConstructionRequests.constructions.add(newConstruction);
        }

//            A.printStackTrace("AFTER ADDED");

//            A.printList(constructions);

        // Rebuild production queue as new building is about to be built
//        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        Queue.get().refresh();

        return true;
    }

    private static boolean invalidNullPositionSoQuit(AUnitType building, ProductionOrder order, Construction newConstruction) {
        ErrorLog.printMaxOncePerMinute("Can't find place for `" + building + "`, " + order);
//                A.printStackTrace("Can't find place for `" + building + "`, " + order);
        if (AbstractPositionFinder._CONDITION_THAT_FAILED != null) {
            ErrorLog.printMaxOncePerMinute("(reason: " + AbstractPositionFinder._CONDITION_THAT_FAILED + ")");
        }
        else {
            ErrorLog.printMaxOncePerMinute("(reason not defined - bug)");
        }

        if (
            building.isSupplyDepot()
                && A.supplyTotal() > 10
                && order != null
                && (
                CountInQueue.count(AUnitType.Terran_Supply_Depot) >= 2
                    || AGame.supplyFree() >= 3
            )
        ) order.cancel();

//        ErrorLog.printMaxOncePerMinute("(Construction: " + newConstruction + ")");

        if (order != null) {
            if (order.maximumDistance() < 0) {
                ErrorLog.printMaxOncePerMinute("(Max search distance was not defined - bug)");
            }
            else {
                ErrorLog.printMaxOncePerMinute("(Max search distance was: " + order.maximumDistance() + ")");
            }
        }

//        if (order != null) order.cancel();
        AbstractPositionFinder.clearCache();
//        newConstruction.findPositionForNewBuilding();

        return false;
    }

    private static boolean handleRequirementMissingFor(AUnitType building) {
        if (We.protoss() && AGame.supplyTotal() <= 10) return false;

        AUnitType requiredBuilding = building.whatIsRequired();

        if (requiredBuilding == null) return false;

//        System.out.println("requiredBuilding for " + building + " = " + requiredBuilding);

//        if (Count.existing(requiredBuilding) == 0 && Count.inProductionOrInQueue(requiredBuilding) == 0) {
        if (Count.existing(requiredBuilding) == 0) {
//            System.out.println("---------- NOOOOO QUIT");
            return true;
        }
//        System.out.println("OK");

        // =========================================================
//        if (Count.existing(requiredBuilding) == 0 && Count.inProductionOrInQueue(requiredBuilding) == 0) {
//            requestConstructionOf(requiredBuilding); // WTF
//            return true;
//        }
        // =========================================================

//        ErrorLog.printMaxOncePerMinute(
//            "Uhmmm... shouldn't reach here. "
//                + "\nbuilding=" + building
//                + "\nEXISTING_BUILDING=" + Count.existing(building)
//                + "\nIN_PROD_BUILDING" + Count.inProductionOrInQueue(building)
//                + "\nEXISTING_REQ=" + Count.existing(requiredBuilding)
//                + "\nIN_PROD_REQ" + Count.inProductionOrInQueue(requiredBuilding)
//        );
//        ErrorLog.printMaxOncePerMinute(building + " // " + requiredBuilding);
        return false;
    }
}
