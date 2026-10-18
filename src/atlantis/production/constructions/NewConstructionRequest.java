package atlantis.production.constructions;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.dynamic.protoss.reinforce.BuildPylonFirst;
import atlantis.units.select.Select;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.requirements.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import atlantis.util.AConsole;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import bwapi.TilePosition;

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

        PositionFulfillsAllConditions.clearRefusalBreakdown();

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
//            AConsole.printStackTrace("Requested BASE");
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
//            AConsole.errPrintln("Construction had the same position, find new: " + newConstruction);
            newConstruction.findPositionForNewBuilding();
        }

        if (ConstructionRequests.alreadyExists(newConstruction, true)) {
//            ErrorLog.printMaxOncePerMinute("Cancel as construction already exists: " + newConstruction);
            newConstruction.cancel(building + " already being constructed");
//            if (order.isBuilding() && !order.unitType().isMissileTurret()) {
//            }
            // Kept (not temporary): a request refused as a duplicate is otherwise invisible,
            // and "no new construction appeared" then has two indistinguishable causes - the
            // producer was gated out, or the request was made and rejected. NEXT.md #59 could
            // not be diagnosed without this line; it fires only under BUILDER_LOG=1.
            if (atlantis.config.env.Env.builderLog()) {
                EventLog.key(LogKey.REQUEST_REFUSED_DUPLICATE)
                    .param("type", building.name())
                    .param("existing", ConstructionRequests.countAllOfType(building))
                    .log(A.now(), A.minSec());
            }
            return null;
        }
        else {
            // Add to list of pending orders
            ConstructionRequests.addConstruction(newConstruction);
            EventLog.key(LogKey.CONSTRUCTION_REQUESTED)
                .param("type", building.name())
                .param("cid", newConstruction.id())
                .param("oid", order == null ? null : order.id())
                .param("tile", newConstruction.buildPosition() == null ? "-"
                    : "[" + newConstruction.buildPosition().tx() + ","
                    + newConstruction.buildPosition().ty() + "]")
                .param("order", order == null ? "-" : order.toString())
                .param("caller", requestingCaller())
                .param("supplyUsed", A.supplyUsed())
                .param("minerals", A.minerals())
                .param("totalOfType", ConstructionRequests.countAllOfType(building))
                .log(A.now(), A.minSec());
            // Power inputs at decision time (a site the engine later refuses on
            // power passed every check we have - so record what the checks saw:
            // our rule, the engine's answer, and the pylon list it ran on.
            // Rare (once per construction), builderLog-gated like the line above.
            if (building.needsPower() && atlantis.config.env.Env.builderLog()) {
                EventLog.key(LogKey.POWER_AT_REQUEST)
                    .param("site", String.valueOf(newConstruction.buildPosition()))
                    .param("ourCheck", IsPoweredByAPylon.check(newConstruction.buildPosition(), building))
                    .param("engineHasPower", atlantis.Atlantis.game().hasPower(
                        new TilePosition(
                            newConstruction.buildPosition().tx(), newConstruction.buildPosition().ty()),
                        building.ut()))
                    .param("pylons", String.valueOf(Select.ourOfType(AUnitType.Protoss_Pylon).list()))
                    .log(A.now(), A.minSec());
            }
        }

//            AConsole.printStackTrace("AFTER ADDED");

//            AConsole.printList(constructions);

        // Rebuild production queue as new building is about to be built
//        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        Queue.get().refresh();

//        System.err.println(building + " OK! " + newConstruction.status());

        return newConstruction;
    }

    /**
     * Who asked for this request: the first stack frame outside the
     * construction plumbing. One class+method, not a trace - enough to
     * split legacy re-requests from V2/expiry paths per add (NEXT #57)
     * without the 15-line stacks the single-door used to print.
     */
    private static String requestingCaller() {
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String cls = frame.getClassName();
            if (cls.startsWith("java.lang.Thread")
                || cls.contains("NewConstructionRequest")
                || cls.contains("ConstructionRequests")) continue;
            String simple = cls.substring(cls.lastIndexOf('.') + 1);
            return simple + "." + frame.getMethodName();
        }
        return "-";
    }

    private static String dominantRefusal(AUnitType building) {
        java.util.Map<String, Integer> breakdown = building.isPylon()
            ? ProtossPositionFinder.lastRefusalBreakdown()
            : PositionFulfillsAllConditions.lastRefusalBreakdown();
        return dominantRefusal(breakdown,
            AbstractPositionFinder._STATUS == null ? "-" : AbstractPositionFinder._STATUS);
    }

    /**
     * The sweep ends wherever the rings end, so the final status names an
     * edge tile while the bulk died elsewhere. Public as a seam: the
     * choice itself pins without a game.
     */
    public static String dominantRefusal(java.util.Map<String, Integer> breakdown, String fallback) {
        String best = null;
        int bestCount = 0;
        int total = 0;
        for (java.util.Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            total += entry.getValue();
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        if (best == null) {
            return fallback == null ? "-" : fallback;
        }
        return best + " x" + bestCount + " (of " + total + ")";
    }

    /**
     * Pull-forward predicate (Phase 4): a power-dominant failure for a
     * power-needing building with an order behind it means "build the
     * pylon first". Pure except reading nothing - dominant string, type
     * flag and order presence decide. Physical failures, pylon orders
     * themselves and missing orders all answer false.
     */
    public static boolean shouldPullPylon(AUnitType building, String dominant, ProductionOrder order) {
        if (dominant == null || building == null || order == null) return false;
        if (!building.needsPower()) return false;
        return dominant.contains("power");
    }

    private static boolean invalidNullPositionSoQuit(AUnitType building, ProductionOrder order, Construction newConstruction) {        ErrorLog.printMaxOncePerMinute("[LVL=CRITICAL] " + A.minSec() + ": Can't find place for `" + building + "`, " + order);
        // Dominant refusal, not the last one: the sweep ends wherever the
        // rings end, so the final _STATUS names an edge tile, not the rule
        // that killed the search (measured: 8x Gateway "Not powered" while
        // the bulk was elsewhere).
        String dominant = dominantRefusal(building);
        EventLog.key(LogKey.PLACEMENT_FAILED)
            .param("type", building.name())
            .param("order", order == null ? "-" : order.toString())
            .param("reason", dominant == null ? "-" : dominant)
            .log(A.now(), A.minSec());

        // Owner 2026-10-17 (Phase 4): power verdict pulls a Pylon instead
        // of churning. The legacy finder never checks power, so an
        // unpowered area fails every gateway row forever (measured: 12/13
        // placement failures in a 20-min game). A pylon near the searched
        // area, at most one coming (needsPylon reads 5.3 tiles around it),
        // and the row retries on later frames. Physical failures are left
        // alone - no pylon fixes a full map.
        if (shouldPullPylon(building, dominant, order)) {
            HasPosition near = order.aroundPosition() != null
                ? order.aroundPosition()
                : Select.mainOrAnyBuilding();
            if (near != null && BuildPylonFirst.needsPylon(near)) {
                BuildPylonFirst.requestNear(near);
            }
        }
//                AConsole.printStackTrace("Can't find place for `" + building + "`, " + order);
        if (AbstractPositionFinder._STATUS != null) {
            ErrorLog.printMaxOncePerMinute("(reason: " + AbstractPositionFinder._STATUS + ")");
        }
        else {
            ErrorLog.printMaxOncePerMinute("(reason not defined - bug)");
        }

        boolean cancelled = cancelNullPositionConstructionIfNeeded(building, order);

//        System.err.println("cancelled = " + cancelled);
//        if (true) throw new RuntimeException("cancelled = " + cancelled + " / " + building);

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

    private static boolean cancelNullPositionConstructionIfNeeded(AUnitType building, ProductionOrder order) {
        boolean cancelled = false;

        if (building.isSupplyDepot()) {
            if (A.supplyTotal() > 10
                && order != null
                && (CountInQueue.count(AUnitType.Terran_Supply_Depot) >= 2 || AGame.supplyFree() >= 3)
            ) {
                cancelled = true;
                order.cancel("Null position for supply depot");
            }
        }
        else if (!building.isPylon()) {
            if (A.s >= 70 && A.supplyTotal() >= 11) {
                cancelled = true;
                order.cancel("Null position for " + building);
                AConsole.errPrintln(A.minSec() + ": Cancelled order: " + order);
            }
        }
        return cancelled;
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
