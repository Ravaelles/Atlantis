package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.production.constructions.ConstructionRequests;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceOrdersFromQueue;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProductionOrdersCommander extends Commander {
    /**
     * production-v2 state (M4): created once, kept across frames so the plan of
     * the previous frame can be logged and compared. It owns no unit state.
     */
    private static final ProductionEngine V2_ENGINE = new ProductionEngine();

    @Override
    public boolean applies() {
        if (A.isUms() && !A.hasMinerals(350)) return false;

        return Count.workers() > 0 && Select.ourBuildings().notEmpty();
    }

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     *
     * <p>
     * When {@code PRODUCTION_V2} is DRY_RUN the legacy pipeline below still
     * plays the game and the v2 engine runs alongside it, logging what it would
     * have ordered from the same state - the comparison step of the cutover. In
     * LIVE the v2 engine issues the orders instead.
     * </p>
     */
    @Override
    protected boolean handle() {
        if (atlantis.config.env.Env.productionV2().isLive()) {
            V2_ENGINE.updateFrame();
            return false;
        }

        if (atlantis.config.env.Env.productionV2().isDryRun()) {
            V2_ENGINE.updateFrame();
        }

        if (A.everyNthGameFrame(GamePhase.isEarlyGame() ? 19 : 71)) Queue.get().refresh();

        for (ProductionOrder order : Queue.get().readyToProduceOrders().list()) {
            if (newBaseInProgressAndCantAffordThisOrder(order)) return false;

            ProduceOrdersFromQueue.handleProductionOrder(order);
        }
        return false;
    }

    private static boolean newBaseInProgressAndCantAffordThisOrder(ProductionOrder order) {
        return !A.hasMinerals(AtlantisRaceConfig.BASE.mineralPrice() + order.mineralPrice())
            && ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.BASE) > 0;
    }
}
