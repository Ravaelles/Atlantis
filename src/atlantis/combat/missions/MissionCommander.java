package atlantis.combat.missions;

import atlantis.architecture.Commander;

public class MissionCommander extends Commander {

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            GlobalMissionCommander.class,
        };
    }

}
