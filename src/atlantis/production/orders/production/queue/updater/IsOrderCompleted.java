package atlantis.production.orders.production.queue.updater;

import atlantis.information.tech.ATech;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Counter;

public class IsOrderCompleted {
    protected static boolean isCompleted(ProductionOrder order, Counter<AUnitType> existingCounter) {
        // === Building

        // A building order is done when its building stands completed - not
        // when the warp starts (Protoss construction flips FINISHED at warp
        // start, long before the building is up) and not never (the old
        // shape: this method answered for tech/upgrades only, so every
        // building order sat IN_PROGRESS forever, clogging counts and the
        // production log with buildings that finished minutes ago).
        // Units keep the old path (their training completes through
        // producers, untouched here).
        if (order.unitType() != null && order.unitType().isABuilding()) {
            if (order.construction() == null) return completedByExistence(order);
            // Linked but the engine reports no building unit (OpenBW: buildUnit
            // is always null): only fall back when no work is active, else a
            // warping Nexus-2 would complete against its standing main.
            if (order.construction().buildingUnit() == null) {
                return isConstructionActive(order.construction())
                    ? false
                    : completedByExistence(order);
            }

            return order.construction().buildingUnit().isCompleted();
        }

        // === Unit

        // For units this will happen in OnOurUnitCompleted
//        if (order.unitType() != null) {
//            if (order.construction() == null) return false;
//            if (order.construction().buildingUnit() == null) return false;
//            if (!order.construction().buildingUnit().isCompleted()) return false;
//
//            System.err.println(order + " construction unit = " + order.construction().buildingUnit());
//
//            return !checkIfWeHaveLessUnitsThanExpected(order, existingCounter);
//        }

        // === Tech

        if (order.tech() != null) {
            return ATech.isResearchedWithOrder(order.tech(), order);
        }

        // === Upgrade

        else if (order.upgrade() != null) {
            return ATech.isResearchedWithOrder(order.upgrade(), order);
        }

        // === Unknown

//        ErrorLog.printMaxOncePerMinute("@@@@@@@@@@@ Unknown order type: " + order);
        return false;
    }

    /**
     * Work is active while the construction runs (started, warping) or a
     * living builder is assigned to a not-started one (walking to the site).
     * Anything else (finished warp, dead or no builder) is orphaned: the
     * order tracks no specific future building, so existence decides.
     */
    private static boolean isConstructionActive(Construction construction) {
        if (construction.status() == ConstructionOrderStatus.FINISHED) return false;
        // Nobody works on it: dead, missing, or never-assigned builder. A
        // finished building elsewhere then satisfies the row by existence,
        // so built rows leave the next-orders list (owner 2026-10-14).
        if (construction.builder() == null || !construction.builder().isAlive()) return false;
        // Living builder, any non-finished status: walking, issuing, warping.
        return true;
    }

    /**
     * A building order with no linked Construction - or a linked one with no
     * work active (game 172954: build-order rows built by the legacy path,
     * which never links one; game 174710: linked Assimilator whose unit the
     * engine never reports) - is done when its building stands completed.
     * Same-type rows are fungible but counted: more standing buildings than
     * already-finished rows finishes this one, so two Gateway rows with one
     * standing Gateway finish exactly one.
     */
    private static boolean completedByExistence(ProductionOrder order) {
        return completedOfType(order.unitType()) > finishedOrdersOfType(order.unitType());
    }

    private static int completedOfType(AUnitType type) {
        return Select.ourOfType(type).onlyCompleted().count();
    }

    private static int finishedOrdersOfType(AUnitType type) {
        if (Queue.get() == null) return 0;
        int total = 0;
        for (ProductionOrder finished : Queue.get().finishedOrders().list()) {
            if (type.equals(finished.unitType())) total++;
        }
        return total;
    }

    private static boolean checkIfWeHaveLessUnitsThanExpected(ProductionOrder order, Counter<AUnitType> expectedCounter) {
        AUnitType type = order.unitType();
        expectedCounter.incrementValueFor(type);

        int existingUnits = existingCompletedUnitsCount(type);
        if (existingUnits == 0) return false;

        int expectedUnits = expectedUnitsCount(type, expectedCounter);

//        if (type.is(AUnitType.Protoss_Pylon)) {
//            System.err.println(A.minSec() + " EXIZ/EXPEC = " + existingUnits + " / " + expectedUnits);
//
////            Queue.get().completedOrders().print("Completed orders at supply: " + A.supplyUsed());
////            Select.our().print("Ourz");
////            Select.ourOfType(AUnitType.Protoss_Pylon).print("Ourssssssss");
//        }

        // If we don't have this unit, add it to the current production queue.
        return existingUnits < expectedUnits;
    }

    private static int expectedUnitsCount(AUnitType type, Counter<AUnitType> expectedCounter) {
//        return ThisManyUnitsByDefault.numOfUnits(type) + expectedCounter.getValueFor(type);
        return ThisManyUnitsByDefault.numOfUnits(type)
            + Queue.get().finishedOrInProgress().size();
    }

    private static int existingCompletedUnitsCount(AUnitType type) {
        return Select.countOurOfTypeWithUnfinished(type);
    }
}
