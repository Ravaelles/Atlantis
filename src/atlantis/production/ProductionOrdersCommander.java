package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.production.orders.production.ProductionOrderHandler;
import atlantis.production.orders.production.ProductionQueueMode;
import atlantis.units.AUnitType;

import java.util.ArrayList;

public class ProductionOrdersCommander extends Commander {
    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    @Override
    protected void handle() {
        // Get sequence of units (Production Orders) based on current build order
        ArrayList<ProductionOrder> queue = CurrentProductionQueue.ordersToProduceNow(
            ProductionQueueMode.WITH_REQUIREMENTS_FULFILLED
        );

        for (ProductionOrder order : queue) {
            AUnitType base = AtlantisRaceConfig.BASE;

            if (ConstructionRequests.countNotStartedOfType(base) > 0) {
                if (!A.hasMinerals(base.getMineralPrice() + order.mineralPrice())) {
                    return;
                }
            }

            try {
                (new ProductionOrderHandler(order)).invoke();
            } catch (Exception e) {
                CurrentProductionQueue.remove(order);
                System.err.println("Cancelled " + order + " as there was a problem with it.");
                throw e;
            }
        }
    }
}
