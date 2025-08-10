package atlantis.production.orders.production.queue.updater;

import atlantis.information.tech.ATech;
import atlantis.information.tech.IsAnyBuildingResearching;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class IsOrderInProgress {
    public static boolean isInProgress(ProductionOrder order) {
        // === Unit

        // For units this will happen in OnOurUnitCreated

//        if (order.unitType() != null) {
//            return forUnit(order);
//        }

        // === Tech

        if (order.tech() != null) {
            return forTech(order);
        }

        // === Upgrade

        else if (order.upgrade() != null) {
            return forUpgrade(order);
        }

        // === Unknown

//        A.errPrintln("Unknown order type: " + order);
        return false;
    }

    // @Deprecated
//    private static boolean forUnit(ProductionOrder order) {
//        int other = countOtherOfTheSameType(order);
//        int existing = countExisting(order.unitType());
//        int inProgress = existing - other;
//
//        return inProgress > 0;
//    }

    private static int countOtherOfTheSameType(ProductionOrder order) {
        return otherUnitOrdersOfTheSameType(order).size();

//        Orders ordersOfTheSameType = otherUnitOrdersOfTheSameType(order);
//
//        int earlierOrdersOfTheSameType = 0;
//        for (ProductionOrder otherOrder : ordersOfTheSameType.list()) {
//            if (otherOrder.minSupply() < order.minSupply()) earlierOrdersOfTheSameType++;
//        }
//
////        if (earlierOrdersOfTheSameType > 0) {
////            if (order.unitType() != null && order.unitType().is(AUnitType.Terran_Barracks)) {
////                System.err.println("earlierOrdersOfTheSameType = " + earlierOrdersOfTheSameType + " / " + order.unitType());
////            }
////        }
//        return earlierOrdersOfTheSameType;
    }

    private static int countExisting(AUnitType type) {
        return Select.countOurOfTypeWithUnfinished(type);
    }

    private static Orders otherUnitOrdersOfTheSameType(ProductionOrder order) {
        if (order.unitType() == null) return new Orders();
        Queue queue = Queue.get();
        if (queue == null) return new Orders();

        queue.clearCache();

        Orders orders = queue.finishedOrInProgress().ofType(order.unitType()).exclude(order);

//        if (!orders.isEmpty()) System.err.println(order.unitType() + " / list.size = " + orders.size());

        return orders;
    }

    private static boolean forTech(ProductionOrder order) {
        return !ATech.isResearchedWithOrder(order.tech(), order) && IsAnyBuildingResearching.tech(order.tech());
    }

    private static boolean forUpgrade(ProductionOrder order) {
        return !ATech.isResearchedWithOrder(order.upgrade(), order) && IsAnyBuildingResearching.upgrade(order.upgrade());
    }
}
