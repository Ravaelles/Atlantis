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
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProductionOrdersCommander extends Commander {
    /**
     * production-v2 state (M4): created once, kept across frames so the plan of
     * the previous frame can be logged and compared. It owns no unit state.
     * The race policy is read exactly once, here, from the same source the
     * migrated branches read - never re-resolved per frame.
     */
    private final ProductionEngine v2Engine =
        new ProductionEngine(ProductionRacePolicy.ambient());

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
            v2Engine.updateFrame();
            return false;
        }

        // Read-only supply witness for OFF games (NEXT #150 follow-up):
        // DRY_RUN runs the engine alongside (its twin fires), LIVE is
        // the twin's own path - only OFF never rows a 90/90 block.
        if (!atlantis.config.env.Env.productionV2().isEnabled()) {
            AUnitType provider = SupplyBlockedTwin.legacyProvider();
            SupplyBlockedTwin.sample(
                provider, A.supplyFree(), GameStateSnapshot.supplyProvidersComing(provider));
        }

        if (atlantis.config.env.Env.productionV2().isDryRun()) {
            v2Engine.updateFrame();
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
