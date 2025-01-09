package atlantis.production.requests.produce;

import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderHandler;
import atlantis.util.log.ErrorLog;

public class ProduceOrdersFromQueue {
    public static void handleProductionOrder(ProductionOrder order) {
        try {
            (new ProductionOrderHandler(order)).invokeCommander();
        } catch (Exception e) {
            order.setStatus(OrderStatus.READY_TO_PRODUCE);
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Cancelled " + order + " as there was: " + e.getClass());
            ErrorLog.printMaxOncePerMinute("Problem with " + order + ", there was EXCEPTION: " + e.getClass());
            e.printStackTrace();
        }
    }
}
