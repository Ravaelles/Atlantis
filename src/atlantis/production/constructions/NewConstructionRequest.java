package atlantis.production.constructions;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.requirements.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class NewConstructionRequest {
    /**
     * Issues request of constructing new building. It will automatically find position and builder unit for it.
     */
    public static Construction requestConstructionOf(AUnitType building) {
        return requestConstructionOf(building, null, null);
    }

    public static Construction requestConstructionOf(AUnitType building, HasPosition near) {
        return requestConstructionOf(building, near, null);
    }

    public static Construction requestConstructionOf(ProductionOrder order) {
        return requestConstructionOf(order.unitType(), order.aroundPosition(), order);
    }

    /**
     * Find a place for new building and assign a worker.
     * <p>
     * WARNING: passed order parameter can later override nearTo parameter.
     */
    public static Construction requestConstructionOf(AUnitType building, HasPosition near, ProductionOrder order) {

        // Validate
        if (!building.isABuilding()) {
            throw new RuntimeException("Requested construction of not building!!! Type: " + building);
        }

//        if (ConstructionRequests.constructions.size() >= 15) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Too many constructions, can't add more: " + building);
//            return false;
//        }

        if (SpecificConstructionRequests.handledAsSpecialBuilding(building, order) != null) return order.construction();

//        System.err.println("Requested CONSTRUCTION:");
//        System.err.println(building + " / " + near + " / " + order);

        if (!Requirements.hasRequirements(building)) {
//            System.err.println("------- NO REQUIREMENTS");
            handleRequirementMissingFor(building);
            return null;
        }

        // =========================================================
        // Create ConstructionOrder object, assign random worker for the time being

//        if (building.isBase()) {
//            A.printStackTrace("Requested BASE");
//        }

//        if (near == null) {
//            near = DefineNearTo.defineNearTo(building, null);
//        }

//        if (near == null) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("NearTo is null for " + order + ", god damn it!");
//            return false;
//        }

        Construction newConstruction = new Construction(building);
        newConstruction.setProductionOrder(order);
        newConstruction.setNearTo(near);
        newConstruction.setMaxDistance(order != null ? order.maximumDistance() : 66);
        newConstruction.assignRandomBuilderForNow();

        if (newConstruction.builder() == null) {
            if (AGame.supplyUsed() >= 7 && Count.bases() > 0 && Count.workers() > 0) {
                ErrorLog.printMaxOncePerMinute("Builder is null, god damn it!");
            }
            return null;
        }

        // =========================================================
        // Find place for new building

        APosition positionToBuild = DefineExactPositionForNewConstruction.exactPositionForNewConstruction(
            building, order, newConstruction
        );

        // =========================================================

        // Couldn't find place for building! That's bad, print descriptive explanation.
        if (positionToBuild == null) {
            invalidNullPositionSoQuit(building, order, newConstruction);
            return null;
        }

        // =========================================================
        // Successfully found position for new building

        APositionFinder.clearCache();

        // Assign optimal builder for this building
        newConstruction.assignOptimalBuilder();

        if (ConstructionRequests.alreadyExists(newConstruction, false)) {
            Select.clearCache();
            Construction.clearCache();
            APositionFinder.clearCache();
//            A.errPrintln("Construction had the same position, find new: " + newConstruction);
            newConstruction.findPositionForNewBuilding();
        }

        if (ConstructionRequests.alreadyExists(newConstruction, true)) {
//            ErrorLog.printMaxOncePerMinute("Cancel as construction already exists: " + newConstruction);
            newConstruction.cancel(building + " already being constructed");
//            if (order.isBuilding() && !order.unitType().isMissileTurret()) {
//            }
            return null;
        }
        else {
            // Add to list of pending orders
            ConstructionRequests.constructions.add(newConstruction);
        }

//            A.printStackTrace("AFTER ADDED");

//            A.printList(constructions);

        // Rebuild production queue as new building is about to be built
//        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        Queue.get().refresh();

//        System.err.println(building + " OK! " + newConstruction.status());

        return newConstruction;
    }

    private static boolean invalidNullPositionSoQuit(AUnitType building, ProductionOrder order, Construction newConstruction) {
        ErrorLog.printMaxOncePerMinute(A.minSec() + ": Can't find place for `" + building + "`, " + order);
//                A.printStackTrace("Can't find place for `" + building + "`, " + order);
        if (AbstractPositionFinder._STATUS != null) {
            ErrorLog.printMaxOncePerMinute("(reason: " + AbstractPositionFinder._STATUS + ")");
        }
        else {
            ErrorLog.printMaxOncePerMinute("(reason not defined - bug)");
        }

        boolean cancelled = false;

        if (building.isSupplyDepot()) {
            if (A.supplyTotal() > 10
                && order != null
                && (CountInQueue.count(AUnitType.Terran_Supply_Depot) >= 2 || AGame.supplyFree() >= 3)
            ) {
                cancelled = true;
                order.cancel("Invalid position for supply depot");
            }
        }
        else if (!building.isPylon()) {
            if (A.s >= 70 && A.supplyUsed() >= 11) {
                cancelled = true;
                order.cancel("Invalid position for " + building);
                A.errPrintln(A.minSec() + ": Cancelled order: " + order);
            }
        }

        // =========================================================

//        ErrorLog.printMaxOncePerMinute("(Construction: " + newConstruction + ")");

        if (order != null) {
            if (order.maximumDistance() < 0) {
                ErrorLog.printMaxOncePerMinute("(Max search distance was not defined - bug)");
            }
            else {
                ErrorLog.printMaxOncePerMinute("(Max search distance was: " + order.maximumDistance() + ") near " + order.aroundPosition());
            }
        }

//        if (order != null) order.cancel();
        AbstractPositionFinder.clearCache();

        if (!cancelled) {
            newConstruction.findPositionForNewBuilding();
        }

        // =========================================================

//        if (building.isGasBuilding() && Count.existing(building) <= 1 && Count.bases() >= 2) {
//            ProductionOrder newOrder = AddToQueue.withStandardPriority(building);
//            newOrder.setMinSupply(A.supplyUsed() + 3);
//        }

        // =========================================================

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
