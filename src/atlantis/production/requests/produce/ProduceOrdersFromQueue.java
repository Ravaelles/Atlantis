package atlantis.production.requests.produce;

import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderHandler;
import atlantis.util.log.ErrorLog;

public class ProduceOrdersFromQueue {
    public static void handleProductionOrder(ProductionOrder order) {
        try {
            (new ProductionOrderHandler(order)).invokedCommander();
        } catch (Exception e) {
            order.setStatus(OrderStatus.READY_TO_PRODUCE);
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Cancelled " + order + " as there was: " + e.getClass());
            // Measured GAME_9FC7B9A3: the raw e.printStackTrace() below fired
            // ~5000 times per game (once per frame per failing order) while
            // the message above was already throttled to once per minute.
            // A per-frame stack is log spam, not diagnostics.
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "Problem with " + order + ", there was EXCEPTION: " + e.getClass());
        }
    }
}
