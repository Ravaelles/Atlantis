package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.Counter;

public class IsOrderNotCompleted {
    protected static boolean isOrderNotCompleted(ProductionOrder order, Counter<AUnitType> existingCounter) {
        // === Unit

        if (order.unitType() != null) {
            return checkIfWeHaveLessUnitsThanExpected(order, existingCounter);
        }

        // === Tech

        else if (order.tech() != null) {
            return !ATech.isResearchedWithOrder(order.tech(), order);
        }

        // === Upgrade

        else if (order.upgrade() != null) {
            return !ATech.isResearchedWithOrder(order.upgrade(), order);
        }

        // === Unknown

        A.errPrintln("Unknown order type: " + order);
        return true;
    }

    private static boolean checkIfWeHaveLessUnitsThanExpected(ProductionOrder order, Counter<AUnitType> existingCounter) {
        AUnitType type = order.unitType();
        existingCounter.incrementValueFor(type);

//        int existingUnits = existingOrInProgressUnitsCount(type);
        int existingUnits = existingUnitsCount(type);
        int expectedUnits = expectedUnitsCount(type, existingCounter);

        // If we don't have this unit, add it to the current production queue.
//        if (type.is(AUnitType.Terran_Supply_Depot)) {
//            System.err.println("SupplyDepotz = " + existingUnits + " / " + expectedUnits);
//        }

        return existingUnits < expectedUnits;
    }

    private static int expectedUnitsCount(AUnitType type, Counter<AUnitType> existingCounter) {
        return ThisManyUnitsByDefault.numOfUnits(existingCounter, type);
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
