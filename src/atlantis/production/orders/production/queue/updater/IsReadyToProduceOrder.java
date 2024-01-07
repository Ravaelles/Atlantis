package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class IsReadyToProduceOrder {
    protected static boolean isReadyToProduce(ProductionOrder order) {
        if (order.isStatus(OrderStatus.COMPLETED)) {
//            ErrorLog.printMaxOncePerMinute("Trying to produce completed order: " + order);
            return false;
        }

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

        // Prioritize combat unit production when base is under attack
        if (order.isBuilding() && order.unitType().isBase()) {
            if (EnemyWhoBreachedBase.get() != null) return false;
        }

        boolean notEnoughSupplyResources, noRequirement = false;
        if (
            (notEnoughSupplyResources = (!order.supplyRequirementFulfilled() && !canSkip(order)))
                || (noRequirement = !order.checkIfHasWhatRequired())
        ) {
            return false;
        }

        if (cantAffordAndDidntExpandYet(order)) return false;

        return true;
    }

    private static boolean canSkip(ProductionOrder order) {
        AUnitType type = order.unitType();

        return A.supplyUsed() >= 12
            && A.supplyUsed() <= order.minSupply() + 3
            && (order.isUnit() && type.isResource())
            && A.canAfford(type.mineralPrice() + 100, type.gasPrice() > 0 ? type.gasPrice() + 50 : 0);
    }

    private static boolean cantAffordAndDidntExpandYet(ProductionOrder order) {
        return A.hasMinerals(550)
            && (A.seconds() >= 700 && Count.basesWithUnfinished() <= 1);
    }

    public static boolean canAffordWithReserved(ProductionOrder order) {
//        int mineralsAvailable = mineralsAvailable(order);
//        int gasAvailable = gasAvailable(order);

        if (true) return false;

        if (order.isBuilding()) return true;

        return (A.hasMinerals(600) || mineralsAvailable(order) >= order.mineralPrice())
            && (A.hasGas(500) || gasAvailable(order) >= order.gasPrice());
    }

    // =========================================================

    private static int mineralsAvailable(ProductionOrder order) {
        int mineralsBonus = mineralsBonusForEarlyConstruction(order);
        int reservedMinerals = Math.min(500, ReservedResources.minerals());

        return A.minerals() - reservedMinerals + order.reservations().minerals() + mineralsBonus;
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
