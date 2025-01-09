package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.production.constructions.ConstructionRequests;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceOrdersFromQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProductionOrdersCommander extends Commander {
    @Override
    public boolean applies() {
        if (A.isUms() && !A.hasMinerals(350)) return false;

        return Count.workers() > 0 && Select.ourBuildings().notEmpty();
    }

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    @Override
    protected void handle() {
        if (A.everyNthGameFrame(GamePhase.isEarlyGame() ? 19 : 71)) Queue.get().refresh();

        for (ProductionOrder order : Queue.get().readyToProduceOrders().list()) {
            if (newBaseInProgressAndCantAffordThisOrder(order)) return;

            ProduceOrdersFromQueue.handleProductionOrder(order);
        }
    }

    private static boolean newBaseInProgressAndCantAffordThisOrder(ProductionOrder order) {
        return !A.hasMinerals(AtlantisRaceConfig.BASE.mineralPrice() + order.mineralPrice())
            && ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.BASE) > 0;
    }
}
