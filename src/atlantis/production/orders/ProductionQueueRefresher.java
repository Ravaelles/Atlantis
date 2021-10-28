package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.Count;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;

import java.util.ArrayList;

public class ProductionQueueRefresher {

//    public static void applyNow(ABuildOrder buildOrder) {
//        buildFullBuildOrderSequenceBasedOnRawOrders();
//    }
//
//    // =========================================================
//
//    /**
//     * Converts (and repeats if needed) shortcut build order notations like:
//     6 - Barracks
//     8 - Supply Depot
//     8 - Marine - x2
//     Marine - x3
//     15 - Supply Depot
//
//     To full build order sequence like this:
//     - SCV
//     - SCV
//     - Barracks
//     - SCV
//     - Supply Depot
//     - Marine
//     - Marine
//     - Marine
//     - Marine
//     - Marine
//     - SCV
//     - SCV
//     - SCV
//     - SCV
//     - Supply Depot
//     */
//    protected static void buildFullBuildOrderSequenceBasedOnRawOrders() {
//        ArrayList<ProductionOrder> newInitialQueue = new ArrayList<>();
//
////        System.out.println();
////        System.out.println();
////        System.out.println("Initial queue");
////        for (ProductionOrder productionOrder : initialProductionQueue) {
////            System.out.print(productionOrder.getRawFirstColumnInFile() + ":  ");
////            System.out.println(productionOrder.shortName());
////        }
////        System.out.println();
////        System.out.println();
//
//        ABuildOrder buildOrder = ProductionQueue.get();
//
//        int lastSupplyFromFile = -1;
//        for (int currentSupply = 4; currentSupply <= 200; currentSupply++) {
//
//            // If no more orders left, exit the loop
//            if (buildOrder.productionOrders().isEmpty()) {
//                break;
//            }
//
//            ProductionOrder order = buildOrder.productionOrders().get(0);
//
//            // === Check if should worker build order ========================================
//
//            int orderSupplyRequired;
//            try {
//                orderSupplyRequired = Integer.parseInt(order.getRawFirstColumnInFile());
//            }
//            catch (NumberFormatException e) {
//                orderSupplyRequired = lastSupplyFromFile + 1; // Take last order supply value and increment it
//            }
//            lastSupplyFromFile = orderSupplyRequired;
//
//            // =========================================================
//
//            // Insert additional worker build order
//            if (orderSupplyRequired < 0 || currentSupply < orderSupplyRequired) {
//                ProductionOrder workerOrder = new ProductionOrder(AtlantisConfig.WORKER);
//                newInitialQueue.add(workerOrder);
//            }
//
//            // Add build order from file
//            else {
////            System.out.println("NAME: " + order.shortName());
////            System.out.println("MODIFIER: " + order.getModifier());
//
//                if (order.getModifier() != null && order.getModifier().charAt(0) == 'x' && order.getUpgrade() == null) {
//                    int timesToMultiply = 1;
//                    if (order.getModifier() != null) {
//                        timesToMultiply = Integer.parseInt(order.getModifier().substring(1)) - 1;
//                    }
//                    for (int multiplyCounter = 0; multiplyCounter < timesToMultiply; multiplyCounter++) {
//                        ProductionOrder newOrder = order.copy();
//                        newInitialQueue.add(newOrder);
//                    }
//                }
//
//                ProductionOrder newOrder = order.copy();
//                // @TODO - WHAT THE FUCK
////                ProductionQueue.initialProductionQueue.remove(0);
//                newInitialQueue.add(newOrder);
//            }
//        }
//
//        // Update sequence of production orders
//        ProductionQueue.currentProductionQueue.clear();
//        ProductionQueue.currentProductionQueue.addAll(newInitialQueue);
//
//        // Replace old initial queue with new
////        ProductionQueue.initialProductionQueue.clear();
////        ProductionQueue.initialProductionQueue.addAll(newInitialQueue);
////        ProductionQueue.currentProductionQueue.addAll(newInitialQueue);
//    }

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
                    isOkayToAdd = true;
                }
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
}
