package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.strategy.Strategy;
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
            return false;
        }

        if (We.protoss()) {
            if (
                A.supplyTotal() <= 12
                    && order.isBuilding()
                    && !order.unitType().isPylon()
                    && Count.pylons() == 0
            ) return false;

            if (order.isBuilding() && order.unitType().isAssimilator() && !Strategy.get().isGoingHiddenUnits()) {
                if (Select.ourWithUnfinished().ofType(AUnitType.Protoss_Cybernetics_Core).isEmpty()) {
                    return false;
                }
            }
        }

        Construction construction = order.construction();
        if (construction != null && construction.buildingUnit() != null && construction.buildingUnit().hp() > 0) {
            return false;
        }


        // Prioritize combat unit production when base is under attack
        if (order.isBuilding() && order.unitType().isBase()) {
            if (EnemyUnitBreachedBase.get() != null) return false;
        }

        boolean isFarFromMainBaseSoTravelEarly = !isFarFromMainBaseSoTravelEarly(order);
        boolean supplyOk = isSupplyRequirementFulfilled(order, isFarFromMainBaseSoTravelEarly);
        boolean resourcesOk = hasEnoughResourcesFor(order, isFarFromMainBaseSoTravelEarly);
        boolean requirementsOk = order.checkIfHasWhatRequired();
        if (!supplyOk || !resourcesOk || !requirementsOk) {
            // Diagnostic, not a behavior change (a Nexus order sat in the queue
            // from 6:19 to game end with no REQUEST, no refusal and no cancel -
            // three indistinguishable silences). Names the refusing term with
            // the numbers, throttled; buildings only, units churn through here
            // every frame by design.
            if (order.isBuilding()) {
                atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                    "ORDER_NOT_READY " + order.unitType()
                        + " supplyOk=" + supplyOk + "(" + A.supplyUsed() + "/" + order.minSupply() + ")"
                        + " resourcesOk=" + resourcesOk + "(min=" + A.minerals()
                        + " reserved=" + ReservedResources.minerals() + ")"
                        + " requirementsOk=" + requirementsOk);
            }
            return false;
        }

        if (cantAffordAndDidntExpandYet(order)) return false;

        return true;
    }

    private static boolean isSupplyRequirementFulfilled(ProductionOrder order, boolean isFarFromMainBaseSoTravelEarly) {
        if (
            A.supplyUsed() <= 16
                && !A.hasMinerals(234)
                && order.is(AUnitType.Protoss_Gateway)
                && !order.supplyRequirementFulfilled(0)
        ) return false;

        return order.supplyRequirementFulfilled(isFarFromMainBaseSoTravelEarly ? 1 : 0);
    }

    private static boolean isFarFromMainBaseSoTravelEarly(ProductionOrder order) {
        if (!order.isBuilding()) return false;
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


        return A.supplyUsed() + (A.supplyUsed() >= 19 ? 3 : 1) >= order.minSupply()
            && (canAffordNow || applySpecialPriority(type));
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
        }


        return A.canAffordWithReserved(minerals + reservedMinerals, gas + reservedGas);

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
