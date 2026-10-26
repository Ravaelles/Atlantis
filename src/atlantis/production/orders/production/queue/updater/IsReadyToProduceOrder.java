package atlantis.production.orders.production.queue.updater;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.strategy.Strategy;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.PriorityWaiters;
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
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class IsReadyToProduceOrder {
    /**
     * Owner bank rule (NEXT #202, live stall with banked gas): with 550
     * minerals in the bank every production is affordable; gas blocks only
     * below the late-game amount (300 at most 150 supply, 400 above) - and
     * a gas-free order never waits on gas. Named constants, pinned by
     * {@code RichBankBypassTest}: the 550/300/400 split is the owner's
     * measurement (Arbiter's 350 gas is the ceiling), not tuning.
     */
    private static final int RICH_BANK_MINERALS = 550;

    private static final int RICH_BANK_GAS_EARLY = 300;

    private static final int RICH_BANK_GAS_LATE = 400;

    private static final int RICH_BANK_LATE_SUPPLY = 150;

    private static boolean richBankCoversEverything(ProductionOrder order) {
        if (!A.hasMinerals(RICH_BANK_MINERALS)) return false;

        AUnitType type = order.unitType();
        if (type != null && type.gasPrice() <= 0) return true;

        return A.hasGas(
            A.supplyUsed() <= RICH_BANK_LATE_SUPPLY ? RICH_BANK_GAS_EARLY : RICH_BANK_GAS_LATE);
    }

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
            // Refusing term with numbers, buildings only (units churn through
            // here every frame by design). Unconditional: a silent queue is
            // undiagnosable (NEXT #58), and the throttle bounds the volume.
            // Workerless games skip the twin, not the refusal: with zero
            // workers no builder exists, so every building row would name
            // the same structural state every refresh (measured: 1v1 UMS
            // map twins ORDER_NOT_READY for Pylons that can never build).
            // Completions still flow (ProtossWarping marks FINISHED on warp,
            // queue completion is supply-independent), so only the twin
            // is gated, never the decision.
            if (order.isBuilding() && atlantis.units.select.Count.workers() > 0
                && A.hasMinerals(order.mineralPrice() - 32)) {
                EventLog.key(LogKey.ORDER_NOT_READY)
                    .param("type", order.unitType().name())
                    .param("supplyOk", supplyOk)
                    .param("supplyUsed", A.supplyUsed())
                    .param("supplyExpected", order.minSupply())
                    .param("resourcesOk", resourcesOk)
                    .param("minerals", A.minerals())
                    .param("reserved", ReservedResources.minerals())
                    .param("requirementsOk", requirementsOk)
                    .log(A.now(), A.minSec());
            }
            return false;
        }

        if (cantAffordAndDidntExpandYet(order)) {
            // Named, buildings only (units churn here by design): a silent
            // queue is undiagnosable, and this is the signature the NEXT #71
            // game proof looks for.
            if (order.isBuilding()) {
                atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                    "ORDER_SAVING_FOR_EXPANSION " + order.unitType() + " @" + A.now()
                        + " (min=" + A.minerals() + " s=" + A.seconds()
                        + " bases=" + Count.basesWithUnfinished() + ")");
            }
            return false;
        }

        // D3 priority waiting: a strictly-higher unfinished order waits and
        // the bank does not cover both - lower spending pauses (self
        // excluded, so a HIGH waiter itself is never blocked here). The
        // rich-bank rule above already returned for a covered bank, so a
        // block here means genuinely uncovered savings, never a stale
        // earmark.
        if (!richBankCoversEverything(order)
            && PriorityWaiters.blocksSpending(order, order.mineralPrice(), order.gasPrice())) {
            if (order.isBuilding() && (order.is(AUnitType.Protoss_Nexus) || order.is(AUnitType.Protoss_Photon_Cannon))) {
                atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                    "ORDER_WAITING " + order.unitType() + " @" + A.now()
                        + " min=" + A.minerals() + " gas=" + A.gas()
                        + " for=" + PriorityWaiters.describeAbove(order.priority(), order));
            }
            return false;
        }

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

    /**
     * Builder headroom in minerals (owner: the golden middle between a
     * builder idling 30 s on site and starting ~15 s late): readiness
     * fires once the bank is this close to the price, so the builder
     * walks while the last minerals are mined instead of queuing behind
     * them. Only below, the aim-while-poor path and the twin are
     * untouched; only above, early dispatch is already affordable.
     */
    private static final int EARLY_DISPATCH_HEADROOM_MINERALS = 50;

    private static boolean hasEnoughResourcesFor(ProductionOrder order, boolean isFarFromMainBaseSoTravelEarly) {
        AUnitType type = order.unitType();

        if (type == null) return true;
        if (type.isBase()) return A.hasMinerals(310);
        if (isFarFromMainBaseSoTravelEarly) return true;
        if (type.isForge() && A.supplyUsed() == 9) return true;

        boolean canAffordNow = A.canAffordWithReserved(Math.min(220, type.mineralPrice()), type.gasPrice());
        boolean canAffordSoon = type.isABuilding() && A.supplyUsed() >= order.minSupply()
            && A.canAffordWithReserved(
                Math.max(0, Math.min(220, type.mineralPrice()) - EARLY_DISPATCH_HEADROOM_MINERALS),
                type.gasPrice());

        if (A.supplyUsed() >= order.minSupply()) return canAffordNow || canAffordSoon;
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

    private static boolean cantAffordAndDidntExpandYet(ProductionOrder order) {
        // NEXT #71: the name is the spec - block when the base CANT AFFORD,
        // saving for the expansion. Unnegated this halted every order while
        // RICH, and a rich bank only grows, so it never released (livelock).
        return !A.hasMinerals(550)
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
