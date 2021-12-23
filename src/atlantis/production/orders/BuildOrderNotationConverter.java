//package atlantis.production.orders;
//
//import atlantis.AtlantisConfig;
//import atlantis.production.ProductionOrder;
//
//import java.util.ArrayList;
//
//public class BuildOrderNotationConverter {
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
//     * @return
//     */
//    protected static ArrayList<ProductionOrder> convertNotation(ABuildOrder buildOrder) {
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
//        return newInitialQueue;
//
//        // Update sequence of production orders
////        ProductionQueue.currentProductionQueue.clear();
////        ProductionQueue.currentProductionQueue.addAll(newInitialQueue);
//
//        // Replace old initial queue with new
////        ProductionQueue.initialProductionQueue.clear();
////        ProductionQueue.initialProductionQueue.addAll(newInitialQueue);
////        ProductionQueue.currentProductionQueue.addAll(newInitialQueue);
//    }
//
//}
