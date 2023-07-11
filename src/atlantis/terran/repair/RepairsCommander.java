package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;


public class RepairsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        if (!We.terran()) {
            return new Class[] {};
        }

        return new Class[] {
            NumberOfRepairersCommander.class,
            RepairerCommander.class,
            ProtectorCommander.class,
            EnsureThereAreMineralsForRepairsCommander.class,
        };
    }
}
