package atlantis.production.orders.production.queue.updater;

import atlantis.information.tech.ATech;
import atlantis.information.tech.IsAnyBuildingResearching;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.AConsole;

public class IsOrderInProgress {
    public static boolean isInProgress(ProductionOrder order) {
        // === Unit

        // Enabled again (2026-10-04, B-9). It used to be commented out with "for
        // units this will happen in OnOurUnitCreated", and that event only ever
        // *set* the status: nothing checked it afterwards, so the next
        // Queue.refresh() asked IsReadyToProduceOrder, found no linked
        // Construction, and turned an unfinished building's order back into
        // READY_TO_PRODUCE - duplicate production for a building that was still
        // rising. Progress is now derived from the units themselves, which is the
        // question the queue can answer without a Construction link.
        if (order.unitType() != null) {
            return forUnit(order);
        }

        // === Tech

        if (order.tech() != null) {
            return forTech(order);
        }

        // === Upgrade

        else if (order.upgrade() != null) {
            return forUpgrade(order);
        }

        // === Unknown

//        AConsole.errPrintln("Unknown order type: " + order);
        return false;
    }

    /**
     * Is the unit this order asks for already on the way?
     *
     * <p>Counted, not linked: we have N units of this type, and M other orders of
     * the same type are finished or in progress, so this order is in progress when
     * N &gt; M. A building at 3 hit points with most of its build time left counts
     * - {@link Select#countOurOfTypeWithUnfinished} is what makes the difference
     * between "exists" and "finished".</p>
     */
    private static boolean forUnit(ProductionOrder order) {
        int other = countOtherOfTheSameType(order);
        int existing = countExisting(order.unitType());
        int inProgress = existing - other;

        return inProgress > 0;
    }

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
