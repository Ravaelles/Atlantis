package atlantis.production;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.DecisionExecutor;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.production.requests.produce.ProduceUnit;
import atlantis.production.requests.produce.ProduceWorker;
import atlantis.units.AUnit;

/**
 * The domain half of the decision door: carries out issued decisions
 * through the existing executors. Registered once by the game root
 * (composition root may know everything).
 *
 * <p>Strangler, Phase A: legacy gates still live inside the executors;
 * questions only add the audit trail (what/why) and the single door.
 * Pure gate migration is follow-up work, not this class.</p>
 */
public class ProductionDecisionExecutor implements DecisionExecutor {

    public static void register() {
        atlantis.decisions.board.DecisionDispatcher.register(new ProductionDecisionExecutor());
    }

    @Override
    public boolean execute(ADecision decision) {
        if (decision == null) return false;

        switch (decision.kind()) {
            case TRAIN_WORKER:
                // The base travels in the payload - without it there is
                // nothing to order (the wrapper always attaches one).
                if (decision.payload() instanceof AUnit) {
                    return AutoProduceWorkersCommander.produceWorker((AUnit) decision.payload());
                }
                return false;

            case TRAIN_ARMY:
                if (decision.payload() instanceof ProductionOrder) {
                    ProductionOrder order = (ProductionOrder) decision.payload();
                    if (order.unitType() != null) {
                        return ProduceUnit.orderUnit(order.unitType(), order);
                    }
                }
                return false;

            case BUILD:
                if (decision.payload() instanceof ProductionOrder) {
                    ProductionOrder order = (ProductionOrder) decision.payload();
                    if (order.unitType() != null) {
                        return ProduceBuilding.requestBuilding(order.unitType(), order);
                    }
                }
                return false;

            default:
                return false;
        }
    }
}
