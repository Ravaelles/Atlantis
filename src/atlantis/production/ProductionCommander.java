package atlantis.production;

import atlantis.architecture.Commander;
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
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProductionOrdersCommander.class,
            SupplyCommander.class,
            ConstructionsCommander.class,
            DynamicProductionCommander.class,
//            RemoveExcessiveOrders.class,
        };
    }

    @Override
    public boolean applies() {
        if (A.isUms() && !A.hasMinerals(350)) return false;

        return (Have.base() && Count.workers() >= 4);
    }
}
