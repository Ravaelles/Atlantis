package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ProductionOrder;

public class IsReadyToProduceOrder {
    protected static boolean isReadyToProduce(ProductionOrder order) {
//        if (order.tech() == TechType.Stim_Packs) {
//        A.errPrintln("----------- Order: " + order);
//        A.errPrintln(
//            "order.supplyRequirementFulfilled() = "
//                + order.supplyRequirementFulfilled() + " // "
//                + order.minSupply() + "/" + AGame.supplyUsed()
//        );
//        A.errPrintln("order.checkIfHasWhatRequired() = " + order.checkIfHasWhatRequired());
//        A.errPrintln("-----------");
//        }

        if (!order.supplyRequirementFulfilled() || !order.checkIfHasWhatRequired()) return false;
        if (!canAffordWithReserved(order)) return false;

        return true;

//        boolean isReady = order.supplyRequirementFulfilled() && order.checkIfHasWhatRequired();
    }

    private static boolean canAffordWithReserved(ProductionOrder order) {
        return (A.minerals() - ReservedResources.minerals() >= order.mineralPrice())
            && (A.gas() - ReservedResources.gas() >= order.gasPrice());

//        return A.canAffordWithReserved(order.mineralPrice(), order.gasPrice());
    }
}
