package atlantis.production.orders.production.queue.updater;

import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Counter;

public class IsOrderCompleted {
    protected static boolean isCompleted(ProductionOrder order, Counter<AUnitType> existingCounter) {
        // === Unit

        if (order.unitType() != null) {
            if (order.construction() == null) return false;
            
            return !checkIfWeHaveLessUnitsThanExpected(order, existingCounter);
        }

        // === Tech

        else if (order.tech() != null) {
            return ATech.isResearchedWithOrder(order.tech(), order);
        }

        // === Upgrade

        else if (order.upgrade() != null) {
            return ATech.isResearchedWithOrder(order.upgrade(), order);
        }

        // === Unknown

//        A.errPrintln("Unknown order type: " + order);
        return false;
    }

    private static boolean checkIfWeHaveLessUnitsThanExpected(ProductionOrder order, Counter<AUnitType> expectedCounter) {
        AUnitType type = order.unitType();
        expectedCounter.incrementValueFor(type);

        int existingUnits = existingUnitsCount(type);
        int expectedUnits = expectedUnitsCount(type, expectedCounter);

        // If we don't have this unit, add it to the current production queue.
//        if (type.is(AUnitType.Terran_Academy)) {
//            System.err.println("@" + A.now() + " EXIZ/EXPEC = " + existingUnits + " / " + expectedUnits);
//            Select.our().print("Ourz");
//            Select.ourOfType(AUnitType.Terran_Academy).print("Academies");
//        }

        return existingUnits < expectedUnits;
    }

    private static int expectedUnitsCount(AUnitType type, Counter<AUnitType> expectedCounter) {
        return ThisManyUnitsByDefault.numOfUnits(expectedCounter, type);
    }

    private static int existingUnitsCount(AUnitType type) {
        return Select.countOurOfType(type);
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
