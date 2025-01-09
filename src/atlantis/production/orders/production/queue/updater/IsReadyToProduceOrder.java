package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class IsReadyToProduceOrder {
    public static boolean isReady(ProductionOrder order) {
        if (order.isStatus(OrderStatus.FINISHED)) {
//            ErrorLog.printMaxOncePerMinute("Trying to produce completed order: " + order);
            return false;
        }

        Construction construction = order.construction();
        if (construction != null && construction.buildingUnit() != null && construction.buildingUnit().hp() > 0) {
            return false;
        }

//        if (order.unitType() == AUnitType.Terran_Machine_Shop) {
//        if (order.tech() == TechType.Stim_Packs) {
//        if (order.upgrade() == ResearchU238.upgradeType()) {
//        A.errPrintln("----------- Order: " + order + " / now: " + A.now());
//        A.errPrintln(
//            "supplyRequirementFulfilled: " + order.supplyRequirementFulfilled() + "\r\n"
//                + "hasEnoughResourcesFor: " + hasEnoughResourcesFor(order) + " / res_min:"
//                + ReservedResources.minerals() + "\r\n"
//                + "canAffordWithReserved: " + A.canAffordWithReserved(150, 0) + "\r\n"
//                + "canAfford bonus: " + A.canAfford(150 + 150, 0) + "\r\n"
//                + "checkIfHasWhatRequired: " + order.checkIfHasWhatRequired() + "\r\n"
//                + order.minSupply() + "/" + A.supplyUsed()
//        );
//        A.errPrintln("-----------");
//        }

        // Prioritize combat unit production when base is under attack
        if (order.isBuilding() && order.unitType().isBase()) {
            if (EnemyUnitBreachedBase.get() != null) return false;
        }

        boolean isFarFromMainBaseSoTravelEarly = !isFarFromMainBaseSoTravelEarly(order);
        boolean notEnoughResources, notEnoughSupplyResources, noRequirement = false;
        if (
            (notEnoughSupplyResources = !order.supplyRequirementFulfilled(isFarFromMainBaseSoTravelEarly ? 1 : 0))
//            (notEnoughSupplyResources = !order.supplyRequirementFulfilled(0))
                || (notEnoughResources = !hasEnoughResourcesFor(order, isFarFromMainBaseSoTravelEarly))
                || (noRequirement = !order.checkIfHasWhatRequired())
        ) {
//            if (noRequirement || !isFarFromMainBaseSoTravelEarly) return false;
            return false;
        }

        if (cantAffordAndDidntExpandYet(order)) return false;

        return true;
    }

    private static boolean isFarFromMainBaseSoTravelEarly(ProductionOrder order) {
        if (order.aroundPosition() == null) return false;

        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return false;

        return order.aroundPosition().distTo(main) >= 17;
    }

    private static boolean hasEnoughResourcesFor(ProductionOrder order, boolean isFarFromMainBaseSoTravelEarly) {
        AUnitType type = order.unitType();

        if (type == null) return true;
        if (type.isBase()) return A.hasMinerals(310);
        if (isFarFromMainBaseSoTravelEarly) return true;
        if (type.isForge() && A.supplyUsed() == 9) return true;

        boolean canAffordNow = A.canAffordWithReserved(Math.min(220, type.mineralPrice()), type.gasPrice());

        if (A.supplyUsed() >= order.minSupply()) return canAffordNow;
        else if (!canAffordWithReserved(order, type.mineralPrice(), type.gasPrice())) return false;

//        System.err.println("      SUP?!? = " + (A.supplyUsed() + 3 >= order.minSupply()));
//        System.err.println("      type.mineralPrice() = " + type.mineralPrice());
//        System.err.println("      mineralBonusToHave(type) = " + mineralBonusToHave(type));
//        System.err.println("      ReservedResources.minerals() = " + ReservedResources.minerals());

//        return A.supplyUsed() + 3 >= order.minSupply()
        return A.supplyUsed() + (A.supplyUsed() >= 19 ? 3 : 1) >= order.minSupply()
//            && (order.isUnit() && type.isResource())
            && (canAffordNow || applySpecialPriority(type));
//            && A.canAfford(type.mineralPrice() + 100, type.gasPrice() > 0 ? type.gasPrice() + 50 : 0);
    }

    private static boolean applySpecialPriority(AUnitType unitType) {
        if (We.protoss()) {
            return unitType.is(
                AUnitType.Protoss_Cybernetics_Core,
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Protoss_Assimilator
            );
        }

        return false;
    }

    private static boolean canAffordWithReserved(ProductionOrder order, int minerals, int gas) {
        int reservedMinerals = 0;
        int reservedGas = 0;

        Orders otherEarlierOrders = Queue.get().readyToProduceOrders().exclude(order);
        for (ProductionOrder o : otherEarlierOrders.list()) {
            reservedMinerals += o.reservations().minerals();
            reservedGas += o.reservations().gas();
//            reservedMinerals += o.reservations().minerals();
//            reservedGas += o.reservations().gas();
        }

//        if (reservedMinerals > 0)
//            System.err.println("At supply " + A.supplyUsed() + " reservedMinerals = " + reservedMinerals);

        return A.canAffordWithReserved(minerals + reservedMinerals, gas + reservedGas);

//        return A.canAffordWithReserved(
//            unitType.mineralPrice() + mineralBonusToHave(unitType), unitType.gasPrice()
//        );
    }

    private static int mineralBonusToHave(AUnitType type) {
        if (type.isABuilding()) return -48;

        return 34;
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
