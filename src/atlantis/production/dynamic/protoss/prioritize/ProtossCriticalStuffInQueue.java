package atlantis.production.dynamic.protoss.prioritize;

import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.We;


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

            // Warp-window pump calm (NEXT #83): an IN_PROGRESS construction with
            // no building yet is a warp that may still be settling server-side.
            // Measured on Wine as 100-mineral burn cycles (attempt spends near
            // ~188, warp drops silently, demote, retry spends again: bank
            // 92->8->88->4 while capped 25/25). Pausing here protects the bank
            // until the building exists; linked warps (entity present) and rich
            // banks (no protection needed) are unaffected by the 250 gate above.
            if (hasUnconfirmedWarp()) return false;
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

    /**
     * A warp the server may not have settled yet: IN_PROGRESS but no building
     * entity linked. Normal warps link within frames (unaffected); a silently
     * dropped warp sits here while the pump would otherwise spend the recovery
     * bank into the next burn cycle.
     */
    private static boolean hasUnconfirmedWarp() {
        for (Construction construction : ConstructionRequests.constructions) {
            if (
                construction.status() == ConstructionOrderStatus.IN_PROGRESS
                    && construction.buildingUnit() == null
            ) return true;
        }
        return false;
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
