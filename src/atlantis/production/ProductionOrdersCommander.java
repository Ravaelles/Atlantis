package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.production.constructing.ConstructionRequests;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderHandler;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class ProductionOrdersCommander extends Commander {
    @Override
    public boolean applies() {
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

            handleProductionOrder(order);
        }
    }

    private static void handleProductionOrder(ProductionOrder order) {
        try {
            (new ProductionOrderHandler(order)).invokeCommander();
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
