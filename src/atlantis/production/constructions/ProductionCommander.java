package atlantis.production.constructions;
import atlantis.production.forceexit.ForceExitWhenOpeningProven;
import atlantis.production.forceexit.ForceExitLocallyInOpenBW;
import atlantis.production.forceexit.ForceExitAtSupply;
import atlantis.production.dynamic.supply.ProductionHealthWatcher;
import atlantis.production.engine.ProductionOrdersCommander;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.production.constructions.ConstructionsCommander;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

/**
 * Manages construction of new buildings.
 */
public class ProductionCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        // Single policy, no version switch (NEXT #227): production-v2 is
        // the only production policy - the legacy dynamic and supply
        // commanders ran two policies over the same economy, which is the
        // double-ordering the redesign exists to remove. The two commanders
        // here are not policy: ConstructionsCommander executes the builder
        // side of orders production-v2 issues (the strangler seam), and
        // ProductionOrdersCommander is the one that runs the v2 engine.
        return new CommanderFactory[]{
            ProductionOrdersCommander::new,
            ConstructionsCommander::new,
            ForceExitWhenOpeningProven::new,
            ForceExitAtSupply::new,
            ForceExitLocallyInOpenBW::new,
            // Read-only health twins, no policy.
            ProductionHealthWatcher::new,
        };
    }

    @Override
    public boolean applies() {
        if (A.isUms() && !A.hasMinerals(350)) return false;

        return (Have.base() && Count.workers() >= 1);
    }
}
