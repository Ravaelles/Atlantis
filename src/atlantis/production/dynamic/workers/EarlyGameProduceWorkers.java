package atlantis.production.dynamic.workers;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class EarlyGameProduceWorkers {
    protected static Decision decision() {
        if (We.zerg()) return Decision.TRUE("ZergEarlyGameProduceWorkers");
        if (Count.workers() >= 20) return Decision.INDIFFERENT;

        Decision decision = null;

        if (We.protoss()) decision = forProtoss();

        if (decision != null) return decision;

        return Decision.TRUE("ConstantEarlyFlow");
    }

    private static Decision forProtoss() {
        if (A.minerals() <= 209 && ConstructionRequests.countNotStartedOfType(AUnitType.Protoss_Cybernetics_Core) > 0) {
            ProductionOrder order = Queue.get().notFinished().ofType(AUnitType.Protoss_Cybernetics_Core).first();
            if (order != null && order.supplyRequirementFulfilled(0)) {
                return Decision.FALSE("PrioritizeCore");
            }
        }

        return null;
    }
}
