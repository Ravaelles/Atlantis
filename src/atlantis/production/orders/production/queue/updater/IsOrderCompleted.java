package atlantis.production.orders.production.queue.updater;

import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Counter;

public class IsOrderCompleted {
    protected static boolean isCompleted(ProductionOrder order, Counter<AUnitType> existingCounter) {
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
//
//    private static int existingOrInProgressUnitsCount(AUnitType type) {
//        int weHaveThisManyUnits = Count.existingOrInProduction(type);
//        if (type.isBuilding()) {
//            weHaveThisManyUnits += ConstructionRequests.countNotStartedOfType(type);
//        }
////        System.err.println("weHaveThisManyUnits = " + weHaveThisManyUnits + " / " + type);
//        return weHaveThisManyUnits;
//    }
}
