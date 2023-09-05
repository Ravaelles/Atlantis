package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionOrderHandler;
import atlantis.production.orders.production.ProductionQueueMode;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class ProductionOrdersCommander extends Commander {
    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    @Override
    protected void handle() {
        // Get sequence of units (Production Orders) based on current build order
        ArrayList<ProductionOrder> queue = CurrentProductionQueue.get(ProductionQueueMode.REQUIREMENTS_FULFILLED);

        for (ProductionOrder order : queue) {
            if (newBaseInProgressAndCantAffordThisOrder(order)) return;

            handleProductionOrder(order);
        }
    }

    private static void handleProductionOrder(ProductionOrder order) {
        try {
            (new ProductionOrderHandler(order)).invoke();
        } catch (Exception e) {
            CurrentProductionQueue.remove(order);
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Cancelled " + order + " as there was a problem.");
        }
    }

    private static boolean newBaseInProgressAndCantAffordThisOrder(ProductionOrder order) {
        return !A.hasMinerals(AtlantisRaceConfig.BASE.getMineralPrice() + order.mineralPrice())
            && ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.BASE) > 0;
    }
}
