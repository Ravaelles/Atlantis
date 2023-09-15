package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.terran.tech.U238;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.TechType;

public class IsReadyToProduceOrder {
    protected static boolean isReadyToProduce(ProductionOrder order) {
//        if (order.unitType() == AUnitType.Terran_Machine_Shop) {
//        if (order.tech() == TechType.Stim_Packs) {
//        if (order.upgrade() == U238.upgradeType()) {
//            A.errPrintln("----------- Order: " + order + " / now: " + A.now());
//            A.errPrintln(
//                "order.supplyRequirementFulfilled() = "
//                    + order.supplyRequirementFulfilled() + " // "
//                    + order.minSupply() + "/" + AGame.supplyUsed()
//            );
//            A.errPrintln("order.checkIfHasWhatRequired() = " + order.checkIfHasWhatRequired());
//            A.errPrintln("-----------");
//        }

        if (!order.supplyRequirementFulfilled() || !order.checkIfHasWhatRequired()) return false;
        if (!canAffordWithReserved(order)) return false;

        return true;

//        boolean isReady = order.supplyRequirementFulfilled() && order.checkIfHasWhatRequired();
    }

    public static boolean canAffordWithReserved(ProductionOrder order) {
        return (A.minerals() - ReservedResources.minerals() >= order.mineralPrice())
            && (A.gas() - ReservedResources.gas() >= order.gasPrice());

//        return A.canAffordWithReserved(order.mineralPrice(), order.gasPrice());
    }
}
