package atlantis.production.orders.production.queue.updater;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class IsReadyToProduceOrder {
    protected static boolean isReadyToProduce(ProductionOrder order) {
        if (AUnitType.Terran_Medic.equals(order.unitType())) {
            System.err.println("Medic");
            System.err.println("order.supplyRequirementFulfilled() = " + order.supplyRequirementFulfilled());
            System.err.println("order.calculateIfHasWhatRequired() = " + order.calculateIfHasWhatRequired());
        }
        return order.supplyRequirementFulfilled() && order.calculateIfHasWhatRequired();
    }
}
