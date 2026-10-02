package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.terran.chokeblockers.ChokeBlockersCommander;
import atlantis.terran.repair.protect.ProtectorCommander;
import atlantis.util.We;

public class TerranRepairsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            NewRepairsCommander::new,
            RepairerCommander::new,
            ProtectorCommander::new,
            DontRepairWithoutMineralsCommander::new
        };
    }
}
