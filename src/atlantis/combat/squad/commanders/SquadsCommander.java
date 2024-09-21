package atlantis.combat.squad.commanders;

import atlantis.architecture.Commander;
import atlantis.combat.squad.transfers.SquadTransfersCommander;

public class SquadsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            SquadTransfersCommander.class,
//            SquadStateCommander.class,
            ActWithSquadsCommander.class,
        };
    }
}
