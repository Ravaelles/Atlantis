package atlantis.production.orders;

import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;

public class ProductionQueueRebuilder {

    /**
     * If new unit is created (it doesn't need to exist, it's enough that it's just started training) or your
     * unit is destroyed, we need to rebuild the production orders queue from the beginning (based on initial
     * queue read from file). <br />
     * This method will detect which units we lack and assign to <b>currentProductionQueue</b> list next units
     * that we need. Note this method doesn't check if we can afford them, it only sets up proper sequence of
     * next units to produce.
     */
    public static void rebuildProductionQueue() {

        // Clear old production queue.
        ProductionQueue.currentProductionQueue.clear();

        // It will store [UnitType->(int)howMany] mapping as we gonna process initial
        // production queue and check if we currently have units needed
        MappingCounter<AUnitType> virtualCounter = new MappingCounter<>();

        // =========================================================

        for (ProductionOrder order : ProductionQueue.get().productionOrders()) {
            boolean isOkayToAdd = false;

            // === Unit ========================================

            if (order.getUnitOrBuilding() != null) {
                isOkayToAdd = addUnitOrBuilding(order, virtualCounter);
            }

            // === Tech ========================================

            else if (order.getTech() != null) {
                isOkayToAdd = !ATech.isResearched(order.getTech(), order);
            }

            // === Upgrade ========================================

            else if (order.getUpgrade() != null) {
                isOkayToAdd = !ATech.isResearched(order.getUpgrade(), order);
            }

            // =========================================================

            if (isOkayToAdd) {
                ProductionQueue.currentProductionQueue.add(order);
                if (ProductionQueue.currentProductionQueue.size() >= 15) {
                    break;
                }
            }
        }
    }

    // =========================================================

    private static boolean addUnitOrBuilding(ProductionOrder order, MappingCounter<AUnitType> virtualCounter) {
        AUnitType type = order.getUnitOrBuilding();
        virtualCounter.incrementValueFor(type);

        int shouldHaveThisManyUnits = (type.isWorker() ? 4 : 0)
                + (type.isBase() ? (type.isPrimaryBase() ? 1 : 0) : 0)
                + (type.isOverlord() ? 1 : 0) + virtualCounter.getValueFor(type);

        int weHaveThisManyUnits = Count.unitsOfGivenTypeOrSimilar(type);

        if (type.isBuilding()) {
            weHaveThisManyUnits += AConstructionRequests.countNotFinishedConstructionsOfType(type);
        }

        // If we don't have this unit, add it to the current production queue.
        if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
            return true;
        }

        return false;
    }
}
