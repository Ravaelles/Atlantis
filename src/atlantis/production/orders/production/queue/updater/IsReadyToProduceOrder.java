package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import bwapi.TechType;

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
        return order.supplyRequirementFulfilled() && order.checkIfHasWhatRequired();
    }
}
