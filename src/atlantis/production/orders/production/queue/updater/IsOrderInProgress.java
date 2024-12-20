package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.information.tech.IsAnyBuildingResearching;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;

import java.util.ArrayList;
import java.util.List;

public class IsOrderInProgress {
    public static boolean check(ProductionOrder order) {
        // === Unit

        if (order.unitType() != null) {
            return forUnit(order);
        }

        // === Tech

        else if (order.tech() != null) {
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

    private static boolean forUnit(ProductionOrder order) {
        List<ProductionOrder> ordersOfTheSameType = otherUnitOrdersOfTheSameType(order);
        int earlierOrdersOfTheSameType = 0;
        for (ProductionOrder otherOrder : ordersOfTheSameType) {
            if (otherOrder.minSupply() < order.minSupply()) earlierOrdersOfTheSameType++;
        }

//        if (earlierOrdersOfTheSameType > 0) {
//            if (order.unitType() != null && order.unitType().is(AUnitType.Terran_Barracks)) {
//                System.err.println("earlierOrdersOfTheSameType = " + earlierOrdersOfTheSameType + " / " + order.unitType());
//            }
//        }

        int ordersBeingProduced = Count.inProduction(order.unitType());
        int ordersInProgress = ordersBeingProduced - earlierOrdersOfTheSameType;

        return ordersInProgress > 0;
    }

    private static List<ProductionOrder> otherUnitOrdersOfTheSameType(ProductionOrder order) {
        if (order.unitType() == null) return new ArrayList<>();
        Queue queue = Queue.get();
        if (queue == null) return new ArrayList<>();

        List<ProductionOrder> list = queue.inProgressOrders().ofType(order.unitType()).exclude(order).list();

        queue.clearCache();

        return list;
    }

    private static boolean forTech(ProductionOrder order) {
        return !ATech.isResearchedWithOrder(order.tech(), order) && IsAnyBuildingResearching.tech(order.tech());
    }

    private static boolean forUpgrade(ProductionOrder order) {
        return !ATech.isResearchedWithOrder(order.upgrade(), order) && IsAnyBuildingResearching.upgrade(order.upgrade());
    }
}
