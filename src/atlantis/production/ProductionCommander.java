package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
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
