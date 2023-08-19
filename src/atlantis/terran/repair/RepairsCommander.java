package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.util.We;


public class RepairsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        if (!We.terran()) {
            return new Class[]{};
        }

        return new Class[]{
            NewRepairsCommander.class,
            RepairerCommander.class,
            ProtectorCommander.class,
            EnsureThereAreMineralsForRepairsCommander.class,
        };
    }
}
