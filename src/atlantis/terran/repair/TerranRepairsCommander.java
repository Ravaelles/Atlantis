package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.terran.chokeblockers.ChokeBlockersCommander;
import atlantis.terran.repair.protect.ProtectorCommander;
import atlantis.util.We;

public class TerranRepairsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            NewRepairsCommander.class,
            RepairerCommander.class,
            ProtectorCommander.class,
            DontRepairWithoutMineralsCommander.class
        };
    }
}
