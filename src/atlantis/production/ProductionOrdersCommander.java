package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.engine.ProductionEngine;
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
     * Single policy, no version switch (NEXT #227): the v2 engine is the
     * only production policy. The legacy drain (Queue.refresh +
     * ready-to-produce loop) and its DRY_RUN twin run are gone with the
     * modes that needed them - dead code that could still issue orders.
     * </p>
     */
    @Override
    protected boolean handle() {
        v2Engine.updateFrame();

        return false;
    }
}
