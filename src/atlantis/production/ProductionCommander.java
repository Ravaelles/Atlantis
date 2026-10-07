package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.production.constructions.ConstructionsCommander;
import atlantis.production.dynamic.DynamicProductionCommander;
import atlantis.units.buildings.SupplyCommander;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

/**
 * Manages construction of new buildings.
 */
public class ProductionCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        // The cutover (M6 of _AI/redesign/01_PRODUCTION.md): with
        // PRODUCTION_V2=LIVE the new engine is the only production policy.
        // Leaving the legacy dynamic and supply commanders in the list would
        // run two policies over the same economy - which is not a cutover, it is
        // the double-ordering the redesign exists to remove. The two commanders
        // that stay are not policy: ConstructionsCommander executes the builder
        // side of orders production-v2 issues (the strangler seam), and
        // ProductionOrdersCommander is the one that runs the v2 engine.
        System.out.println("Production Commander = " + Env.productionV2().isLive());
        if (Env.productionV2().isLive()) {
            return new CommanderFactory[]{
                ProductionOrdersCommander::new,
                ConstructionsCommander::new,
//                DynamicProductionCommander::new,
            };
        }

        return new CommanderFactory[]{
            ProductionOrdersCommander::new,
            SupplyCommander::new,
            ConstructionsCommander::new,
            DynamicProductionCommander::new,
//            RemoveExcessiveOrders.class,
        };
    }

    @Override
    public boolean applies() {
        if (A.isUms() && !A.hasMinerals(350)) return false;

        return (Have.base() && Count.workers() >= 1);
    }
}
