package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.ConstructionRequests;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderHandler;
import atlantis.util.log.ErrorLog;

public class ProductionOrdersCommander extends Commander {
    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    @Override
    protected void handle() {
        // Get sequence of units (Production Orders) based on current build order
//        ArrayList<ProductionOrder> queue = CurrentProductionQueue.get(ProductionQueueMode.REQUIREMENTS_FULFILLED);

        for (ProductionOrder order : Queue.get().readyToProduceOrders().list()) {
            if (newBaseInProgressAndCantAffordThisOrder(order)) return;

            handleProductionOrder(order);
        }
    }

    private static void handleProductionOrder(ProductionOrder order) {
        try {
            (new ProductionOrderHandler(order)).invoke();
        } catch (Exception e) {
            order.setStatus(OrderStatus.COMPLETED);
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Cancelled " + order + " as there was: " + e.getClass());
            ErrorLog.printMaxOncePerMinute("Cancelled " + order + " as there was: " + e.getClass());
            e.printStackTrace();
        }
    }

    private static boolean newBaseInProgressAndCantAffordThisOrder(ProductionOrder order) {
        return !A.hasMinerals(AtlantisRaceConfig.BASE.getMineralPrice() + order.mineralPrice())
            && ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.BASE) > 0;
    }
}
