package atlantis.production.dynamic.protoss.prioritize;

import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.We;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProtossCriticalStuffInQueue {
    public static boolean hasEnoughResources() {
        return hasEnoughMinerals() && hasEnoughGas();
    }

    public static boolean hasEnoughMinerals() {
        if (!We.protoss()) return true;

        if (!A.hasMinerals(250)) {
            if (isImminent(AUnitType.Protoss_Robotics_Facility)) return false;
            if (isImminent(AUnitType.Protoss_Photon_Cannon)) return false;
            // A queued Cybernetics Core pauses the worker pump below 250 minerals
            // (owner ask: no reservation existed, so the pump ate the bank and the
            // Core waited until a supply block accidentally stopped workers - the
            // Core is the tech the whole early game pivots on, like Robo/Cannon).
            if (isImminent(AUnitType.Protoss_Cybernetics_Core)) return false;
        }

        return true;
    }

    /**
     * A not-started critical item reserves the bank only when it is actually up
     * next (its supply gate met). The queue is pre-seeded with the whole build
     * order at frame 0 ({@code QueueInitializer}), so mere presence must not
     * pause anything: measured on Wine (last-wine.log) as `CriticalStuff` from
     * @0 at 50 minerals with an empty future, first worker only once the bank
     * touched 250. Same predicate as the PrioritizeCore rule in
     * {@code EarlyGameProduceWorkers}.
     */
    private static boolean isImminent(AUnitType type) {
        if (CountInQueue.countNotStarted(type) == 0) return false;
        ProductionOrder order = Queue.get().notStarted().ofType(type).first();
        if (order == null) return false;
        // An explicitly top-priority request means "build now" regardless of
        // the minSupply bookkeeping (which inherits the queue tail).
        if (order.priority() != null && order.priority().isTop()) return true;
        return order.supplyRequirementFulfilled(0);
    }

    public static boolean hasEnoughGas() {
        if (!We.protoss()) return true;

        if (!A.hasGas(250) && CountInQueue.countNotStarted(AUnitType.Protoss_Robotics_Facility) > 0) {
            return false;
        }

        if (!A.hasGas(210) && CountInQueue.countNotStarted(AUnitType.Protoss_Observatory) > 0 && Have.factoryWithUnfinished()) {
            return false;
        }

        return true;
    }
}
