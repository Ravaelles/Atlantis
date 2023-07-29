package atlantis.combat.squad;

import atlantis.architecture.Commander;

public class SquadsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            SquadTransfersCommander.class,
//            SquadStateCommander.class,
            ActWithSquadsCommander.class,
        };
    }
}
