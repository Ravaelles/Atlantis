package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionsCommander;
import atlantis.production.dynamic.DynamicProductionCommander;
import atlantis.units.buildings.SupplyCommander;

/**
 * Manages construction of new buildings.
 */
public class ProductionCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            SupplyCommander.class,
            ProductionOrdersCommander.class,
            ConstructionsCommander.class,
            DynamicProductionCommander.class,
        };
    }

    @Override
    public boolean applies() {
        return !AGame.isUms();
    }
}
