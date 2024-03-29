package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.util.We;

public class TerranRepairsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            NumberOfRepairersCommander.class,
            RepairerCommander.class,
            ProtectorCommander.class,
            EnsureThereAreMineralsForRepairsCommander.class
        };
    }
}
