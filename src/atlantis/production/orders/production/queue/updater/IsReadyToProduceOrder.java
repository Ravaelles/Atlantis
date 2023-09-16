package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ProductionOrder;

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
        int mineralsAvailable = mineralsAvailable(order);
        int gasAvailable = gasAvailable(order);

        return (mineralsAvailable >= order.mineralPrice()) && (gasAvailable >= order.gasPrice());
    }

    // =========================================================

    private static int mineralsAvailable(ProductionOrder order) {
        int mineralsBonus = mineralsBonusForEarlyConstruction(order);

        return A.minerals() - ReservedResources.minerals() + order.reservations().minerals() + mineralsBonus;
    }

    private static int gasAvailable(ProductionOrder order) {
        int gasBonus = gasBonusForEarlyConstruction(order);

        return A.gas() - ReservedResources.gas() + order.reservations().gas() + gasBonus;
    }

    private static int mineralsBonusForEarlyConstruction(ProductionOrder order) {
        return order.isBuilding() ? (order.unitType().isBase() ? 150 : 80) : 0;
    }

    private static int gasBonusForEarlyConstruction(ProductionOrder order) {
        return order.isBuilding() ? 30 : 0;
    }
}
