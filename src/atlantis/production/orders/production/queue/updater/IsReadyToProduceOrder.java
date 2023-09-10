package atlantis.production.orders.production.queue.updater;

import atlantis.production.orders.production.queue.order.ProductionOrder;

public class IsReadyToProduceOrder {
    protected static boolean isReadyToProduce(ProductionOrder order) {
//        if (AUnitType.Terran_Medic.equals(order.unitType())) {
//            System.err.println("Medic");
//            System.err.println("order.supplyRequirementFulfilled() = " + order.supplyRequirementFulfilled() + " // " + order.minSupply() + "/" + AGame.supplyUsed());
//            System.err.println("order.calculateIfHasWhatRequired() = " + order.calculateIfHasWhatRequired());
//        }
        return order.supplyRequirementFulfilled() && order.checkIfHasWhatRequired();
    }
}
