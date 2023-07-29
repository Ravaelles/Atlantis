package atlantis.production.orders.production;

import atlantis.information.tech.ATech;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.MappingCounter;

import java.util.Comparator;

public class ProductionQueueRebuilder {

    /**
     * If new unit is created (it doesn't need to exist, it's enough that it's just started training) or your
     * unit is destroyed, we need to rebuild the production orders queue from the beginning (based on initial
     * queue read from file). <br />
     * This method will detect which units we lack and assign to <b>currentProductionQueue</b> list next units
     * that we need. Note this method doesn't check if we can afford them, it only sets up proper sequence of
     * next units to produce.
     */
    public static void rebuildProductionQueueToExcludeProducedOrders() {

        // Clear old production queue.
        ProductionQueue.nextInQueue.clear();

        // It will store [UnitType->(int)howMany] mapping as we gonna process initial
        // production queue and check if we currently have units needed
        MappingCounter<AUnitType> virtualCounter = new MappingCounter<>();

        // =========================================================

        for (ProductionOrder order : CurrentBuildOrder.get().productionOrders()) {
            boolean isOkayToAdd = false;

            // === Unit

            if (order.unitType() != null) {
                isOkayToAdd = addUnitOrBuildingIfDontHaveIt(order, virtualCounter);
            }

            // === Tech

            else if (order.tech() != null) {
                isOkayToAdd = !ATech.isResearchedWithOrder(order.tech(), order);
            }

            // === Upgrade

            else if (order.upgrade() != null) {
                isOkayToAdd = !ATech.isResearchedWithOrder(order.upgrade(), order);
            }

            // =========================================================

            if (isOkayToAdd) {
                ProductionQueue.nextInQueue.add(order);
//                System.err.println("Enqueue " + order + " (now " + ProductionQueue.nextInQueue.size() + ")");
                if (ProductionQueue.nextInQueue.size() >= 12) {
                    break;
                }
            }
        }

        // It may happen that due to invalid build order sequence the supply order is not maintained
        // Make sure to sort by supply needed for the order.
        ProductionQueue.nextInQueue.sort(Comparator.comparingInt(ProductionOrder::minSupply));
    }

    // =========================================================

    private static boolean addUnitOrBuildingIfDontHaveIt(ProductionOrder order, MappingCounter<AUnitType> counterFromBO) {
//    private static boolean addUnitOrBuildingIfDontHaveIt(ProductionOrder order) {
        AUnitType type = order.unitType();
        counterFromBO.incrementValueFor(type);

        int shouldHaveThisManyUnits = (type.isWorker() ? 4 : 0)
                + (type.isBase() ? (type.isPrimaryBase() ? 1 : 0) : 0)
                + (type.isOverlord() ? 1 : 0) + counterFromBO.getValueFor(type);

        int weHaveThisManyUnits = Count.existingOrInProduction(type);

        if (type.isBuilding()) {
            weHaveThisManyUnits += ConstructionRequests.countNotStartedOfType(type);
        }

        // If we don't have this unit, add it to the current production queue.
//        if (type.is(AUnitType.Protoss_Gateway)) {
//        if (type.is(AUnitType.Protoss_Zealot)) {
//            System.out.println(A.supplyUsed() + ",  " + type + " // have("
//                    + weHaveThisManyUnits + ") < need(" + shouldHaveThisManyUnits + ")");
////                    + "),   (notStarted = " + ConstructionRequests.countNotStartedConstructionsOfType(type) + ")");
//        }
        if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
            return true;
        }

        return false;
    }

}
