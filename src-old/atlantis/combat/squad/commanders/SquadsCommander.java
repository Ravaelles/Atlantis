package atlantis.combat.squad.commanders;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.combat.squad.transfers.SquadTransfersCommander;

public class SquadsCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            SquadTransfersCommander::new,
//            SquadStateCommander.class,
            ActWithSquadsCommander::new,
        };
    }
}
